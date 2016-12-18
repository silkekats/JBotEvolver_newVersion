package taskexecutor.results;

import result.Result;

public class SimpleFitnessResult extends Result {
	private int chromosomeId;
	private double fitness = 0;
    private int secondFitness = 0;
	

	public SimpleFitnessResult(int chromosomeId, double fitness, int secondFitness) {
		super();
		this.chromosomeId = chromosomeId;
		this.fitness = fitness;
		this.secondFitness = secondFitness;
	}

	public double getFitness() {
		return fitness;
	}
    
    public int getSecondFitness() {
        return secondFitness;
    }

	public int getChromosomeId() {
		return chromosomeId;
	}
	
	
}
