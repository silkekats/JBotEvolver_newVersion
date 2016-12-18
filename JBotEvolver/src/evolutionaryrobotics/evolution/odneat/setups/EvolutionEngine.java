package evolutionaryrobotics.evolution.odneat.setups;


import taskexecutor.TaskExecutor;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.ExtendedJBotEvolver;

public class EvolutionEngine {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		double time = System.currentTimeMillis();

		String[] paramsFile = {
				//high range for light sensor in deceptive phototaxis task
				"confs/sequential_phototaxis_evolution_alone_no_macros.conf",
				"confs/sequential_phototaxis_evolved_macros.conf",
				"confs/sequential_phototaxis_hierarchical_evolved_macros.conf",
				//low range
				"confs/sequential_phototaxis_evolution_alone_no_macros_low_range.conf",
				"confs/sequential_phototaxis_evolved_macros_low_range.conf",
				"confs/sequential_phototaxis_hierarchical_evolved_macros_low_range.conf",
				//dual task
				"confs/dual_task_evolution_alone_no_macros.conf",
				"confs/dual_task_evolved_macros.conf",
				"confs/dual_task_hierarchical_evolved_macros.conf"
		};

		for(String file : paramsFile){
			args = new String[]{new String(file)};

			ExtendedJBotEvolver jBotEvolver = new ExtendedJBotEvolver(args);
			TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(
					jBotEvolver, jBotEvolver.getArguments().get("--executor"));
			taskExecutor.start();
			Evolution evo = Evolution.getEvolution(jBotEvolver, taskExecutor, 
					jBotEvolver.getArguments().get("--evolution"));
			evo.executeEvolution();
			taskExecutor.stopTasks();

			double timeSeconds = (System.currentTimeMillis() - time)/1000;
			System.out.println(timeSeconds + " seconds.");
		}
		System.exit(0);
	}
}
