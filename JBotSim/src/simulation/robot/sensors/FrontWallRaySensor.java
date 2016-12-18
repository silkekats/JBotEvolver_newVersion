package simulation.robot.sensors;


import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class FrontWallRaySensor extends WallRaySensor {

	protected final int TOTAL_NUMBER_SENSORS = 8;
	
	public FrontWallRaySensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public void setupPositions(int numberSensors) {
		double delta = 2 * Math.PI / TOTAL_NUMBER_SENSORS;
		//the three front sensors
		//center
		angles[0] = 0;
		//right
		angles[1] = delta;
		//left
		angles[2] = delta * (TOTAL_NUMBER_SENSORS)-1;
	}

}

