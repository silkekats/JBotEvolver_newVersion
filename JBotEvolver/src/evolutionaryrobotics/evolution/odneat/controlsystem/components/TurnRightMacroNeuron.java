package evolutionaryrobotics.evolution.odneat.controlsystem.components;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

//import org.encog.engine.network.activation.ActivationSteepenedSigmoid;

public class TurnRightMacroNeuron extends ProgrammedMacroNeuron implements Serializable{

	protected String macroId;
	protected final int UNITS_RIGHT_MOVEMENT;
	protected double priority, actionLeft, actionRight;

	protected boolean executing = false;
	protected int timesExecuted = 0;

	protected final double LEFT_ACTION = 0.1;
	protected final double RIGHT_ACTION = 0;

	public TurnRightMacroNeuron(long innovationNumber, String macroId, int unitsMovement){
		this.innovationNumber = innovationNumber;
		this.macroId = new String(macroId);
		this.UNITS_RIGHT_MOVEMENT = unitsMovement >= 1 ? unitsMovement : 1;
		this.type = Neuron.PROGRAMMED_MACRO_NEURON;
		this.incomingSynapses = new ArrayList<Synapse>();
		this.incomingNeurons = new ArrayList<Neuron>();
		this.outgoingConnections = new HashMap<Long, String>();
	}

	@Override
	protected double getActivationValue(long synapseId) {
		//System.out.println(Arrays.toString(this.outgoingConnections.values().toArray()));
		String key = this.outgoingConnections.get(synapseId);
		if(key.equals("a1"))
			return actionLeft;

		if(key.equals("a2"))
			return actionRight;

		return 0;
	}

	@Override
	protected double getPriorityValue(long synapseId) {
		return this.priority;
	}

	@Override
	public void step() {
		if(!this.executing){
			double averageWeightedReading = 0;
			for(int i = 0; i < this.incomingSynapses.size(); i++){
				averageWeightedReading += incomingSynapses.get(i).getWeight() 
						* this.incomingNeurons.get(i).getActivationValue();
			}

			averageWeightedReading /= this.incomingSynapses.size();

			averageWeightedReading += this.BIAS;
			double[] actValue = new double[]{averageWeightedReading};
			//new ActivationSteepenedSigmoid().activationFunction(actValue, 0, 1);
			averageWeightedReading = actValue[0];
			
			this.priority = averageWeightedReading;
			if(priority > 0){
				this.timesExecuted = this.UNITS_RIGHT_MOVEMENT;
				this.actionLeft = this.LEFT_ACTION;
				this.actionRight = this.RIGHT_ACTION;
				this.executing = true;
			}
		}
		else {
			this.timesExecuted--;
			if(this.timesExecuted <= 0)
				this.reset();
		}
	}

	protected double clampValueBetween(double minValue, double maxValue, double value) {
		if(value < minValue)
			value = minValue;
		else if(value > maxValue)
			value = maxValue;

		return value;
	}

	@Override
	/**
	 * should not be used, but instead "getActivationValue(long synapseId)" because
	 * this neurons is expected to emit signals to two other output neurons.
	 */
	public double getActivationValue() {
		return 0;
	}

	@Override
	public void reset() {
		this.executing = false;
		this.timesExecuted = 0;
		this.actionLeft = 0;
		this.actionRight = 0;
		this.priority = 0;
	}
	
	@Override
	public String getMacroId() {
		return this.macroId;
	}

	@Override
	public double getAverageActivationValue() {
		return this.priority;
	}
	
	@Override
	public double getBias() {
		return 0;
	}
	
	public boolean containsNeuron(long id){
		return id == this.innovationNumber;
	}

}

