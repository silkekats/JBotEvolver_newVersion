package evolutionaryrobotics.evolution.odneat.controlsystem;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;


import evolutionaryrobotics.evolution.odneat.controlsystem.components.ProgrammedMacroNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.MergedSynapse;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.NeuralNetLayer;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.Neuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.StandardNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.Synapse;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

/**
 *
 *
 */

public class MacroNetwork extends NeuralNetwork implements Serializable{

	private static final long serialVersionUID = 1L;
	private NeuralNetLayer[] layers;
	//for improving efficiency
	//private ArrayList<BPTeachingMacroneuron> teaching;

	public MacroNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		this.create(inputs, outputs);
	}

	public MacroNetwork(NeuralNetLayer[] layers) {
		this.layers = layers;
	}

	@Override
	public void create(Vector<NNInput> inputs, Vector<NNOutput> outputs) {
		super.create(inputs, outputs);
	}

	@Override
	protected double[] propagateInputs(double[] inputValues) {
		return this.processInputs(inputValues);
	}

	public double[] processInputs(double[] input) {
		//System.out.println("I: " + Arrays.toString(input));
		ArrayList<Neuron> inputNeurons = layers[0].getNeurons();
		for(int i = 0; i < input.length; i++){
			((StandardNeuron) inputNeurons.get(i)).setActivationValue(input[i]);
		}
		for(NeuralNetLayer layer : layers)
			layer.step();

		//we already have the output values
		double[] outputValues = layers[layers.length - 1].getNeuronActivations();
		
		return outputValues;
	}



	@Override
	public void reset() {
		for(NeuralNetLayer layer : layers){
			layer.reset();
		}
	}


	public void controlStep(double time) {
		super.controlStep(time);
	}

	public void updateStructure(MacroNetwork newStructure) {
		this.layers = newStructure.layers;
	}

	public int getTotalNumberOfNeurons() {
		int count = 0;
		for(NeuralNetLayer l : this.layers){
			ArrayList<Neuron> neurons = l.getNeurons();
			count += neurons.size();
		}
		return count;
	}

	public int getTotalNumberOfConnections() {
		ArrayList<Long> connectionIds = new ArrayList<Long>();
		for(NeuralNetLayer l : this.layers){
			ArrayList<Neuron> neurons = l.getNeurons();
			for(Neuron n : neurons){
				ArrayList<Synapse> connections = n.getIncomingConnections();
				for(Synapse s : connections){
					if(!connectionIds.contains(s.getInnovationNumber()))
						connectionIds.add(s.getInnovationNumber());
				}
			}
		}
		return connectionIds.size();
	}

	public double[] getInputReadings() {
		return this.inputNeuronStates.clone();
	}

	public double[] getOutputReadings() {
		return this.getOutputNeuronStates().clone();
	}

	public ArrayList<String> getListOfActiveMacros() {
		ArrayList<String> result = new ArrayList<String>();
		//we know the input layer will never have any macro neuron, skip it
		for(int i = 1; i < this.layers.length; i++){
			NeuralNetLayer layer = layers[i];
			for(Neuron n : layer.getNeurons()){
				double averageValue = 0;
				String macroId = null;
				if(n instanceof ProgrammedMacroNeuron){
					ProgrammedMacroNeuron macro = (ProgrammedMacroNeuron) n;
					averageValue = macro.getAverageActivationValue();
					macroId = new String(macro.getMacroId());
				}
				
				if(averageValue > 0){
					result.add(macroId);
				}
			}
		}
		return result;
	}

	public ArrayList<Double> getMacroActivationValues() {
		ArrayList<Double> result = new ArrayList<Double>();
		//we know the input layer will never have any macro neuron, skip it
		for(int i = 1; i < this.layers.length; i++){
			NeuralNetLayer layer = layers[i];
			for(Neuron n : layer.getNeurons()){
				double averageValue = 0;
				if(n instanceof ProgrammedMacroNeuron){
					ProgrammedMacroNeuron macro = (ProgrammedMacroNeuron) n;
					averageValue = macro.getAverageActivationValue();
				}
				if(averageValue > 0){
					result.add(averageValue);
				}
			}
		}
		return result;
	}

	public ArrayList<Synapse> getAllLinks() {
		ArrayList<Synapse> links = new ArrayList<Synapse>();
		for(int i = 0; i < this.layers.length; i++){
			NeuralNetLayer layer = layers[i];
			for(Neuron n : layer.getNeurons()){
				ArrayList<Synapse> incoming = n.getIncomingConnections();
				for(Synapse s : incoming){
					if(!links.contains(s)){
						links.add(s);
					}
				}
			}
		}
		return links;
	}

}
