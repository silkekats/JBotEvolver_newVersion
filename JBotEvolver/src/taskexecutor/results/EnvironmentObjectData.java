package taskexecutor.results;


import java.util.ArrayList;

import mathutils.Vector2d;

import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;

public class EnvironmentObjectData {

	private int id;
	private PhysicalObjectType type;
	private String name;
	private ArrayList<DataEntry> dataEntries;
	
	public EnvironmentObjectData(int id, PhysicalObjectType type, String name){
		this.type = type;
		this.name = name;
		this.id = id;
		dataEntries = new ArrayList<DataEntry>();
	}


	public boolean addDataEntry(PhysicalObject object, double time){
		
		Vector2d position = object.getPosition();
		switch(this.type){
		case ROBOT:
			dataEntries.add(new RobotDataEntry(position.getX(), position.getY(), object.getOrientation(), time, object.getRadius()));
			return true;
		case WALL:
			//wall do not move.
			if(dataEntries.isEmpty()){
				Wall w = (Wall) object;
				dataEntries.add(new WallDataEntry(position.getX(), position.getY(), w.getWidth(), w.getHeight(), time));
				return true;
			}
			break;
		case LIGHTPOLE:
			//empty or position changed
			if(dataEntries.isEmpty() || isInDifferentPosition(dataEntries.get(dataEntries.size() - 1), position)){
				LightPole pole = (LightPole) object;
				dataEntries.add(new LightpoleDataEntry(position.getX(), position.getY(), pole.getRadius(), time, pole.getPoleType()));
				return true;
			}
			break;
		default:
			break;
		}
		
		return false;
	}
	
	private boolean isInDifferentPosition(DataEntry dataEntry, Vector2d position) {
		return dataEntry.getX() != position.getX() || dataEntry.getY() != position.getY();
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the dataEntries
	 */
	public ArrayList<DataEntry> getDataEntries() {
		return dataEntries;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @return the type
	 */
	public PhysicalObjectType getType() {
		return type;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(PhysicalObjectType type) {
		this.type = type;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnvironmentObjectData other = (EnvironmentObjectData) obj;
		if (id != other.id)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}


	public void clearEntries() {
		this.dataEntries.clear();
	}
	
	
}

