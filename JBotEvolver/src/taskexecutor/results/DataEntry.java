package taskexecutor.results;


public abstract class DataEntry {

	protected double time, x, y;
	
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	public double getTime(){
		return this.time;
	}
	
	public void setTime(double newTime){
		this.time = newTime;
	}
	
	public abstract String toString();
	
}

