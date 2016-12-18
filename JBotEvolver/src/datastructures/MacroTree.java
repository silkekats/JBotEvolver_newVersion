package datastructures;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;

/**
 * operations such as add or remove should be executed here to keep a coherent view
 * of the structure of the entire tree, and not using macro tree node methods
 * @author fernando
 *
 */
public class MacroTree implements Serializable{

	private MacroTreeNode root;

	//keep a list of all nodes in the tree, for faster access;
	private ArrayList<MacroTreeNode> allNodes;
	

	public MacroTree(MacroTreeNode root){
		this.root = root;
		this.allNodes = new ArrayList<MacroTreeNode>();
		//this.allNodes.add(root);
	}

	public MacroTreeNode getRoot(){
		return this.root;
	}

	public void setRoot(MacroTreeNode root){
		this.root = root;
	}

	public boolean addChildToRoot(MacroTreeNode child, ArrayList<ODNEATLinkGene> oldOutputsOfCandidate){
		//simple node
		this.allNodes.add(child);
		return root.addChild(child, oldOutputsOfCandidate);
	}

	public boolean addChild(MacroTreeNode child, MacroTreeNode parent, ArrayList<ODNEATLinkGene> oldOutputsOfCandidate){
		if(!allNodes.contains(parent)){
			return false;
		}

		if(parent.askAcceptsChilds()){
			allNodes.add(child);
			return parent.addChild(child, oldOutputsOfCandidate);
		}

		return false;
	}

	public ArrayList<MacroTreeNode> getAllNodes(){
		//a little hack here.
		ArrayList<MacroTreeNode> list = new ArrayList<MacroTreeNode>();
		list.add(root);
		list.addAll(this.allNodes);
		return list;
	}

	public int getNumberOfNodes(){
		//+ 1 = root
		return allNodes.size() + 1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MacroTree other = (MacroTree) obj;
		if (allNodes == null) {
			if (other.allNodes != null)
				return false;
		} else if (!allNodes.equals(other.allNodes))
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}

	public MacroTree copy(){
		MacroTree copy = new MacroTree(this.root.copy());

		for(MacroTreeNode node : this.allNodes){
			copy.allNodes.add(node.copy());
		}

		return copy;
	}

	public ArrayList<MacroTreeNode> getLeafs(){
		ArrayList<MacroTreeNode> leafs = new ArrayList<MacroTreeNode>();
		for(MacroTreeNode node : this.allNodes){
			if(!node.askAcceptsChilds()){
				leafs.add(node);
			}
		}
		return leafs;
	}

	public boolean hasParameters() {
		for(MacroTreeNode node : allNodes){
			if(node.getNodeMacroGene().getBehaviourGene().hasParameters()){
				return true;
			}
		}
		return false;
	}

	public HashMap<String, Double> getParameters() {
		HashMap<String, Double> parameters = new HashMap<String, Double>();
		for(MacroTreeNode node : allNodes){
			if(node.getNodeMacroGene().getBehaviourGene().hasParameters()){
				parameters.putAll(node.getNodeMacroGene().getBehaviourGene().getParameters());
			}
		}
		return parameters;
	}

	public double normaliseParameterValue(String key, double value) {
		for(MacroTreeNode node : allNodes){
			MacroBehaviourGene behave = node.getNodeMacroGene().getBehaviourGene();
			if(behave.hasParameters() && behave.getParameters().containsKey(key)){
				return behave.normaliseParameterValue(key, value);
			}
		}
		return value;
	}

	public int getMaxLevel() {
		int maxLevel = 0;
		for(MacroTreeNode node : allNodes){
			maxLevel = Math.max(maxLevel, node.getLevel());
		}
		
		return maxLevel;
	}


	public void removeNode(MacroTreeNode node) {
		allNodes.remove(node);
		MacroTreeNode nodeParent = node.getParent();
		nodeParent.removeChild(node);
	}

	public void addNodeToList(MacroTreeNode treenode) {
		this.allNodes.add(treenode);
	}


}

