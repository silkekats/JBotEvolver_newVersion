package evolutionaryrobotics.evolution.odneat.controlsystem.components;



public abstract class MacroNeuron extends Neuron {

	public abstract void reset();

	@Override
	public abstract void step();


	@Override
	public abstract void sortIncomingConnections();

	public abstract boolean containsNeuron(long id);
}

