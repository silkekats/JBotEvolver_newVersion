package evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm;


import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Crossover;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Selector;

public interface Species <E>{

	public E getSpeciesRepresentative();
	
	public E produceOffspring(Selector<E> selector, Crossover<E> crossover, Mutator<E> mutator, String offspringId);
	
	public boolean containsMember(E e);
	
	public void addNewSpeciesMember(E e);
	
	public int getSpeciesSize();
	public int getSpeciesId();
}

