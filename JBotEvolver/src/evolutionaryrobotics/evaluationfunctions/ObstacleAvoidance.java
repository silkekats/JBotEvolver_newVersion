// Made by Jacq new on 29-09-2016
package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.RobotSensor;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class ObstacleAvoidance extends EvaluationFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int numberOfBumps = 0;

	public ObstacleAvoidance(Arguments args) {
		super(args);
	}
	

	//@Override
	public double getFitness() {
		return fitness;
	}
	
	public int getSecondFitness() {
		return numberOfBumps;
	}

	//@Override
	public void update(Simulator simulator) {			
		Robot r = simulator.getRobots().get(0);
		DifferentialDriveRobot robot = (DifferentialDriveRobot) r;

		/**
		 * wall sensor readings, values between 0 and 1
		 */
		WallRaySensor wallSensor = (WallRaySensor) robot.getSensorByType(WallRaySensor.class);
		int readings = wallSensor.getNumberOfSensors();
		double wallMaxReading = 0;
		for(int i = 0; i < readings; i++){
			wallMaxReading = Math.max(wallMaxReading, wallSensor.getSensorReading(i));
		}
		
		if(wallMaxReading>=0.9){
			numberOfBumps++;
		}
		
		
		/**
		 * actuation values.
		 */
		TwoWheelActuator actuator = (TwoWheelActuator) robot.getActuatorByType(TwoWheelActuator.class);
		double maxSpeed = actuator.getMaxSpeed();

		//components
		double ts = (robot.getLeftWheelSpeed() + robot.getRightWheelSpeed()+ 2*maxSpeed)/(4*maxSpeed) ;//normalised left speed [0,1]
		double rs = Math.abs(robot.getLeftWheelSpeed() - robot.getRightWheelSpeed())/(2*maxSpeed);
		
		//System.out.println("rs: " + rs + " left speed " + robot.getLeftWheelSpeed() + " right speed " + robot.getRightWheelSpeed() + " abs " + Math.abs(robot.getLeftWheelSpeed() - robot.getRightWheelSpeed()) + " max " + 2*maxSpeed );
		
		//[0:1] fitness
		fitness += ts * (1-rs) * (1 - wallMaxReading) ;
		
	
	}


}

