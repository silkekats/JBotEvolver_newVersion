package evolutionaryrobotics;



import java.util.ArrayList;
import java.util.HashMap;

import evolutionaryrobotics.populations.ERNEATPopulation;
import evolutionaryrobotics.evolution.odneat.controlsystem.OnlineController;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.OnlineEA;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;

//import org.encog.ml.MLMethod;
//import org.encog.neural.neat.NEATCODEC;
//import org.encog.neural.neat.NEATNetwork;
//import org.encog.neural.neat.training.NEATGenome;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.populations.OnlineEvoShallowPopulation2;

public class ExtendedJBotEvolver extends JBotEvolver {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtendedJBotEvolver(HashMap<String, Arguments> arguments,
			long randomSeed) {
		super(arguments, randomSeed);
	}

	public ExtendedJBotEvolver(String[] args) throws Exception {
		super(args);
	}

	/*public void setMLControllers(ArrayList<Robot> robots, MLMethod network) {
		for (Robot r : robots) {
			setIndividualMLController(r, network);
		}	
	}

	private void setIndividualMLController(Robot r, MLMethod method) {
		if(r.getController() instanceof ANNODNEATController){
			ANNODNEATController controller = (ANNODNEATController) r.getController();
			NEATNetwork net = ANNODNEATController.createCopyNetwork((NEATNetwork) method);
			controller.setNetwork(net);
		}
	}*/

	@Override
	public void setupBestIndividual(Simulator simulator) {

		ArrayList<Robot> robots;

		if(simulator.getRobots().isEmpty()) {
			robots = createRobots(simulator);
			simulator.addRobots(robots);
		} else
			robots = simulator.getRobots();

		Population p = getPopulation();
		setupControllers(p, robots);
	}

	private void setupControllers(Population pop, ArrayList<Robot> robots) {
		if(pop instanceof OnlineEvoShallowPopulation2){
			OnlineEvoShallowPopulation2 onlinePop = (OnlineEvoShallowPopulation2) pop;
			ArrayList<OnlineEA<?>> instances = onlinePop.getInstances();
			for(int i = 0; i < robots.size(); i++){
				OnlineEA<?> instance = instances.get(i);
				Robot robot = robots.get(i);
				if(robot.getController() instanceof OnlineController){
					System.out.println("setting up online controller...");
					OnlineController<?> controller = (OnlineController<?>) robot.getController();
					controller.setOnlineEAInstance(instance);
					//TODO:
					/*Vector2d oldPosition = instance.getRobotInstance().getPosition();
					robot.teleportTo(oldPosition);*/
					instance.setRobot(robot);
					instance.setEvolutionStatus(true);
					ODNEATGenome odneatGenome = (ODNEATGenome) instance.getActiveGenome();
					System.out.println("id: " + odneatGenome.getId() + 
							"; e-level: " + odneatGenome.getEnergyLevel() +
							"; fit: " + odneatGenome.getFitness()  
							+"; nodes: " + odneatGenome.getNumberOfNodeGenes() 
							+ "; links: " + odneatGenome.getNumberOfLinkGenes(false));
					controller.updateStructure(instance.resetParametersForEvaluation(instance.getActiveGenome()));
				}
			}
		}
		else if(pop instanceof ERNEATPopulation){
			ERNEATPopulation neatPop = (ERNEATPopulation) pop;
//			NEATGenome best = neatPop.getBestGenome();
//			NEATNetwork net = (NEATNetwork) new NEATCODEC().decode(best);
//			for(Robot r : robots) {
//				setIndividualMLController(r, net);
//			}
		}
		else {
			Chromosome c = pop.getBestChromosome();
			for(Robot r : robots) {
				if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
					FixedLenghtGenomeEvolvableController fc = (FixedLenghtGenomeEvolvableController)r.getController();
					if(fc.getNNWeights() == null) {
						fc.setNNWeights(c.getAlleles());
					}
				}
			}
		}
	}
	
//	public void setMLControllers(ArrayList<Robot> robots, MLMethod network) {
//		for (Robot r : robots) {
//			setIndividualMLController(r, network);
//		}	
//	}

//	private void setIndividualMLController(Robot r, MLMethod method) {
//		if(r.getController() instanceof NEATNetworkController) {
//			NEATNetworkController controller = (NEATNetworkController) r.getController();
//			NEATNetwork network = (NEATNetwork) method;
//			controller.setNetwork(network);
//		}
//	}

}


