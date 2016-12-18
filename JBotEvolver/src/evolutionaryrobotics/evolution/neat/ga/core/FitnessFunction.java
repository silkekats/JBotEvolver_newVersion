package evolutionaryrobotics.evolution.neat.ga.core;

/**
 * @author MSimmerson
 *
 */
public interface FitnessFunction extends Operator {
	public double evaluate(Chromosome genoType);
    //public int evaluateSecond(Chromosome genoType);
	public int requiredChromosomeSize();
}
