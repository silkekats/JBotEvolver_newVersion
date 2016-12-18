package evolutionaryrobotics.evolution.odneat.geneticcomponents;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class StandardGenome implements ODNEATGenome {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;
	protected double fitness = 0;
    protected int secondFitness = 0;
	protected double adjustedFitness = 0;
	protected double energyLevel = 0;
	protected int speciesId = -1;
	protected int updatesCount = 0;

	protected ArrayList<ODNEATLinkGene> linkGenes;
	protected ArrayList<ODNEATNodeGene> nodeGenes;

	protected StandardGenome(){
		linkGenes = new ArrayList<ODNEATLinkGene>();
		nodeGenes = new ArrayList<ODNEATNodeGene>();
	}

	public StandardGenome(String id, Collection<ODNEATLinkGene> linkGenes, Collection<ODNEATNodeGene> nodeGenes){
		this.linkGenes = new ArrayList<ODNEATLinkGene>();
		this.nodeGenes = new ArrayList<ODNEATNodeGene>();

		this.id = id;
		this.linkGenes.addAll(linkGenes);
		this.nodeGenes.addAll(nodeGenes);
		/*for(ODNEATNodeGene node : nodeGenes)
			System.out.println("TYPE " + node.getType());*/
	}

	public StandardGenome(String id, ODNEATLinkGene[] linkGenes, ODNEATNodeGene[] nodeGenes){
		this.linkGenes = new ArrayList<ODNEATLinkGene>();
		this.nodeGenes = new ArrayList<ODNEATNodeGene>();

		this.id = id;

		for(ODNEATLinkGene link : linkGenes)
			this.linkGenes.add(link);

		for(ODNEATNodeGene node : nodeGenes)
			this.nodeGenes.add(node);
	}

	@Override
	public Collection<ODNEATLinkGene> getLinkGenes(boolean onlyEnabled) {
		if(onlyEnabled){
			ArrayList<ODNEATLinkGene> genes = new ArrayList<ODNEATLinkGene>();
			for(ODNEATLinkGene gene : this.linkGenes)
				if(gene.isEnabled())
					genes.add(gene);

			return genes;
		}
		return this.linkGenes;
	}

	@Override
	public Collection<ODNEATNodeGene> getNodeGenes() {
		return this.nodeGenes;
	}

	@Override
	public void insertLinkGenes(Collection<ODNEATLinkGene> newLinks) {
		for(ODNEATLinkGene link : newLinks){
			if(!this.linkGenes.contains(link)){
				this.linkGenes.add(link);
			}
		}
	}

	@Override
	public void insertNodeGenes(Collection<ODNEATNodeGene> newNodes) {
		for(ODNEATNodeGene node : newNodes){
			if(!this.nodeGenes.contains(node)){
				this.nodeGenes.add(node);
			}
		}
	}

	@Override
	public int getNumberOfNodeGenes(){
		return this.nodeGenes.size();
	}

	@Override
	public int getNumberOfLinkGenes(boolean onlyEnabled){
		return this.getLinkGenes(onlyEnabled).size();
	}

	public double getFitness(){
		return fitness;
	}
    
    public int getSecondFitness(){
        return secondFitness;
    }

	public double getAdjustedFitness(){
		return adjustedFitness;
	}

	public double getEnergyLevel(){
		return energyLevel;
	}

	public void setAdjustedFitness(double newAdjustedFitness){
		this.adjustedFitness = newAdjustedFitness;
	}

	public void setFitness(double newFitness){
		this.fitness = newFitness;
	}
    
    public void setFitness(int newSecondFitness){
        this.secondFitness = newSecondFitness;
    }

	public void setEnergyLevel(double newLevel){
		this.energyLevel = newLevel;
	}

	public String getId(){
		return id;
	}

	public void setId(String newId){
		this.id = newId;
	}

	public StandardGenome copy(){
		StandardGenome copy = new StandardGenome();

		for(ODNEATLinkGene linkGene : this.linkGenes)
			copy.linkGenes.add(linkGene.copy());

		for(ODNEATNodeGene nodeGene : this.nodeGenes)
			copy.nodeGenes.add(nodeGene.copy());

		//copy of the fields.
		copy.id = new String(id);
		copy.fitness = fitness;
        copy.secondFitness = secondFitness;
		copy.adjustedFitness = adjustedFitness;
		copy.energyLevel = energyLevel;
		copy.speciesId = speciesId;
		copy.updatesCount = updatesCount;

		return copy;
	}

	@Override
	public boolean equals(Object o){
		if((! (o instanceof StandardGenome)) || o == null)
			return false;

		StandardGenome other = (StandardGenome) o;
		if(other.getId().equalsIgnoreCase(this.getId()))
			return true;

		if(this.linkGenes.size() != other.linkGenes.size() || this.nodeGenes.size() != other.nodeGenes.size())
			return false;

		for(ODNEATLinkGene link : this.linkGenes){
			if(!other.linkGenes.contains(link))
				return false;
		}

		for(ODNEATNodeGene node : this.nodeGenes){
			if(!other.nodeGenes.contains(node))
				return false;
		}

		return true;
	}

	public ODNEATNodeGene getNodeGeneWithInnovationNumber(long in) {
		for(ODNEATNodeGene g : this.nodeGenes){
			if(g.getInnovationNumber() == in)
				return g;
		}
		return null;
	}



	public ODNEATLinkGene getLinkGeneWithInnovationNumber(long in) {
		for(ODNEATLinkGene l : this.linkGenes){
			if(l.isEnabled() && l.getInnovationNumber() == in){
				return l;
			}
		}
		return null;
	}

	@Override
	public void insertSingleLinkGene(ODNEATLinkGene newLink) {
		if(!linkGenes.contains(newLink))
			this.linkGenes.add(newLink);
	}

	@Override
	public void insertSingleNodeGene(ODNEATNodeGene newNode) {
		if(!this.nodeGenes.contains(newNode))
			this.nodeGenes.add(newNode);
	}

	public void setSpeciesId(int speciesId){
		this.speciesId = speciesId;
	}

	public int getSpeciesId(){
		return this.speciesId;
	}

	@Override
	public void updateEnergyLevel(double newEnergyLevel) {
		this.energyLevel = newEnergyLevel;
		this.updatesCount++;
		this.fitness = this.fitness + (energyLevel - fitness)/(updatesCount);
		/*System.out.println("updating: " + fitness + "; " + energyLevel + "; " + this.updatesCount);
		if(updatesCount == 100)
			System.exit(0);*/
	}

	public int getNumberOfNodeGenesWithType(int type){
		int count = 0;

		for(ODNEATNodeGene nodeGene : this.nodeGenes){
			if(nodeGene.getType() == type)
				count++;
		}
		return count;
	}

	@Override
	public void setUpdatesCount(int numberOfUpdates) {
		this.updatesCount = numberOfUpdates;
	}

	public void sortGenes(){
		Collections.sort(this.linkGenes);
	}

	@Override
	public ArrayList<Long> getNodeGenesByType(int type) {
		ArrayList<Long> nodesId = new ArrayList<Long>();
		for(ODNEATNodeGene node : this.nodeGenes){
			if(node.getType() == type && !nodesId.contains(node.getInnovationNumber())){
				nodesId.add(node.getInnovationNumber());
			}
		}

		return nodesId;
	}

	@Override
	public long getInnovationRange() {
		long max = this.linkGenes.get(0).getInnovationNumber();
		for(int i = 1; i < linkGenes.size(); i++){
			max = Math.max(max, linkGenes.get(i).getInnovationNumber());
		}
		return max;
	}

	@Override
	public void setSecondFitness(int fitness) {
		// TODO Auto-generated method stub
		
	}
}

