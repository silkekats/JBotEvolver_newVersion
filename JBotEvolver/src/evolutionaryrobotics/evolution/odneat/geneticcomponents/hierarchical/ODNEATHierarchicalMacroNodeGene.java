package evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical;


import java.io.Serializable;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATTemplateLinkGene;

public class ODNEATHierarchicalMacroNodeGene extends ODNEATMacroNodeGene implements Serializable{

	

	
	public ODNEATHierarchicalMacroNodeGene(String macroId,
			long innovationNumber, int type, MacroBehaviourGene gene) {
		super(macroId, innovationNumber, type, gene);
	}
	
	@Override
	public boolean equals(Object o){
		if((! (o instanceof ODNEATHierarchicalMacroNodeGene)) || o == null)
			return false;

		ODNEATHierarchicalMacroNodeGene other = (ODNEATHierarchicalMacroNodeGene) o;
		boolean basicMatch = other.getMacroId().equalsIgnoreCase(this.macroId)
				&& other.getInnovationNumber() == this.getInnovationNumber() &&
				other.type == this.type
				&& other.behaviourGene.equals(this.behaviourGene)
				&& this.getTemplateInputs().size() == other.getTemplateInputs().size();
		
		if(!basicMatch || !other.getTemplateInputs().containsAll(this.getTemplateInputs()))
			return false;
		
		return true;
	}

	public ODNEATHierarchicalMacroNodeGene copy(){
		ODNEATHierarchicalMacroNodeGene copy = new ODNEATHierarchicalMacroNodeGene(new String(this.macroId), 
				innovationNumber, type, this.behaviourGene.copy());
		
		for(ODNEATTemplateLinkGene link : this.getTemplateInputs()){
			copy.addTemplateInput(link.copy());
		}

		
		return copy;
	}

}

