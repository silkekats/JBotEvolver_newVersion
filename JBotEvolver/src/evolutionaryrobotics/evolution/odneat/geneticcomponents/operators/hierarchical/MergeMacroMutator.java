package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical;


import java.util.ArrayList;

import datastructures.MacroTree;
import datastructures.MacroTreeNode;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATTemplateLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ArbitratorBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ArbitratorMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ODNEATHierarchicalMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;

public class MergeMacroMutator extends Mutator<ODNEATGenome>{

	private ODNEATInnovationManager dib;
	//id to distinguish between robot or instance of odneat or whatever.
	private int id;
	private int hierarchiesFormed = 0, arbitratorsCreated = 0;

	private AuxMacroOperator auxOp = new AuxMacroOperator();

	public MergeMacroMutator(ODNEATInnovationManager dib, int id){
		this.dib = dib;
		this.id = id;
	}

	@Override
	public void applyMutation(ODNEATGenome e) {
		ArrayList<ODNEATMacroNodeGene> macronodes = auxOp.getMacroNodeGenes(e);
		if(random.nextDouble() < this.mutationProb && macronodes.size() > 1){
			int index1 = random.nextInt(macronodes.size());
			int index2 = random.nextInt(macronodes.size());
			while(index2 == index1){
				index2 = random.nextInt(macronodes.size());
			}
			ODNEATMacroNodeGene g1 = macronodes.get(index1), g2 = macronodes.get(index2);
			//System.out.println("MERGING: " + g1.getMacroId() + "; " + g2.getMacroId());
			// both are non-hierarchical macro-nodes, create a hierarchy
			if(! (g1.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene) 
					&& ! (g2.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene) ){
				createHierarchy(e, g1, g2, macronodes);

			}
			else {
				int g1Genes = g1.getBehaviourGene().getNumberOfBehaviourGenes(), g2Genes = g2.getBehaviourGene().getNumberOfBehaviourGenes();
				ODNEATMacroNodeGene parent = g1Genes > g2Genes ? g1 : g2;
				ODNEATMacroNodeGene child = parent == g1 ? g2 : g1;
				HierarchicalMacroBehaviourGene hierarchy = (HierarchicalMacroBehaviourGene) parent.getBehaviourGene();
				MacroGenome macrogenome = (MacroGenome) e;
				if(! (child.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene)){
					addToHierarchy(macrogenome, child, hierarchy, hierarchy.getMacroId());
					//remove from the genome because it will be maintained in the hierarchy.
					macrogenome.getMacroNodeGenes().remove(child.getInnovationNumber());
					
				}
				else {
					//System.out.println("P: " + parent.getInnovationNumber() + "; " + child.getMacroId());
					//System.out.println("C: " + child.getInnovationNumber() + "; " + child.getMacroId());
					this.createHierarchyFromSubstrees(e, (ODNEATHierarchicalMacroNodeGene) child, (ODNEATHierarchicalMacroNodeGene) parent, macronodes);
				}
			}
		}
	}


	private void createHierarchyFromSubstrees(ODNEATGenome e,
			ODNEATHierarchicalMacroNodeGene h1, ODNEATHierarchicalMacroNodeGene h2,
			ArrayList<ODNEATMacroNodeGene> macronodes) {
		MacroGenome macrogenome = (MacroGenome) e;
		//step 1, create the new hierarchical node and top-level arbitrator.
		ArbitratorBehaviourGene arb = new ArbitratorBehaviourGene(this.dib.nextInnovationNumber());
		long inNumber = dib.nextInnovationNumber();
		String macroId = "h." + this.id + "." + hierarchiesFormed++;
		HierarchicalMacroBehaviourGene hierarchy = new HierarchicalMacroBehaviourGene(macroId, inNumber);
		String arbitratorId = "a." + this.id + "." + this.arbitratorsCreated++;
		MacroTreeNode root = new MacroTreeNode(new ArbitratorMacroNodeGene(arbitratorId, dib.nextInnovationNumber(), 
				ODNEATNodeGene.ARBITRATOR_MACRO_NODE, arb));
		root.setAcceptsChilds(true);
		root.setLevel(0);
		hierarchy.initialise(root);

		/**
		 * 
		 */
		addSubtreeToHierarchy(macrogenome, h1, hierarchy);
		addSubtreeToHierarchy(macrogenome, h2, hierarchy);		



		//now simply add the other nodes in the tree.
		MacroTree current = hierarchy.getTree();
		MacroTree h1Tree = ((HierarchicalMacroBehaviourGene) h1.getBehaviourGene()).getTree(), 
				h2Tree = ((HierarchicalMacroBehaviourGene) h2.getBehaviourGene()).getTree();
		ArrayList<MacroTreeNode> h1nodes = h1Tree.getAllNodes(), h2nodes = h2Tree.getAllNodes();
		MacroTreeNode previoush1root = h1Tree.getRoot(),
				previoush2root = h2Tree.getRoot();

		//add other nodes, and update level.
		addNodesToList(current, h1nodes, previoush1root);
		addNodesToList(current, h2nodes, previoush2root);

		/*System.out.println("TREE NODES: " + current.getAllNodes().size());
		for(MacroTreeNode treenode : current.getAllNodes()){
			System.out.println("LEVEL " + treenode.getLevel() + 
					"; I: " + treenode.getInputLinks().size() + 
					"; ID: " + treenode.getNodeMacroGene().getMacroId() + 
					"; T-Is: " + treenode.getNodeMacroGene().getTemplateInputs().size());
		}*/

		//add the new hierarchical node to the genome.		
		long hierarchicalMacroId = dib.nextInnovationNumber();
		//System.out.println("ID: " + macroId);
		macrogenome.insertSingleNodeGene(new ODNEATHierarchicalMacroNodeGene(macroId, hierarchicalMacroId, 
				ODNEATNodeGene.HIERARCHICAL_MACRO_NODE,	hierarchy));

		//now remove the old ones.
		macrogenome.getMacroNodeGenes().remove(h1.getInnovationNumber());
		macrogenome.getMacroNodeGenes().remove(h2.getInnovationNumber());
		
		
	}

	private void addSubtreeToHierarchy(MacroGenome macrogenome,
			ODNEATHierarchicalMacroNodeGene previous,
			HierarchicalMacroBehaviourGene hierarchy) {
		MacroTree tree = hierarchy.getTree();
		MacroTree previousTree = ((HierarchicalMacroBehaviourGene) previous.getBehaviourGene()).getTree();
		ArrayList<ODNEATLinkGene> copyoutputsNode1 = auxOp.getOutputLinks(macrogenome, previous, true);
		ArrayList<ODNEATLinkGene> realOutputsNode1 = auxOp.getOutputLinks(macrogenome, previous, false);
		//rewire outputs (genome)
		for(ODNEATLinkGene link : realOutputsNode1){
			link.setFromId(tree.getRoot().getNodeMacroGene().getInnovationNumber());
		}
		for(ODNEATLinkGene link: realOutputsNode1){
			//check for duplicates
			macrogenome.filterRepeatedTemplateLinks(link.getFromId(), link.getToId());
		}
		//remove from the genome
		macrogenome.getMacroNodeGenes().remove(previous.getInnovationNumber());
		ArbitratorMacroNodeGene previousRoot = (ArbitratorMacroNodeGene) previousTree.getRoot().getNodeMacroGene();
		MacroTreeNode child = new MacroTreeNode(previousRoot);
		child.setAcceptsChilds(true);
		tree.addChildToRoot(child, copyoutputsNode1);

		//re-add and update info 
		//children
		//Note: do not update node level, will be done by "add nodes to list"
		previousTree.getRoot().setLevel(1);
		for(MacroTreeNode node : previousTree.getRoot().getChildren()){
			node.setParent(child);
			child.getChildren().add(node);
		}
		//input links
		child.getInputLinks().addAll(previousTree.getRoot().getInputLinks());
		//child.getChildren().addAll(previousTree.getRoot().getChildren());



		//previous.getTemplateInputs().clear();		
	}

	private void addNodesToList(MacroTree current,
			ArrayList<MacroTreeNode> nodes, MacroTreeNode previousRoot) {
		previousRoot.setLevel(previousRoot.getLevel() + 1);
		for(MacroTreeNode treenode : nodes){
			if(treenode != previousRoot){
				treenode.setLevel(treenode.getLevel() + 1);
				current.addNodeToList(treenode);
			}
		}
	}

	private long addToHierarchy(MacroGenome macrogenome, ODNEATMacroNodeGene g1, HierarchicalMacroBehaviourGene hierarchy, 
			String hierarchyId) {
		MacroTree tree = hierarchy.getTree();
		ArrayList<ODNEATLinkGene> copyoutputsNode1 = auxOp.getOutputLinks(macrogenome, g1, true);
		ArrayList<ODNEATLinkGene> realOutputsNode1 = auxOp.getOutputLinks(macrogenome, g1, false);
		//rewire outputs
		//System.out.println("OUTPUTS OF: " + g1.getMacroId() + " " + copyoutputsNode1.size());
		//System.out.println("T-INPUTS OF: " + g1.getMacroId() + " " + g1.getTemplateInputs().size());
		for(ODNEATLinkGene link : realOutputsNode1){
			link.setFromId(tree.getRoot().getNodeMacroGene().getInnovationNumber());
		}
		for(ODNEATLinkGene link: realOutputsNode1){
			//check for duplicates
			macrogenome.filterRepeatedTemplateLinks(link.getFromId(), link.getToId());
		}
		//remove from the genome
		macrogenome.getMacroNodeGenes().remove(g1.getInnovationNumber());
		MacroTreeNode child = new MacroTreeNode(g1);
		child.setAcceptsChilds(false);
		tree.addChildToRoot(child, copyoutputsNode1);
		//now mark the input connections to the macro-node in a different manner
		//set them as inputs to a hierarchy
		ArrayList<ODNEATTemplateLinkGene> templateInputs = g1.getTemplateInputs();
		for(ODNEATTemplateLinkGene link : templateInputs){
			link.setBelongsToHierarchy(true);
			link.setHierarchyId(hierarchyId);
		}

		return tree.getRoot().getNodeMacroGene().getInnovationNumber();
	}

	private long createHierarchy(ODNEATGenome e, ODNEATMacroNodeGene g1,
			ODNEATMacroNodeGene g2, ArrayList<ODNEATMacroNodeGene> macronodes) {
		MacroGenome macrogenome = (MacroGenome) e;
		//create the hierarchy.
		ArbitratorBehaviourGene arb = new ArbitratorBehaviourGene(this.dib.nextInnovationNumber());
		long inNumber = dib.nextInnovationNumber();
		String macroId = "h." + this.id + "." + hierarchiesFormed++;
		HierarchicalMacroBehaviourGene hierarchy = new HierarchicalMacroBehaviourGene(macroId, inNumber);
		String arbitratorId = "a." + this.id + "." + this.arbitratorsCreated++;
		MacroTreeNode root = new MacroTreeNode(new ArbitratorMacroNodeGene(arbitratorId, dib.nextInnovationNumber(), 
				ODNEATNodeGene.ARBITRATOR_MACRO_NODE, arb));
		root.setAcceptsChilds(true);
		root.setLevel(0);
		hierarchy.initialise(root);
		addToHierarchy(macrogenome, g1, hierarchy, macroId);
		addToHierarchy(macrogenome, g2, hierarchy, macroId);
		long hierarchicalMacroId = dib.nextInnovationNumber();
		macrogenome.insertSingleNodeGene(new ODNEATHierarchicalMacroNodeGene(macroId, hierarchicalMacroId, 
				ODNEATNodeGene.HIERARCHICAL_MACRO_NODE,	hierarchy));

		
		return hierarchicalMacroId;
	}
}

