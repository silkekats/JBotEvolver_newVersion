package evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm;


import java.io.Serializable;

import java.util.Random;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.GenomeBuilder;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATLinkGene;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATNodeGene;
import simulation.util.Arguments;

/**
 * 
 * @author Fernando
 * 
 */
public class ODNEATInnovationManager implements Serializable {

	private static final long serialVersionUID = 1L;
	protected long START_TIME = System.nanoTime();


	private transient GenomeBuilder builder;

	public ODNEATInnovationManager(Random random, double connectionWeightRange) {
		this.builder = new GenomeBuilder(random, connectionWeightRange);
	}

	public long nextInnovationNumber() {
		long result = System.nanoTime() - START_TIME;
		return result;
	}

	public ODNEATNodeGene submitNodeInnovation() {
		long innovationNumber = this.nextInnovationNumber();

		ODNEATNodeGene gene = new ODNEATNodeGene(innovationNumber,	ODNEATNodeGene.HIDDEN);
		return gene;
	}
	
	
	public ODNEATLinkGene submitLinkInnovation(long from, long to) {
		long innovationNumber = this.nextInnovationNumber();
		// the 0 weight is a place holder
		ODNEATLinkGene gene = new ODNEATLinkGene(innovationNumber, true, from,
				to, 0);
		return gene;
	}

	public ODNEATGenome initialiseInnovation(String newGenomeId, int inputs,
			int outputs, Arguments args) {
		String classname = args.getArgumentAsStringOrSetDefault("classname",
				"macrogenome").toLowerCase();
		switch (classname) {
		case "macrogenome":
			return builder.createMacroGenome(newGenomeId, inputs, outputs, args);
		case "standardgenome":
			return builder.createStandardGenome(newGenomeId, inputs, outputs);
		default:
			try {
				throw new Exception("unknown encoding.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		return null;
	}

}

