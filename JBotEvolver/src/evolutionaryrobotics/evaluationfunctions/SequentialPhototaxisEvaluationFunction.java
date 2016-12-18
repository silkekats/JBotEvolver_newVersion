// Jacq: not checked but can stay here as an extra task in future
package evolutionaryrobotics.evaluationfunctions;



import simulation.environment.RoundForageEnvironment;
import simulation.robot.sensors.LightpoleSensor;
import evolutionaryrobotics.evolution.odneat.controlsystem.OnlineController;
import evolutionaryrobotics.evolution.odneat.evolutionaryalgorithm.ODNEAT;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SequentialPhototaxisEvaluationFunction extends ODNEATEvaluationFunction {

	protected final double PENALTY_STEP;
	protected final double PROXIMITY_THRESHOLD = 0.5;
	
	public SequentialPhototaxisEvaluationFunction(Arguments args) {
		super(args);
		this.maxEnergyValue = args.getArgumentAsDoubleOrSetDefault("max", 100.0);
		this.initialEnergyFactor = args.getArgumentAsDoubleOrSetDefault("default", 0.75);
		this.PENALTY_STEP = args.getArgumentAsDoubleOrSetDefault("penalty", 0.01);
	}
	
	@Override
	public void update(Simulator simulator) {
		for(Robot r:  simulator.getRobots()){
			if(r.getController() instanceof OnlineController){
				OnlineController<?> controller = (OnlineController<?>) r.getController();
				ODNEAT instance = (ODNEAT) controller.getEAInstance();
				instance.executeOnlineEvolution(simulator.getEnvironment(), simulator.getRobots(), simulator.getTime());
			}
		}
		
		//save the statistics
		//stats.recordStats(simulator, simulator.getRobots(), this.outputDirectory);
	}

	@Override
	public double updateEnergyLevel(double currentEnergy, Robot r,
			Environment environment) {
		RoundForageEnvironment env = (RoundForageEnvironment) environment;
		LightpoleSensor lightSensor = (LightpoleSensor) r.getSensorByType(LightpoleSensor.class);
		double sensorRange = lightSensor.getRange();
		/**
		 * first, see if the robot is close to any given area.
		 */
		LightPole[] areas = env.getLightPoles();
		boolean nearSource = false;
		short areaType = -1;
		for(int i = 0; !nearSource && i < areas.length; i++){
			LightPole area = areas[i];
			//robot is close to a given area.
			if(area.getPosition().distanceTo(r.getPosition()) < (area.getRadius() + sensorRange)){
				nearSource = true;
				areaType = area.getPoleType();
			}
		}
		
		return computeNewEnergyLevel(currentEnergy, areaType, lightSensor);
	}

	protected double computeNewEnergyLevel(double currentEnergy, short areaType,
			LightpoleSensor lightSensor) {
		//not close to any area, loose a constant amount of energy.
		if(areaType == -1)
			return currentEnergy - PENALTY_STEP;

		double maxReading = 0;
		for(int i = 0; i < lightSensor.getNumberOfSensors(); i++){
			maxReading = Math.max(lightSensor.getSensorReading(i), maxReading);
		}
		
		switch(areaType){
		case LightPole.BENEFICIAL:
			return maxReading > this.PROXIMITY_THRESHOLD ? (currentEnergy + maxReading) : currentEnergy;
			//return currentEnergy + maxReading;
		case LightPole.DETRIMENTAL:
			//the closer the robot gets, the more energy it loses
			return currentEnergy - PENALTY_STEP - maxReading;
		case LightPole.NEUTRAL:
			return currentEnergy - PENALTY_STEP;
		}
		return 0;
	}

	@Override
	public double getDefaultEnergyValue(Robot r) {
		return this.maxEnergyValue * this.initialEnergyFactor;
	}

}

