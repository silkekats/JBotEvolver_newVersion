package evolutionaryrobotics.evolution;

import java.util.Random;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.OnlineEA;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ALGDescriptor;

//import org.encog.Encog;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import evolutionaryrobotics.ExtendedJBotEvolver;
import evolutionaryrobotics.populations.OnlineEvoShallowPopulation;
import taskexecutor.results.OnlineEvolutionResult;
import taskexecutor.tasks.OnlineEvolutionTask;

public class OnlineEvolution extends Evolution {

	protected boolean supressMessages = false;
	protected DiskStorage[] diskStorages;
	protected String output = "";
	/**
	 * the population is *only* used to save/serialize the instances of the online EAs
	 */
	protected OnlineEvoShallowPopulation[] populations;
	protected Population pop;
	protected int tasks;
	protected ALGDescriptor descriptor;

	protected final String RUN_SUFFIX = "run_";
	
	public OnlineEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor,
			Arguments args) throws Exception {
		super(jBotEvolver, taskExecutor, args);
		Arguments populationArguments = jBotEvolver.getArguments().get("--population"), 
				outputArguments = jBotEvolver.getArguments().get("--output"),
				evolutionArguments = jBotEvolver.getArguments().get("--evolution");

		this.tasks = evolutionArguments.getArgumentAsIntOrSetDefault("runs", 10);
	

		this.descriptor = new ALGDescriptor(evolutionArguments.getArgumentAsStringOrSetDefault("path", "forageDNEAT.conf"));

		supressMessages = args.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;
		
		this.populations = new OnlineEvoShallowPopulation[tasks];
		this.diskStorages = new DiskStorage[this.tasks];
		
		output = outputArguments.getCompleteArgumentString();
		Random runsSeedGenerator = new Random(jBotEvolver.getRandomSeed());
		
		for(int i = 0; i < this.tasks; i++){
			populations[i] = (OnlineEvoShallowPopulation) Population.getPopulation(populationArguments);
			//setting the seed for evolution
			populations[i].setGenerationRandomSeed(runsSeedGenerator.nextLong());
			if (outputArguments != null) {
				diskStorages[i] = new DiskStorage(output + this.RUN_SUFFIX + (i+1));
				try {
					diskStorages[i].start();
					diskStorages[i].saveCommandlineArguments(jBotEvolver.getArguments());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}		
	}

	@Override
	public void executeEvolution() {
		print("initialising evolution...");

		taskExecutor.prepareArguments(jBotEvolver.getArguments());	
		taskExecutor.setTotalNumberOfTasks(this.tasks);
		for(int i = 0; i < tasks; i++){
			taskExecutor.addTask(new OnlineEvolutionTask(
					new ExtendedJBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed()),
					populations[i].getGenerationRandomSeed(), i, descriptor, this.diskStorages[i].getOutputDirectory()));
					pop = populations [i];
					setCurrentNumberOfGen();
		}

		int tasksToComplete = tasks;
		while(tasksToComplete-- > 0){
			
			OnlineEvolutionResult result = (OnlineEvolutionResult) taskExecutor.getResult();
			int id = result.getResultId();

			print("RUN " + id + "\n");
			for(OnlineEA<?> ea : result.getInstances()){
				populations[id].addInstance(ea);
				print(ea.toString());
			}
			populations[id].computeFitnessStats();
			print("!");
			print("\nHighest: "+populations[id].getHighestFitness()+
					"\tAverage: "+populations[id].getAverageFitness()+
					"\tLowest: "+populations[id].getLowestFitness()+"\n");

			try {
				diskStorages[id].savePopulation(populations[id]);
				diskStorages[id].close();
			} catch(Exception e) {e.printStackTrace();}

			populations[id].clearListOfInstances();
		}

		//Encog.getInstance().shutdown();
		//System.out.println("DONE!");
	}

	protected void print(String s) {
		if(!supressMessages)
			System.out.print(s);
	}

	
	public Population getPopulation() {
		//System.out.println("fout!!!!");
		return pop;
	}
	
	public void setCurrentNumberOfGen() {
		pop.setNumberOfGenerations(100);
	}
}


