package evolutionaryrobotics.populations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.CompatibilityCalculator;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATSpecies;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATTabuList;
import evolutionaryrobotics.populations.RobotPopulation;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Crossover;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Selector;

public class ODNEATPopulation implements Serializable, RobotPopulation<ODNEATGenome>{

	private static final long serialVersionUID = 1L;

	protected ArrayList<ODNEATSpecies> speciesList;

	protected Random random;

	protected int speciesCount, robotId;

	protected double sumAverageSpeciesFitness;

	protected int maxSize;

	protected String currentId;
	
	protected ODNEATInnovationManager inManager;	
	protected CompatibilityCalculator calc;

	public ODNEATPopulation(Random random, int robotId, CompatibilityCalculator calc, int maxSize){
		speciesList = new ArrayList<ODNEATSpecies>();
		this.speciesCount = 0;
		this.random = random;
		this.robotId = robotId;
		this.calc = calc;
		this.maxSize = maxSize;
	}

	public void setCurrentId(String currentId){
		this.currentId = currentId;
	}

	public ODNEATSpecies containsChromosome(ODNEATGenome current) {
		for(ODNEATSpecies s : this.speciesList){
			if(s.containsMember(current))
				return s;
		}

		return null;
	}

	public ODNEATGenome reproduce(String newId, ODNEATTabuList tabuList, 
			Selector<ODNEATGenome> selector, Crossover<ODNEATGenome> crossover,
			Mutator<ODNEATGenome> mutator) {
		//make room for the new one if necessary.
		//the addition method verifies the size.
		/*if(this.computeSize() > this.maxSize)
			this.removeWorstAdjustedFitness(currentId, tabuList);*/

		ODNEATSpecies species = chooseParentSpecies();
		ODNEATGenome newChromosome = species.produceOffspring(selector, crossover, mutator, newId);
		
		this.addODNEATGenome(newChromosome, tabuList, false);		
		this.setCurrentId(String.valueOf(newId));
		return newChromosome;
	}

	protected ODNEATSpecies chooseParentSpecies() {
		double[] probs = getAllSpeciesSpawnProbabilities();

		double value = random.nextDouble();
		ODNEATSpecies selected = null;
		for(int i = 0; i < probs.length; i++){
			value -= probs[i];
			if(value <= 0){
				selected = this.speciesList.get(i);
				i = probs.length;
			}
		}

		return selected;
	}

	public double getSpawnProbability(int specieId){
		boolean found = false;
		double value = 0;
		for(int i = 0; !found && i < this.speciesList.size(); i++){
			ODNEATSpecies s = this.speciesList.get(i);
			if(s.getSpeciesId() == specieId){
				found = true;
				value = s.getSpeciesAvgAdjustedFitness() / this.sumAverageSpeciesFitness;
			}
		}

		return value;
	}

	public double[] getAllSpeciesSpawnProbabilities(){
		double[] spawnValues = new double[this.speciesList.size()];
		for(int i = 0; i < speciesList.size(); i++){
			spawnValues[i] = this.speciesList.get(i).getSpeciesAvgAdjustedFitness() / this.sumAverageSpeciesFitness;
		}
		return spawnValues;
	}

	public void updateGenome(ODNEATGenome genome, double currentEnergyLevel){
		boolean found = false;
		for(int i = 0; !found && i < this.speciesList.size(); i++){
			ODNEATSpecies species = this.speciesList.get(i);
			if(species.containsMember(genome)){
				found = true;
				//System.out.println("==========\n found =========\n");
				species.updateEnergyLevel(genome.getId(), currentEnergyLevel);
				this.computeSumAdjustedFitness();
			}
		}
		
		if(!found){
			System.out.println("not found.");
		}
	}
	
	
	//restriction based insertion.
	//depends on how good it is for the population
	public void addConditionallyODNEATGenome(ODNEATGenome possibleAddition, ODNEATTabuList tabuList) {
		//here we can just add the chromosome because: 1
		/**
		 * 1 - if pop.size < max size, then it will be added without any problem
		 * 2 - if pop.size >= max size, then it will be added and the one with the worst adjusted fitness will be removed.
		 * 2.1 - if the new addition is the worst, it will be removed after being added, no harms done.
		 */
		this.addODNEATGenome(possibleAddition, tabuList, true);


		//see if it will have an adjusted fitness better than the worst one.

		//one, first the worst adjusted fitness
		/*double worstAdjustedFitness = Double.MIN_VALUE;
		ODNEATSpecies worstSpecies = null;
		int worstAdjustedIndex = -1;
		for(ODNEATSpecies s : speciesList){
			for(int i = 0; i < s.getSpeciesSize(); i++){
				ODNEATChromosome current = s.getChromosomeAt(i);
				if(!current.getId().equals(currentId) && current.getAdjustedFitness() < worstAdjustedFitness){
					worstAdjustedFitness = current.getAdjustedFitness();
					worstSpecies = s;
					worstAdjustedIndex = i;
				}
			}
		}

		double additionAdjustedFitness = -1;
		//second, test the new chromosome
		boolean foundSpecies = false;
		for(int i = 0; !foundSpecies && i < speciesList.size(); i++){
			ODNEATSpecies species = speciesList.get(i);
			if(species.isChromosomeCompatible(possibleAddition)){
				foundSpecies = true;
				additionAdjustedFitness = possibleAddition.getFitness() / species.getSpeciesSize();
			}
		}

		// the "possible addition" is novel enough to be part of a new species
		if(!foundSpecies)
			additionAdjustedFitness = possibleAddition.getFitness();

		//the "possible addition" is better, so let's add it
		if(additionAdjustedFitness > worstAdjustedFitness){
			this.addODNEATChromosome(possibleAddition, tabuList);
			tabuList.addElement(worstSpecies.getChromosomeAt(worstAdjustedIndex));
			worstSpecies.removeAtIndex(worstAdjustedIndex);
			computeSumAdjustedFitness();
		}*/

	}

	public void addODNEATGenome(ODNEATGenome c, ODNEATTabuList tabuList, boolean currentIdMatters){
		//check if we are over or equal the population size
		int size = computeSize();
		if(size >= this.maxSize){
			this.removeWorstAdjustedFitness(tabuList, currentIdMatters);
		}

		boolean foundSpecies = false;
		for(int i = 0; !foundSpecies && i < speciesList.size(); i++){
			if(isChromosomeCompatible(c, speciesList.get(i))){
				speciesList.get(i).addNewSpeciesMember(c);
				foundSpecies = true;
			}
		}
		if(!foundSpecies){
			assignToNewSpecies(c);
		}

		computeSumAdjustedFitness();
	}

	private boolean isChromosomeCompatible(ODNEATGenome applicant,
			ODNEATSpecies species) {
		ODNEATGenome representative = species.getSpeciesRepresentative();

		double compatScore = calc.computeCompatibilityScore(applicant, representative);

		return compatScore < calc.getCompatibilityThreshold();
	}

	public int computeSize() {
		int size = 0;
		for(ODNEATSpecies s : this.speciesList){
			size += s.getSpeciesSize();
		}
		return size;
	}

	protected void computeSumAdjustedFitness() {
		this.sumAverageSpeciesFitness = 0;
		for(ODNEATSpecies s : this.speciesList){
			this.sumAverageSpeciesFitness += s.getSpeciesAvgAdjustedFitness();
		}

		this.sumAverageSpeciesFitness /= this.speciesList.size();
	}

	protected void assignToNewSpecies(ODNEATGenome c) {
		speciesCount++;
		ODNEATSpecies species = new ODNEATSpecies(random, speciesCount);
		species.addNewSpeciesMember(c);
		this.speciesList.add(species);
		
	}

	//remove the worst adjusted excluding the current
	public void removeWorstAdjustedFitness(ODNEATTabuList tabuList, boolean currentIdMatters){
		if(currentIdMatters){
			this.removeWorstAdjustedWithRestriction(tabuList);
		}
		else
			removeWorstAdjustedWithoutRestriction(tabuList);

		eliminateExtinctSpecies();

		computeSumAdjustedFitness();
	}

	private void eliminateExtinctSpecies() {
		Iterator<ODNEATSpecies> it = this.speciesList.iterator();
		while(it.hasNext()){
			ODNEATSpecies current = it.next();
			if(current.getSpeciesSize() == 0)
				it.remove();
		}
	}

	protected void removeWorstAdjustedWithoutRestriction(ODNEATTabuList tabuList) {
		double worstAdjustedFitness = Double.MAX_VALUE;
		ODNEATSpecies worstSpecies = null;
		int worstAdjustedIndex = -1;
		for(ODNEATSpecies s : speciesList){
			for(int i = 0; i < s.getSpeciesSize(); i++){
				ODNEATGenome current = s.getChromosomeAt(i);
				if(current.getAdjustedFitness() < worstAdjustedFitness){
					worstAdjustedFitness = current.getAdjustedFitness();
					worstSpecies = s;
					worstAdjustedIndex = i;
				}
			}
		}
		tabuList.addElement(worstSpecies.getChromosomeAt(worstAdjustedIndex));
		worstSpecies.removeAtIndex(worstAdjustedIndex);		
	}

	protected void removeWorstAdjustedWithRestriction(ODNEATTabuList tabuList) {
		double worstAdjustedFitness = Double.MAX_VALUE;
		ODNEATSpecies worstSpecies = null;
		int worstAdjustedIndex = -1;
		for(ODNEATSpecies s : speciesList){
			for(int i = 0; i < s.getSpeciesSize(); i++){
				ODNEATGenome current = s.getChromosomeAt(i);
				if(!current.getId().equals(currentId) && current.getAdjustedFitness() < worstAdjustedFitness){
					worstAdjustedFitness = current.getAdjustedFitness();
					worstSpecies = s;
					worstAdjustedIndex = i;
				}
			}
		}
		tabuList.addElement(worstSpecies.getChromosomeAt(worstAdjustedIndex));
		worstSpecies.removeAtIndex(worstAdjustedIndex);		
	}

	public double getSumAverageSpeciesFitness(){
		return this.sumAverageSpeciesFitness;
	}

	public double getProportionateAdjustedFitness(int speciesId) {

		for(ODNEATSpecies s : this.speciesList){
			if(s.getSpeciesId() == speciesId)
				return s.getSpeciesAvgAdjustedFitness() / this.getSumAverageSpeciesFitness();
		}
		return 0;
	}

	public ArrayList<ODNEATSpecies> getSpeciesList() {
		return this.speciesList;
	}

	@Override
	public int getCurrentPopulationSize() {
		return this.computeSize();
	}

	@Override
	public int getMaxPopulationSize() {
		return this.maxSize;
	}

	@Override
	//we use a different "reproduce" method (more parameters).
	public ODNEATGenome reproduce() {
		return null;
	}

	@Override
	public ODNEATGenome getFittestIndividual() {
		double maxFitness = Double.MIN_VALUE;
		ODNEATGenome best = null;
		
		for(ODNEATSpecies species : this.speciesList){
			for(ODNEATGenome current : species.getGenomes()){
				if(current.getFitness() > maxFitness){
					maxFitness = current.getFitness();
					best = current;
				}
			}
		}
		return best;
	}

	public CompatibilityCalculator getCompatibilityScoreCalculator() {
		return this.calc;
	}
}


