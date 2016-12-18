package evolutionaryrobotics.evolution.odneat.controlsystem.components;


import java.io.Serializable;
import java.util.ArrayList;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ArbitratorMacroNodeGene.FUNCTION;

public class ArbitratorMacroNeuron extends Neuron implements Serializable{

	private FUNCTION function;
	private double activationValue = 0;
	private int type;

	public ArbitratorMacroNeuron(long innovationNumber, FUNCTION function) {
		this.innovationNumber = innovationNumber;
		this.type = ARBITRATOR_MACRO_NEURON;
		this.incomingSynapses = new ArrayList<Synapse>();
		this.incomingNeurons = new ArrayList<Neuron>();
		this.function = function;
	}
	
	public int getType(){
		return type;
	}

	@Override
	public void step() {
		double[] receivedValues = new double[this.incomingSynapses.size()];
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			receivedValues[i] = incomingSynapses.get(i).getWeight() * incomingNeurons.get(i).getActivationValue();
		}
		
		computeActivation(receivedValues);
	}

	private void computeActivation(double[] receivedValues) {
		switch(function){
		case MAX:
			computeMaxValue(receivedValues);
			break;
		case MIN:
			computeMinValue(receivedValues);
			break;
		case AVG:
			computeAvgValue(receivedValues);
			break;
		}
	}

	private void computeAvgValue(double[] receivedValues) {
		double average = 0;
		for(double d : receivedValues){
			average += d;
		}
		
		average /= receivedValues.length;
		this.activationValue = average;
	}

	private void computeMinValue(double[] receivedValues) {
		double min = Double.MAX_VALUE;
		for(double d : receivedValues){
			min = Math.min(d, min);
		}
		
		this.activationValue = min;
	}

	private void computeMaxValue(double[] receivedValues) {
		double max = Double.MIN_VALUE;
		for(double d : receivedValues){
			max = Math.max(d, max);
		}
		
		this.activationValue = max;
	}

	@Override
	public double getActivationValue() {
		return this.activationValue;
	}

	@Override
	public void reset() {
		this.activationValue = 0;
	}

	@Override
	public double getBias() {
		return 0;
	}

	@Override
	public void sortIncomingConnections() {}

}

