package taskexecutor.results;


public class LightpoleDataEntry extends DataEntry {

	private double radius;
	private short poleType;
	
	public LightpoleDataEntry(double x, double y, double radius, double time,
			short poleType) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.time = time;
		this.poleType = poleType;
	}
	
	public String toString(){
		return time + " " + x + " " + y + " " + radius + " " + poleType;
	}

	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @return the poleType
	 */
	public short getPoleType() {
		return poleType;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * @param poleType the poleType to set
	 */
	public void setPoleType(short poleType) {
		this.poleType = poleType;
	}

}

