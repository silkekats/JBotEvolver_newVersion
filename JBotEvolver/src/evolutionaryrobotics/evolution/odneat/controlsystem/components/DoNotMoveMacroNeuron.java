package evolutionaryrobotics.evolution.odneat.controlsystem.components;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DoNotMoveMacroNeuron extends ProgrammedMacroNeuron implements Serializable{

	protected String macroId;

	//fixed priority behaviour
	protected final double MOVEMENT_PRIORITY = 1.0;


	public DoNotMoveMacroNeuron(long innovationNumber, String macroId){
		this.macroId = new String(macroId);
		this.innovationNumber = innovationNumber;
		this.type = Neuron.PROGRAMMED_MACRO_NEURON;
		this.incomingSynapses = new ArrayList<Synapse>();
		this.incomingNeurons = new ArrayList<Neuron>();
		this.outgoingConnections = new HashMap<Long, String>();
	}

	protected double getActivationValue(long synapseId){
		return 0.0;
	}

	protected double getPriorityValue(long synapseId){
		return MOVEMENT_PRIORITY;
	}

	@Override
	public void step() {

	}

	@Override
	public double getActivationValue() {
		return 0.0;
	}

	@Override
	public void reset() {
	}

	@Override
	public String getMacroId() {
		return this.macroId;
	}
	
	@Override
	public double getAverageActivationValue() {
		return MOVEMENT_PRIORITY;
	}

	@Override
	public double getBias() {
		return 0;
	}
	
	public boolean containsNeuron(long id){
		return id == this.innovationNumber;
	}
}

