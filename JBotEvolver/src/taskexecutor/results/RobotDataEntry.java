package taskexecutor.results;

public class RobotDataEntry extends DataEntry{

	private double orientation, radius;
	
	public RobotDataEntry(double x, double y, double orientation, double time, double radius){
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.orientation = orientation;
		this.time = time;
	}

	public String toString(){
		return time + " " + x + " " + y + " " + orientation + " " + radius;
	}

	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}


	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}


	/**
	 * @return the orientation
	 */
	public double getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	
}

