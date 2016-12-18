package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical;


import java.util.ArrayList;
import java.util.HashMap;

import datastructures.MacroTreeNode;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ArbitratorMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ArbitratorMacroNodeGene.FUNCTION;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;

public class ArbitratorFunctionMutator extends Mutator<ODNEATGenome>{

	@Override
	public void applyMutation(ODNEATGenome e) {
		if(!(e instanceof MacroGenome))
			return;
		MacroGenome genome = (MacroGenome) e;
		HashMap<Long, ODNEATMacroNodeGene> macroMap = genome.getMacroNodeGenes();
		//ArrayList<ODNEATMacroNodeGene> macros = (ArrayList<ODNEATMacroNodeGene>) genome.getMacroNodeGenes().values();
		
		for(long key : macroMap.keySet()){
			ODNEATMacroNodeGene macro = macroMap.get(key);
			if(macro instanceof ArbitratorMacroNodeGene){
				ArbitratorMacroNodeGene arb = (ArbitratorMacroNodeGene) macro;
				mutate(arb);
			} else if(macro.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene){
				HierarchicalMacroBehaviourGene hierarchy = (HierarchicalMacroBehaviourGene) macro.getBehaviourGene();
				for(MacroTreeNode inner : hierarchy.getTree().getAllNodes()){
					if(inner.getNodeMacroGene() instanceof ArbitratorMacroNodeGene){
						ArbitratorMacroNodeGene arb = (ArbitratorMacroNodeGene) inner.getNodeMacroGene();
						mutate(arb);
					}
				}
			}
		}
	}

	private void mutate(ArbitratorMacroNodeGene arb) {
		if(random.nextDouble() < this.mutationProb){
			FUNCTION[] functions = arb.getFunctions();
			FUNCTION newFunction = functions[random.nextInt(functions.length)];
			arb.setFunction(newFunction);
		}
	}

}

