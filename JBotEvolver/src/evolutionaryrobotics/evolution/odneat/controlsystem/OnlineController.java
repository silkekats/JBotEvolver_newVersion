package evolutionaryrobotics.evolution.odneat.controlsystem;


import java.util.Random;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.OnlineEA;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.Genome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.ALGDescriptor;
import simulation.robot.Robot;
import simulation.util.Arguments;

public interface OnlineController<E extends Genome> {

	public void initialise(Random random, int robotId, ALGDescriptor descriptor, 
			Robot r, EvaluationFunction eval, Arguments genomeArguments);
	
	public OnlineEA<E> getEAInstance();
	
	public void updateStructure(Object object);
	
	public void setOnlineEAInstance(OnlineEA<?> instance);
}

