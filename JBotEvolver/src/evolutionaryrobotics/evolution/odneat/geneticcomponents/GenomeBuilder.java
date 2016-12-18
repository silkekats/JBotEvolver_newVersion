package evolutionaryrobotics.evolution.odneat.geneticcomponents;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import evolutionaryrobotics.evolution.odneat.controlsystem.behaviours.EvolvedBehaviourDecoder;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.behaviours.BehaviourGeneLoader;
import simulation.util.Arguments;

public class GenomeBuilder {

	// standard macro neurons
	protected static final String MACRO_FIELD = "macro";
	protected static final String MACRO_INPUTS_FIELD = "sensors";
	protected static final String MACRO_OUTPUTS_FIELD = "actuators";
	// evolved macro neurons
	protected static final String EVOLVED_MACRO_FIELD = "evolved_macro";
	protected static final String EVOLVED_MACRO_INPUTS_FIELD = "evsensors";
	protected static final String EVOLVED_MACRO_OUTPUTS_FIELD = "evactuators";

	protected static final String TYPE_FIELD = "type";
	protected static final String TYPE_LEARNING = "learning";

	protected static final String FIELD_DELIMITER = "-";

	protected double connectionWeightRange;
	protected Random random;

	private long nextInnovation;

	public GenomeBuilder(Random random, double connectionWeightRange){
		this.connectionWeightRange = connectionWeightRange;
		this.random = random;
	}

	public ODNEATGenome createMacroGenome(String newGenomeId, int inputs,
			int outputs, Arguments args) {
		// first we create a basic genome with each input connected to every
		// other output.
		StandardGenome genome = (StandardGenome) this.createStandardGenome(newGenomeId, inputs, outputs, 0);
		MacroGenome macrogenome = new MacroGenome(genome);
		nextInnovation = macrogenome.getHighestInnovationNumber() + 1;
		// now we alter the genome to place each macro gene.
		addProgrammedMacros(macrogenome, args);
		addEvolvedMacros(macrogenome, args);

		return macrogenome;
	}

	private void addEvolvedMacros(MacroGenome macrogenome, Arguments args) {
		// now we add the evolved macros.
		int evolvedMacros = args.getArgumentAsIntOrSetDefault("evolvedmacros", 0);
		double disableConnectionProb = args.getArgumentAsDoubleOrSetDefault("disableprob", 0.0);
		//System.out.println("LESS OPTIMISED PROB: " + disableConnectionProb);
		//ODNEATEvolvedMacroNodeGene [] evolved = new ODNEATEvolvedMacroNodeGene[evolvedMacros];
		for (int i = 0; i < evolvedMacros; i++) {
			int macroId = i + 1;
			String macroName = args.getArgumentAsString(EVOLVED_MACRO_FIELD + macroId);
			String type = args.getArgumentAsStringOrSetDefault(TYPE_FIELD + macroId, "");
			System.out.println("EVOLVED MACRO NAME: " + macroName);
			System.out.println(args.getArgumentAsString(EVOLVED_MACRO_INPUTS_FIELD + macroId));
			ODNEATMacroNodeGene evolvedMacroGene = loadEvolvedMacroGene(macroName, disableConnectionProb, type);
			//System.out.println("evolved test: " + evolvedMacroGene.getMacroId());
			//EvolvedANNBehaviourGene gene = (EvolvedANNBehaviourGene) evolvedMacroGene.getBehaviourGene();
			//System.out.println("NODES: " + gene.getNodesList().size());
			//System.out.println("LINKS: " + (gene.getLinkGenes(true).size() + evolvedMacroGene.getTemplateInputs().size()));
			//nextInnovation = evolvedMacroGene.compatibiliseInnovationNumbers(nextInnovation, macroInputs, macroOutputs);
			//evolved[i] = evolvedMacroGene;
			//inputsMacros[i] = evolvedMacroInputs;
			//outputsMacros[i] = evolvedMacroOutputs;
			String[] macroInputs = args.getArgumentAsString(EVOLVED_MACRO_INPUTS_FIELD + macroId).split(FIELD_DELIMITER);
			String[] macroOutputs = args.getArgumentAsString(EVOLVED_MACRO_OUTPUTS_FIELD + macroId).split(FIELD_DELIMITER);
			System.out.println("inputs: " + macroInputs.length);
			System.out.println("outputs: " + macroOutputs.length);
			nextInnovation++;
			nextInnovation = this.insertEvolvedMacros(macrogenome, macroInputs, macroOutputs, evolvedMacroGene, nextInnovation);
		}
		//nextInnovation = new MacroGenomeRefactor().insertEvolvedMacroGene(evolved, macrogenome, inputsMacros, outputsMacros, nextInnovation);

	}

	private long insertEvolvedMacros(MacroGenome macrogenome,
			String[] inputs, String[] outputs,
			ODNEATMacroNodeGene macrogene, long nextInnovation) {
		deleteStandardConnections(inputs, outputs, macrogenome);
		//ArrayList<ODNEATLinkGene> inputConnections = macrogene.re
		long[] inputsIds = new long[inputs.length];
		for(int i = 0; i < inputs.length; i++){
			inputsIds[i] = Long.valueOf(inputs[i]);
		}
		
		nextInnovation = macrogene.getBehaviourGene().adjustInnovationNumbers(nextInnovation, inputsIds);
		//we want to keep the input links in the outer part of the macro node (as template inputs)
		//and input **nodes** in the genome (they are already there so just remove the duplicates from the macro node).
		ArrayList<ODNEATLinkGene> macroInputConnections = removeInputConnections(macrogene);
		removeInputNodes(macrogene);
		//ArrayList<Long> listInputs = macrogene.getBehaviourGene().getOrderedListInputNodeGenes();
		ArrayList<Long> listOutputs = macrogene.getBehaviourGene().getOrderedListOutputNodeGenes();

		for(ODNEATLinkGene link : macroInputConnections){
			ODNEATTemplateLinkGene template = new ODNEATTemplateLinkGene(link.getInnovationNumber(), true, link.getFromId(), link.getToId(), link.getWeight(),
					macrogene.getMacroId(), true);
			macrogene.addTemplateInput(template);
			if(macrogene.getBehaviourGene() instanceof EvolvedANNBehaviourGene){
				EvolvedANNBehaviourGene evolved = (EvolvedANNBehaviourGene) macrogene.getBehaviourGene();
				evolved.removeConnection(template.getFromId(), template.getToId());
			}
		}

		// now, the outcoming connections (from the macro neuron to the actuator).
		for (int index = 0; index < outputs.length; index++) {
			String macroOutput = outputs[index];
			long from = listOutputs.get(index);
			long to = Long.valueOf(macroOutput);
			ODNEATTemplateLinkGene link = new ODNEATTemplateLinkGene(nextInnovation++, true, 
					from, to, 1.0, 
					macrogene.getMacroId(), false);

			macrogenome.insertSingleLinkGene(link);
			//macrogene.addTemplateOutput(link);
			//macrogene.addTemplateNodeGene(macrogenome.getNodeGeneWithInnovationNumber(from));
		}
		// finally, add the macro node gene itself.
		macrogenome.insertSingleNodeGene(macrogene);

		return nextInnovation;
	}

	private void removeInputNodes(ODNEATMacroNodeGene macrogene) {
		MacroBehaviourGene bGene = macrogene.getBehaviourGene();
		ArrayList<ODNEATNodeGene> nodes = null;
		if(bGene instanceof EvolvedANNBehaviourGene){
			EvolvedANNBehaviourGene evolved = (EvolvedANNBehaviourGene) bGene;
			nodes = evolved.getNodesList();
		}
		/*else if(bGene instanceof TeachingANNMacroBehaviourGene){
			TeachingANNMacroBehaviourGene teaching = (TeachingANNMacroBehaviourGene) bGene;
			nodes = teaching.getTeacherNodes();
		}*/
		Iterator<ODNEATNodeGene> it = nodes.iterator();
		while(it.hasNext()){
			ODNEATNodeGene current = it.next();
			if(current.getType() == ODNEATNodeGene.INPUT){
				it.remove();
			}
		}
	}

	private ArrayList<ODNEATLinkGene> removeInputConnections(
			ODNEATMacroNodeGene macrogene) {
		ArrayList<ODNEATLinkGene> removed = new ArrayList<ODNEATLinkGene>();
		MacroBehaviourGene bGene = macrogene.getBehaviourGene();
		ArrayList<ODNEATLinkGene> links = null;
		if(bGene instanceof EvolvedANNBehaviourGene){
			EvolvedANNBehaviourGene evolved = (EvolvedANNBehaviourGene) bGene;
			links = evolved.getLinkGenes(true);
		}
		/*else if(bGene instanceof TeachingANNMacroBehaviourGene){
			TeachingANNMacroBehaviourGene teaching = (TeachingANNMacroBehaviourGene) bGene;
			links = teaching.getTeacherLinks();
		}*/
		Iterator<ODNEATLinkGene> it = links.iterator();
		ArrayList<Long> inputs = bGene.getOrderedListInputNodeGenes();
		while(it.hasNext()){
			ODNEATLinkGene current = it.next();
			if(inputs.contains(current.getFromId())){
				removed.add(current);
				it.remove();
			}
		}

		return removed;
	}

	protected ODNEATMacroNodeGene loadEvolvedMacroGene(String macroName, double disableConnectionProb, String type) {
		MacroBehaviourGene gene = null;
		/*if(type.equalsIgnoreCase(TYPE_LEARNING)){
			EvolvedANNBehaviourGene learner = (EvolvedANNBehaviourGene) EvolvedBehaviourDecoder.loadEvolvedBehaviour(macroName, 
					nextInnovation++, disableConnectionProb);
			EvolvedANNBehaviourGene teacher = new EvolvedANNBehaviourGene(macroName, nextInnovation++);
			int inputs = learner.countNodesWithType(ODNEATNodeGene.INPUT), 
					outputs = learner.countNodesWithType(ODNEATNodeGene.OUTPUT);
			//create a temporary standard genome
			ODNEATGenome genome = this.createStandardGenome(macroName, inputs, outputs, nextInnovation++);
			nextInnovation += genome.getNumberOfNodeGenes() + genome.getNumberOfLinkGenes(true);
			for(ODNEATNodeGene node : genome.getNodeGenes()){
				teacher.insertSingleNodeGene(node);
			}
			for(ODNEATLinkGene link : genome.getLinkGenes(true)){
				teacher.insertSingleLinkGene(link);
			}

			TeachingANNMacroBehaviourGene tGene = new TeachingANNMacroBehaviourGene(teacher, learner, nextInnovation++, macroName);
			//set parameters
			double minValue = tGene.getMinValue(), maxValue = tGene.getMaxValue();
			tGene.setParameter(tGene.getKeyTeacherRate(), this.random.nextDouble() * maxValue - minValue);
			tGene.setParameter(tGene.getKeyLearnerRate(), this.random.nextDouble() * maxValue - minValue);
			tGene.setParameter(tGene.getKeyTeacherMomemtum(), this.random.nextDouble() * maxValue - minValue);
			tGene.setParameter(tGene.getKeyLearnerMomemtum(), this.random.nextDouble() * maxValue - minValue);

			gene = tGene;
		}
		else {*/
		gene = EvolvedBehaviourDecoder.loadEvolvedBehaviour(macroName, nextInnovation++, disableConnectionProb);
		//}
		return new ODNEATMacroNodeGene(macroName, nextInnovation++, ODNEATMacroNodeGene.MACRO_NODE, gene);

	}


	private void addProgrammedMacros(MacroGenome macrogenome, Arguments args) {
		int numberOfMacros = args.getArgumentAsInt("macros");
		System.out.println("MACROS: " + numberOfMacros);
		for (int i = 0; i < numberOfMacros; i++) {
			// ids start at 1.
			int macroId = i + 1;
			String macroName = args.getArgumentAsString(MACRO_FIELD + macroId);
			System.out.println("MACRO NAME: " + macroName);
			ODNEATMacroNodeGene macroGene = loadProgrammedMacroGene(macroName, nextInnovation++);
			String[] macroInputs = args.getArgumentAsString(MACRO_INPUTS_FIELD + macroId).split(FIELD_DELIMITER);
			String[] macroOutputs = args.getArgumentAsString(MACRO_OUTPUTS_FIELD + macroId).split(FIELD_DELIMITER);
			//System.out.println("inputs: " + macroInputs.length);
			//System.out.println("outputs: " + macroOutputs.length);
			this.insertProgrammedMacroNodeInGenome(macrogenome, macroGene, 
					macroInputs, macroOutputs);
		}
	}

	protected void insertProgrammedMacroNodeInGenome(MacroGenome macrogenome,
			ODNEATMacroNodeGene macroGene, String[] inputs, String[] outputs) {
		// first, remove "standard" connections
		deleteStandardConnections(inputs, outputs, macrogenome);

		// now, add the new connections to the genome.
		// first, the inputs to the macro neuron.
		for (String macroInput : inputs) {
			long from = Long.valueOf(macroInput);
			long to = macroGene.getInnovationNumber();
			// flag "true" means the link is enabled. value "1.0" is the initial weight
			ODNEATTemplateLinkGene link = new ODNEATTemplateLinkGene(nextInnovation++, true, from, to, 1.0, 
					macroGene.getMacroId(), true);
			//macrogenome.insertSingleLinkGene(link);
			macroGene.addTemplateInput(link);
			//macroGene.addTemplateNodeGene(macrogenome.getNodeGeneWithInnovationNumber(from));
		}

		// now, the outcoming connections (from the macro neuron to the actuator).
		for (String macroOutput : outputs) {
			long from = macroGene.getInnovationNumber();
			long to = Long.valueOf(macroOutput);
			// String macroId, long innovationNumber, boolean enabled, long
			// fromId, long toId
			ODNEATPriorityLinkGene priority = new ODNEATPriorityLinkGene(nextInnovation++, 
					true, from, to, macroGene.getMacroId());
			ODNEATActionLinkGene action = new ODNEATActionLinkGene(nextInnovation++, 
					true, from, to, macroGene.getMacroId());
			//"register" the links within the macro-neuron
			//macroGene.addTemplateOutput(priority);
			//macroGene.addTemplateOutput(action);
			//macroGene.addTemplateNodeGene(macrogenome.getNodeGeneWithInnovationNumber(to));

			macrogenome.insertSingleLinkGene(priority);
			macrogenome.insertSingleLinkGene(action);
		}
		// finally, add the macro node gene itself.
		macrogenome.insertSingleNodeGene(macroGene);
	}

	protected void deleteStandardConnections(String[] inputs, String[] outputs,
			MacroGenome macrogenome) {
		for (String macroInput : inputs) {
			long input = Long.valueOf(macroInput);
			for (String macroOutput : outputs) {
				long output = Long.valueOf(macroOutput);
				macrogenome.deleteConnectionBetween(input, output);
			}
		}
	}

	protected ODNEATMacroNodeGene loadProgrammedMacroGene(String macroName,
			long macroInnovation) {
		MacroBehaviourGene gene = BehaviourGeneLoader.load(macroName, macroInnovation);
		return new ODNEATMacroNodeGene(macroName, macroInnovation, ODNEATMacroNodeGene.MACRO_NODE, gene);
	}

	public ODNEATGenome createStandardGenome(String newGenomeId, int inputs,
			int outputs, long startInnovationId) {
		final boolean linkEnabled = true;
		ArrayList<ODNEATNodeGene> neuronsList = new ArrayList<ODNEATNodeGene>();
		ArrayList<ODNEATLinkGene> linksList = new ArrayList<ODNEATLinkGene>();

		long innovationID = startInnovationId;
		// first inputs
		for (int i = 0; i < inputs; i++) {
			ODNEATNodeGene gene = new ODNEATNodeGene(innovationID++, ODNEATNodeGene.INPUT);
			neuronsList.add(gene);
		}

		// then outputs
		for (int i = 0; i < outputs; i++) {
			ODNEATNodeGene gene = new ODNEATNodeGene(innovationID++, ODNEATNodeGene.OUTPUT);
			neuronsList.add(gene);
		}

		// and now links
		for (int i = 0; i < inputs; i++) {
			for (int j = 0; j < outputs; j++) {
				long fromID = neuronsList.get(i).getInnovationNumber();
				long toID = neuronsList.get(inputs + j).getInnovationNumber();
				double w = this.generateRandomWeight();
				ODNEATLinkGene gene = new ODNEATLinkGene(innovationID++, linkEnabled, fromID, toID, w);
				linksList.add(gene);
			}
		}

		return new StandardGenome(newGenomeId, linksList, neuronsList);
	}

	protected double generateRandomWeight() {
		return this.random.nextDouble() * this.connectionWeightRange * 2 - this.connectionWeightRange;
	}

	public ODNEATGenome createStandardGenome(String newGenomeId, int inputs,
			int outputs) {
		return this.createStandardGenome(newGenomeId, inputs, outputs, 0);
	}
}

