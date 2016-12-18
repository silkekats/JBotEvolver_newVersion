//This is for ODNEAT
package evolutionaryrobotics.populations;


import java.io.Serializable;
import java.util.ArrayList;

import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.OnlineEA;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.Genome;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class OnlineEvoShallowPopulation extends Population implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ArrayList<OnlineEA<?>> instances;
	protected double lowestFitness = Double.MAX_VALUE, averageFitness = 0,
			highestFitness = Double.MIN_VALUE;
	
	
	public OnlineEvoShallowPopulation(Arguments arguments) {
		super(arguments);
		this.instances = new ArrayList<OnlineEA<?>>();
	}
	
	public void addInstance(OnlineEA<?> ea){
		this.instances.add(ea);
	}
	
	public ArrayList<OnlineEA<?>> getInstances(){
		return this.instances;
	}
	
	public void clearListOfInstances(){
		this.instances.clear();
		resetFitnessInformation();
	}
	
	protected void resetFitnessInformation() {
		lowestFitness = Double.MAX_VALUE;
		averageFitness = 0;
		highestFitness = Double.MIN_VALUE;
	}

	public void computeFitnessStats(){
		this.resetFitnessInformation();
		
		for(OnlineEA<?> ea : this.instances){
			Genome g = ea.getActiveGenome();
			double fitness = g.getFitness();
			this.averageFitness += fitness;
			this.highestFitness = Math.max(fitness, this.highestFitness);
			this.lowestFitness = Math.min(fitness, lowestFitness);
		}
		
		this.averageFitness /= this.instances.size();
	}
	
	@Override
	public double getLowestFitness() {
		return this.lowestFitness;
	}

	@Override
	public double getAverageFitness() {
		return this.averageFitness;
	}

	@Override
	public double getHighestFitness() {
		return this.highestFitness;
	}

		
	/*********************************************************************************************
	 * *******************************************************************************************
	 * none of the remaining methods is used.
	 *********************************************************************************************
	 *********************************************************************************************/
	
	@Override
	public void createRandomPopulation() {
	}

	@Override
	public boolean evolutionDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumberOfCurrentGeneration() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int getPopulationSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfChromosomesEvaluated() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Chromosome getNextChromosomeToEvaluate() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setEvaluationResult(Chromosome chromosome, double fitness) {
		// TODO Auto-generated method stub
		
	}

	public void setEvaluationResultForId(int pos, double fitness) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createNextGeneration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Chromosome getBestChromosome() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chromosome[] getTopChromosome(int number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chromosome getChromosome(int chromosomeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setupIndividual(Robot r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Chromosome[] getChromosomes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEvaluationResult(Chromosome chromosome, double fitness, int secondFitness) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEvaluationResultForId(int pos, double fitness, int secondFitness) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NEATPopulation4J getNEATPopulation4J() {
		// TODO Auto-generated method stub
		return null;
	}

}


