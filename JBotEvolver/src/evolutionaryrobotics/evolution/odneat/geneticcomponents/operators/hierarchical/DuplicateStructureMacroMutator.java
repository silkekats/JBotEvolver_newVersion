package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import datastructures.MacroTree;
import datastructures.MacroTreeNode;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.EvolvedANNBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATTemplateLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;

public class DuplicateStructureMacroMutator extends Mutator<ODNEATGenome> {

	private ODNEATInnovationManager dib;
	private double rewiringProb;
	private AuxMacroOperator auxOp = new AuxMacroOperator();

	public DuplicateStructureMacroMutator(ODNEATInnovationManager manager, double rewiringProb){
		this.dib = manager;
		this.rewiringProb = rewiringProb;
	}

	@Override
	public void applyMutation(ODNEATGenome e) {
		ArrayList<ODNEATMacroNodeGene> macronodes = auxOp.getMacroNodeGenes(e);
		if(!macronodes.isEmpty()){
			//for(ODNEATMacroNodeGene node : macronodes){
			ODNEATMacroNodeGene node = macronodes.get(random.nextInt(macronodes.size()));	
			if(random.nextDouble() < this.mutationProb){
				this.duplicateAndDifferentiateNode(node, e);
			}
			//}
		}
	}

	private void duplicateAndDifferentiateNode(ODNEATMacroNodeGene node,
			ODNEATGenome e) {
		//System.out.println("DUPL.. " + node.getInnovationNumber());
		ODNEATMacroNodeGene newNode = node.copy();
		ArrayList<Long> netInputNodes = e.getNodeGenesByType(ODNEATNodeGene.INPUT);
		//node.setInnovationNumber(dib.nextInnovationNumber());
		ArrayList<ODNEATLinkGene> outputs = auxOp.getOutputLinks(e, newNode, true);
		//first replace
		HashMap<Long, Long> newInnovations = newNode.replaceAllInnovations(dib);
		for(ODNEATLinkGene link : outputs){
			link.setFromId(newInnovations.get(link.getFromId()));
			link.setInnovationNumber(dib.nextInnovationNumber());
		}
		
		//System.out.println(newInnovations.keySet().toString());
		//System.out.println(newInnovations.values().toString());
		ArrayList<ODNEATTemplateLinkGene> inputs;
		if(newNode.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene){
			HierarchicalMacroBehaviourGene hgene = (HierarchicalMacroBehaviourGene) newNode.getBehaviourGene();
			inputs = new ArrayList<ODNEATTemplateLinkGene>();
			MacroTree tree = hgene.getTree();
			ArrayList<MacroTreeNode> treenodes = tree.getAllNodes();
			for(MacroTreeNode treenode : treenodes){
				inputs.addAll(treenode.getNodeMacroGene().getTemplateInputs());
			}
		}
		else {
			inputs = newNode.getTemplateInputs();
		}
		for(ODNEATLinkGene link : inputs){
			rewireLink(netInputNodes, link);
		}

		e.insertSingleNodeGene(newNode);
		e.insertLinkGenes(outputs);
	}

	private void rewireLink(ArrayList<Long> netInputNodes, ODNEATLinkGene link) {
		long from = link.getFromId();
		if(netInputNodes.contains(from)){
			if(random.nextDouble() < this.rewiringProb){
				long newFrom = netInputNodes.get(random.nextInt(netInputNodes.size()));
				link.setFromId(newFrom);
			}
		}
	}
}

