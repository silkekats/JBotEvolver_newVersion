/*
 * Created on Oct 12, 2004
 *
 */
package evolutionaryrobotics.evolution.neat.data.csv;

import evolutionaryrobotics.evolution.neat.data.core.ExpectedOutputSet;
import evolutionaryrobotics.evolution.neat.data.core.NetworkDataSet;
import evolutionaryrobotics.evolution.neat.data.core.NetworkInputSet;

/**
 * @author MSimmerson
 *
 */
public class CSVDataSet implements NetworkDataSet {
	private NetworkInputSet inputSet;
	private ExpectedOutputSet expectedOutputSet;
	
	public CSVDataSet() {
		// deliberate empty constructor
	}
	
	public CSVDataSet(NetworkInputSet inputSet, ExpectedOutputSet expectedOutputSet) {
		this.inputSet = inputSet;
		this.expectedOutputSet = expectedOutputSet;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkDataSet#inputSet()
	 */
	public NetworkInputSet inputSet() {
		return (this.inputSet);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkDataSet#expectedOutputSet()
	 */
	public ExpectedOutputSet expectedOutputSet() {
		return (this.expectedOutputSet);
	}
}
