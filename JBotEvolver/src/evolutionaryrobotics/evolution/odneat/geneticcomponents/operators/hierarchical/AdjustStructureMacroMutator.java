package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical;


import java.util.ArrayList;

import datastructures.MacroTree;
import datastructures.MacroTreeNode;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.ODNEATHierarchicalMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.Mutator;

public class AdjustStructureMacroMutator extends Mutator<ODNEATGenome>{

	private AuxMacroOperator auxOp = new AuxMacroOperator();
	private ODNEATInnovationManager dib;


	public AdjustStructureMacroMutator(ODNEATInnovationManager in){
		this.dib = in;
	}

	@Override
	public void applyMutation(ODNEATGenome e) {
		MacroGenome macrogenome = (MacroGenome) e;
		ArrayList<ODNEATHierarchicalMacroNodeGene> hierarchicalNodes = auxOp.getHierarchicalNodes(macrogenome);
		for(ODNEATHierarchicalMacroNodeGene node : hierarchicalNodes){
			if(random.nextDouble() < this.mutationProb){
				//randomly choose one leaf and move it one level below.
				if(random.nextBoolean())
					this.movePartToHigherLevel(node);
				else 
					this.movePartToLowerLevel(node);

				//node.setInnovationNumber(dib.nextInnovationNumber());
			}
		}
	}

	private void movePartToHigherLevel(ODNEATHierarchicalMacroNodeGene node) {
		MacroTree tree = ((HierarchicalMacroBehaviourGene) node.getBehaviourGene()).getTree();
		ArrayList<MacroTreeNode> leafs = tree.getLeafs();
		MacroTreeNode leafToMove = leafs.get(random.nextInt(leafs.size()));
		//already in the highest level possible
		if(leafToMove.getLevel() < 2){
			return;
		}

		MacroTreeNode parent = leafToMove.getParent();
		MacroTreeNode newParent = parent.getParent();
		
		ArrayList<ODNEATLinkGene> links = parent.removeLinksFrom(leafToMove);
		tree.getAllNodes().remove(leafToMove);
		tree.addChild(leafToMove, newParent, links);

		//check if the previous parent is really necessary.
		if(parent.getChildren().size() == 0){
			tree.removeNode(parent);
		}
	}

	private void movePartToLowerLevel(ODNEATHierarchicalMacroNodeGene node) {
		MacroTree tree = ((HierarchicalMacroBehaviourGene) node.getBehaviourGene()).getTree();
		ArrayList<MacroTreeNode> leafs = tree.getLeafs();
		
		MacroTreeNode leafToMove = leafs.get(random.nextInt(leafs.size()));
		int maxLevel = tree.getMaxLevel();
		if(maxLevel == leafToMove.getLevel()){
			return;
		}

		MacroTreeNode parent = leafToMove.getParent();
		
		//get all that are not leaves
		ArrayList<MacroTreeNode> notLeaves = parent.getNodeChildren();
		if(notLeaves.size() > 0){
			int index = random.nextInt(notLeaves.size());
			MacroTreeNode newParent = notLeaves.get(index);
			
			ArrayList<ODNEATLinkGene> links = parent.removeLinksFrom(leafToMove);
			tree.getAllNodes().remove(leafToMove);
			tree.addChild(leafToMove, newParent, links);
		}
		
	}
}

