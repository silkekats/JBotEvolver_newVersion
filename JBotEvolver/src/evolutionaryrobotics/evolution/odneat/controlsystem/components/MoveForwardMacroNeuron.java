package evolutionaryrobotics.evolution.odneat.controlsystem.components;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MoveForwardMacroNeuron extends ProgrammedMacroNeuron implements Serializable{

	protected String macroId;
	protected double priority = 0, actionValue = 0;
	//the maximum speed of the robot

	//the number of units to move forward when the behaviour is executed.
	protected final int UNITS_FORWARD_MOVEMENT;

	//fixed priority behaviour
	protected final double MOVEMENT_PRIORITY = 0.5;

	//if the behaviour is executing.
	protected boolean executing = false;
	protected int timesExecuted = 0;

	public MoveForwardMacroNeuron(long innovationNumber, String macroId, int unitsForwardMovement){
		this.macroId = new String(macroId);
		this.innovationNumber = innovationNumber;
		this.type = Neuron.PROGRAMMED_MACRO_NEURON;
		this.incomingSynapses = new ArrayList<Synapse>();
		this.incomingNeurons = new ArrayList<Neuron>();
		//ensuring positive values.
		this.UNITS_FORWARD_MOVEMENT = unitsForwardMovement >= 1 ? unitsForwardMovement : 1;
		this.outgoingConnections = new HashMap<Long, String>();
		//System.out.println("units mov: " + this.UNITS_FORWARD_MOVEMENT);
	}

	//this method can be used when sending different instructions to different actuators
	//(not the case of this behaviour, moving forward = same speed on the two wheels).
	protected double getActivationValue(long synapseId){
		return actionValue;
	}

	protected double getPriorityValue(long synapseId){
		return this.priority;
	}

	@Override
	/**
	 * the behaviour assumes it is fed with readings of the robot's front sensors
	 */
	public void step() {
		if(!this.executing){
			double averageWeightedReading = 0;
			for(int i = 0; i < this.incomingSynapses.size(); i++){
				averageWeightedReading += incomingSynapses.get(i).getWeight() * this.incomingNeurons.get(i).getActivationValue();
				//averageWeightedReading += this.incomingNeurons.get(i).getActivationValue();
			}
			averageWeightedReading /= this.incomingSynapses.size();
			/*averageWeightedReading += this.BIAS;
			double[] actValue = new double[]{averageWeightedReading};
			new ActivationSteepenedSigmoid().activationFunction(actValue, 0, 1);
			averageWeightedReading = actValue[0];*/

			if(averageWeightedReading == 0.0){
				//System.out.println("triggered move forward");
				this.priority = MOVEMENT_PRIORITY;
				this.timesExecuted = this.UNITS_FORWARD_MOVEMENT;
				//action value = 1.0 = max value to the actuator.
				this.actionValue = 1.0;
				this.executing = true;
			}
		}
		else {
			this.timesExecuted--;
			if(this.timesExecuted <= 0)
				this.reset();
		}
	}

	@Override
	public double getActivationValue() {
		return actionValue;
	}

	@Override
	public void reset() {
		this.actionValue = 0;
		this.priority = 0;
		this.timesExecuted = 0;
		this.executing = false;
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

