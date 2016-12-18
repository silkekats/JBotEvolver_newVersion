package evolutionaryrobotics.evolution.odneat.controlsystem.components;


import java.io.Serializable;

public class MergedSynapse extends Synapse implements Serializable {

	public MergedSynapse(long innovationNumber, double weight, long from,
			long to) {
		super(innovationNumber, weight, from, to);
	}

}

