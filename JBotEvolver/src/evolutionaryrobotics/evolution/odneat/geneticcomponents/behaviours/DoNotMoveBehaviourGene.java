package evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours;


import java.util.ArrayList;
import java.util.HashMap;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;

public class DoNotMoveBehaviourGene implements MacroBehaviourGene, PreprogrammedGene {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected long innovationNumber;
	
	public DoNotMoveBehaviourGene(long innovationNumber){
		this.innovationNumber = innovationNumber;
	}
	
	@Override
	public long getInnovationNumber() {
		return this.innovationNumber;
	}

	@Override
	public MacroBehaviourGene copy() {
		DoNotMoveBehaviourGene copy = new DoNotMoveBehaviourGene(this.innovationNumber);
		
		return copy;
	}

	@Override
	public boolean hasParameters() {
		return false;
	}

	@Override
	public HashMap<String, Double> getParameters() {
		return null;
	}

	@Override
	public int getNumberOfBehaviourGenes() {
		return 1;
	}

	@Override
	public long adjustInnovationNumbers(long nextInnovation, long[] inputsIds) {
		return nextInnovation;
	}

	@Override
	public ArrayList<Long> getOrderedListInputNodeGenes() {
		return null;
	}

	@Override
	public ArrayList<Long> getOrderedListOutputNodeGenes() {
		return null;
	}

	@Override
	public double normaliseParameterValue(String key, double value) {
		return value;
	}

	@Override
	public void replaceAllInnovations(HashMap<Long, Long> oldToNew,
			ODNEATInnovationManager dib) {
		if(!oldToNew.containsKey(this.innovationNumber)){
			oldToNew.put(this.innovationNumber, dib.nextInnovationNumber());
		}
		this.innovationNumber = oldToNew.get(this.innovationNumber);
	}

	@Override
	public boolean containsSubNodeWithId(long id) {
		return id == this.innovationNumber;
	}

	@Override
	public ArrayList<ODNEATLinkGene> getConnectionsList() {
		return new ArrayList<ODNEATLinkGene>();
	}
}

