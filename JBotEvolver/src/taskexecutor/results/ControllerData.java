package taskexecutor.results;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import evolutionaryrobotics.evolution.odneat.controlsystem.MacroController;
import evolutionaryrobotics.evolution.odneat.controlsystem.MacroNetwork;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEAT;

import simulation.Simulator;
import simulation.robot.Robot;

public class ControllerData implements Serializable{


	private static final long serialVersionUID = -5739487493831033673L;
	protected int nodesCount, synapsesCount;
	protected double age;
	protected double energy, fitness, secondFitness;

	protected String controllerId;

	protected double[] inputs, outputs;

	protected final String INTERMEDIATE_SEPARATOR = "-";
	//protected ArrayList<String> activeMacros = new ArrayList<String>();
	//protected ArrayList<Double> activeMacrosValues = new ArrayList<Double>();
	
	/**
	 * for fastening things up.
	 */
	
	//protected HashMap<String, Integer> macroIdToId;
	
	public ControllerData(){
		/*this.macroIdToId = new HashMap<String,Integer>();
		
		this.macroIdToId.put("MoveForwardBehaviourGene", 1);
		this.macroIdToId.put("TurnLeftBehaviourGene", 2);
		this.macroIdToId.put("TurnRightBehaviourGene", 3);
		
		this.macroIdToId.put("move_forward", 4);
		this.macroIdToId.put("turn_left", 5);
		this.macroIdToId.put("turn_right", 6);*/
	}

	public void update(Robot robot, MacroController controller, Simulator simulator){
		MacroNetwork net = controller.getNetwork();
		nodesCount = net.getTotalNumberOfNeurons();
		synapsesCount = net.getTotalNumberOfConnections();
		ODNEAT instance = (ODNEAT) controller.getEAInstance();
		age = (double) instance.getAge() * simulator.getTimeDelta();
		energy = instance.getActiveGenome().getEnergyLevel();
		fitness = instance.getActiveGenome().getFitness();
        secondFitness = instance.getActiveGenome().getSecondFitness();
		this.controllerId = new String(controller.getControllerId());
		this.inputs = controller.getInputReadings();
		this.outputs = controller.getOutputReadings();
		/*activeMacros.clear();
		this.activeMacros = controller.getListOfActiveMacros();
		this.activeMacrosValues.clear();
		this.activeMacrosValues = controller.getMacroActivationValues();*/
	}

	public static String getHeader(int numberOfRobots, String separator) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < numberOfRobots; i++){
			builder.append("CONTROLLER ");
			builder.append(i);
			builder.append(separator);
			builder.append("AGE ");
			builder.append(i);
			builder.append(separator);
			builder.append("ENERGY ");
			builder.append(i);
			builder.append(separator);
			builder.append("FITNESS ");
			builder.append(i);
			builder.append(separator);
			builder.append("NODES ");
			builder.append(i);
			builder.append(separator);
			builder.append("CONNECTIONS ");
			builder.append(i);
			builder.append(separator);
			builder.append("INPUTS ");
			builder.append(i);
			builder.append(separator);
			builder.append("OUTPUTS ");
			builder.append(i);
			/*builder.append(separator);

			builder.append("ACTIVE MACRO");
			builder.append(i);
			builder.append(separator);

			builder.append("ACTIVE MACRO VALUES");
			builder.append(i);*/

		}
		return builder.toString();
	}

	public String toString(int robot, String separator) {
		StringBuilder builder = new StringBuilder();
		builder.append(this.controllerId.replace(robot + ".", ""));
		builder.append(separator);

		builder.append(this.age);
		builder.append(separator);

		builder.append(String.format("%.3f",energy));
		builder.append(separator);

		builder.append(String.format("%.3f",fitness));
		builder.append(separator);

		builder.append(this.nodesCount);
		builder.append(separator);

		builder.append(this.synapsesCount);
		builder.append(separator);

		builder.append(toString(inputs));
		builder.append(separator);

		builder.append(toString(outputs));
		/*builder.append(separator);

		builder.append(toString(this.activeMacros));
		builder.append(separator);

		builder.append(toStringNumeric(this.activeMacrosValues));*/

		return builder.toString();
	}

	/*private String toStringNumeric(ArrayList<Double> values) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < values.size(); i++){
			builder.append(String.format("%.5f", values.get(i)));
			if(i < values.size() - 1)
				builder.append(INTERMEDIATE_SEPARATOR);
		}
		return builder.toString();
	}*/

	/*private String toString(ArrayList<String> values) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < values.size(); i++){
			Integer replacementId = this.macroIdToId.get(values.get(i));
			builder.append(String.valueOf(replacementId));
			if(i < values.size() - 1)
				builder.append(INTERMEDIATE_SEPARATOR);
		}
		return builder.toString();
	}*/

	private String toString(double[] values) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < values.length; i++){
			builder.append(String.format("%.3f", values[i]));
			if(i < values.length - 1)
				builder.append(INTERMEDIATE_SEPARATOR);
		}
		return builder.toString();
	}
}
