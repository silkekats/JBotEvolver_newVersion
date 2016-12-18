package evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm;


import java.io.Serializable;
import java.util.Collection;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.Genome;
import evolutionaryrobotics.populations.RobotPopulation;
import simulation.environment.Environment;
import simulation.robot.Robot;

public interface OnlineEA<E extends Genome> extends Serializable {
	
	public boolean willBroadcastGenome();
	
	public void transmitGenome(E e, OnlineEA<E> otherInstance);
	
	public void receiveSingleGenome(E e);

	public void processGenomesReceived(Collection<E> received);
	
	public E getActiveGenome();
	
	public E reproduce();
	
	public RobotPopulation<E> getPopulation();
	
	public void executeOnlineEvolution(Environment environment, Collection<Robot> robots, double time);

	public String toString();
	
	public void setEvolutionStatus(boolean evolutionsStatus);

	public E resetParametersForEvaluation(Object activeGenome);

	public void setRobot(Robot robot);

	public Robot getRobotInstance();
}

