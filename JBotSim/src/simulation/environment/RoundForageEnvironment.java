package simulation.environment;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;



public class RoundForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private static final long serialVersionUID = 1L;
	protected double wallThickness = 0.1;
	protected double randomizeOrientationValue = 6.28;
	protected Prey preyCarried;
	Vector2d lastposition;
	
	@ArgumentsAnnotation(name="maxpickdistance", defaultValue="0.05")
	protected double maxPickDistance = 0.05;
	protected Vector2d temp = new Vector2d();
	
	@ArgumentsAnnotation(name="nestlimit", defaultValue="0.5")
	private double nestLimit;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	private double forageLimit;
	
	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="5.0")
	private	double forbiddenArea;
	
	@ArgumentsAnnotation(name="path", defaultValue="test")
	private String outputfolder;
	
	@ArgumentsAnnotation(name="locationRobot", defaultValue="0")
	private double storeLocationRobot;
	
	@ArgumentsAnnotation(name="stateRobot", defaultValue="0")
	private double storeStateRobot;
	
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="20")
	private int numberOfPreys;
	
	@ArgumentsAnnotation(name="densityofpreys", defaultValue="")
	
	private Nest nest;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;
	
	private Simulator simulator;
	private Arguments args;
	File f;
	File pucks;
	File carriedPuck;
	File robot;
	
	

	public RoundForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;
		this.args = arguments;
		storeStateRobot = arguments.getArgumentIsDefined("stateRobot") ? arguments.getArgumentAsDouble("stateRobot")       : 0;
		storeLocationRobot = arguments.getArgumentIsDefined("locationRobot") ? arguments.getArgumentAsDouble("locationRobot")       : 0;
		nestLimit       = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")       : .5;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")       : 2.0;
		forbiddenArea   = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")       : 5.0;
		outputfolder	= arguments.getArgumentIsDefined("path") ? arguments.getArgumentAsString("path")       : "test";
		
		robot = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_currentRobot.log");
		int robotNumber=0;
		if(robot.exists()){
		try {
	        Scanner scan = new Scanner(robot);
	        robotNumber = scan.nextInt();
	        } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	        }
		}
		
		f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_onlineLocation"+Integer.toString(robotNumber)+".log");
		pucks = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_onlineLocationPucks"+Integer.toString(robotNumber)+".log");
		carriedPuck = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_carriedPuck"+Integer.toString(robotNumber)+".log");
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		this.setupWalls(simulator);
		
		placeRobots(simulator);
		
		
		this.random = simulator.getRandom();
		
		if(args.getArgumentIsDefined("densityofpreys")){
			double densityoffood = args.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = args.getArgumentIsDefined("numberofpreys") ? args.getArgumentAsInt("numberofpreys") : 20;
		}
		
		
		int booleanPuck=0;
		int puckId=0;
		int puckNumber=0;
		if(carriedPuck.exists()){
			try{
				Scanner scan = new Scanner(carriedPuck);
		        booleanPuck = (int) scan.nextDouble();
		        puckId = (int) (scan.nextDouble());
		        //System.out.println(puckId);
		        puckNumber = puckId-100004;
		        //System.out.println(puckNumber);
		        
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		
		double[][] onlinelocationPucks = new double[numberOfPreys][2];
		if(pucks.exists()){
			try {
		        Scanner scan = new Scanner(pucks);
		        int i=0;

		        while(scan.hasNextDouble() && i<numberOfPreys)
		        {	
		        	int iD = (int) (scan.nextDouble() - 100004);
		        	onlinelocationPucks[i][0] = scan.nextDouble();
		        	onlinelocationPucks[i][1] = scan.nextDouble();
		        	
		        	if(booleanPuck==1&& puckNumber==i){
		        			addPrey(new Prey(simulator, "Prey "+i, new Vector2d(onlinelocationPucks[i][0],onlinelocationPucks[i][1]), 0, PREY_MASS, PREY_RADIUS));
		        			Prey preyCarried = (Prey) simulator.getEnvironment().getPrey().get(puckNumber);
		        			//System.out.println("HAVE PUCK ID " + preyCarried.getId());
		        			Robot r = simulator.getRobots().get(0);
		        			preyCarried.setCarrier(r);
		        			PreyCarriedSensor sensor = (PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class);
							sensor.havePrey();
		        		
		        	} else{
		        	
		        	//System.out.println("ID " + iD);
		        	Prey preynew = new Prey(simulator, "Prey "+i, new Vector2d(onlinelocationPucks[i][0],onlinelocationPucks[i][1]), 0, PREY_MASS, PREY_RADIUS);
		        	addPrey(preynew);
		        	//System.out.println("ID new" + preynew.getId());
		        	}
		            i++;
		        }

		    } catch (FileNotFoundException e1) {
		            e1.printStackTrace();
		    }
		}
		
			if(!pucks.exists()){
	
			for(int i = 0; i < numberOfPreys; i++ ){
				addPrey(new Prey(simulator, "Prey "+i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
			}
			}
		
			nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
			addObject(nest);
		
		
	}
	
	protected void placeRobots(Simulator simulator) {
		
		
		double[] onlinelocation = new double[3];
		//System.out.println(onlinelocation[0]);
		
		if(f.exists()){
		try {
	        Scanner scan = new Scanner(f);
	        int i=0;

	        while(scan.hasNextDouble() && i<=2)
	        {
	            onlinelocation[i] = scan.nextDouble();
	            i++;
	        }

	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
		}
		

		
		
		
		
			Random random = simulator.getRandom();
			// place robots in a random position, with a random orientation, at the beginning of the simulation
		
		
		
			for(Robot r : this.robots){
				Vector2d randomPosition = generateRandomPosition(simulator, this.width, this.height,wallThickness);
			
				//System.out.println("position " + r.getPosition());
				//r.teleportTo(new Vector2d(0,0));
				if((onlinelocation[0]+onlinelocation[1]+onlinelocation[2]==0) || Math.abs(onlinelocation[0])>((width/2)-wallThickness) || Math.abs(onlinelocation[1])>((height/2)-wallThickness) ){
					r.teleportTo(randomPosition);
					//robots current orientation +- a given offset
					double orientation = r.getOrientation() + (random.nextDouble()*2-1) * this.randomizeOrientationValue;
					r.setOrientation(orientation);
					//System.out.println("random location");
				} else{
					r.teleportTo(new Vector2d(onlinelocation[0],onlinelocation[1]));
					r.setOrientation(onlinelocation[2]);
					
					//System.out.println("use previous location");
				}
			}
	}
	
	protected Vector2d generateRandomPosition(Simulator simulator, double width, double height, double wall) {		
		Random random = simulator.getRandom();
		double x = random.nextDouble() * (width-wall*4) - (width-wall*4) /2 ;
		double y = random.nextDouble() * (height-wall*4) - (height-wall*4) /2 ;
		
		return new Vector2d(x,y); 
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit-wallThickness)+nestLimit*1.1;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	@Override
	public void update(double time) {
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			
			 double distance = nextPrey.getPosition().length();
			if(!nextPrey.isEnabled() && distance < nestLimit){

				 if(distance == 0){
					 System.out.println("ERRO--- zero");
				 }
				 
			
				 Robot robot = nextPrey.getHolder();
				 PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class);
				 sensor.noPrey();
				 nextPrey.teleportTo(newRandomPosition());
				 nextPrey.setCarrier(null);
				 
				 FileWriter newLocationPucks;
					if(pucks.exists()){
					try {
						newLocationPucks = new FileWriter(pucks, false);
						for (Prey prey : simulator.getEnvironment().getPrey()){
						newLocationPucks.write(nextPrey.getId()+"\n"+prey.getPosition().x +"\n"+ prey.getPosition().y + "\n");
						}
						newLocationPucks.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // true to append
					                                                  
					}
					
					
				if(storeStateRobot==1){	
				FileWriter hasPuck;
				if(carriedPuck.exists()){
					try{
						hasPuck = new FileWriter(carriedPuck,false);
						hasPuck.write("0\n0\n");
						hasPuck.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				}
				 
				 numberOfFoodSuccessfullyForaged++;
			 }
			
			
		}
		
		for(Robot robot: robots){
			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class);
			//if robot is not carrying prey check if there is one close
			if (sensor != null && !sensor.preyCarried()){
				double bestLength = maxPickDistance;
				Prey bestPrey = null;
				ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
				CloseObjectIterator iterator = closePreys.iterator();
				while (iterator.hasNext()) {
					Prey closePrey = (Prey) (iterator.next().getObject());
					if (closePrey.isEnabled()) {
						getTemp().set(closePrey.getPosition());
						getTemp().sub(robot.getPosition());
						double length = getTemp().length() - robot.getRadius();
						if (length < bestLength) {
							bestPrey = closePrey;
							bestLength = length;
							
						}
					}
				}
				if (bestPrey != null) {
					preyCarried = bestPrey;
					bestPrey.setCarrier(robot);
					sensor.havePrey();
					 
				
					
					if(storeStateRobot==1){	
					FileWriter hasPuck;
					
						try{
							hasPuck = new FileWriter(carriedPuck,false);
							hasPuck.write("1\n" + bestPrey.getId() + "\n");
							hasPuck.close();
						} catch (IOException e) {
							e.printStackTrace();
						
						}
					}
				}
			
			}
		lastposition = robot.getPosition();
		
		if(storeLocationRobot==1){	
		FileWriter newLocation;
		try {
			newLocation = new FileWriter(f, false);
			newLocation.write(lastposition.x +"\n"+ lastposition.y + "\n" + lastposition.getAngle()+ "\n");
			newLocation.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // true to append 
		}                                                 
		
		} 
		
		
		
		FileWriter newLocationPucks;
		if(!pucks.exists()){
		try {
			newLocationPucks = new FileWriter(pucks, false);
			for (Prey nextPrey : simulator.getEnvironment().getPrey()){
			newLocationPucks.write(nextPrey.getId()+"\n"+nextPrey.getPosition().x +"\n"+ nextPrey.getPosition().y + "\n");
			}
			newLocationPucks.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // true to append
		                                                  
		}
	
	
	}
		
	
	protected void setupWalls(Simulator simulator) {
		createHorizontalWalls(simulator);
		createVerticalWalls(simulator);
	}
	
	protected Wall createWall(Simulator simulator, double x, double y, double width, double height) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,PhysicalObjectType.WALL);
		this.addObject(w);
		
		return w;
	}
	
	private void createHorizontalWalls(Simulator simulator) {
		createWall(simulator, 0, this.height/2 + wallThickness/2, width + wallThickness, wallThickness);

		createWall(simulator, 0, -this.height/2 - wallThickness/2,
				width + wallThickness, wallThickness);
	}
	
	private void createVerticalWalls(Simulator simulator) {
		createWall(simulator, -this.width/2 - wallThickness/2, 0, 
				wallThickness, height + wallThickness);
		
		createWall(simulator, width/2 + wallThickness/2, 0, wallThickness, height + wallThickness);
	}

	
	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public double getNestRadius() {
		return nestLimit;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	public Vector2d getTemp() {
		return temp;
	}
	/**
	 * Pickup a prey (no checks are made).
	 */

	public LightPole[] getLightPoles() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
