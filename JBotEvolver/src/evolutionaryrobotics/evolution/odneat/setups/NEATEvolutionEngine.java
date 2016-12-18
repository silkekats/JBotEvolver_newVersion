package evolutionaryrobotics.evolution.odneat.setups;


import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import taskexecutor.TaskExecutor;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.ExtendedJBotEvolver;

public class NEATEvolutionEngine {

	protected static final String OBJECTIVES_KEY = "INSERT_OBJECTIVES";
	protected static final String WEIGHTS_KEY = "INSERT_WEIGHTS";
	protected static final String OUTPUT_KEY = "INSERT_OUTPUT";

	public static void main(String[] args) throws Exception {
		int runFrom = 1, runTo = 30;
		/*String[] confFiles = new String[]{"conf_examples/navigation_static_obstacles.conf",
				"conf_examples/navigation_random_obstacles.conf"};*/
		String[] confFiles = new String[]{
				"confs/neat/neat_turn_left_behaviour.conf",
				"confs/neat/neat_turn_right_behaviour.conf", 
				"confs/neat/neat_move_forward_behaviour.conf"};
		
		int[] objectives = {1};
		String[] weights = new String[]{"1.0"};

		for(String confFile : confFiles){
			System.out.println(confFile);
			for(int objectiveIndex = 0; objectiveIndex < objectives.length; objectiveIndex++){
				for(int run = runFrom; run <= runTo; run++){
					System.out.println("RUN: " + run + "/" + runTo);
					double time = System.currentTimeMillis();
					String paramsFile = processConfigurationFile(confFile, 
							objectives[objectiveIndex], weights[objectiveIndex], run);
					args = new String[]{paramsFile};

					ExtendedJBotEvolver jBotEvolver = new ExtendedJBotEvolver(args);
					TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
					taskExecutor.start();
					Evolution evo = Evolution.getEvolution(jBotEvolver, taskExecutor, 
							jBotEvolver.getArguments().get("--evolution"));
					evo.executeEvolution();
					taskExecutor.stopTasks();

					double timeSeconds = (System.currentTimeMillis() - time)/1000;
					System.out.println(timeSeconds + " seconds.");
				}
			}
		}
	}

	private static String processConfigurationFile(String confFile, int numberOfObjectives,
			String objectivesWeights, int currentRun) throws IOException {
		String outputDirectory = confFile.replace(".conf","_v" + currentRun);
		String newConfFile = outputDirectory + ".conf";
		outputDirectory = "evolution_macro_neurons/" + outputDirectory.replace("confs/", "");
		
		Scanner sc = new Scanner(new FileReader(confFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				newConfFile));
		
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			line = line.replace(OBJECTIVES_KEY, String.valueOf(numberOfObjectives));
			line = line.replace(WEIGHTS_KEY, objectivesWeights);
			line = line.replace(OUTPUT_KEY, outputDirectory);
			writer.write(line);
			writer.newLine();
		}
		sc.close();
		writer.close();
		
		return newConfFile;
	}
}

