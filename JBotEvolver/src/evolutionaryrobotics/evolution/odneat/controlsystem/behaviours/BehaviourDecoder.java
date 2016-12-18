package evolutionaryrobotics.evolution.odneat.controlsystem.behaviours;



import java.util.ArrayList;
import java.util.Collections;

import evolutionaryrobotics.evolution.odneat.controlsystem.components.DoNotMoveMacroNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.MacroNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.ProgrammedMacroNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.MoveForwardMacroNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.NeuralNetLayer;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.Neuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.Synapse;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.TurnLeftMacroNeuron;
import evolutionaryrobotics.evolution.odneat.controlsystem.components.TurnRightMacroNeuron;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.MacroBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATMacroNodeGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.DoNotMoveBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.MoveForwardBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.TurnLeftBehaviourGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.TurnRightBehaviourGene;
import evolutionaryrobotics.evolution.odneat.genotypePhenotypeMapping.ExtendedMacroANNCodec;

public class BehaviourDecoder {

	public MacroNeuron decodePreprogrammed(ODNEATMacroNodeGene node, MacroBehaviourGene gene) {
		return this.mapProgrammedGeneToNeuron(gene, node.getInnovationNumber(), new String(node.getMacroId()), node);
	}

	protected ProgrammedMacroNeuron mapProgrammedGeneToNeuron(MacroBehaviourGene gene, long innovationNumber,
			String macroId, ODNEATMacroNodeGene node) {
		ProgrammedMacroNeuron macro = null;
		if(gene instanceof MoveForwardBehaviourGene){
			MoveForwardBehaviourGene g = (MoveForwardBehaviourGene) gene;
			macro = new MoveForwardMacroNeuron(innovationNumber, macroId, (int) Math.round(g.getMovementParameter()));
		}
		else if(gene instanceof TurnLeftBehaviourGene){
			TurnLeftBehaviourGene g = (TurnLeftBehaviourGene) gene;
			macro = new TurnLeftMacroNeuron(innovationNumber, macroId, (int) Math.round(g.getMovementParameter()));
		}
		else if(gene instanceof TurnRightBehaviourGene){
			TurnRightBehaviourGene g = (TurnRightBehaviourGene) gene;
			macro = new TurnRightMacroNeuron(innovationNumber, macroId, (int) Math.round(g.getMovementParameter()));
		}
		else if(gene instanceof DoNotMoveBehaviourGene){
			//DoNotMoveBehaviourGene g = (DoNotMoveBehaviourGene) gene;
			macro = new DoNotMoveMacroNeuron(innovationNumber, macroId);
		}

		return macro;
	}
	
	/*public FastEvolvedMacroNeuron createANNMacroNeuron(ArrayList<Synapse> synapses, ArrayList<Neuron> neurons, int type, String macroId, long innovationNumber){
		FastEvolvedMacroNeuron n = new FastEvolvedMacroNeuron(innovationNumber, type, macroId);
		
		NeuralNetLayer[] layers = this.constructNetFromStructure(synapses, neurons, type, macroId, innovationNumber);
		
		n.setInteriorLayers(layers);
		//add reference to the incoming connections and neurons
		n.autoFillInputSynapsesAndNeurons();
		return n;
	}*/
	
	public NeuralNetLayer[] constructNetFromStructure(ArrayList<Synapse> synapses, ArrayList<Neuron> neurons, int type, String macroId, long innovationNumber){
		ExtendedMacroANNCodec decoder = new ExtendedMacroANNCodec(null);
		//recursively assign the depth values
		decoder.assignNeuronDepth(decoder.getOutputNeurons(neurons), 0);

		ArrayList<Integer> layerIds = decoder.getLayersIds(neurons);
		//sort into an ascending order
		Collections.sort(layerIds);

		NeuralNetLayer[] layers = decoder.computeLayers(neurons, layerIds);
		
		return layers;
	}

	/**public Neuron decode(ODNEATEvolvedMacroNodeGene node) {
		Neuron n = null;
		if(node instanceof ODNEATEvolvedGraphMacroNodeGene){
			ODNEATEvolvedGraphMacroNodeGene e = (ODNEATEvolvedGraphMacroNodeGene) node;
			n = new FastEvolvedMacroNeuron(e.getInnovationNumber(), Neuron.EVOLVED_MACRO_NEURON, e.getMacroId());
			//take advantage of the macro ANN codec
			MacroANNCODEC decoder = new MacroANNCODEC(null);
			ArrayList<ODNEATNodeGene> nodes = e.getNodes();
			ArrayList<ODNEATLinkGene> links = e.getLinkGenes(true);

			ArrayList<Synapse> synapses = decoder.createSynapses(links);
			ArrayList<Neuron> neurons = decoder.createNeurons(nodes, synapses);
			
			
			((FastEvolvedMacroNeuron) n).registerOutgoingConnections(e.getOutgoingConnections());
		}
		return n;
	}**/

}

