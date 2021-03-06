package evolutionaryrobotics.neuralnetworks;


import java.util.ArrayList;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import simulation.Simulator;
import simulation.robot.Robot;
//import simulation.robot.behaviors.Behavior;
import simulation.util.Arguments;

public class BehaviorController extends NeuralNetworkController implements FixedLenghtGenomeEvolvableController {
	
	private boolean[] parallelController;
	protected ArrayList<Controller> subControllers = new ArrayList<Controller>();
	protected ArrayList<Controller> parallelSubControllers = new ArrayList<Controller>();
	protected int currentSubNetwork = 0;
	boolean keepFeeding = false;
	boolean resetChosen = true;
	boolean debugMax = false;
	private int fixedOutput = -1;
	
	public BehaviorController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		setupControllers(simulator, args);
		fixedOutput = args.getArgumentAsIntOrSetDefault("fixedoutput", fixedOutput);
	}
	
	@Override
	public void controlStep(double time) {
		
		if(!subControllers.isEmpty()) {
			int output = chooseOutput();
			
			if(fixedOutput >= 0)
				output = fixedOutput;
			
			boolean skip = false;
			
//			if(subControllers.get(currentSubNetwork) instanceof Behavior) {
//				//Behavior b = (Behavior)subControllers.get(currentSubNetwork);
//				skip = b.isLocked();
//			}
			
			if(output != currentSubNetwork && !skip) {
				currentSubNetwork = output;
				if(resetChosen) {
					subControllers.get(currentSubNetwork).reset();
				}
			}
			
			neuralNetwork.controlStep(time);
			
			//Feed these first. The chosen network should be the first to act.
			//This will not work correctly if some of the behavior primitive networks activate
			//some actuator that others do... I'm relying on the last controller to override the
			//actuator values.
			if(keepFeeding) {
				for(int i = 0 ; i < subControllers.size() ; i++) {
					if(i != currentSubNetwork) {
						subControllers.get(i).controlStep(time,neuralNetwork.getOutputNeuronStates()[i]);
					}
				}
			}
			
			subControllers.get(currentSubNetwork).controlStep(time,neuralNetwork.getOutputNeuronStates()[currentSubNetwork]);
		}
		
		if(!parallelSubControllers.isEmpty())
			executeParallelNetworks(time);
	}
	
	private void executeParallelNetworks(double time) {
		for(int i = 0 ; i < parallelSubControllers.size(); i++)
			parallelSubControllers.get(i).controlStep(time,neuralNetwork.getOutputNeuronStates()[currentSubNetwork]);
	}
	
	private int chooseOutput() {
		
		double[] outputStates = neuralNetwork.getOutputNeuronStates();
		
		int maxIndex = -1;
		
		do{
			maxIndex++;
		} while(parallelController[maxIndex]);
		
		for(int i = maxIndex+1 ; i < outputStates.length ; i++)
			if((outputStates[i] > outputStates[maxIndex] && !parallelController[i]) || (debugMax && outputStates[i] >= outputStates[maxIndex]))
				maxIndex = i;
		return maxIndex;
	}
	
	@Override
	public void reset() {
		neuralNetwork.reset();
		for(Controller c : subControllers)
			c.reset();
	}
	
	private void setupControllers(Simulator simulator, Arguments args) {
		
		if(!args.getArgumentAsString("subcontrollers").isEmpty()) {
			Arguments subControllerArgs = new Arguments(args.getArgumentAsString("subcontrollers"));
			
			parallelController = new boolean[subControllerArgs.getNumberOfArguments()];
			
			for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
				
				boolean parallel = subControllerArgs.getArgumentAt(i).startsWith("_");
				parallelController[i] = parallel;
				
				Arguments currentSubControllerArgs = new Arguments(subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
				
				Controller c = Controller.getController(simulator, robot, currentSubControllerArgs);
				
				if(parallel) 
					parallelSubControllers.add(c);
				
				subControllers.add(c);
			}
		}
		
		//Setting up main Controller
		neuralNetwork = (NeuralNetwork)NeuralNetwork.getNeuralNetwork(simulator, robot, new Arguments(args.getArgumentAsString("network")));
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
		
		resetChosen = args.getArgumentAsIntOrSetDefault("resetchosen", 1) == 1;
		keepFeeding = args.getArgumentAsIntOrSetDefault("keepfeeding", 0) == 1;
		debugMax = args.getArgumentAsIntOrSetDefault("debugmax", 0) == 1;
	}
	
	public ArrayList<Controller> getSubControllers() {
		return subControllers;
	}
	
	public int getCurrentSubNetwork() {
		return currentSubNetwork;
	}
}
