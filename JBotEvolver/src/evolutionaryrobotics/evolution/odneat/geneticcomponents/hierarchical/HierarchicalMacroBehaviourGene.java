package evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import datastructures.MacroTree;
import datastructures.MacroTreeNode;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;

public class HierarchicalMacroBehaviourGene implements MacroBehaviourGene, Serializable {

	private String macroId;
	private long innovationNumber;
	private MacroTree hierarchy;
	
	public HierarchicalMacroBehaviourGene(String macroId,
			long innovationNumber) {
		this.macroId = macroId;
		this.innovationNumber = innovationNumber;
	}
	
	public void initialise(MacroTreeNode root){
		hierarchy = new MacroTree(root);
	}
	
	@Override
	public long getInnovationNumber() {
		return innovationNumber;
	}

	@Override
	public HierarchicalMacroBehaviourGene copy() {
		HierarchicalMacroBehaviourGene copy = new HierarchicalMacroBehaviourGene(new String(macroId), innovationNumber);
		copy.hierarchy = this.hierarchy.copy();
		return copy;
	}

	@Override
	public boolean hasParameters() {
		return hierarchy.hasParameters();
	}

	@Override
	public HashMap<String, Double> getParameters() {
		return hierarchy.getParameters();
	}

	@Override
	public double normaliseParameterValue(String key, double value) {
		return hierarchy.normaliseParameterValue(key, value);
	}

	@Override
	public int getNumberOfBehaviourGenes() {
		return hierarchy.getNumberOfNodes();
	}

	public String getHierarchyRepresentation(){
		return "";
	}
	
	public boolean equals(Object obj){
		if(obj == null || (!(obj instanceof HierarchicalMacroBehaviourGene))){
			return false;
		}
		HierarchicalMacroBehaviourGene other = (HierarchicalMacroBehaviourGene) obj;
		return this.macroId.equals(other.macroId) && this.innovationNumber == other.innovationNumber &&
				this.hierarchy.equals(other.hierarchy);
	}
	
	public MacroTree getTree() {
		return this.hierarchy;
	}
	
	public String getMacroId(){
		return this.macroId;
	}
	
	@Override
	public void replaceAllInnovations(HashMap<Long, Long> oldToNew,
			ODNEATInnovationManager dib) {
		if(!oldToNew.containsKey(this.innovationNumber)){
			oldToNew.put(this.innovationNumber, dib.nextInnovationNumber());
		}
		this.innovationNumber = oldToNew.get(this.innovationNumber);
		for(MacroTreeNode node : this.hierarchy.getAllNodes()){
			node.replaceAllInnovations(oldToNew, dib);
		}
	}
	
	@Override
	public boolean containsSubNodeWithId(long id) {
		if(id == this.innovationNumber)
			return true;
		ArrayList<MacroTreeNode> nodes = this.hierarchy.getAllNodes();
		
		for(MacroTreeNode node : nodes){
			if(node.getNodeMacroGene().getBehaviourGene().containsSubNodeWithId(id))
				return true;
		}
		
		return false;
	}
	
	@Override
	public ArrayList<ODNEATLinkGene> getConnectionsList() {
		ArrayList<ODNEATLinkGene> links = new ArrayList<ODNEATLinkGene>();
		
		for(MacroTreeNode node : hierarchy.getAllNodes()){
			links.addAll(node.getInputLinks());
			links.addAll(node.getNodeMacroGene().getTemplateInputs());
			links.addAll(node.getNodeMacroGene().getBehaviourGene().getConnectionsList());
		}
		
		return links;
	}
	
	//===========================000 not used
	
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

}

