package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;

public class ODNEATMacroParameterMutator extends Mutator<ODNEATGenome> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double magnitude;

	public ODNEATMacroParameterMutator(double prob, double magnitude){
		this.mutationProb = prob;
		this.magnitude = magnitude;
	}

	@Override
	public void applyMutation(ODNEATGenome e) {
		if(! (e instanceof MacroGenome))
			return;
		MacroGenome macrogenome = (MacroGenome) e;
		HashMap<Long, ODNEATMacroNodeGene> macroMap = macrogenome.getMacroNodeGenes();
		//ArrayList<ODNEATMacroNodeGene> macronodes = (ArrayList<ODNEATMacroNodeGene>) macrogenome.getMacroNodeGenes().values();
		for(long key : macroMap.keySet()){
			ODNEATMacroNodeGene macronode = macroMap.get(key);
			if(macronode.getBehaviourGene().hasParameters()){
				this.mutateParametersOfGene(macronode.getBehaviourGene());
			}
		}
	}

	protected void mutateParametersOfGene(MacroBehaviourGene bGene) {
		HashMap<String, Double> parameters = bGene.getParameters();
		Set<String> keySet = parameters.keySet();
		for(String key : keySet){
			if(random.nextDouble() < this.mutationProb){
				double newValue = this.mutateIndividualParameter(parameters.get(key));
				newValue = bGene.normaliseParameterValue(key, newValue);
				parameters.put(key, newValue);
			}
		}
	}

	/**
	 * gaussian mutation of the value of the parameter: +- magnitude
	 */
	protected double mutateIndividualParameter(double value) {
		return value * (1 + random.nextGaussian() * this.magnitude);
	}

}

