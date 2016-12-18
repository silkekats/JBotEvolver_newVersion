package simulation.environment;

import java.io.File;
import java.util.Random;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class SimpleArenaEnvironment extends Environment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double wallThickness = 0.1;
	//6.28 radians = +- 2 pi
	protected double randomizeOrientationValue = 6.28;
	protected double OBSTACLES_SIDE = 0.5;
	//distance between the center of obstacles
	protected final double DISTANCE_BETWEEN_OBSTACLES = 1.0;
	protected int numberObstacles;
	Vector2d lastposition;
	private Simulator simulator;
	private Arguments args;
	File f;
	File robot;
	
	@ArgumentsAnnotation(name="path", defaultValue="test")
	private String outputfolder;
	
	@ArgumentsAnnotation(name="locationRobot", defaultValue="0")
	private double storeLocationRobot;

	
	public SimpleArenaEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;
		this.args = arguments;
		this.numberObstacles = arguments.getArgumentAsIntOrSetDefault("numberobstacles", 5);
		//setupWalls(simulator, args);
		outputfolder = arguments.getArgumentIsDefined("path") ? arguments.getArgumentAsString("path")       : "test";
		storeLocationRobot = arguments.getArgumentIsDefined("locationRobot") ? arguments.getArgumentAsDouble("locationRobot")       : 0;
		robot = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_currentRobot.log");
		int robotNumber=0;
		try {
	        Scanner scan = new Scanner(robot);
	        robotNumber = scan.nextInt();
	        } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	        }
		
		f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+outputfolder+"_onlineLocation"+Integer.toString(robotNumber)+".log");
		
	}

	@Override
	public void setup(Simulator simulator){
		super.setup(simulator);
		this.setupWalls(simulator);
		Wall[] obstacles = setupObstacles(simulator);
		placeRobots(simulator,obstacles);
	}
	
	protected Wall[] setupObstacles(Simulator simulator) {
		Wall[] obstacles = new Wall[this.numberObstacles];
		for(int i = 0; i < numberObstacles; i++){
			Vector2d position = getPositionForObstacle(i);
			obstacles[i] = this.createWall(simulator, position.getX(), position.getY(), 
					OBSTACLES_SIDE, OBSTACLES_SIDE);
		}

		return obstacles;
	}

	protected Vector2d getPositionForObstacle(int obstacleId) {
		obstacleId = obstacleId % 5;
		Vector2d position = null;

		/**
		 * -0.39941627066844076; 1.1974703876535557
			-0.5975370661294478; -0.37320065369925437
			0.8950071223988234; -0.9457213741598273
			0.7583405777487116; 0.6412197538203499
			1.0444882561405937; -0.13266914572589927
		 */
		switch(obstacleId){
		case 0:
			position = new Vector2d(-0.39941627066844076, 1.1974703876535557);
			break;
		case 1:
			position = new Vector2d(-0.5975370661294478, -0.37320065369925437);
			break;
		case 2:
			position = new Vector2d(0.8950071223988234, -0.9457213741598273);
			break;
		case 3:
			position = new Vector2d(0.7583405777487116, 0.6412197538203499);
			break;
		case 4:
			position = new Vector2d(1.0444882561405937, -0.13266914572589927);
			break;
		default:
			break;
		}
		return position;
	}


	protected void placeRobots(Simulator simulator, Wall[] obstacles) {
		
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
		for(Robot r : this.robots){
			
			Vector2d position = this.generateRandomPosition(simulator, this.width, this.height,wallThickness);
			
			if((onlinelocation[0]+onlinelocation[1]+onlinelocation[2]==0) || (Math.abs(onlinelocation[0])) >= this.width/2-wallThickness || (Math.abs(onlinelocation[1])) >= this.width/2-wallThickness ){
				while(intersectsObstacles(obstacles, position, r.getRadius())){
					position = this.generateRandomPosition(simulator, this.width,this.height,wallThickness);
				}
				r.teleportTo(position);
				//robots current orientation +- a given offset
				double orientation = r.getOrientation() + (random.nextDouble()*2-1) * this.randomizeOrientationValue;
				r.setOrientation(orientation);

			} else{
				r.teleportTo(new Vector2d(onlinelocation[0],onlinelocation[1]));
				r.setOrientation(onlinelocation[2]);
				
				//System.out.println("use previous location");
			}

		}

	}
	
	protected boolean intersectsObstacles(Wall[] obstacles, Vector2d robotPos, 
			double robotRadius) {
		
		for(Wall w : obstacles){
			double distanceCenterToCorner = Math.sqrt(Math.pow(OBSTACLES_SIDE/2, 2) + Math.pow(OBSTACLES_SIDE/2, 2));
			if(robotPos.distanceTo(w.getPosition()) < distanceCenterToCorner + robotRadius){
				return true;
			}
		}
		return false;
	}

	
	protected Vector2d generateRandomPosition(Simulator simulator, double width, double height, double wall) {		
		Random random = simulator.getRandom();
		double x = random.nextDouble() * (width-wall*4) - (width-wall*4) /2 ;
		double y = random.nextDouble() * (height-wall*4) - (height-wall*4) /2 ;
		
		return new Vector2d(x,y); 
	}
	
	protected void setupWalls(Simulator simulator) {
		createHorizontalWalls(simulator);
		createVerticalWalls(simulator);
	}

	@Override
	public void update(double time) {
		
		for(Robot robot: robots){
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
			}
		}
		}
		
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

}

