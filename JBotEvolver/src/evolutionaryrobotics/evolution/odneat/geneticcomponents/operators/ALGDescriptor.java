 package evolutionaryrobotics.evolution.odneat.geneticcomponents.operators;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class ALGDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double pXover;
	private double pAddLink;
	private double pAddNode;
	private double pToggleLink;
	private double pMutation;
	private double pMutateBias;
	private double pWeightReplaced;

	private double disjointCoeff;
	private double excessCoeff;
	private double weightCoeff;
	private double threshold;

	private double maxPerturb;
	private double maxBiasPerturb;
	protected double weightRange;

	private int popSize;
	private double weightChange;

	private double magnitudeMacroParameterChange, probMacroParameterChange, probMacroRemove;
	private double probMacroDuplication, probMacroDuplicationRewiring, probMacroMerge;
	private double probArbitratorFunctionMutation;
	private double probMacroAdjustStructure;
	private Double macroParameterCoeff;
	private Double macroDisjointCoeff;
	private Double macroExcessCoeff;
	private Double macroMatchingCoeff;


	/**
	 * @return the probArbitratorFunctionMutation
	 */
	public double getProbArbitratorFunctionMutation() {
		return probArbitratorFunctionMutation;
	}

	/**
	 * @param probArbitratorFunctionMutation the probArbitratorFunctionMutation to set
	 */
	public void setProbArbitratorFunctionMutation(
			double probArbitratorFunctionMutation) {
		this.probArbitratorFunctionMutation = probArbitratorFunctionMutation;
	}

	/**
	 * @return the probMacroMerge
	 */
	public double getProbMacroMerge() {
		return probMacroMerge;
	}

	/**
	 * @param probMacroMerge the probMacroMerge to set
	 */
	public void setProbMacroMerge(double probMacroMerge) {
		this.probMacroMerge = probMacroMerge;
	}

	public ALGDescriptor(String parametersFile){
		Properties p = new Properties();
		try {
			System.out.println("FILE: " + parametersFile);
			p.load(new FileInputStream(parametersFile));
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initialize(p);
	}

	private void initialize(Properties p) {
		initialiseLinkAndNodeValues(p);
		initialiseSpeciationCoefficients(p);
		initialiseMacroParameters(p);

		this.popSize = Integer.valueOf(p.getProperty("POP.SIZE", "30"));	
	}

	private void initialiseMacroParameters(Properties p) {
		this.probMacroParameterChange = Double.valueOf(p.getProperty("PROB.MACRO.PARAM.CHANGE", "0.05"));
		this.magnitudeMacroParameterChange = Double.valueOf(p.getProperty("MAGN.MACRO.PARAM.CHANGE", "0.05"));
		this.probMacroRemove = Double.valueOf(p.getProperty("PROB.MACRO.REMOVE", "0.03"));
		this.probMacroDuplication = Double.valueOf(p.getProperty("PROB.MACRO.DUPLICATION", "0.05"));
		this.probMacroDuplicationRewiring = Double.valueOf(p.getProperty("PROB.MACRO.DUPLICATION.REWIRING.INPUTS", "0.05"));
		this.probMacroMerge = Double.valueOf(p.getProperty("PROB.MACRO.MERGE", "0.05"));
		this.probArbitratorFunctionMutation = Double.valueOf(p.getProperty("PROB.MACRO.ARBITRATOR.FUNCTION", "0.1"));
		this.probMacroAdjustStructure = Double.valueOf(p.getProperty("PROB.MACRO.STRUCTURE.ADJUST", "0.1"));

		/**MACRO.PARAMETER.COEFFICIENT=0.4
		MACRO.DISJOINT.COEFFICIENT=1.0
		MACRO.EXCESS.COEFFICIENT=1.0
		MACRO.MATCHING.COEFFICIENT=0.4*/
		this.macroParameterCoeff = Double.valueOf(p.getProperty("MACRO.PARAMETER.COEFFICIENT", "0.4"));
		this.macroDisjointCoeff = Double.valueOf(p.getProperty("MACRO.DISJOINT.COEFFICIENT", "1.0"));
		this.macroExcessCoeff = Double.valueOf(p.getProperty("MACRO.EXCESS.COEFFICIENT", "1.0"));
		this.macroMatchingCoeff = Double.valueOf(p.getProperty("MACRO.MATCHING.COEFFICIENT", "0.4"));
	}

	/**
	 * @return the macroParameterCoeff
	 */
	public Double getMacroParameterCoeff() {
		return macroParameterCoeff;
	}

	/**
	 * @return the macroDisjointCoeff
	 */
	public Double getMacroDisjointCoeff() {
		return macroDisjointCoeff;
	}

	/**
	 * @return the macroExcessCoeff
	 */
	public Double getMacroExcessCoeff() {
		return macroExcessCoeff;
	}

	/**
	 * @return the macroMatchingCoeff
	 */
	public Double getMacroMatchingCoeff() {
		return macroMatchingCoeff;
	}

	/**
	 * @param macroParameterCoeff the macroParameterCoeff to set
	 */
	public void setMacroParameterCoeff(Double macroParameterCoeff) {
		this.macroParameterCoeff = macroParameterCoeff;
	}

	/**
	 * @param macroDisjointCoeff the macroDisjointCoeff to set
	 */
	public void setMacroDisjointCoeff(Double macroDisjointCoeff) {
		this.macroDisjointCoeff = macroDisjointCoeff;
	}

	/**
	 * @param macroExcessCoeff the macroExcessCoeff to set
	 */
	public void setMacroExcessCoeff(Double macroExcessCoeff) {
		this.macroExcessCoeff = macroExcessCoeff;
	}

	/**
	 * @param macroMatchingCoeff the macroMatchingCoeff to set
	 */
	public void setMacroMatchingCoeff(Double macroMatchingCoeff) {
		this.macroMatchingCoeff = macroMatchingCoeff;
	}

	private void initialiseSpeciationCoefficients(Properties p) {
		disjointCoeff = Double.valueOf(p.getProperty("DISJOINT.COEFFICIENT", "1.0"));
		excessCoeff = Double.valueOf(p.getProperty("EXCESS.COEFFICIENT", "1.0"));
		weightCoeff = Double.valueOf(p.getProperty("WEIGHT.COEFFICIENT", "0.4"));
		threshold = Double.valueOf(p.getProperty("COMPATIBILITY.THRESHOLD", "3.0"));

	}

	private void initialiseLinkAndNodeValues(Properties p) {
		pXover = Double.valueOf(p.getProperty("PROBABILITY.CROSSOVER", "0.75"));
		pAddLink = Double.valueOf(p.getProperty("PROBABILITY.ADDLINK", "0.05"));
		pAddNode = Double.valueOf(p.getProperty("PROBABILITY.ADDNODE", "0.03"));
		pToggleLink = Double.valueOf(p.getProperty("PROBABILITY.TOGGLELINK", "0.25"));
		pMutation = Double.valueOf(p.getProperty("PROBABILITY.MUTATION", "0.8"));
		this.weightChange = Double.valueOf(p.getProperty("PROBABILITY.WEIGHT.CHANGE", "0.8"));
		pMutateBias = Double.valueOf(p.getProperty("PROBABILITY.MUTATEBIAS", "0.3"));
		pWeightReplaced = Double.valueOf(p.getProperty("PROBABILITY.WEIGHT.REPLACED", "0.1"));
		this.weightRange = Double.valueOf(p.getProperty("WEIGHT.RANGE", "5"));
		maxPerturb = Double.valueOf(p.getProperty("MAX.PERTURB", "0.5"));
		maxBiasPerturb = Double.valueOf(p.getProperty("MAX.BIAS.PERTURB", "0.1"));
	}

	/**
	 * @return the probMacroDuplication
	 */
	public double getProbMacroDuplication() {
		return probMacroDuplication;
	}

	/**
	 * @return the probMacroDuplicationRewiring
	 */
	public double getProbMacroDuplicationRewiring() {
		return probMacroDuplicationRewiring;
	}

	/**
	 * @param probMacroDuplication the probMacroDuplication to set
	 */
	public void setProbMacroDuplication(double probMacroDuplication) {
		this.probMacroDuplication = probMacroDuplication;
	}

	/**
	 * @param probMacroDuplicationRewiring the probMacroDuplicationRewiring to set
	 */
	public void setProbMacroDuplicationRewiring(double probMacroDuplicationRewiring) {
		this.probMacroDuplicationRewiring = probMacroDuplicationRewiring;
	}

	/**
	 * @return the probMacroRemove
	 */
	public double getProbMacroRemove() {
		return probMacroRemove;
	}

	/**
	 * @param probMacroRemove the probMacroRemove to set
	 */
	public void setProbMacroRemove(double probMacroRemove) {
		this.probMacroRemove = probMacroRemove;
	}

	/**
	 * @return Returns the pWeightReplaced.
	 */
	public double getPWeightReplaced() {
		return pWeightReplaced;
	}

	public double getProbCrossover(){
		return this.pXover;
	}

	/**
	 * @param weightReplaced The pWeightReplaced to set.
	 */
	public void setPWeightReplaced(double weightReplaced) {
		pWeightReplaced = weightReplaced;
	}


	/**
	 * @return Returns the disjointCoeff.
	 */
	public double getDisjointCoeff() {
		return disjointCoeff;
	}
	/**
	 * @param disjointCoeff The disjointCoeff to set.
	 */
	public void setDisjointCoeff(double disjointCoeff) {
		this.disjointCoeff = disjointCoeff;
	}
	/**
	 * @return Returns the excessCoeff.
	 */
	public double getExcessCoeff() {
		return excessCoeff;
	}
	/**
	 * @param excessCoeff The excessCoeff to set.
	 */
	public void setExcessCoeff(double excessCoeff) {
		this.excessCoeff = excessCoeff;
	}

	/**
	 * @return Returns the threshold.
	 */
	public double getThreshold() {
		return threshold;
	}
	/**
	 * @param threshold The threshold to set.
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	/**
	 * @return Returns the weightCoeff.
	 */
	public double getWeightCoeff() {
		return weightCoeff;
	}
	/**
	 * @param weightCoeff The weightCoeff to set.
	 */
	public void setWeightCoeff(double weightCoeff) {
		this.weightCoeff = weightCoeff;
	}
	/**
	 * @return Returns the pAddLink.
	 */
	public double getPAddLink() {
		return pAddLink;
	}
	/**
	 * @param addLink The pAddLink to set.
	 */
	public void setPAddLink(double addLink) {
		pAddLink = addLink;
	}
	/**
	 * @return Returns the pAddNode.
	 */
	public double getPAddNode() {
		return pAddNode;
	}
	/**
	 * @param addNode The pAddNode to set.
	 */
	public void setPAddNode(double addNode) {
		pAddNode = addNode;
	}
	/**
	 * @param disableLink The pDisableLink to set.
	 */
	public void setPToggleLink(double toggleLink) {
		pToggleLink = toggleLink;
	}
	/**
	 * @return Returns the pMutation.
	 */
	public double getPMutation() {
		return pMutation;
	}
	/**
	 * @param mutation The pMutation to set.
	 */
	public void setPMutation(double mutation) {
		pMutation = mutation;
	}
	/**
	 * @return Returns the pXover.
	 */
	public double getPXover() {
		return pXover;
	}
	/**
	 * @param xover The pXover to set.
	 */
	public void setProbCrossover(double xover) {
		pXover = xover;
	}


	/**
	 * @return Returns the pToggleLink.
	 */
	public double getPToggleLink() {
		return pToggleLink;
	}

	public double getPMutateBias() {
		return pMutateBias;
	}
	public void setPMutateBias(double mutateBias) {
		pMutateBias = mutateBias;
	}

	public double getMaxBiasPerturb() {
		return maxBiasPerturb;
	}
	public void setMaxBiasPerturb(double maxBiasPerturb) {
		this.maxBiasPerturb = maxBiasPerturb;
	}
	public double getMaxPerturb() {
		return maxPerturb;
	}
	public void setMaxPerturb(double maxPerturb) {
		this.maxPerturb = maxPerturb;
	}

	public int getPopulationSize() {
		return popSize;
	}

	public double getWeightRange() {
		return this.weightRange;
	}

	public double getPWeightMutation() {
		return this.weightChange;
	}

	public double getMagnitudeChangeParameter() {
		return this.magnitudeMacroParameterChange;
	}

	public double getProbChangeParameter() {
		return this.probMacroParameterChange;
	}

	public double getProbMacroAdjustStructure() {
		return this.probMacroAdjustStructure;
	}

}

