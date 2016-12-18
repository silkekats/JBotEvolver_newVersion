package evolutionaryrobotics.evolution.odneat.controlsystem.components;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public abstract class ProgrammedMacroNeuron extends MacroNeuron implements Serializable{
	
	protected HashMap<Long, String> outgoingConnections;

	protected abstract double getActivationValue(long synapseId);
	
	protected abstract double getPriorityValue(long synapseId);
	
	public void registerOutgoingConnections(HashMap<Long, String> map){
		for(Long key : map.keySet()){
			this.outgoingConnections.put(key, new String(map.get(key)));
		}
	}
	
	public void sortIncomingConnections(){
		//sort by the innovation number of the incoming neurons.
		Collections.sort(incomingSynapses, new Comparator<Object>() {
			public int compare(Object key1, Object key2) {
				Long id1 = ((Synapse) key1).getFromNeuron();
				Long id2 = ((Synapse) key2).getFromNeuron();
				return Long.compare(id1, id2);
			}
		});
	}


	protected ArrayList<PrioritySynapse> getPriorityLinks(ArrayList<Synapse> list) {
		ArrayList<PrioritySynapse> priorities = new ArrayList<PrioritySynapse>();

		for(Synapse s : list){
			if(s instanceof PrioritySynapse)
				priorities.add((PrioritySynapse) s);
		}
		return priorities;
	}

	public abstract String getMacroId();

	/**
	 * for statistical purposes
	 * @return
	 */
	public abstract double getAverageActivationValue();
}

