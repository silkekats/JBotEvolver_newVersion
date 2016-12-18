package evolutionaryrobotics.evolution.odneat.controlsystem.behaviours;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import evolutionaryrobotics.populations.ERNEATPopulation;

//import org.encog.neural.neat.NEATNeuronType;
//import org.encog.neural.neat.training.NEATGenome;
//import org.encog.neural.neat.training.NEATLinkGene;
//import org.encog.neural.neat.training.NEATNeuronGene;

import evolutionaryrobotics.populations.Population;

/**
 * load a previously evolved behaviour (ANN) and store it in a convenient format
 * for easy reuse.
 * @author fernando
 *
 */
public class ANNBehaviourLoader {

	public static void main(String[] args) throws ClassNotFoundException, IOException{
		int runs = 30, bestPopulation = 100;
		String folder = "evolution_macro_neurons/";
		String outputFolder = "repository_evolved_behaviours/";
		String[] behaviours = {"neat_move_forward_behaviour", 
				"neat_turn_left_behaviour", "neat_turn_right_behaviour"};

		for(String behaviour : behaviours){
			//assuming neat genomes here.
//			NEATGenome bestSoFar = null;
//			for(int i = 1; i <= runs; i++){
//				String behaviourFolder = folder + behaviour + "_v" + i + "/";
//				ANNBehaviourLoader currentBehaviour = new ANNBehaviourLoader(behaviourFolder);
//				currentBehaviour.load(bestPopulation);
//				if(bestSoFar == null || currentBehaviour.getFitness() > bestSoFar.getScore()){
//					bestSoFar = currentBehaviour.bestSolution;
//				}
			}
//			System.out.println(behaviour);
//			System.out.println("BEST SOLUTION: " + bestSoFar.getScore());
//			int links = countLinks(bestSoFar);
//			int nodes = countNodes(bestSoFar);
//			System.out.println("COMPLEXITY: " + links + " LINKS; " + nodes + " NODES");	
//			System.out.println("I: " + bestSoFar.getInputCount() + "; O: " + bestSoFar.getOutputCount());
//			System.out.println("=====================================================");
//
//			saveSolution(behaviour, outputFolder, bestSoFar);
		//}
	}

//	protected static void saveSolution(String file, String outputFolder,
//			NEATGenome bestSoFar) throws IOException {
//
//		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + file + ".evo"));
//		ArrayList<NEATLinkGene> links = (ArrayList<NEATLinkGene>) bestSoFar.getLinksChromosome();
//		ArrayList<NEATNeuronGene> nodes = (ArrayList<NEATNeuronGene>) bestSoFar.getNeuronsChromosome();
//		boolean hasBias = true;
//		long biasId = getBiasId(nodes);
//		if(biasId == -1)
//			hasBias = false;
//
//		long[] inputsId = getInputsId(nodes);
//		long[] outputsId = getOutputsId(nodes);
//		
//		writer.write("instance;" + "ANN");
//		writer.newLine();
//		writer.write("bias;" + hasBias + ";" + biasId);
//		writer.newLine();
//		writer.write("inputs;" + inputsId.length + ";" + getTextualRepresentation(inputsId, ";"));
//		writer.newLine();
//		writer.write("outputs;" + outputsId.length + ";" + getTextualRepresentation(outputsId, ";"));
//		writer.newLine();
//
//		//tHe genome represents an ANN, save it as a set of triples <input, output, connection weight, connection id>
//		for(NEATLinkGene link : links){
//			if(link.isEnabled()){
//				long fromNeuron = link.getFromNeuronID(), toNeuron = link.getToNeuronID();
//				double weight = link.getWeight();
//				long connectionId = link.getInnovationId();
//				writer.write(fromNeuron + ";" + toNeuron + ";" + weight + ";" + connectionId);
//				writer.newLine();
//			}
//		}
//		writer.close();
//	}

	private static String getTextualRepresentation(long[] values,
			String separator) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < values.length; i++){
			builder.append(values[i]);
			if(i < values.length - 1)
				builder.append(separator);
		}
		
		return builder.toString();
	}

//	protected static long[] getOutputsId(ArrayList<NEATNeuronGene> nodes) {
//		ArrayList<Long> ids = new ArrayList<Long>();
//		for(NEATNeuronGene node : nodes){
//			if(node.getNeuronType() == NEATNeuronType.Output && !ids.contains(node.getId()))
//				ids.add(node.getId());
//		}
//		Collections.sort(ids);
//		long[] result = new long[ids.size()];
//		for(int i = 0; i < ids.size(); i++){
//			result[i] = ids.get(i);
//		}
//
//		return result;
//	}
//
//	protected static long[] getInputsId(ArrayList<NEATNeuronGene> nodes) {
//		ArrayList<Long> ids = new ArrayList<Long>();
//		for(NEATNeuronGene node : nodes){
//			if(node.getNeuronType() == NEATNeuronType.Input && !ids.contains(node.getId()))
//				ids.add(node.getId());
//		}
//		Collections.sort(ids);
//		long[] result = new long[ids.size()];
//		for(int i = 0; i < ids.size(); i++){
//			result[i] = ids.get(i);
//		}
//
//		return result;
//	}
//
//	private static long getBiasId(ArrayList<NEATNeuronGene> nodes) {
//		for(NEATNeuronGene node : nodes){
//			if(node.getNeuronType() == NEATNeuronType.Bias)
//				return node.getId();
//		}
//
//		return -1;
//	}
//
//	protected static int countNodes(NEATGenome bestSoFar) {
//		ArrayList<Long> nodesFound = new ArrayList<Long>();
//		ArrayList<NEATLinkGene> links = (ArrayList<NEATLinkGene>) bestSoFar.getLinksChromosome();
//		for(NEATLinkGene link : links){
//			if(link.isEnabled()){
//				long fromNeuron = link.getFromNeuronID();
//				long toNeuron = link.getToNeuronID();
//				if(!nodesFound.contains(fromNeuron))
//					nodesFound.add(fromNeuron);
//				if(!nodesFound.contains(toNeuron))
//					nodesFound.add(toNeuron);
//			}
//		}
//		return nodesFound.size();
//	}
//
//	private static int countLinks(NEATGenome bestSoFar) {
//		int count = 0;
//		ArrayList<NEATLinkGene> links = (ArrayList<NEATLinkGene>) bestSoFar.getLinksChromosome();
//		for(NEATLinkGene link : links){
//			if(link.isEnabled())
//				count++;
//		}
//		return count;
//	}
//
//	/**
//	 * class definition
//	 */
//	protected String folder;
//	protected Population p;
//	protected NEATGenome bestSolution;
//
//	protected ANNBehaviourLoader(String folder){
//		this.folder = folder;
//	}
//
//	protected void load(int populationNumber) throws ClassNotFoundException, IOException {
//		String populationFile = folder + "populations/" + "population" + populationNumber;
//		p = getPopulation(populationFile);
//		loadBestGenome();
//	}
//
//	protected void loadBestGenome(){
//		if(p instanceof ERNEATPopulation){
//			ERNEATPopulation neatPop = (ERNEATPopulation) p;
//			bestSolution = neatPop.getBestGenome();
//			//NEATNetwork net = (NEATNetwork) new NEATCODEC().decode(best);
//		}
//	}
//
//	public double getFitness(){
//		return bestSolution.getScore();
//	}
//
//	protected Population getPopulation(String populationFile) throws IOException, ClassNotFoundException {
//		FileInputStream fis = new FileInputStream(populationFile);
//		GZIPInputStream gzipIn = new GZIPInputStream(fis);
//		ObjectInputStream in = new ObjectInputStream(gzipIn);
//		Population population = (Population) in.readObject();
//		in.close();
//
//		return population;
//	}
}

