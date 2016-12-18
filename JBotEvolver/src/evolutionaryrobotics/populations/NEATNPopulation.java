// This is the NEAT population, but neat chroms are stored in NEATPopulation4J
package evolutionaryrobotics.populations;


import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.evolution.neat.NEATSerializer;
import evolutionaryrobotics.evolution.neat.core.NEATNetDescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATNeuralNet;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;



public class NEATNPopulation extends Population implements Serializable {
	private static final long serialVersionUID = 1L;


	@ArgumentsAnnotation(name="size", defaultValue="100")
	public int populationSize;
	public int currentGeneration;
	public int generationNumber = 0;
	public Chromosome bestChromosome;
	public Chromosome chromosomes[];

	public double bestFitness;
	public double accumulatedFitness;
	public double worstFitness;
	public double lastEvaluatedFitness;
	public int numberOfChromosomesEvaluated;
	public int nextChromosomeToEvaluate;

	
	public double[] initialWeights;
	public NEATPopulation4J pop;

	public NEATNPopulation(Arguments arguments) {
		super(arguments);
		populationSize = arguments.getArgumentAsIntOrSetDefault("size",100);
		numberOfGenerations = arguments.getArgumentAsIntOrSetDefault("generations",500);
		numberOfSamplesPerChromosome = arguments.getArgumentAsIntOrSetDefault("samples",5);
		mutationRate = arguments.getArgumentAsDoubleOrSetDefault("mutationrate", 0.1);
		
		
	}

	@Override
	public void createNextGeneration() {
		randomNumberGenerator.setSeed(getGenerationRandomSeed());
		resetGeneration();
		currentGeneration++;
		
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	protected void resetGeneration() {
		bestFitness = -1e10;
		accumulatedFitness = 0;
		worstFitness = 1e10;
		numberOfChromosomesEvaluated = 0;
		nextChromosomeToEvaluate = 0;
	}

	@Override
	public void createRandomPopulation() {
		
		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		chromosomes = new Chromosome[populationSize];
		bestChromosome = chromosomes[0];

		resetGeneration();
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	@Override
	public Chromosome getBestChromosome() {
		return bestChromosome;
	}
	
	
	@Override
	public Chromosome[] getTopChromosome(int number) {
		Chromosome[] top = new Chromosome[number];
		LinkedList<Chromosome> sortedChromosomes = new LinkedList<Chromosome>();
		for (int i = 0; i < populationSize; i++)
			sortedChromosomes.add(chromosomes[i]);

		Collections.sort(sortedChromosomes,
				new Chromosome.CompareChromosomeFitness());

		Iterator<Chromosome> sortedIterator = sortedChromosomes.iterator();

		for (int i = 0; i < top.length; i++) {
			top[i] = sortedIterator.next();
		}

		return top;
	}
    

	@Override
	public double getLowestFitness() {
		return worstFitness;
	}

	@Override
	public double getAverageFitness() {
		return accumulatedFitness / (double) chromosomes.length;
	}

	@Override
	public double getHighestFitness() {
		return bestFitness;
	}

	@Override
	public Chromosome getNextChromosomeToEvaluate() {
			return null;
	}

	@Override
	public int getNumberOfChromosomesEvaluated() {
		return numberOfChromosomesEvaluated;
	}

	// @Override
	public int getPopulationSize() {
		return populationSize;
	}

	// @Override
	public int getNumberOfCurrentGeneration() {
		return currentGeneration;
	}

	@Override
	public void setEvaluationResult(Chromosome chromosome, double fitness, int secondFitness) {
		if (chromosome.getFitnessSet()) {
			throw new java.lang.RuntimeException("Fitness of " + chromosome
					+ " already set -- trying to set it again");
		}

		chromosome.setFitness(fitness);
        chromosome.setSecondFitness(secondFitness);
        
		numberOfChromosomesEvaluated++;
		accumulatedFitness += fitness;

		if (fitness > bestFitness) {
			bestChromosome = chromosome;
			bestFitness = fitness;
		}

		if (fitness < worstFitness) {
			worstFitness = fitness;
		}
        lastEvaluatedFitness = fitness;
	}

	@Override
	public void setEvaluationResultForId(int pos, double fitness, int secondFitness) {
		if (pos >= chromosomes.length) {
			throw new java.lang.RuntimeException("No such position: " + pos
					+ " on the population");
		}

		chromosomes[pos].setFitness(fitness);
        chromosomes[pos].setSecondFitness(secondFitness);
        
		numberOfChromosomesEvaluated++;
		accumulatedFitness += fitness;

		if (fitness > bestFitness) {
			bestChromosome = chromosomes[pos];
			bestFitness = fitness;
		}

		if (fitness < worstFitness) {
			worstFitness = fitness;
		}

	}

	// @Override
	public boolean evolutionDone() {
		if (currentGeneration >= numberOfGenerations ||
				(currentGeneration == numberOfGenerations-1 && getNumberOfChromosomesEvaluated() == chromosomes.length) ||
				checkFitnessThreshold(bestFitness))
			return true;
		else
			return false;
	}

	@Override
	public Chromosome getChromosome(int chromosomeId) {
		return chromosomes[chromosomeId];
	}
	
	@Override
	public void setupIndividual(Robot r) {
		Chromosome c = getBestChromosome();
		c.setupRobot(r);
	}
	
	public Chromosome[] getChromosomes() {
		return chromosomes;
	}
	
	public void setChromosomes(Chromosome[] c) {
		this.chromosomes = c;
	}

	public NEATPopulation4J getNEATPopulation4J() {
		return pop;
	}
	
	public void setNEATPopulation4J(NEATPopulation4J pop) {
		this.pop = pop;
	}
	
	public Chromosome convertChromosome(evolutionaryrobotics.evolution.neat.ga.core.Chromosome c, int i) {
		NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
        descr.updateStructure(c);
        NEATNeuralNet network = new NEATNeuralNet();
        network.createNetStructure(descr);
        network.updateNetStructure();
		return new Chromosome(NEATSerializer.serialize(network), i);
	}
}