package taskexecutor.results;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import evolutionaryrobotics.evolution.odneat.controlsystem.MacroController;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;

public class StatisticsManager implements Serializable {

	//double = time
	
	protected transient HashMap<Integer, EnvironmentObjectData> data = new HashMap<Integer, EnvironmentObjectData>();
	protected transient int dataEntriesAdded = 0;
	
	
	protected transient HashMap<Double, StatsRecord> statsMap = new HashMap<Double, StatsRecord>();
	protected final String OUTPUT_FILE = "/data.csv";
	
	protected final int MAX_ENTRIES = 10000;
	//every 1.0 seconds of simulation, i.e., every 10 steps
	protected final double SAVE_STATS_WINDOW = 1;

	protected boolean flushedBefore = false;

	protected int numberOfRobots;
	protected final String SEPARATOR = ";";

	public void recordStats(Simulator simulator, ArrayList<Robot> robots, String outputDirectory) {
		recordStateOfEnvironment(simulator, robots, outputDirectory);
		
		double time = simulator.getTime() * simulator.getTimeDelta();
		this.numberOfRobots = robots.size();
		if(time % SAVE_STATS_WINDOW == 0){
			StatsRecord record = new StatsRecord(time);
 
			for(Robot r : robots){
				ControllerData cont = new ControllerData();
				MacroController controller = (MacroController) r.getController();
				cont.update(r, controller, simulator);
				record.registerControllerData(r.getId(), cont);
			}

			this.statsMap.put(time, record);

			if(statsMap.size() > MAX_ENTRIES){
				flushData(outputDirectory);
			}
		}
	}


	private void recordStateOfEnvironment(Simulator simulator,
			ArrayList<Robot> robots, String outputDirectory) {
		Environment env = simulator.getEnvironment();
		for(PhysicalObject obj : env.getAllObjects()){
			if(! (this.data.containsKey(obj.getId()))){
				this.data.put(obj.getId(), new EnvironmentObjectData(obj.getId(), obj.getType(), obj.getName()));
			}
			EnvironmentObjectData objectData = this.data.get(obj.getId());
			if(objectData.addDataEntry(obj, simulator.getTime()))
				this.dataEntriesAdded++;
		}
		
		if(this.dataEntriesAdded > this.MAX_ENTRIES){
			flushDataEntries(outputDirectory);
			this.dataEntriesAdded = 0;
		}
	}


	private void flushDataEntries(String outputDirectory) {
		for(int key : this.data.keySet()){
			EnvironmentObjectData data = this.data.get(key);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputDirectory.concat(data.getName() + "_" + data.getId()), true));
				for(DataEntry entry : data.getDataEntries()){
					writer.write(entry.toString());
					writer.newLine();
				}
				data.clearEntries();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	protected void flushData(String outputDirectory) {
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputDirectory.concat(OUTPUT_FILE), true));
			if(!flushedBefore){
				writer.write(getLogHeader());
				writer.newLine();
				flushedBefore = true;
			}
			writeToFile(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.statsMap.clear();
	}


	protected void writeToFile(BufferedWriter writer) {
		Set<Double> timeSet = this.statsMap.keySet();
		ArrayList<Double> timeList = new ArrayList<Double>();
		timeList.addAll(timeSet);

		//sort into an ascending order
		Collections.sort(timeList);
		for(double currentTime : timeList){
			StatsRecord record = this.statsMap.get(currentTime);
			try {
				writer.write(currentTime + SEPARATOR + record.toString(this.numberOfRobots, SEPARATOR));
				writer.newLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}


	protected String getLogHeader() {
		StringBuilder builder = new StringBuilder();
		builder.append("TIME");
		builder.append(SEPARATOR);
		for(int i  = 0; i < this.numberOfRobots; i++){
			builder.append(StatsRecord.getHeader(this.numberOfRobots, SEPARATOR));
		}

		return builder.toString();
	}


	public HashMap<Double, StatsRecord> getStatsRecords(){
		return this.statsMap;
	}
}

