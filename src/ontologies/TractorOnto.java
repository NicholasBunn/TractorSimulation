package ontologies;

import jade.content.onto.*;
import jade.content.schema.*;

public class TractorOnto extends Ontology implements TractorVocabulary {
	
	   // ----------> The name identifying this ontology
	   public static final String ONTOLOGY_NAME = "Tractor-Ontology";

	   // ----------> The singleton instance of this ontology
	   private static Ontology instance = new TractorOnto();

	   // ----------> Method to access the singleton ontology object
	   public static Ontology getInstance() { return instance; }
	   
	   // Private Constructor
	   private TractorOnto() {

		      super(ONTOLOGY_NAME, BasicOntology.getInstance());

		      try {
		    	  // Add Concepts
		    	  
		    	  // Tractor
		    	  ConceptSchema cs = new ConceptSchema(TRACTOR);
		    	  add(cs, Tractor.class);
		    	  cs.add(TRACTOR_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
		    	  cs.add(TRACTOR_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
		    	  cs.add(TRACTOR_CONSUMPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  cs.add(TRACTOR_FARMNUMBER, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  cs.add(TRACTOR_FARMLOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  
		    	  // Add AgentActions

		    	  // CreateTractor
		          AgentActionSchema as = new AgentActionSchema(ADD_TRACTOR);
		          add(as, AddTractor.class);
		          as.add(ADD_TRACTOR_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
		    	  
		    	  // PerformRequests
		    	  add(as = new AgentActionSchema(PERFORM_REQUESTS), PerformRequests.class);
		    	  as.add(PERFORM_REQUESTS_CONSUMPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  as.add(PERFORM_REQUESTS_TRACTORID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
		    	  
		    	  // PerformCFP
		    	  add(as = new AgentActionSchema(PERFORM_CFP), PerformCFP.class);
		    	  as.add(PERFORM_CFP_FARMLOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  as.add(PERFORM_CFP_FARMNUMBER, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  as.add(PERFORM_CFP_TRACTORID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
		    	  as.add(PERFORM_CFP_TIMESTAMP, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

		      }
		      catch (OntologyException oe) {
		         oe.printStackTrace();
		      }
		   }

}