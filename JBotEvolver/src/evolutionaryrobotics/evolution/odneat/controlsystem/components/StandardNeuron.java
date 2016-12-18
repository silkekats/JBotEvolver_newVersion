package evolutionaryrobotics.evolution.odneat.controlsystem.components;



import java.io.Serializable;
import java.util.ArrayList;

import evolutionaryrobotics.evolution.neat.nn.core.ActivationFunction;

//import org.encog.engine.network.activation.ActivationFunction;

public class StandardNeuron extends Neuron implements Serializable{

	protected ActivationFunction activationFunction;
	protected double activation = 0;

	public StandardNeuron(long innovationNumber, int type, ActivationFunction function, double bias){
		this.innovationNumber = innovationNumber;
		this.type = type;
		this.activationFunction = function;
		this.incomingNeurons = new ArrayList<Neuron>();
		this.incomingSynapses = new ArrayList<Synapse>();
		this.BIAS = bias;
	}

	@Override
	public void step() {
		if(this.type == Neuron.INPUT_NEURON)
			return;

		//a neuron that performs a weighted sum of the inputs
		double currentActivation = 0;
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			currentActivation += incomingSynapses.get(i).getWeight() * incomingNeurons.get(i).getActivationValue();
		}

		currentActivation += this.BIAS;
		double[] value = new double[]{currentActivation};
		//activationFunction.activationFunction(value, 0, 1);
		this.activation = value[0];
		/*if(Double.isInfinite(activation) || Double.isNaN(activation)){
			System.out.println("VALUE OF STD-NEURON: " + activation);
			System.exit(0);
		}*/
	}

	@Override
	public double getActivationValue() {
		return activation;
	}

	//only used for setting the "activation value" of the input neurons.
	public void setActivationValue(double newActivationValue){
		if(this.type == Neuron.INPUT_NEURON){
			this.activation = newActivationValue;
		}
		else {
			try {
				throw new Exception("invalid operation");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sortIncomingConnections() {
		//being a standard neuron, the incoming connections do not have to be sorted in any particular way
	}

	public void reset(){
		this.activation = 0;
	}

	@Override
	public double getBias() {
		return BIAS;
	}
}

