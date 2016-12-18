package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import datastructures.MacroTree;
import datastructures.MacroTreeNode;

import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.EvolvedANNBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ODNEATHierarchicalMacroNodeGene;

public class AuxMacroOperator implements Serializable {

	public ArrayList<ODNEATLinkGene> getOutputLinksOfHierarchicalMacroBehaviourGene(
			MacroGenome genome, 
			HierarchicalMacroBehaviourGene hgene){
		ArrayList<Long> outputNodes = new ArrayList<Long>();
		ArrayList<ODNEATLinkGene> links = new ArrayList<ODNEATLinkGene>();
		MacroTree tree = hgene.getTree();
		outputNodes.add(tree.getRoot().getNodeMacroGene().getInnovationNumber());

		return links;

	}

	public ArrayList<ODNEATLinkGene> getOutputLinks(ODNEATGenome genome, ODNEATMacroNodeGene node, boolean copy) {
		ArrayList<Long> outputNodes;
		ArrayList<ODNEATLinkGene> links = new ArrayList<ODNEATLinkGene>();
		if(node.getBehaviourGene() instanceof EvolvedANNBehaviourGene){
			EvolvedANNBehaviourGene evolved = (EvolvedANNBehaviourGene) node.getBehaviourGene();
			outputNodes = evolved.getOrderedListOutputNodeGenes();
		}
		else {
			outputNodes = new ArrayList<Long>();
			if(node.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene){
				HierarchicalMacroBehaviourGene hgene = (HierarchicalMacroBehaviourGene) node.getBehaviourGene();
				MacroTree tree = hgene.getTree();
				outputNodes.add(tree.getRoot().getNodeMacroGene().getInnovationNumber());
			}
			else {
				outputNodes.add(node.getInnovationNumber());
			}
		}
		for(ODNEATLinkGene g : genome.getLinkGenes(true)){
			if(outputNodes.contains(g.getFromId())){
				if(copy){
					links.add(g.copy());
				}
				else
					links.add(g);
			}
		}
		//System.out.println("OUTPUT LINKS: " + links.size());
		return links;
	}

	public ArrayList<ODNEATMacroNodeGene> getMacroNodeGenes(ODNEATGenome e) {
		ArrayList<ODNEATMacroNodeGene> macronodes = new ArrayList<ODNEATMacroNodeGene>();
		if(e instanceof MacroGenome){
			MacroGenome g = (MacroGenome) e;
			macronodes.addAll(g.getMacroNodeGenes().values());
		}
		/*ArrayList<ODNEATNodeGene> nodes = (ArrayList<ODNEATNodeGene>) e.getNodeGenes();
		for(int i = 0; i < nodes.size(); i++){
			ODNEATNodeGene node = nodes.get(i);
			if(node instanceof ODNEATMacroNodeGene){
				macronodes.add((ODNEATMacroNodeGene) node);
			}
		}*/

		return macronodes;
	}

	public ArrayList<ODNEATHierarchicalMacroNodeGene> getHierarchicalNodes(
			MacroGenome macrogenome) {
		ArrayList<ODNEATHierarchicalMacroNodeGene> hierarchies = new ArrayList<ODNEATHierarchicalMacroNodeGene>();
		HashMap<Long, ODNEATMacroNodeGene> macros = macrogenome.getMacroNodeGenes();
		for(long key : macros.keySet()){
			ODNEATMacroNodeGene node = macros.get(key);
			if(node instanceof ODNEATHierarchicalMacroNodeGene){
				hierarchies.add((ODNEATHierarchicalMacroNodeGene) node);
			}
		}
		return hierarchies;
	}

	public void removeFromGenome(MacroGenome macrogenome,
			ArrayList<ODNEATLinkGene> linksToRemove) {
		macrogenome.removeLinks(linksToRemove);
	}
}

