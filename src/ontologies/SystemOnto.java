package ontologies;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;

public class SystemOnto extends Ontology implements SystemVocabulary {
	 // ----------> The name identifying this ontology
	   public static final String ONTOLOGY_NAME = "System-Ontology";

	   // ----------> The singleton instance of this ontology
	   private static Ontology instance = new SystemOnto();

	   // ----------> Method to access the singleton ontology object
	   public static Ontology getInstance() { return instance; }
	   
	   // Private Constructor
	   private SystemOnto() {
		   
		   super(ONTOLOGY_NAME, BasicOntology.getInstance());

		   try {
			// Add Concepts
			   
			   // Tractor
			   ConceptSchema cs = new ConceptSchema(TRACTOR);
			   add(cs, Tractor.class);
			   cs.add(TRACTOR_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   cs.add(TRACTOR_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   cs.add(TRACTOR_CONSUMPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   cs.add(TRACTOR_FARMNUMBER, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   cs.add(TRACTOR_FARMLOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  
			   // FuelRequest
			   add(cs = new ConceptSchema(FUEL_REQUEST), FuelRequest.class);
			   cs.add(FUEL_REQUEST_FUELID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   cs.add(FUEL_REQUEST_FUELCONSUMPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

			   // LocationRequest
			   add(cs = new ConceptSchema(LOCATION_REQUEST), LocationRequest.class);
			   cs.add(LOCATION_REQUEST_TRACTORID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   cs.add(LOCATION_REQUEST_TRACTORFARM, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   cs.add(LOCATION_REQUEST_TRACTORLOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   cs.add(LOCATION_REQUEST_TIMESTAMP, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   
			   // AddAgent
			   add(cs = new ConceptSchema(ADD_AGENT_CONCEPT), AddAgentConcept.class);
			   cs.add(ADD_AGENT_CONCEPT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   cs.add(ADD_AGENT_CONCEPT_TYPE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   
			   // RemoveAgent
			   add(cs = new ConceptSchema(REMOVE_AGENT_CONCEPT), RemoveAgentConcept.class);
			   cs.add(REMOVE_AGENT_CONCEPT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   cs.add(REMOVE_AGENT_CONCEPT_TYPE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

			// Add Actions
			   
			   // PerformRequests
			   AgentActionSchema as = new AgentActionSchema(PERFORM_REQUESTS);
			   add(as, PerformRequests.class);
			   as.add(PERFORM_REQUESTS_FUEL, (ConceptSchema)getSchema(FUEL_REQUEST));
		    	  
			   // PerformCFP
			   add(as = new AgentActionSchema(PERFORM_CFP), PerformCFP.class);
			   as.add(PERFORM_CFP_TRACTORID, (ConceptSchema)getSchema(LOCATION_REQUEST));
		   
			   // AddAgent
			   add(as = new AgentActionSchema(ADD_AGENT), AddAgent.class);
			   as.add(ADD_AGENT_AGENT, (ConceptSchema)getSchema(ADD_AGENT_CONCEPT));
			   
			   add(as = new AgentActionSchema(REMOVE_AGENT), RemoveAgent.class);
			   as.add(REMOVE_AGENT_AGENT, (ConceptSchema)getSchema(REMOVE_AGENT_CONCEPT));
			   
			   add(as = new AgentActionSchema(RETRIEVE_DATA), RetrieveData.class);
			   as.add(RETRIEVE_DATA_TRACTOR, (ConceptSchema)getSchema(TRACTOR));
			   
		   } catch (OntologyException oe) {
			   oe.printStackTrace();
		   }
	   }  
}
