// not sure what happens here, I think they dont calculate fitness but update energy (odNEAT is implemented bit different). 
// If we change the ForagingEvaluationFunction to something decentralised, we don't need a different evaluation function for dNEAT

package evolutionaryrobotics.evaluationfunctions;


import evolutionaryrobotics.evolution.odneat.controlsystem.OnlineController;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEAT;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.results.StatisticsManager;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


public class ODNEATEvaluationFunction extends EvaluationFunction{


	private static final long serialVersionUID = 1L;
	protected double maxEnergyValue;
	protected double initialEnergyFactor;
	protected transient StatisticsManager stats;
	protected transient String outputDirectory;
	
	public ODNEATEvaluationFunction(Arguments args) {
		super(args);
		this.maxEnergyValue = args.getArgumentAsDoubleOrSetDefault("maxenergy", 100.0);
		this.initialEnergyFactor = args.getArgumentAsDoubleOrSetDefault("defaultfactor", 0.5);
		this.stats = new StatisticsManager();
	}

	@Override
	public void update(Simulator simulator) {
		//this.simulator = simulator;
		/*System.out.println(simulator == null);
		System.out.println(simulator.getEnvironment() == null);*/
		for(Robot r:  simulator.getRobots()){
			if(r.getController() instanceof OnlineController){
				OnlineController<?> controller = (OnlineController<?>) r.getController();
				ODNEAT instance = (ODNEAT) controller.getEAInstance();
				instance.executeOnlineEvolution(simulator.getEnvironment(), simulator.getRobots(), simulator.getTime());
			}
		}
	}
	
	public double limitEnergyLevel(double currentEnergy){
		if(currentEnergy > maxEnergyValue)
			currentEnergy = maxEnergyValue;
		else if(currentEnergy < 0)
			currentEnergy = 0;
		return currentEnergy;
	}
	
	public double getMaxEnergyValue(){
		return this.maxEnergyValue;
	}
	
	public double getInitialEnergyFactor(){
		return this.initialEnergyFactor;
	}

	public double updateEnergyLevel(double currentEnergy, Robot r, Environment env){
		return currentEnergy;
	}

	public double getDefaultEnergyValue(Robot r){
		return initialEnergyFactor;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}

