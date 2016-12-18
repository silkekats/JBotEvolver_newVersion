package simulation.robot.sensors;


import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class TypeBResourceSensor extends Sensor {

	public final String KEY = "TYPE_B";
	protected double minValue, maxValue;
	
	public TypeBResourceSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		minValue = (args.getArgumentIsDefined("min")) ? args
				.getArgumentAsDouble("min") : 0.0;
		maxValue = (args.getArgumentIsDefined("max")) ? args
				.getArgumentAsDouble("max") : 1.0;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return robot.getParameterAsDouble(KEY).doubleValue();
	}
	
	public double getMinimumValue() {
		return minValue;
	}

	public double getMaximumValue() {
		return maxValue;
	}
	
	@Override
	public String toString() {
		return "TypeBResourceSensor [" + getSensorReading(0) + "]";
	}

	
}

