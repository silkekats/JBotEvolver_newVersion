package evolutionaryrobotics.evolution.odneat.geneticcomponents.hierarchical;



import java.io.Serializable;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;


public class ArbitratorMacroNodeGene extends ODNEATMacroNodeGene implements Serializable{

	public enum FUNCTION{
		MAX, MIN, AVG;
	}

	
	private FUNCTION function;
	private FUNCTION[] functions;
	public ArbitratorMacroNodeGene(String macroId, long innovationNumber,
			int type, MacroBehaviourGene gene) {
		super(macroId, innovationNumber, type, gene);
		functions = new FUNCTION[]{FUNCTION.MAX, FUNCTION.MIN, FUNCTION.AVG};
		function = getFunctions()[(int)(Math.random()*2)];
	}
	
	public FUNCTION getFunction(){
		return function;
	}
	
	public FUNCTION[] getFunctions(){
		return functions;
	}
	
	public void setFunction(FUNCTION newFunction){
		this.function = newFunction;
	}
	
	public ArbitratorMacroNodeGene copy(){
		ArbitratorMacroNodeGene copy = new ArbitratorMacroNodeGene(new String(macroId), this.innovationNumber, this.type, this.getBehaviourGene().copy());
		copy.function = this.function;
		
		return copy;
	}
}

