package evolutionaryrobotics.evolution.odneat.geneticcomponents;

import java.io.Serializable;

public interface Genome extends Serializable {

	public void setId(String newId);
	public String getId();
	
	public double getFitness();
    public int getSecondFitness();
	public void setFitness(double fitness);
    public void setSecondFitness(int fitness);
	
	public Genome copy();
}

