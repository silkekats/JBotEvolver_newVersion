package taskexecutor.results;


import java.util.HashMap;

public class StatsRecord {

	protected double time;
	protected HashMap<Integer, ControllerData> controllerData;
	
	public StatsRecord(double time){
		this.time = time;
		this.controllerData = new HashMap<Integer, ControllerData>();
	}
	
	public void registerControllerData(Integer robotId, ControllerData data){
		this.controllerData.put(robotId, data);
	}
	
	public HashMap<Integer, ControllerData> getData(){
		return this.controllerData;
	}
	
	public double getTime(){
		return this.time;
	}

	public static String getHeader(int numberOfRobots, String separator) {
		return ControllerData.getHeader(numberOfRobots, separator);
	}
	
	public String toString(int numberRobots, String separator){
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < numberRobots; i++){
			ControllerData data = controllerData.get(i);
			builder.append(data.toString(i, separator));
			if(i < numberRobots - 1)
				builder.append(separator);
		}
		return builder.toString();
	}
}

