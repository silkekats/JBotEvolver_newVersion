package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators;


import java.util.Random;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEATInnovationManager;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical.AdjustStructureMacroMutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical.ArbitratorFunctionMutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical.DuplicateStructureMacroMutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical.MergeMacroMutator;
import evolutionaryrobotics.evolution.odneat.geneticcomponents.operators.hierarchical.RemoveMacroMutator;

public class MutationOperatorsSetup {

	public ODNEATCompositeMutator setup(ALGDescriptor descriptor,
			Random random, ODNEATInnovationManager inManager, int id) {
		ODNEATCompositeMutator compMut = new ODNEATCompositeMutator();
		//add link mutator
		setupAddLinkMutator(compMut, descriptor, random, inManager);

		//add node mutator
		setupAddNodeMutator(compMut, descriptor, random, inManager);

		//connection weight mutator
		setupConnectionWeightMutator(compMut, descriptor, random);

		//macro parameter mutator
		setupMacroParameterMutator(compMut, descriptor, random);
		
		//arbitrate function
		setupArbitratorFunctionMutator(compMut, descriptor, random);
		
		//adjust structure
		setupStructureAdjustMutator(compMut, descriptor, inManager, random);
		
		//macro duplication
		setupMacroDuplication(compMut, descriptor, inManager, random);
		
		//macro neuron removal.
		setupMacroRemove(compMut, descriptor, random);
		
		//macro merge
		setupMacroMerge(compMut, descriptor, inManager, random, id);
			
		
		compMut.setMutationProbability(descriptor.getPMutation());
		compMut.setRandom(random);

		return compMut;
	}

	private void setupStructureAdjustMutator(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, ODNEATInnovationManager inManager,
			Random random) {
		AdjustStructureMacroMutator mut = new AdjustStructureMacroMutator(inManager);
		mut.setRandom(random);
		mut.setMutationProbability(descriptor.getProbMacroAdjustStructure());
		
		compMut.registerMutator(mut);
	}

	private void setupArbitratorFunctionMutator(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, Random random) {
		ArbitratorFunctionMutator mut = new ArbitratorFunctionMutator();
		mut.setRandom(random);
		mut.setMutationProbability(descriptor.getProbArbitratorFunctionMutation());
		
		compMut.registerMutator(mut);
	}

	private void setupMacroMerge(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, ODNEATInnovationManager inManager,
			Random random, int id) {
		MergeMacroMutator merge = new MergeMacroMutator(inManager, id);
		merge.setRandom(random);
		merge.setMutationProbability(descriptor.getProbMacroMerge());
		
		compMut.registerMutator(merge);
	}

	private void setupMacroDuplication(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, ODNEATInnovationManager inManager,
			Random random) {
		DuplicateStructureMacroMutator dup = new DuplicateStructureMacroMutator(inManager, descriptor.getProbMacroDuplicationRewiring());
		dup.setMutationProbability(descriptor.getProbMacroDuplication());
		dup.setRandom(random);
		
		compMut.registerMutator(dup);
	}

	private void setupMacroRemove(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, Random random) {
		RemoveMacroMutator remove = new RemoveMacroMutator();
		remove.setRandom(random);
		remove.setMutationProbability(descriptor.getProbMacroRemove());
		
		compMut.registerMutator(remove);
	}

	private void setupMacroParameterMutator(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, Random random) {
		ODNEATMacroParameterMutator parameterMut = new ODNEATMacroParameterMutator(
				descriptor.getProbChangeParameter(), descriptor.getMagnitudeChangeParameter());
		parameterMut.setRandom(random);

		//linkMut, nodeMut, weightMut
		compMut.registerMutator(parameterMut);
	}

	private void setupConnectionWeightMutator(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, Random random) {
		//double pWeightReplaced, double pToggle, double perturbMagnitude, double weightRange
		ODNEATConnectionWeightMutator weightMut = new ODNEATConnectionWeightMutator(descriptor.getPWeightReplaced(),
				descriptor.getPToggleLink(), descriptor.getMaxPerturb(), 
				descriptor.getWeightRange());
		weightMut.setMutationProbability(descriptor.getPWeightMutation());
		weightMut.setRandom(random);
		compMut.registerMutator(weightMut);

	}

	private void setupAddNodeMutator(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, Random random,
			ODNEATInnovationManager inManager) {
		ODNEATAddNodeMutator nodeMut = new ODNEATAddNodeMutator(inManager);
		nodeMut.setMutationProbability(descriptor.getPAddNode());
		nodeMut.setRandom(random);

		compMut.registerMutator(nodeMut);
	}

	private void setupAddLinkMutator(ODNEATCompositeMutator compMut,
			ALGDescriptor descriptor, Random random,
			ODNEATInnovationManager inManager) {
		ODNEATAddLinkMutator linkMut = new ODNEATAddLinkMutator(descriptor.getWeightRange(), inManager);
		linkMut.setMutationProbability(descriptor.getPAddLink());
		linkMut.setRandom(random);
		compMut.registerMutator(linkMut);
	}

}

