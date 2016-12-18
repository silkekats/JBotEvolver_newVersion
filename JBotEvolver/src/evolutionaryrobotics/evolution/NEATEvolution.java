package evolutionaryrobotics.evolution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;

import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.neat.NEATGeneticAlgorithmWrapper;
import evolutionaryrobotics.evolution.neat.PreEvaluatedFitnessFunction;
import evolutionaryrobotics.evolution.neat.core.InnovationDatabase;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.evolution.neat.core.mutators.NEATMutator;
import evolutionaryrobotics.evolution.neat.core.pselectors.TournamentSelector;
import evolutionaryrobotics.evolution.neat.core.xover.NEATCrossover;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATNPopulation;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.TaskExecutor;

public class NEATEvolution extends Evolution {
	
	public int numberPopulations=jBotEvolver.getArguments().get("--population").getArgumentAsInt("numberPopulations");
	public int socialLearning = jBotEvolver.getArguments().get("--population").getArgumentAsInt("socialLearning");
	
	public NEATGADescriptor[] descriptor = new NEATGADescriptor[numberPopulations];
	public NEATNPopulation[] population = new NEATNPopulation[numberPopulations] ;
	public DiskStorage[] diskStorage = new DiskStorage[numberPopulations];
	public String output = "";
	public DecimalFormat df = new DecimalFormat("#.##");
	public InnovationDatabase db;
	public Chromosome[] bestChromosomePop = new Chromosome[numberPopulations];
	File f;
	
	@ArgumentsAnnotation(name="path", defaultValue="test")
	private String outputfolder;
	

	public NEATEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
		
		outputfolder = jBotEvolver.getArguments().get("--environment").getArgumentAsString("path");
		//System.out.print(outputfolder);
		f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_currentRobot.log");
		
		FileWriter robotNbr;
		if(!f.exists()){
			try {
				robotNbr = new FileWriter(f, false);
				robotNbr.write("0\n");
				robotNbr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//init innovation database
		int p=0;
		for(p=0; p<numberPopulations; p++){
		population[p] = (NEATNPopulation) jBotEvolver.getPopulation();

		if (jBotEvolver.getArguments().get("--population").getArgumentIsDefined("generations"))
			population[p].setNumberOfGenerations(
					jBotEvolver.getArguments().get("--population").getArgumentAsInt("generations"));
		population[p].setGenerationRandomSeed(jBotEvolver.getRandomSeed());

		descriptor[p] = new NEATGADescriptor();
		configureDescriptor(descriptor[p], args, population[p]);
		
		

		if (jBotEvolver.getArguments().get("--output") != null) {
			output = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
			diskStorage[p] = new DiskStorage(jBotEvolver.getArguments().get("--output").getCompleteArgumentString(), p);
			try {
				diskStorage[p].start();
				diskStorage[p].saveCommandlineArguments(jBotEvolver.getArguments());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		}
	}

	@Override
	public void executeEvolution() {
		

		NEATGeneticAlgorithmWrapper[] algorithm = new NEATGeneticAlgorithmWrapper[numberPopulations] ;
		
		int p=0;
		for(p=0;p<numberPopulations;p++){
	

		int i = population[p].getNumberOfCurrentGeneration();

		if (i == 0) {
			algorithm[p] = new NEATGeneticAlgorithmWrapper(descriptor[p], this, p);
		} else {			
			algorithm[p] = new NEATGeneticAlgorithmWrapper(descriptor[p], this, p);
			//innovation database also loaded
			algorithm[p].loadPopulation(population[p].getNEATPopulation4J(), db);
		}

		algorithm[p].pluginFitnessFunction(new PreEvaluatedFitnessFunction(Collections.<Chromosome, Float> emptyMap()));
		algorithm[p].pluginCrossOver(new NEATCrossover());
		algorithm[p].pluginMutator(new NEATMutator());
		algorithm[p].pluginParentSelector(new TournamentSelector());

		if (i == 0) {
			algorithm[p].createPopulation();
			population[p].setGenerationRandomSeed(jBotEvolver.getRandomSeed());
			population[p].createRandomPopulation();
			population[p].setNEATPopulation4J((NEATPopulation4J) algorithm[p].population());
			population[p].getNEATPopulation4J().setSpecies(algorithm[p].getSpecies());
		} 

		if (!population[p].evolutionDone())
			taskExecutor.setTotalNumberOfTasks(
					(population[p].getNumberOfGenerations() - population[p].getNumberOfCurrentGeneration())
							* population[p].getPopulationSize());

		double highestFitness = population[p].getHighestFitness();
		
		
		//System.out.println("Init db " + population[p].getNEATPopulation4J().db.innovationId);
		db = algorithm[p].innovationDatabase();
		}
		
		
		

		
		
		
		while (!population[numberPopulations-1].evolutionDone() && executeEvolution) {
			
			
			
			p=0;
			for(p=0;p<numberPopulations;p++){
				
				FileWriter indexPopulation;
				try {
					indexPopulation = new FileWriter(f, false);
					indexPopulation.write(p+ "\n");
					indexPopulation.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			double d = Double.valueOf((population[p].getHighestFitness()));
			//System.out.print(d);

			taskExecutor.setDescription(output + " " + population[p].getNumberOfCurrentGeneration() + "/"
					+ population[p].getNumberOfGenerations() + " " + d);
			
			//maybe not neccesary
			algorithm[p].setInnovationDatabase(db);
			population[p].getNEATPopulation4J().setInnovationDatabase(db);
			
			
			algorithm[p].runEpoch();
			
			
			Chromosome[] fullPopulation = algorithm[p].givePopulation();
			
			//System.out.println(fullPopulation[1].fitness());
			
			try {
				diskStorage[p].savePopulation(population[p]);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			

			
			db = population[p].getNEATPopulation4J().db;
			
			
			// store the best chromosome while you can still reach it (store random etc)
			// save all fullPopulations and at *** exchange chromosomes
			int l;
			for(l=0 ; l<population[p].populationSize ; l++){
				if(fullPopulation[l].fitness()==population[p].bestFitness){
					bestChromosomePop[p] = fullPopulation[l];
					//System.out.println("BEST " + bestChromosomePop[p].genes());
				}
			}
			
			// ***
			
			}
			
			for(p=0;p<numberPopulations;p++){
				
				
			algorithm[p].setInnovationDatabase(db);
			population[p].getNEATPopulation4J().setInnovationDatabase(db);
			
				
			//new chromosomes are created >> check if they are speciated?? how to include new ones from other pops????
			Chromosome[] fullPopulation = algorithm[p].givePopulation();
			// replace last chromosome with best of other robot
			
			
			
			
			//add if statement whether social learning is active. 
			
			
			// HERE!!!! >>> there is a bug in the neat node link with species. Might be better to remove the species of the best chromosome. ???
			
			
			if(socialLearning==1){
			fullPopulation[population[p].populationSize-1]= bestChromosomePop[(p+(numberPopulations-1))%numberPopulations];
			}
			

			
			
			algorithm[p].runEvolutionCycle(fullPopulation);
			


			if (executeEvolution) {
				System.out.println("\nPopulation " + p + "\tGeneration " + population[p].getNumberOfCurrentGeneration() + "\tHighest: "
						+ population[p].getHighestFitness() + "\tAverage: " + population[p].getAverageFitness() + "\tLowest: "
						+ population[p].getLowestFitness());

				/*try {
					
					
					//System.out.print("Lukt dit? " + fullPopulation[1].fitness());
					//diskStorage.savePopulation(population);
				} catch (Exception e) {
					e.printStackTrace();
				}*/

				double highestFitness = population[p].getHighestFitness();
				//System.out.println("High fitness "+ p + " " +highestFitness);
			}
			

			if (population[numberPopulations-1].evolutionDone())
				break;
			
			// stats pop are reset
			population[p].createNextGeneration();
	
			
			// retrieve latest db and re init later. 
			db = population[p].getNEATPopulation4J().db;
			//System.out.println("New db " + population[p].getNEATPopulation4J().db.innovationId);
			
			}
			
			//  
			//population[0].getChromosomes()
			
		}
		
		//remove location file
		//diskStorage.removeFileLocation();
		
		String result = "obstacleavoidance";
		
		String result2 = "forage";
		
		
		for(p=0; p<numberPopulations;p++){
			
		
		
		File f1 = new File(result+"/_onlineLocation"+Integer.toString(p)+".log");
    	
    	if(f1.exists()){
    		f1.delete();
    	}	
				

    	File f = new File(result2 + "/_onlineLocation"+Integer.toString(p)+".log");
    	
    	if(f.exists()){
    		f.delete();
    	}
    	
    	File f2 = new File(result2 + "/_carriedPuck"+Integer.toString(p)+".log");
    	
    	if(f2.exists()){
    		f2.delete();
    	}
    	
    	File f3 = new File(result2 + "/_onlineLocationPucks"+Integer.toString(p)+".log");
    	
    	if(f3.exists()){
    		f3.delete();
    	}
    	
		}
    	
    	
		int a=0;
    	for(a=0;a<numberPopulations;a++){
		
    	InnovationDatabase db2 = population[a].getNEATPopulation4J().db;
		System.err.println("Innovation Database Stats - Hits: " + db2.hits + " - misses: " + db2.misses);
    	}
		

	}

	protected void configureDescriptor(NEATGADescriptor descriptor, Arguments args, Population population) {

		int popSize = population.getPopulationSize();

		int[] neurons = getInputOutputNeurons();

		int inputNodes = neurons[0];
		int outputNodes = neurons[1];
		
		//CROSSOVER/MUTATION PARAMETERS
		//double pXover = args.getArgumentAsDoubleOrSetDefault("pXover", 0.2);
		double pXover= jBotEvolver.getArguments().get("--population").getArgumentAsDouble("pXover"); 
		//double pMutation = args.getArgumentAsDoubleOrSetDefault("pMutation", 0.25);
		double pMutation = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("pMutation"); 
		//double pWeightReplaced = args.getArgumentAsDoubleOrSetDefault("pWeightReplaced", 0.0);
		double pWeightReplaced = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("pWeightReplaced"); 
		double pToggleLink = args.getArgumentAsDoubleOrSetDefault("pToggleLink", 0.0);
		//double maxPerturb = args.getArgumentAsDoubleOrSetDefault("maxPerturb", 0.5);
		double maxPerturb = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("maxPerturb"); 
		//double pMutateBias = args.getArgumentAsDoubleOrSetDefault("pMutateBias", 0.25); // was 0.3 but now equal to pMutation
		double pMutateBias = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("pMutation"); 
		//double maxBiasPerturb = args.getArgumentAsDoubleOrSetDefault("maxBiasPerturb", 0.5); // was 0.1 but is set equal to maxPerturb
		double maxBiasPerturb = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("maxPerturb"); 
		//double pAddLink = args.getArgumentAsDoubleOrSetDefault("pAddLink", 0.05);
		double pAddLink = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("pAddLink"); 
		// double pAddNode = args.getArgumentAsDoubleOrSetDefault("pAddNode", 0.03);
		double pAddNode = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("pAddNode"); 
		
		// SPECIE PARAMETERS
		//int specieCount = args.getArgumentAsIntOrSetDefault("specieCount", 5);
		int specieCount = jBotEvolver.getArguments().get("--population").getArgumentAsInt("specieCount"); 
		// int maxSpecieAge = args.getArgumentAsIntOrSetDefault("maxSpecieAge", 15);
		int maxSpecieAge = jBotEvolver.getArguments().get("--population").getArgumentAsInt("maxSpecieAge"); 
		// double excessCoeff = args.getArgumentAsIntOrSetDefault("excessCoeff", 1);
		double excessCoeff = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("excessCoeff"); 
		// double disjointCoeff = args.getArgumentAsIntOrSetDefault("disjointCoeff", 1);
		double disjointCoeff = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("disjointCoeff"); 
		// double weightCoeff = args.getArgumentAsDoubleOrSetDefault("weightCoeff", 0.4);
		double weightCoeff = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("weightCoeff"); 
		// double threshold = args.getArgumentAsDoubleOrSetDefault("threshold", 0.5);
		double threshold = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("threshold"); 
		// double thresholdChange = args.getArgumentAsDoubleOrSetDefault("thresholdChange", 0.05);
		double thresholdChange = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("thresholdChange"); 
		// int specieAgeThreshold = args.getArgumentAsIntOrSetDefault("specieAgeThreshold", 80);
		int specieAgeThreshold = (int) Math.round(maxSpecieAge * jBotEvolver.getArguments().get("--population").getArgumentAsDouble("specieAgeThreshold")); 
		// int specieYouthThreshold = args.getArgumentAsIntOrSetDefault("specieYouthThreshold", 10);
		int specieYouthThreshold = (int) Math.round(maxSpecieAge * jBotEvolver.getArguments().get("--population").getArgumentAsDouble("specieYouthThreshold")); 
		// double agePenalty = args.getArgumentAsDoubleOrSetDefault("agePenalty", 0.7);
		double agePenalty = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("agePenalty"); 
		// double youthBoost = args.getArgumentAsDoubleOrSetDefault("youthBoost", 1.2);
		double youthBoost = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("youthBoost"); 
		

		//OTHER PARAMETERS
		//double survialThreshold = args.getArgumentAsDoubleOrSetDefault("survialThreshold", 0.2);
		double survialThreshold = jBotEvolver.getArguments().get("--population").getArgumentAsDouble("survialThreshold"); 
		//boolean keepBestEver = args.getArgumentAsIntOrSetDefault("keepBestEver", 0) == 1;
		boolean keepBestEver = (jBotEvolver.getArguments().get("--population").getArgumentAsInt("keepBestEver")==1); 
		//boolean copyBest = args.getArgumentAsIntOrSetDefault("copyBest", 1) == 1;
		boolean copyBest = (jBotEvolver.getArguments().get("--population").getArgumentAsInt("copyBest")==1); 
		
		boolean featureSelection = args.getArgumentAsIntOrSetDefault("featureSelection", 0) == 1;
		int extraAlleles = args.getArgumentAsIntOrSetDefault("extraAlleles", 0);
		boolean eleEvents = args.getArgumentAsIntOrSetDefault("eleEvents", 0) == 1;
		double eleSurvivalCount = args.getArgumentAsDoubleOrSetDefault("eleSurvivalCount", 0.1);
		int eleEventTime = args.getArgumentAsIntOrSetDefault("eleEventTime", 1000);
		boolean recurrencyAllowed = args.getArgumentAsIntOrSetDefault("recurrencyAllowed", 1) == 1;
		double terminationValue = args.getArgumentAsDoubleOrSetDefault("terminationValue", 0.1);
		boolean naturalOrder = args.getArgumentAsIntOrSetDefault("naturalOrder", 0) == 1;
		

		descriptor.setPAddLink(pAddLink);
		descriptor.setPAddNode(pAddNode);
		descriptor.setPToggleLink(pToggleLink);
		descriptor.setPMutateBias(pMutateBias);
		descriptor.setPXover(pXover);
		descriptor.setPMutation(pMutation);
		descriptor.setInputNodes(inputNodes);
		descriptor.setOutputNodes(outputNodes);
		descriptor.setNaturalOrder(naturalOrder);
		descriptor.setPopulationSize(popSize);
		descriptor.setDisjointCoeff(disjointCoeff);
		descriptor.setExcessCoeff(excessCoeff);
		descriptor.setWeightCoeff(weightCoeff);
		descriptor.setThreshold(threshold);
		descriptor.setCompatabilityChange(thresholdChange);
		descriptor.setMaxSpecieAge(maxSpecieAge);
		descriptor.setSpecieAgeThreshold(specieAgeThreshold);
		descriptor.setSpecieYouthThreshold(specieYouthThreshold);
		descriptor.setAgePenalty(agePenalty);
		descriptor.setYouthBoost(youthBoost);
		descriptor.setSpecieCount(specieCount);
		descriptor.setPWeightReplaced(pWeightReplaced);
		descriptor.setSurvivalThreshold(survialThreshold);
		descriptor.setFeatureSelection(featureSelection);
		descriptor.setExtraFeatureCount(extraAlleles);
		descriptor.setEleEvents(eleEvents);
		descriptor.setEleSurvivalCount(eleSurvivalCount);
		descriptor.setEleEventTime(eleEventTime);
		descriptor.setRecurrencyAllowed(recurrencyAllowed);
		descriptor.setKeepBestEver(keepBestEver);
		descriptor.setTerminationValue(terminationValue);
		descriptor.setMaxPerturb(maxPerturb);
		descriptor.setMaxBiasPerturb(maxBiasPerturb);
		descriptor.setCopyBest(copyBest);
	}

	protected int[] getInputOutputNeurons() {
		Simulator sim = jBotEvolver.createSimulator();
		Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers"));

		int in = 0;
		int out = 0;

		if (c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController) c;
			in = controller.getNumberOfInputs();
			out = controller.getNumberOfOutputs();
		}

		return new int[] { in, out };
	}

	public JBotEvolver getJBotEvolver() {
		return jBotEvolver;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	@Override
	public Population getPopulation(int i) {
		// does this result in errors?
		return population[i];
	}

	public boolean continueExecuting() {
		return executeEvolution;
	}

}
