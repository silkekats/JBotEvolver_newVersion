package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;

import evolutionaryrobotics.evolution.odneat.controlsystem.OnlineController;
import evolutionaryrobotics.evaluationfunctions.ODNEATEvaluationFunction;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ALGDescriptor;
import evolutionaryrobotics.evolution.odneat.genotypePhenotypeMapping.GPMapping;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.ExtendedJBotEvolver;
import taskexecutor.results.OnlineEvolutionResult;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.tasks.JBotEvolverTask;

public class OnlineEvolutionTask extends JBotEvolverTask{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int taskId;
	protected ExtendedJBotEvolver evolver;
	protected Random random;
	protected ALGDescriptor descriptor;
	//protected ArrayList<>
	private ArrayList<Robot> robots;

	protected String outputDirectory;
	
	public OnlineEvolutionTask(ExtendedJBotEvolver jBotEvolver, long seed, int id, ALGDescriptor descriptor, 
			String outputDirectory) {
		super(jBotEvolver);
		this.taskId = id;
		this.evolver = jBotEvolver;
		this.random = new Random();
		this.descriptor = descriptor;
		this.outputDirectory = new String(outputDirectory);
	}

	@Override
	public void run() {
		Simulator simulator = evolver.createSimulator(new Random(random.nextLong()));
		simulator.setFileProvider(getFileProvider());

		evolver.getArguments().get("--environment").setArgument("fitnesssample", this.taskId);
		EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(evolver.getArguments().get("--evaluation"));
		//no other way to output info **during long simulations**
		if(eval instanceof ODNEATEvaluationFunction){
			ODNEATEvaluationFunction odneatFunc = (ODNEATEvaluationFunction) eval;
			odneatFunc.setOutputDirectory(this.outputDirectory);
		}
		
		//NOTE: little hack
		GPMapping.setOutputDirectory(outputDirectory);
	
		ArrayList<Robot> robots = evolver.createRobots(simulator);
		for(Robot r : robots){
			OnlineController<?> controller = (OnlineController<?>) r.getController();
			Arguments genomeArguments = new Arguments(evolver.getArguments().get("--controllers").getArgumentAsString("genome"));
			controller.initialise(random, r.getId(), descriptor, r, eval, genomeArguments);
		}
		simulator.addRobots(robots);

		simulator.addCallback(eval);
		simulator.simulate();
		
		this.robots = robots;
	}

	public Result getResult() {
		OnlineEvolutionResult result = new OnlineEvolutionResult(this.taskId);
		for(Robot r : this.robots){
			OnlineController<?> controller = (OnlineController<?>) r.getController();
			result.addEAInstance(controller.getEAInstance());
		}
		return result;
	}

}


