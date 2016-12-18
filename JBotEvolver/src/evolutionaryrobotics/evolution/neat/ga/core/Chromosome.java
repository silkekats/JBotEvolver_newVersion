/*
 * Created on Oct 13, 2004
 *
 */
package evolutionaryrobotics.evolution.neat.ga.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface Chromosome extends Comparable, Serializable {
	public Gene[] genes();
	public int size();
	public void updateChromosome(Gene[] newGenes);
	public void updateFitness(double fitness);
    public void updateSecondFitness(int secondFitness);
	public double fitness();
    public int secondFitness();
	public int getSpecieId();
}
