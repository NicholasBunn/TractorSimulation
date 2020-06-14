package ontologies;

import jade.content.Concept;
import jade.content.onto.BasicOntology;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;

public class FuelRequest implements Concept {
	String fuelId;
	String fuelConsumption;
	
	public String getTractorId() {
		return fuelId;
	}
	
	public String getTractorConsumption() {
		return fuelConsumption;
	}
	
	public void setTractorId(String fuelId) {
		this.fuelId = fuelId;
	}
	
	public void setTractorConsumption(String fuelConsumption) {
		this.fuelConsumption = fuelConsumption;
	}
}
