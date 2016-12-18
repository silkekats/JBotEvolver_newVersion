package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators;


import java.util.ArrayList;
import java.util.HashMap;

import datastructures.MacroTreeNode;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.EvolvedANNBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.PreprogrammedGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical.HierarchicalMacroBehaviourGene;

public class ODNEATAddLinkMutator extends Mutator<ODNEATGenome>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int MAX_LINK_ATTEMPTS = 30;

	protected double connectionWeightRange;

	protected ODNEATInnovationManager dib;

	public ODNEATAddLinkMutator(double weightRange, ODNEATInnovationManager dib){
		this.connectionWeightRange = weightRange;
		this.dib = dib;
	}

	@Override
	public void applyMutation(ODNEATGenome mutatee) {
		double linkRandVal = random.nextDouble();
		ODNEATLinkGene newLink = null;
		/**
		 * network-level operations
		 */
		if (linkRandVal < this.mutationProb) {
			ArrayList<ODNEATNodeGene> fromNodes = selectSourceNodes(mutatee);
			ArrayList<ODNEATNodeGene> toNodes = selectDestinationNodes(mutatee);
			ArrayList<ODNEATLinkGene> links = (ArrayList<ODNEATLinkGene>) mutatee.getLinkGenes(true);
			// find a new available link
			int tries = 0;
			while (newLink == null && tries < MAX_LINK_ATTEMPTS) {
				//try some recurrent
				long toNode = toNodes.get(random.nextInt(toNodes.size())).getInnovationNumber();
				long fromNode = fromNodes.get(random.nextInt(fromNodes.size())).getInnovationNumber();;
				if(!existsLinks(fromNode, toNode, links)){
					newLink = dib.submitLinkInnovation(fromNode, toNode);
					newLink.setWeight(generateRandomWeight());
					mutatee.insertSingleLinkGene(newLink);
				}
				tries++;
			}
		}
		/**
		 * macro node-level operations
		 */
		mutateMacroNodeGenes(mutatee);
		mutatee.sortGenes();
	}


	private void mutateMacroNodeGenes(ODNEATGenome mutatee) {
		if(!(mutatee instanceof MacroGenome))
			return;
		MacroGenome macrogenome = (MacroGenome) mutatee;
		HashMap<Long, ODNEATMacroNodeGene> macros = macrogenome.getMacroNodeGenes();
		for(long key : macros.keySet()){
			ODNEATNodeGene node = macros.get(key);
				ODNEATMacroNodeGene macronode = (ODNEATMacroNodeGene) node;
				if(! (macronode.getBehaviourGene() instanceof PreprogrammedGene) 
						&& !(macronode.getBehaviourGene() instanceof MacroBehaviourGene)){
					try {
						mutateIndividualMacroNodeGene(macronode);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		}
	}

	private void mutateIndividualMacroNodeGene(ODNEATMacroNodeGene node) throws Exception {
		if(node.getBehaviourGene() instanceof EvolvedANNBehaviourGene && random.nextDouble() < this.mutationProb){
			EvolvedANNBehaviourGene bGene = (EvolvedANNBehaviourGene) node.getBehaviourGene();
			mutateEvolvedANNGene(bGene);
		}
		/*else if(node.getBehaviourGene() instanceof TeachingANNMacroBehaviourGene){
			TeachingANNMacroBehaviourGene teaching = (TeachingANNMacroBehaviourGene) node.getBehaviourGene();
			mutateEvolvedANNGene(teaching.getLearner());
			mutateEvolvedANNGene(teaching.getTeacher());
		}*/
		else if(node.getBehaviourGene() instanceof HierarchicalMacroBehaviourGene){
			HierarchicalMacroBehaviourGene hierarchical = (HierarchicalMacroBehaviourGene) node.getBehaviourGene();
			for(MacroTreeNode treeNode : hierarchical.getTree().getAllNodes()){
				if(treeNode.getNodeMacroGene().getBehaviourGene() instanceof EvolvedANNBehaviourGene 
						&& random.nextDouble() < this.mutationProb){
					EvolvedANNBehaviourGene bGene = (EvolvedANNBehaviourGene) treeNode.getNodeMacroGene().getBehaviourGene();
					mutateEvolvedANNGene(bGene);
				}
			}
		}
		else {
			System.out.println(node.getClass());
			throw new Exception("Unsupported type");
		} 
	}

	private void mutateEvolvedANNGene(EvolvedANNBehaviourGene net) {
		ArrayList<ODNEATNodeGene> fromNodes = net.getNodesList();
		ArrayList<ODNEATNodeGene> toNodes = net.getNodesList();
		
		ArrayList<ODNEATLinkGene> links = (ArrayList<ODNEATLinkGene>) net.getLinkGenes(true);
		// find a new available link
		int tries = 0;
		ODNEATLinkGene newLink = null;
		while (newLink == null && tries < MAX_LINK_ATTEMPTS) {
			//try some recurrent
			long toNode = toNodes.get(random.nextInt(toNodes.size())).getInnovationNumber();
			long fromNode = fromNodes.get(random.nextInt(fromNodes.size())).getInnovationNumber();
			if(!existsLinks(fromNode, toNode, links)){
				newLink = dib.submitLinkInnovation(fromNode, toNode);
				newLink.setWeight(generateRandomWeight());
				net.insertSingleLinkGene(newLink);
			}
			tries++;
		}
	}


	/**
	 * all except the macro neurons.
	 */
	protected ArrayList<ODNEATNodeGene> selectSourceNodes(
			ODNEATGenome mutatee) {
		if(mutatee instanceof MacroGenome){
			MacroGenome macro = (MacroGenome) mutatee;
			return macro.getStandardNodeGenes();
		}
		
		return (ArrayList<ODNEATNodeGene>) mutatee.getNodeGenes();
	}

	/**
	 * all except the inputs and the macro nodes.
	 */
	protected ArrayList<ODNEATNodeGene> selectDestinationNodes(ODNEATGenome mutatee) {
		ArrayList<ODNEATNodeGene> destinationNodes = new ArrayList<ODNEATNodeGene>();
		ArrayList<ODNEATNodeGene> nodesToProcess;
		if(mutatee instanceof MacroGenome){
			MacroGenome macroGenome = (MacroGenome) mutatee;
			nodesToProcess = macroGenome.getStandardNodeGenes();
		}
		else {
			nodesToProcess = (ArrayList<ODNEATNodeGene>) mutatee.getNodeGenes();
		}
		
		for(ODNEATNodeGene g : nodesToProcess){
			if(g.getType() != ODNEATNodeGene.INPUT)
				destinationNodes.add(g);
		}
		
		return destinationNodes;
	}

	protected double generateRandomWeight() {
		return random.nextDouble() * connectionWeightRange * 2 - connectionWeightRange;
	}

	protected boolean existsLinks(long fromNode, long toNode, ArrayList<ODNEATLinkGene> links) {
		for(ODNEATLinkGene g : links){
			if(g.getFromId() == fromNode && g.getToId() == toNode)
				return true;
		}
		return false;
	}



}

