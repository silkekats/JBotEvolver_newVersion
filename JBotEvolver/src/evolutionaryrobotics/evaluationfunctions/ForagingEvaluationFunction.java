// This fitness function is centralised meaning that the fitness of all robots is added together. 

package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class ForagingEvaluationFunction extends EvaluationFunction{
	protected Vector2d   nestPosition = new Vector2d(0, 0);
	protected int numberOfFoodForaged = 0;
	protected double factor_prey = 1000;
	protected double factor_carry_prey = 1.0;
	protected double carryPuck=0;

	public ForagingEvaluationFunction(Arguments args) {
		super(args);	
	}

	//@Override
	public double getFitness() {
		return carryPuck + numberOfFoodForaged*factor_prey;
	}
	
	public int getSecondFitness() {
		return numberOfFoodForaged;
	}

	//@Override
	public void update(Simulator simulator) {			
		int numberOfRobotsWithPrey       = 0;
		
		Robot r = simulator.getRobots().get(0);
		DifferentialDriveRobot robot = (DifferentialDriveRobot) r;
		
		if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()) {
			numberOfRobotsWithPrey++;
			}	

		/**
		 * wall sensor readings, values between 0 and 1
		 */
		WallRaySensor wallSensor = (WallRaySensor) robot.getSensorByType(WallRaySensor.class);
		int readings = wallSensor.getNumberOfSensors();
		double wallMaxReading = 0;
		for(int i = 0; i < readings; i++){
			wallMaxReading = Math.max(wallMaxReading, wallSensor.getSensorReading(i));
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
		fitness += (ts * (1-rs) * (1 - wallMaxReading)) + (double) numberOfRobotsWithPrey * factor_carry_prey;
		double carryPuck = (double) numberOfRobotsWithPrey * factor_carry_prey;
		numberOfFoodForaged = ((RoundForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() ;
		
		//System.out.println("number foraged: " + numberOfFoodForaged + " robots with prey: " + numberOfRobotsWithPrey + " total fitness " + fitness );
		
	}
	
}