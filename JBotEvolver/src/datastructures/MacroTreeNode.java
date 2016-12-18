package datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;

public class MacroTreeNode implements Serializable{

	private ODNEATMacroNodeGene ownGene;
	private boolean acceptsChilds;
	private int level;

	private ArrayList<MacroTreeNode> children;
	private ArrayList<ODNEATLinkGene> inputLinks;

	//ref to parent for easier access
	private MacroTreeNode parent;

	public MacroTreeNode(ODNEATMacroNodeGene gene){
		this.ownGene = gene;
		this.children = new ArrayList<MacroTreeNode>();
		this.inputLinks = new ArrayList<ODNEATLinkGene>();
	}

	//also updates the level of the child
	public void setLevel(int newLevel){
		this.level = newLevel;
		/*for(MacroTreeNode node : children){
			node.setLevel(this.level + 1);
		}*/
	}

	public int getLevel(){
		return level;
	}

	public boolean askAcceptsChilds(){
		return this.acceptsChilds;
	}

	public void setAcceptsChilds(boolean acceptsChilds){
		this.acceptsChilds = acceptsChilds;
	}

	public boolean containsChild(MacroTreeNode candidate){
		return this.children.contains(candidate);
	}

	public boolean removeChild(MacroTreeNode candidate){
		boolean remove = this.children.remove(candidate);
		if(remove){
			long innovationNumber = candidate.getNodeMacroGene().getInnovationNumber();
			Iterator<ODNEATLinkGene> it = this.inputLinks.iterator();
			while(it.hasNext()){
				ODNEATLinkGene next = it.next();
				if(next.getFromId() == innovationNumber || next.getToId() == innovationNumber){
					it.remove();
				}
			}
		}
		return remove;
	}

	/**
	 * the output links need to be rewired to the root
	 * @requires connections from root to previous outputs of the child are already rewired.
	 */
	public boolean addChild(MacroTreeNode candidate, ArrayList<ODNEATLinkGene> oldOutputsOfCandidate){
		boolean result = this.children.add(candidate);
		long rootNumber = this.ownGene.getInnovationNumber();
		//rewiring
		for(ODNEATLinkGene link : oldOutputsOfCandidate){
			ODNEATLinkGene newLink = new ODNEATLinkGene(link.getInnovationNumber(),
					true, link.getFromId(), rootNumber, 1.0);
			if(!this.inputLinks.contains(newLink)){
				this.inputLinks.add(newLink);
			}
		}
		candidate.setLevel(this.level + 1);
		candidate.parent = this;
		
		return result;
	}

	public MacroTreeNode getParent(){
		return parent;
	}

	public ArrayList<MacroTreeNode> getChildren(){
		return this.children;
	}

	public ODNEATMacroNodeGene getNodeMacroGene(){
		return this.ownGene;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || (!(obj instanceof MacroTreeNode)))
			return false;
		MacroTreeNode other = (MacroTreeNode) obj;
		if (acceptsChilds != other.acceptsChilds)
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (inputLinks == null) {
			if (other.inputLinks != null)
				return false;
		} else if (!inputLinks.equals(other.inputLinks))
			return false;
		if (level != other.level)
			return false;
		if (ownGene == null) {
			if (other.ownGene != null)
				return false;
		} else if (!ownGene.equals(other.ownGene))
			return false;
		return true;
	}

	public MacroTreeNode copy(){
		MacroTreeNode copy = new MacroTreeNode(this.ownGene.copy());
		copy.acceptsChilds = this.acceptsChilds;
		copy.level = this.level;
		copy.parent = this.parent;

		for(MacroTreeNode node : this.children){
			MacroTreeNode copiedNode = node.copy();
			copiedNode.parent = this;
			copy.children.add(copiedNode);

		}

		for(ODNEATLinkGene link : this.inputLinks){
			copy.inputLinks.add(link.copy());
		}

		return copy;
	}

	//note: removes the links
	public ArrayList<ODNEATLinkGene> removeLinksFrom(MacroTreeNode leafToMove) {
		ArrayList<ODNEATLinkGene> links = new ArrayList<ODNEATLinkGene>();
		Iterator<ODNEATLinkGene> it = this.inputLinks.iterator();
		while(it.hasNext()){
			ODNEATLinkGene link = it.next();
			if(link.getFromId() == leafToMove.getNodeMacroGene().getInnovationNumber()){
				links.add(link);
				it.remove();
			}
		}
		return links;
	}

	public ArrayList<MacroTreeNode> getNodeChildren() {
		ArrayList<MacroTreeNode> nodes = new ArrayList<MacroTreeNode>();
		for(MacroTreeNode node : this.children){
			if(node.acceptsChilds){
				nodes.add(node);
			}
		}

		return nodes;
	}

	public void replaceAllInnovations(HashMap<Long, Long> oldToNew,
			ODNEATInnovationManager dib) {
		for(ODNEATLinkGene link : this.inputLinks){
			if(!oldToNew.containsKey(link.getToId())){
				oldToNew.put(link.getToId(), dib.nextInnovationNumber());
			}
			if(!oldToNew.containsKey(link.getInnovationNumber())){
				oldToNew.put(link.getInnovationNumber(), dib.nextInnovationNumber());
			}
			if(!oldToNew.containsKey(link.getFromId())){
				oldToNew.put(link.getFromId(), dib.nextInnovationNumber());
			}

			link.setFromId(oldToNew.get(link.getFromId()));
			link.setToId(oldToNew.get(link.getToId()));				
			link.setInnovationNumber(oldToNew.get(link.getInnovationNumber()));
		}
		ownGene.replaceAllInnovations(oldToNew, dib);
	}

	public ArrayList<ODNEATLinkGene> getInputLinks(){
		return this.inputLinks;
	}

	public void setParent(MacroTreeNode newParent) {
	
		this.parent = newParent;
	}

}

