package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical;


import java.util.ArrayList;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;

public class RemoveMacroMutator extends Mutator<ODNEATGenome>{

	private AuxMacroOperator auxOp = new AuxMacroOperator();
	
	@Override
	public void applyMutation(ODNEATGenome e) {
		MacroGenome macrogenome = (MacroGenome) e;
		ArrayList<ODNEATMacroNodeGene> macronodes = auxOp.getMacroNodeGenes(e);
		if(!macronodes.isEmpty() && random.nextDouble() < this.mutationProb){
			ODNEATMacroNodeGene nodeToRemove = macronodes.get(random.nextInt(macronodes.size()));
			ArrayList<ODNEATLinkGene> linksToRemove = this.auxOp.getOutputLinks(e, nodeToRemove, false);
			
			//remove macro node gene
			macrogenome.getMacroNodeGenes().remove(nodeToRemove.getInnovationNumber());
			
			//remove the output links (macro node => rest of the net).
			e.getLinkGenes(false).removeAll(linksToRemove);
		}
	}

}

