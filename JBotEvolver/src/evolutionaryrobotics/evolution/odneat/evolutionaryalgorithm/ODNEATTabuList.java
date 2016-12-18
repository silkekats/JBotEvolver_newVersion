package evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;

public class ODNEATTabuList implements Serializable{

	private static final long serialVersionUID = -3433171161777052803L;
	
	private static final double ACCEPTANCE_THRESHOLD = 1.0;
	
	
	protected HashMap<ODNEATGenome, Integer> poorSolutions;
	
	protected final int MAX_TIME = 50;
	protected final int MAX_SIZE = 30;
	
	//for statistical purposes only.
	public int acceptedCount = 0, rejectedCount = 0;
	
	private CompatibilityCalculator comp;
	
	public ODNEATTabuList(CompatibilityCalculator comp){
		poorSolutions = new HashMap<ODNEATGenome, Integer>();
		this.comp = comp;
	}


	public void addElement(ODNEATGenome element){
		//even if the element is already in the list, the insertion operation does what we want by resetting the integer value.
		poorSolutions.put(element, 0);
		if(poorSolutions.size() > MAX_SIZE){
			int toRemove = poorSolutions.size() - MAX_SIZE;
			//given the representation, efficiently remove the "older", i.e., those with a higher time value.
			//keys sorted by value
			ArrayList<ODNEATGenome> sortedKeys = sortKeysByValue();
			for(int i = 0; i < toRemove; i++){
				poorSolutions.remove(sortedKeys.get(sortedKeys.size() - 1 - i));
			}
		}
	}

	private ArrayList<ODNEATGenome> sortKeysByValue() {
		ArrayList <ODNEATGenome> keys = new ArrayList<ODNEATGenome>();
		keys.addAll(poorSolutions.keySet());
		Collections.sort(keys, new Comparator<Object>() {
			@SuppressWarnings("unchecked")
			public int compare(Object key1, Object key2) {
				//the two values
				Object v1 = poorSolutions.get(key1);
				Object v2 = poorSolutions.get(key2);
				if (v1 == null) {
					return (v2 == null) ? 0 : 1;
				}
				else if (v1 instanceof Comparable) {
					return ((Comparable<Object>) v1).compareTo(v2);
				}
				else {
					return 0;
				}
			}
		});
		return keys;
	}


	protected void increaseTime(ODNEATGenome e){
		int time = poorSolutions.get(e);
		time++;
		
		if(time + 1 > MAX_TIME)
			poorSolutions.remove(e);
		else
			poorSolutions.put(e, time);
	}

	public void increaseTimeAllButThese(ArrayList<ODNEATGenome> resettedElements) {
		Set<ODNEATGenome> keys = poorSolutions.keySet();
		Iterator<ODNEATGenome> it = (Iterator<ODNEATGenome>) keys.iterator();
		ArrayList<ODNEATGenome> toIncrease = new ArrayList<ODNEATGenome>();
		while(it.hasNext()){
			ODNEATGenome current = it.next();
			if(!resettedElements.contains(current))
				toIncrease.add(current);
		}
		
		for(ODNEATGenome c : toIncrease)
			this.increaseTime(c);
	}


	public ODNEATGenome elementIsValid(ODNEATGenome toTest) {
		Set<ODNEATGenome> keys = poorSolutions.keySet();
		Iterator<ODNEATGenome> it = (Iterator<ODNEATGenome>) keys.iterator();
		while(it.hasNext()){
			ODNEATGenome current = it.next();
			double compatibility = comp.computeCompatibilityScore(current, toTest);
			
			if(compatibility < ACCEPTANCE_THRESHOLD){
				this.rejectedCount++;
				return current;
			}
		}
		this.acceptedCount++;
		return null;
	}


	public int getRejectedCount() {
		return this.rejectedCount;
	}


	public int getAcceptedCount() {
		return this.acceptedCount;
	}


	public int getCurrentSize() {
		return this.poorSolutions.size();
	}


	public ODNEATGenome containsSimilarOrEqual(ODNEATGenome current) {
		return this.elementIsValid(current);
	}

}

