package evolutionaryrobotics.evolution.odneat.controlsystem;


import java.util.ArrayList;
import java.util.Random;

import evolutionaryrobotics.evaluationfunctions.ODNEATEvaluationFunction;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEAT;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.OnlineEA;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ALGDescriptor;
import evolutionaryrobotics.evolution.odneat.genotypePhenotypeMapping.GPMapping;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

public class MacroController extends Controller implements OnlineController<ODNEATGenome> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected GPMapping<MacroGenome, MacroNetwork> map;
	protected ODNEAT instance;
	protected MacroNetwork network;
	
	@SuppressWarnings("unchecked")
	public MacroController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		network = (MacroNetwork) NeuralNetwork.getNeuralNetwork(simulator, robot, 
				new Arguments(args.getArgumentAsString("network")));
		
		map = GPMapping.getGPMapping(new Arguments(args.getArgumentAsString("mapping")));
	}

	@Override
	public void initialise(Random random, int robotId,
			ALGDescriptor descriptor, Robot r, EvaluationFunction eval, Arguments genomeArguments) {
		int inputs = this.network.getNumberOfInputNeurons(), 
				outputs = this.network.getNumberOfOutputNeurons();
		System.out.println("I: " + inputs + "; O: " + outputs);
		ODNEATEvaluationFunction func = (ODNEATEvaluationFunction) eval;
		this.instance = new ODNEAT(random, robotId, descriptor, r, func, inputs, outputs, genomeArguments);
		this.updateStructure(instance.getActiveGenome());
		
	}

	@Override
	public OnlineEA<ODNEATGenome> getEAInstance() {
		return instance;
	}

	@Override
	public void updateStructure(Object object) {
		this.network.updateStructure(map.decode((MacroGenome) object));
	}

	@Override
	public void setOnlineEAInstance(OnlineEA<?> instance) {
		this.instance = (ODNEAT) instance;
	}
	
	@Override
	public void begin() {
	}

	@Override
	public void controlStep(double time) {
		network.controlStep(time);
	}

	@Override
	public void end() {
	}

	@Override
	public void reset() {
		super.reset();
		network.reset();
	}

	public MacroNetwork getNetwork() {
		return network;
	}
	
	public String getControllerId(){
		return new String(this.instance.getActiveGenome().getId());
	}

	public double[] getInputReadings() {
		return network.getInputReadings();
	}

	public double[] getOutputReadings() {
		return network.getOutputReadings();
	}

	public ArrayList<String> getListOfActiveMacros() {
		return network.getListOfActiveMacros();
	}

	public ArrayList<Double> getMacroActivationValues() {
		return network.getMacroActivationValues();
	}

}

