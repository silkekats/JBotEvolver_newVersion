package taskexecutor.results;

import java.util.ArrayList;

import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.OnlineEA;

import result.Result;

/**
 * this result keeps the **instances** of the ONLINE EAs used.
 * @author fernando
 *
 */
public class OnlineEvolutionResult extends Result {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int id;
	protected ArrayList<OnlineEA<?>> eas;
	
	public OnlineEvolutionResult(int id){
		eas = new ArrayList<OnlineEA<?>>();
		this.id = id;
	}
	
	public void addEAInstance(OnlineEA<?> instance){
		this.eas.add(instance);
	}
	
	public ArrayList<OnlineEA<?>> getInstances(){
		return this.eas;
	}
	
	public void setId(int newId){
		this.id = newId;
	}
	
	public int getResultId(){
		return this.id;
	}

	
}

