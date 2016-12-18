package evolutionaryrobotics.evolution.neat;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATGeneticAlgorithm;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATNPopulation;

import simulation.util.Arguments;
import simulation.util.Factory;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.GenerationalTask;
import tasks.Task;
import simulation.robot.Robot;

public class NEATGeneticAlgorithmWrapper extends NEATGeneticAlgorithm {

	protected NEATEvolution evo;
	public int populationNumber;

	public NEATGeneticAlgorithmWrapper(NEATGADescriptor descriptor, NEATEvolution evo, int p) {
		super(descriptor);
		this.evo = evo;
		this.populationNumber = p;
	}

	@Override
	protected void evaluatePopulation(Chromosome[] genotypes) {
		int i;

		evolutionaryrobotics.neuralnetworks.Chromosome[] convertedGenotypes = new evolutionaryrobotics.neuralnetworks.Chromosome[genotypes.length];

		for (i = 0; i < genotypes.length && evo.continueExecuting(); i++) {

			int samples = evo.getPopulation(populationNumber).getNumberOfSamplesPerChromosome();
			Chromosome neatChromosome = genotypes[i];

			evolutionaryrobotics.neuralnetworks.Chromosome jBotChromosome = ((NEATNPopulation) evo.getPopulation(populationNumber))
					.convertChromosome(neatChromosome, i);

			convertedGenotypes[i] = jBotChromosome;

			String taskClass = GenerationalTask.class.getName();
			if (evo.getJBotEvolver().getArgumentsCopy().get("--evolution").getArgumentIsDefined("task")) {
				Arguments evolutionArguments = new Arguments(
						evo.getJBotEvolver().getArgumentsCopy().get("--evolution").getArgumentAsString("task"));
				if (evolutionArguments.getArgumentIsDefined("classname")) {
					taskClass = evolutionArguments.getArgumentAsString("classname");
				}
			}

			Task task = (Task) Factory.getInstance(taskClass,
					new JBotEvolver(evo.getJBotEvolver().getArgumentsCopy(), evo.getJBotEvolver().getRandomSeed()),
					samples, jBotChromosome, evo.getPopulation(populationNumber).getGenerationRandomSeed());

			 GenerationalTask neatTask = new GenerationalTask(
			 new JBotEvolver(evo.getJBotEvolver().getArgumentsCopy(),
			 evo.getJBotEvolver().getRandomSeed()),
			 samples, jBotChromosome,
			 evo.getPopulation(populationNumber).getGenerationRandomSeed());
			 //System.out.println(evo.getJBotEvolver().getArgumentsCopy());

			evo.getTaskExecutor().addTask(task);
			//System.out.print(".");
		//}

		//System.out.println();

		//for (i = 0; i < genotypes.length && evo.continueExecuting(); i++) {
			SimpleFitnessResult r = (SimpleFitnessResult) evo.getTaskExecutor().getResult();
			evo.getPopulation(populationNumber).setEvaluationResult(convertedGenotypes[r.getChromosomeId()], r.getFitness(),r.getSecondFitness());
			
			//System.out.println("GET FTINESS");
			//System.out.println(r.getFitness());
			//System.out.println(r.getSecondFitness());
			
			genotypes[r.getChromosomeId()].updateFitness(r.getFitness());
			genotypes[r.getChromosomeId()].updateSecondFitness(r.getSecondFitness());
			
        	//System.out.println("FTINESS SET?");
        	//System.out.println(genotypes[r.getChromosomeId()].fitness());
			//System.out.println(genotypes[r.getChromosomeId()].secondFitness());
			
			//System.out.print("!");
		}
	}

}