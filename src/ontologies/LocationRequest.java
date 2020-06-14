package ontologies;

import jade.content.Concept;

public class LocationRequest implements Concept {
	String tractorId;
	String tractorFarm;
	String tractorLocation;
	String timeStamp;
	
	public String getTractorId() {
		return tractorId;
	}
	
	public String getTractorFarm() {
		return tractorFarm;
	}
	
	public String getTractorLocation() {
		return tractorLocation;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	
	public void setTractorId(String tractorId) {
		this.tractorId = tractorId;
	}
	
	public void setTractorFarm(String tractorFarm) {
		this.tractorFarm = tractorFarm;
	}
	
	public void setTractorLocation(String tractorLocation) {
		this.tractorLocation = tractorLocation;
	}
	
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
}
