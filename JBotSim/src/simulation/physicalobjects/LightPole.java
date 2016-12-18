package simulation.physicalobjects;


import java.awt.Color;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class LightPole extends MovableObject {
	
	boolean turnedOn = true;
	protected short poleType;

	public static final short BENEFICIAL = 1;
	public static final short NEUTRAL = 2;
	public static final short DETRIMENTAL = 3;
	
	public LightPole(Simulator simulator,  String name, double x, double y, double radius) {
		super(simulator, name, x, y, 0, 0, PhysicalObjectType.LIGHTPOLE);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, 0, 0, 2 * radius, radius);
		//by default
		this.poleType = BENEFICIAL;
	}
	
	public Vector2d getPosition() {
		return super.getPosition();
	}
	
	public boolean isTurnedOn() {
		return turnedOn;
	}

	public void setTurnedOn(boolean turnedOn) {
		this.turnedOn = turnedOn;
	}
	
	public void setPoleType(short type){
		this.poleType = type;
	}
	
	public short getPoleType(){
		return this.poleType;
	}

	public Color getColor() {
		return getColor();
	}
	
}

