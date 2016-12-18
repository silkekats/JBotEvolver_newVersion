package taskexecutor.results;


public class WallDataEntry extends DataEntry {

	private double width, height;
	
	public WallDataEntry(double x, double y, double width, double height, double time){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.time = time;
	}

	public String toString(){
		return time + " " + x + " " + y + " " + width + " " + height;
	}
	
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
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
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

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}
	
	
	
}

