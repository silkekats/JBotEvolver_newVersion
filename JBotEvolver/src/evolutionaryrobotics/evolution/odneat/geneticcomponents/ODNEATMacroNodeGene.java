package evolutionaryrobotics.evolution.odneat.geneticcomponents;


import java.util.ArrayList;
import java.util.HashMap;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;

public class ODNEATMacroNodeGene extends ODNEATNodeGene {

	private static final long serialVersionUID = 1L;

	protected String macroId;
	protected MacroBehaviourGene behaviourGene;

	private ArrayList<ODNEATTemplateLinkGene> inputs;


	public ODNEATMacroNodeGene(String macroId, long innovationNumber, 
			int type, MacroBehaviourGene gene) {
		super(innovationNumber, type);
		this.macroId = new String(macroId);
		this.behaviourGene = gene.copy();
		this.inputs = new ArrayList<ODNEATTemplateLinkGene>();
	}

	public ArrayList<ODNEATTemplateLinkGene> getTemplateInputs(){
		return this.inputs;
	}

	public void addTemplateInput(ODNEATTemplateLinkGene input){
		if(!this.inputs.contains(input))
			this.inputs.add(input);
	}

	public void removeTemplateInput(ODNEATTemplateLinkGene input){
		this.inputs.remove(input);
	}


	public String getMacroId(){
		return macroId;
	}

	public void setMacroId(String newId){
		this.macroId = newId;
	}

	public MacroBehaviourGene getBehaviourGene(){
		return this.behaviourGene;
	}

	@Override
	public boolean equals(Object o){
		if((! (o instanceof ODNEATMacroNodeGene)) || o == null)
			return false;

		ODNEATMacroNodeGene other = (ODNEATMacroNodeGene) o;
		boolean basicMatch = other.getMacroId().equalsIgnoreCase(this.macroId)
				&& other.getInnovationNumber() == this.getInnovationNumber() &&
				other.type == this.type
				&& other.behaviourGene.equals(this.behaviourGene)
				&& this.inputs.size() == other.inputs.size();

		if(!basicMatch || !other.inputs.containsAll(this.inputs))
			return false;

		return true;
	}

	public ODNEATMacroNodeGene copy(){
		ODNEATMacroNodeGene copy = new ODNEATMacroNodeGene(new String(this.macroId), 
				innovationNumber, type, this.behaviourGene.copy());

		for(ODNEATTemplateLinkGene link : this.inputs){
			copy.addTemplateInput(link.copy());
		}


		return copy;
	}

	public HashMap<Long, Long> replaceAllInnovations(ODNEATInnovationManager dib) {
		HashMap<Long, Long> oldToNew = new HashMap<Long, Long>();
		oldToNew.put(this.innovationNumber, dib.nextInnovationNumber());
		this.innovationNumber = oldToNew.get(this.innovationNumber);

		for(ODNEATLinkGene link : this.inputs){
			if(!oldToNew.containsKey(link.getToId())){
				oldToNew.put(link.getToId(), dib.nextInnovationNumber());
			}
			if(!oldToNew.containsKey(link.getInnovationNumber())){
				oldToNew.put(link.getInnovationNumber(), dib.nextInnovationNumber());
			}
			link.setToId(oldToNew.get(link.getToId()));				
			link.setInnovationNumber(oldToNew.get(link.getInnovationNumber()));
		}
		behaviourGene.replaceAllInnovations(oldToNew, dib);
		return oldToNew;
	}

	/**
	 * here we can replace the "from connection" id
	 * @param oldToNew
	 * @param dib
	 */
	public void replaceAllInnovations(HashMap<Long, Long> oldToNew,
			ODNEATInnovationManager dib) {
		if(!oldToNew.containsKey(this.getInnovationNumber())){
			oldToNew.put(this.innovationNumber, dib.nextInnovationNumber());
		}
		this.innovationNumber = oldToNew.get(this.innovationNumber);
		for(ODNEATLinkGene link : this.inputs){
			if(!oldToNew.containsKey(link.getToId())){
				oldToNew.put(link.getToId(), dib.nextInnovationNumber());
			}
			/*if(!oldToNew.containsKey(link.getFromId())){
				oldToNew.put(link.getFromId(), dib.nextInnovationNumber());
			}*/
			if(!oldToNew.containsKey(link.getInnovationNumber())){
				oldToNew.put(link.getInnovationNumber(), dib.nextInnovationNumber());
			}
			//link.setFromId(oldToNew.get(link.getFromId()));
			link.setToId(oldToNew.get(link.getToId()));				
			link.setInnovationNumber(oldToNew.get(link.getInnovationNumber()));
		}
		behaviourGene.replaceAllInnovations(oldToNew, dib);
	}

	public ArrayList<ODNEATLinkGene> getAllConnections() {
		ArrayList<ODNEATLinkGene> links = new ArrayList<ODNEATLinkGene>();
		for(ODNEATLinkGene link : this.inputs){
			if(!links.contains(link)){
				links.add(link);
			}
		}
		ArrayList<ODNEATLinkGene> innerLinks = this.getBehaviourGene().getConnectionsList();
		for(ODNEATLinkGene link : innerLinks){
			if(!links.contains(link)){
				links.add(link);
			}
		}
		
		return links;
	}

}

