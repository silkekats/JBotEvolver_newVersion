package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators;


import java.util.ArrayList;
import java.util.HashMap;

import datastructures.MacroTreeNode;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.EvolvedANNBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATActionLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATPriorityLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATTemplateLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.PreprogrammedGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;

public class ODNEATAddNodeMutator extends Mutator<ODNEATGenome> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ODNEATInnovationManager dib;

	public ODNEATAddNodeMutator(ODNEATInnovationManager dib) {
		this.dib = dib;
	}

	@Override
	public void applyMutation(ODNEATGenome mutatee) {
		double nodeRandVal = random.nextDouble();
		ArrayList<ODNEATLinkGene> enabledLinks;
		// ArrayList nodes;
		ODNEATLinkGene chosen, newUpper;
		ODNEATNodeGene newNode;
		// System.out.println(pAddNode);
		if (nodeRandVal < this.mutationProb && ((enabledLinks = (ArrayList<ODNEATLinkGene>) mutatee.getLinkGenes(true)).size() > 0)) {
			chosen = enabledLinks.get(random.nextInt(enabledLinks.size()));
			// disable old link
			//chosen.setEnabled(false);
			newNode = dib.submitNodeInnovation();
			//requires that these are **outputs** of the macro-neurons (the inputs are maintained by the macro-neurons themselves)
			//newLower = dib.submitLinkInnovation(chosen.getFromId(), newNode.getInnovationNumber());
			newUpper = dib.submitLinkInnovation(newNode.getInnovationNumber(), chosen.getToId());
			//newLower.setWeight(1);
			newUpper.setWeight(chosen.getWeight());
			chosen.setWeight(1.0);
			chosen.setToId(newNode.getInnovationNumber());
			//removed from the genome so that it is not re-enabled
			/*if(verifyTypeOfNewConnection(newLower, chosen)){
				mutatee.getLinkGenes(true).remove(chosen);
			}*/			
			// now update the chromosome with new node and 2 new links
			mutatee.insertSingleNodeGene(newNode);
			//mutatee.insertSingleLinkGene(newLower);
			mutatee.insertSingleLinkGene(newUpper);
		}

		mutateMacroGenes(mutatee);

		//mutatee.sortGenes();
	}

	private boolean verifyTypeOfNewConnection(ODNEATLinkGene newLower,
			ODNEATLinkGene chosen) {
		ODNEATLinkGene temp = null;
		boolean hasUnwantedType = false;
		if(chosen instanceof ODNEATPriorityLinkGene){
			ODNEATPriorityLinkGene result = (ODNEATPriorityLinkGene) chosen;
			temp = new ODNEATPriorityLinkGene(newLower.getInnovationNumber(), true, newLower.getFromId(), newLower.getToId(),
					result.getMacroId());
			hasUnwantedType = true;
		}
		else if(chosen instanceof ODNEATActionLinkGene){
			ODNEATActionLinkGene result = (ODNEATActionLinkGene) chosen;
			temp = new ODNEATActionLinkGene(newLower.getInnovationNumber(), true, newLower.getFromId(), newLower.getToId(),
					result.getMacroId());
			hasUnwantedType = true;
		}
		else if(chosen instanceof ODNEATTemplateLinkGene){
			ODNEATTemplateLinkGene result = (ODNEATTemplateLinkGene) chosen;
			temp = new ODNEATTemplateLinkGene(newLower.getInnovationNumber(), true, newLower.getFromId(), newLower.getToId(), 
					newLower.getWeight(), result.getMacroId(), result.isInput());
		}

		if(temp != null){
			temp.setWeight(newLower.getWeight());
			newLower = temp;
		}

		return hasUnwantedType;
	}

	private void mutateMacroGenes(ODNEATGenome mutatee) {
		if(!(mutatee instanceof MacroGenome))
			return;
		MacroGenome macroGenome = (MacroGenome) mutatee;
		HashMap<Long, ODNEATMacroNodeGene> macroMap = macroGenome.getMacroNodeGenes();
		//ArrayList<ODNEATMacroNodeGene> macronodes = (ArrayList<ODNEATMacroNodeGene>) macroGenome.getMacroNodeGenes().values();
		for(long key : macroMap.keySet()){
			ODNEATMacroNodeGene node = macroMap.get(key);
			this.mutateIndividualMacroGene(node, mutatee);
		}
	}

	private void mutateIndividualMacroGene(ODNEATMacroNodeGene node, ODNEATGenome mutatee) {

		if(node.getBehaviourGene() instanceof PreprogrammedGene){
			if(random.nextDouble() < this.mutationProb)
				splitTemplateInputsOf(node, mutatee);
		}
		else if(node.getBehaviourGene() instanceof EvolvedANNBehaviourGene){
			performMutationOnEvolvedANN(node, mutatee);

		}
		else if(node.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene){
			HierarchicalMacroBehaviourGene hierarchy = (HierarchicalMacroBehaviourGene) node.getBehaviourGene();
			for(MacroTreeNode treeNode : hierarchy.getTree().getAllNodes()){
				if(treeNode.getNodeMacroGene().getBehaviourGene() instanceof PreprogrammedGene 
						&& random.nextDouble() < this.mutationProb){
					this.splitTemplateInputsOf(treeNode.getNodeMacroGene(), mutatee);
				}
				else if(treeNode.getNodeMacroGene().getBehaviourGene() instanceof EvolvedANNBehaviourGene){
					performMutationOnEvolvedANN(treeNode.getNodeMacroGene(), mutatee);
				}
			}
		}
	}

	private void performMutationOnEvolvedANN(ODNEATMacroNodeGene node, ODNEATGenome mutatee) {
		if(random.nextDouble() < this.mutationProb){
			//split input nodes vs. split something in the inner part of the net, 50/50
			if(random.nextBoolean()){
				splitTemplateInputsOf(node, mutatee);
			}
			else {
				EvolvedANNBehaviourGene bGene = (EvolvedANNBehaviourGene) node.getBehaviourGene();
				mutateEvolvedANNGene(bGene);
			}
		}
	}

	private void mutateEvolvedANNGene(EvolvedANNBehaviourGene bGene) {
		ArrayList<ODNEATLinkGene> enabledLinks = bGene.getLinkGenes(true);
		ODNEATLinkGene chosen = enabledLinks.get(random.nextInt(enabledLinks.size()));
		//chosen.setEnabled(false);
		ODNEATNodeGene newNode = dib.submitNodeInnovation();
		//ODNEATLinkGene newLower = dib.submitLinkInnovation(chosen.getFromId(), newNode.getInnovationNumber());
		ODNEATLinkGene newUpper = dib.submitLinkInnovation(newNode.getInnovationNumber(), chosen.getToId());
		//newLower.setWeight(1);
		newUpper.setWeight(chosen.getWeight());
		chosen.setWeight(1.0);
		chosen.setToId(newNode.getInnovationNumber());
		//add
		//bGene.insertSingleLinkGene(newLower);
		bGene.insertSingleLinkGene(newUpper);
		bGene.insertSingleNodeGene(newNode);
	}

	private void splitTemplateInputsOf(ODNEATMacroNodeGene node,
			ODNEATGenome mutatee) {
		ODNEATNodeGene newNode = dib.submitNodeInnovation();
		ArrayList<ODNEATTemplateLinkGene> links = node.getTemplateInputs();
		ODNEATTemplateLinkGene chosen = links.get(random.nextInt(links.size()));
		ODNEATLinkGene newLower = dib.submitLinkInnovation(chosen.getFromId(), 
				newNode.getInnovationNumber());
		newLower.setWeight(1.0);
		chosen.setFromId(newNode.getInnovationNumber());
		mutatee.insertSingleLinkGene(newLower);
		mutatee.insertSingleNodeGene(newNode);
	}


}

