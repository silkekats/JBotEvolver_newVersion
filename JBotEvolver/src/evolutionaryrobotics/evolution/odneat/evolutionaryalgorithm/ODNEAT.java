package evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import evolutionaryrobotics.evolution.odneat.controlsystem.OnlineController;
import evolutionaryrobotics.evaluationfunctions.ODNEATEvaluationFunction;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ALGDescriptor;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Crossover;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.MacroCrossover;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.MutationOperatorsSetup;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ODNEATCompositeMutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ODNEATTournamentSelector;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Selector;
import evolutionaryrobotics.populations.ODNEATPopulation;
import evolutionaryrobotics.populations.RobotPopulation;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ODNEAT implements OnlineEA<ODNEATGenome>, Serializable{

	private static final long serialVersionUID = 1L;
	/**
	 * odneat components and parameters
	 */
	protected ODNEATPopulation population;
	protected ODNEATTabuList tabuList;
	protected ODNEATGenome current;
	protected ODNEATInnovationManager inManager;

	protected int controllersGenerated = 0;
	public final int MATURATION_PERIOD = 500;

	protected boolean conductingEvolution = true;
	protected final double BROADCAST_RANGE = 0.25;

	//age of the current controller/active genome (number of control cycles)
	protected int age = 0;
	protected final double CONTROLLER_FREQUENCY_UPDATE = 10;

	public static final String ENERGY = "e";

	protected ODNEATEvaluationFunction eval;
	/**
	 * evolutionary operators
	 */
	protected Mutator<ODNEATGenome> mutator;
	protected Crossover<ODNEATGenome> crossover;
	protected Selector<ODNEATGenome> selector;

	protected Random random;
	protected ArrayList<ODNEATGenome> receivedInformation;
	private int robotId;
	protected transient Robot robot;

	public ODNEAT(Random random, int robotId, ALGDescriptor descriptor, Robot r,
			ODNEATEvaluationFunction eval, int inputs, int outputs, Arguments args){
		//System.out.println("odneat init: " + inputs + "; " + outputs);
		this.random = random;
		this.robotId = robotId;
		//we need to access the robot so that we can set the new controller when needed.
		this.robot = r;
		this.eval = eval;

		CompatibilityCalculator calc = new CompatibilityCalculator(descriptor.getExcessCoeff(), descriptor.getDisjointCoeff(), 
				descriptor.getWeightCoeff(), descriptor.getThreshold());
		calc.initialiseMacroCompatibilityCalculator(descriptor.getMacroParameterCoeff(), 
				descriptor.getMacroDisjointCoeff(), descriptor.getMacroExcessCoeff(), descriptor.getMacroMatchingCoeff());
		setupComponents(descriptor, args, calc);
		initialisePopulation(descriptor, calc);
		this.initializeFirstGenome(inputs, outputs, args);
	}

	public void initialisePopulation(ALGDescriptor descriptor, CompatibilityCalculator calc) {
		
		this.population = new ODNEATPopulation(random, robotId, calc, descriptor.getPopulationSize());
	}

	//in case we just want to see the robot's behaviour.
	public void setEvolutionStatus(boolean evolutionStatus){
		this.conductingEvolution = evolutionStatus;
	}

	protected void setupComponents(ALGDescriptor descriptor, Arguments args, CompatibilityCalculator calc) {
		inManager = new ODNEATInnovationManager(random, descriptor.getWeightRange());
		tabuList = new ODNEATTabuList(calc);
		receivedInformation = new ArrayList<ODNEATGenome>();

		initialiseEvolutionaryOperators(descriptor);
	}

	public void initialiseEvolutionaryOperators(ALGDescriptor descriptor) {
		this.mutator = initialiseMutator(descriptor, this.random);
		this.selector = new ODNEATTournamentSelector();
		selector.setRandom(random);
		this.crossover = new MacroCrossover(random);
		crossover.setCrossoverProbability(descriptor.getPXover());		
	}

	protected ODNEATCompositeMutator initialiseMutator(ALGDescriptor descriptor, Random random) {

		ODNEATCompositeMutator compMut = new MutationOperatorsSetup().setup(descriptor, random, this.inManager, this.robotId);

		return compMut;
	}

	public void initializeFirstGenome(int inputs, int outputs, Arguments args){
		String firstId = assignNewId();
		current = this.inManager.initialiseInnovation(firstId, inputs, outputs, args);
		current.setEnergyLevel(eval.getDefaultEnergyValue(robot));
		robot.setParameter(ENERGY, new Double(current.getEnergyLevel()));
		population.setCurrentId(firstId);

		population.addODNEATGenome(current, tabuList, false);
		//this.current = current.copy();

		this.resetAge();
	}

	@Override
	public boolean willBroadcastGenome() {
		double prob = this.random.nextDouble();
		return prob < population.getProportionateAdjustedFitness(current.getSpeciesId());
	}

	@Override
	public void processGenomesReceived(Collection<ODNEATGenome> receivedInformation) {
		ArrayList<ODNEATGenome> resettedElements = new ArrayList<ODNEATGenome>();

		for(ODNEATGenome c : receivedInformation){
			//this way, we only have to search for it once.
			ODNEATSpecies species = population.containsChromosome(c);
			//population does not contain the current chromosome
			if(species == null){
				discardOrAcceptChromosme(c, resettedElements);
			}
			//not a new chromosome, update energy level and so forth
			else
				updateGenome(c, c.getEnergyLevel());
		}
		//update tabu list time.
		tabuList.increaseTimeAllButThese(resettedElements);
	}

	private void discardOrAcceptChromosme(ODNEATGenome c, ArrayList<ODNEATGenome> resettedElements) {
		ODNEATGenome similar = tabuList.containsSimilarOrEqual(c);
		if(similar != null){
			tabuList.addElement(similar);
			resettedElements.add(similar);
		}
		else {
			population.addConditionallyODNEATGenome(c, tabuList);
		}
	}

	public void updateGenome(ODNEATGenome genome, double energyValue){
		population.updateGenome(genome, energyValue);
	}

	@Override
	public ODNEATGenome reproduce() {	
		String newChromosomeId = assignNewId();
		current = population.reproduce(newChromosomeId, 
				tabuList, this.selector, this.crossover, 
				this.mutator);
		current.setEnergyLevel(eval.getDefaultEnergyValue(this.robot));
		this.robot.setParameter(ENERGY, current.getEnergyLevel());

		//current = temp.copy();

		this.age = 0;

		/**
		 * re-speciate the population
		 */
		//clear
		/*ArrayList<ODNEATGenome> genomes = new ArrayList<ODNEATGenome>();
		for(ODNEATSpecies s : this.population.getSpeciesList()){
			genomes.addAll(s.getGenomes());
			s.clear();
		}

		//remove the species without elements
		Iterator<ODNEATSpecies> it = this.population.getSpeciesList().iterator();
		while(it.hasNext()){
			ODNEATSpecies currentSpecies = it.next();
			if(currentSpecies.species.isEmpty())
				it.remove();
		}
		
		//add randomly
		while(!genomes.isEmpty()){
			ODNEATGenome g = genomes.remove(random.nextInt(genomes.size()));
			this.population.addODNEATGenome(g, tabuList, false);
		}*/
		
		return current;
	}

	private String assignNewId() {
		controllersGenerated++;
		return this.robotId+ "." + controllersGenerated;
	}

	public int getTabuListSize() {
		return this.tabuList.getCurrentSize();
	}

	public int getTabuRejectedCount() {
		return this.tabuList.getRejectedCount();
	}

	public int getTabuAcceptedCount() {
		return this.tabuList.getAcceptedCount();
	}

	public int getChromosomesCount() {
		return this.controllersGenerated;
	}

	public RobotPopulation<ODNEATGenome> getPopulation() {
		return this.population;
	}

	public int getControllersGenerated() {
		return this.controllersGenerated;
	}

	public void transmitGenome(ODNEATGenome e, OnlineEA<ODNEATGenome> otherInstance) {
		ODNEATGenome toTransmit = e.copy();
		otherInstance.receiveSingleGenome(toTransmit);
	}

	public void receiveSingleGenome(ODNEATGenome g){
		if(!this.receivedInformation.contains(g)){
			this.receivedInformation.add(g);
		}
	}

	@Override
	public ODNEATGenome getActiveGenome() {
		return current;
	}

	public void executeOnlineEvolution(Environment environment, Collection<Robot> robots, double time){
		if(this.conductingEvolution){

			double energy = robot.getParameterAsDouble(ENERGY).doubleValue();
			//see if there is any robot in range
			LinkedList<Robot> closeRobots = getCloseRobots(robots);
			//broadcast 
			broadcastController(closeRobots);
			//process genomes received.
			this.processGenomesReceived(this.receivedInformation);
			this.receivedInformation.clear();
			//update the energy level.
			energy = updateEnergyLevel(energy, environment);
			
			//we count the "age" of the controller
			this.increaseAge();
			analyseTaskPerformance(energy, time);
		}
		else{
			double energy = robot.getParameterAsDouble(ENERGY).doubleValue();
			energy = this.updateEnergyLevel(energy, environment);
			this.increaseAge();

			//just update the energy, there is no further evolution.
			energy = eval.limitEnergyLevel(energy);
			updateCurrentController(time, energy);
			this.robot.setParameter(ENERGY, energy);
		}
	}

	protected void increaseAge() {
		age++;
	}

	protected void analyseTaskPerformance(double energy, double time) {
		if(energy <= 0){
			if(getAge() >= MATURATION_PERIOD){
				this.generateNewController();
				//the default virtual energy level was already set the method "generate new controller".
				energy = current.getEnergyLevel();
			}
			else{
				energy = 0;
				this.current.setEnergyLevel(energy);
			}
		}
		else{
			energy = eval.limitEnergyLevel(energy);
			this.current.setEnergyLevel(energy);
		}
		updateCurrentController(time, energy);
		this.robot.setParameter(ENERGY, energy);
	}

	protected void updateCurrentController(double currentTime, double energyValue) {
		if(currentTime % CONTROLLER_FREQUENCY_UPDATE == 0){
			this.updateGenome(this.current, energyValue);
		}
	}

	private void generateNewController() {
		current = this.reproduce();
		this.updateGenome(current, current.getEnergyLevel());
		@SuppressWarnings("unchecked")
		OnlineController<ODNEATGenome> controller = (OnlineController<ODNEATGenome>) this.robot.getController();
		controller.updateStructure(current);
		this.resetAge();
	}


	protected void resetAge() {
		this.setAge(0);
	}

	private void setAge(int newAge) {
		this.age = newAge;
	}

	public int getAge(){
		return age;
	}

	protected double updateEnergyLevel(double currentEnergy, Environment env) {
		return eval.updateEnergyLevel(currentEnergy, this.robot, env);
	}

	protected void broadcastController(LinkedList<Robot> closeRobots) {
		if(this.willBroadcastGenome()){
			for(Robot r : closeRobots){
				//System.out.println("broadcasting...");
				OnlineController<?> robotController = (OnlineController<?>) r.getController();
				ODNEAT otherInstance = (ODNEAT) robotController.getEAInstance();

				//the method that transmits the genome is responsible for making a copy of the genome.
				this.transmitGenome(current, otherInstance);
			}
		}
	}

	protected LinkedList<Robot> getCloseRobots(Collection<Robot> robots) {
		LinkedList<Robot> closeRobots = new LinkedList<Robot>();
		Iterator<Robot> iterator = robots.iterator();
		Vector2d coord = robot.getPosition();
		while(iterator.hasNext()){
			Robot r = (Robot) iterator.next();
			//it's not this robot
			if(r.getId() != this.robot.getId()){
				double distance = coord.distanceTo(r.getPosition());
				//System.out.println("d: " + distance);
				//System.out.println(r.getPosition().toString());
				if(distance <= BROADCAST_RANGE){
					closeRobots.add(r);
				}
			}
		}
		return closeRobots;
	}

	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("id: " + this.robotId + "\n");
		builder.append("pop-size: " + this.population.getCurrentPopulationSize() + "\n");
		builder.append("tabu-list-size: " + this.tabuList.getCurrentSize() + 
				"; acc: " + tabuList.getAcceptedCount()
				+ "; rej: " + tabuList.getRejectedCount() + "\n");
		builder.append("species: " + this.population.getSpeciesList().size() + "\n");
		ODNEATGenome g = this.getActiveGenome();
		builder.append("active-genome: ID: " + g.getId() + "; " +
				"E-LEVEL: " + g.getEnergyLevel() + "; " +
				"FIT: " + g.getFitness() + "; " + 
				"ADJ-FIT: " + g.getAdjustedFitness() + "\n");
		builder.append("total nodes: " + g.getNumberOfNodeGenes() + "\n");
		builder.append("total connections: " + g.getNumberOfLinkGenes(true) + "\n");
		builder.append("age: " + (this.age/10) + " secs \n");
		builder.append("===========================\n");
		return builder.toString();
	}

	@Override
	public ODNEATGenome resetParametersForEvaluation(Object activeGenome) {
		ODNEATGenome g = (ODNEATGenome) activeGenome;
		g.setEnergyLevel(this.eval.getDefaultEnergyValue(robot));
		g.setFitness(0);
		g.setAdjustedFitness(0);
		g.setUpdatesCount(0);

		this.robot.setParameter(ENERGY, new Double(g.getEnergyLevel()));

		return g;
	}

	public void setRobot(Robot robot){
		this.robot = robot;
		this.robotId = robot.getId();
	}

	@Override
	public Robot getRobotInstance() {
		return this.robot;
	}

}

