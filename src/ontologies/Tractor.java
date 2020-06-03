package ontologies;

import jade.content.*;

public class Tractor implements Concept {
	private String id;
	private String name;
	private String consumption;
	private String farmNumber;
	private String farmLocation;
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getConsumption() {
		return consumption;
	}
	
	public String getFarmNumber() {
		return farmNumber;
	}
	
	public String getfarmLocation() {
		return farmLocation;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setConsumption(String consumption) {
		this.consumption = consumption;
	}
	
	public void setFarmNumber(String farmNumber) {
		this.farmNumber = farmNumber;
	}
	
	public void setFarmLocation(String farmLocation) {
		this.farmLocation = farmLocation;
	}
	
}