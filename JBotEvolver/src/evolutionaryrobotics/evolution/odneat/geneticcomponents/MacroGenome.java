package evolutionaryrobotics.evolution.odneat.geneticcomponents;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class MacroGenome extends StandardGenome {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<Long, ODNEATMacroNodeGene> macroNodeGenes;

	protected MacroGenome(){
		super();
		this.macroNodeGenes = new HashMap<Long, ODNEATMacroNodeGene>();
	}

	public MacroGenome(StandardGenome g){
		this(g.getId(), g.getLinkGenes(false), g.getNodeGenes());
	}

	public MacroGenome(String id, Collection<ODNEATLinkGene> linkGenes,
			Collection<ODNEATNodeGene> nodeGenes) {
		this();
		this.id = new String(id);
		this.linkGenes.addAll(linkGenes);
		for(ODNEATNodeGene node : nodeGenes){
			if(node instanceof ODNEATMacroNodeGene){
				this.macroNodeGenes.put(node.getInnovationNumber(), (ODNEATMacroNodeGene) node);
			}
			else {
				this.nodeGenes.add(node);
			}
		}
		//super(id, linkGenes, nodeGenes);
		//addMacroNeurons(nodeGenes);
	}

	/*private void addMacroNeurons(Collection<ODNEATNodeGene> nodeGenes) {
		for(ODNEATNodeGene nodegene : nodeGenes){
			if(nodegene instanceof ODNEATMacroNodeGene){
				this.macroNodeGenes.put(nodegene.getInnovationNumber(), (ODNEATMacroNodeGene) nodegene);
			}
		}
	}*/

	@Override
	public MacroGenome copy(){
		MacroGenome copy = new MacroGenome();

		for(ODNEATLinkGene linkGene : this.linkGenes)
			copy.linkGenes.add(linkGene.copy());

		for(ODNEATNodeGene nodeGene : this.nodeGenes)
			copy.nodeGenes.add(nodeGene.copy());

		for(long key : this.macroNodeGenes.keySet()){
			copy.macroNodeGenes.put(key, this.macroNodeGenes.get(key).copy());
		}

		//copy of the fields.
		copy.id = new String(this.id);
		copy.fitness = fitness;
		copy.adjustedFitness = adjustedFitness;
		copy.energyLevel = energyLevel;
		copy.speciesId = speciesId;
		copy.updatesCount = updatesCount;

		return copy;
	}

	@Override
	public boolean equals(Object o){
		if((! (o instanceof MacroGenome)) || o == null)
			return false;

		MacroGenome other = (MacroGenome) o;
		if(other.getId().equalsIgnoreCase(this.getId()))
			return true;

		if(this.linkGenes.size() != other.linkGenes.size() || this.nodeGenes.size() != other.nodeGenes.size() 
				|| this.macroNodeGenes.size() != other.macroNodeGenes.size())
			return false;

		for(ODNEATLinkGene link : this.linkGenes){
			if(!other.linkGenes.contains(link))
				return false;
		}

		for(ODNEATNodeGene node : this.nodeGenes){
			if(!other.nodeGenes.contains(node))
				return false;
		}

		for(long key : this.macroNodeGenes.keySet()){
			if(!other.macroNodeGenes.containsKey(key) || (!other.macroNodeGenes.get(key).equals(this.macroNodeGenes.get(key))))
				return false;
		}

		return true;
	}

	public long getHighestInnovationNumber() {
		long max = 0;
		for(ODNEATNodeGene g : this.nodeGenes){
			if(g.innovationNumber > max)
				max = g.innovationNumber;
		}

		for(long l : this.macroNodeGenes.keySet()){
			if(l > max)
				max = l;
		}

		for(ODNEATLinkGene g : this.linkGenes){
			if(g.innovationNumber > max)
				max = g.innovationNumber;
		}

		return max;
	}

	public void deleteConnectionBetween(long input, long output) {
		Iterator<ODNEATLinkGene> it = this.linkGenes.iterator();
		while(it.hasNext()){
			ODNEATLinkGene link = it.next();
			if(link.fromId == input && link.toId == output)
				it.remove();
		}
	}

	public HashMap<Long, ODNEATMacroNodeGene> getMacroNodeGenes() {
		return this.macroNodeGenes;
	}

	public void filterRepeatedTemplateLinks(long fromId, long toId) {
		ArrayList<ODNEATLinkGene> repeatedLinks = new ArrayList<ODNEATLinkGene>();
		for(ODNEATLinkGene link : this.linkGenes){
			if(link.getFromId() == fromId && link.getToId() == toId){
				repeatedLinks.add(link);
			}
		}
		//random removal of overlapping links
		Random random = new Random();
		
		while(repeatedLinks.size() > 1){
			int index = random.nextInt(repeatedLinks.size());
			ODNEATLinkGene linkRemoved = repeatedLinks.get(index);
			repeatedLinks.remove(index);
			index = linkGenes.indexOf(linkRemoved);
			this.linkGenes.remove(index);
		}
	}

	@Override
	public void insertSingleNodeGene(ODNEATNodeGene node) {
		if(node instanceof ODNEATMacroNodeGene){
			this.macroNodeGenes.put(node.getInnovationNumber(), (ODNEATMacroNodeGene) node);
		}
		else {
			this.nodeGenes.add(node);
		}
	}
	
	public ArrayList<ODNEATNodeGene> getStandardNodeGenes(){
		return this.nodeGenes;
	}

	@Override
	public Collection<ODNEATNodeGene> getNodeGenes() {
		try{
			throw new UnsupportedOperationException();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void insertNodeGenes(Collection<ODNEATNodeGene> newNodes) {
		for(ODNEATNodeGene node : newNodes){
			if(node instanceof ODNEATMacroNodeGene){
				this.macroNodeGenes.put(node.getInnovationNumber(), (ODNEATMacroNodeGene) node);
			}
			else {
				this.nodeGenes.add(node);
			}
		}
	}

	@Override
	public int getNumberOfNodeGenes(){
		return this.nodeGenes.size() + this.macroNodeGenes.size();
	}

	public void removeLinks(ArrayList<ODNEATLinkGene> linksToRemove) {
		for(ODNEATLinkGene link : linksToRemove){
			boolean removed = this.linkGenes.remove(link);
			if(!removed){
				try {
					throw new Exception("Link did not exist");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}

