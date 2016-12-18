package simulation.robot.sensors;


import java.util.ArrayList;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class RobotCountSensor extends Sensor {

	private double range;
	private int numberOtherRobots;
	private Environment env;

	public RobotCountSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range",DEFAULT_RANGE);
		this.numberOtherRobots = args.getArgumentAsInt("otherrobots");
		this.env = simulator.getEnvironment();
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		ArrayList<Robot> robots = env.getRobots();

		double value = 0;

		for(Robot r : robots) {
			if(this.robot.getId() != r.getId() && r.getPosition().distanceTo(this.robot.getPosition()) < range) {
				value++;
			}
		}
		
		return value/this.numberOtherRobots;
	}
	
	public int getNumberOtherRobots(){
		return this.numberOtherRobots;
	}

}
