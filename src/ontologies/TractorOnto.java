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
		    	  

//		         // Account
//		         ConceptSchema cs = new ConceptSchema(ACCOUNT);
//		         add(cs, Account.class);
//		         cs.add(ACCOUNT_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
//		         cs.add(ACCOUNT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
//		         cs.add(ACCOUNT_BALANCE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
//
//		         // Problem
//		         add(cs = new ConceptSchema(PROBLEM), Problem.class);
//		         cs.add(PROBLEM_NUM, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
//		         cs.add(PROBLEM_MSG, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
//
//		         // Operation
//		         add(cs = new ConceptSchema(OPERATION), Operation.class);
//		         cs.add(OPERATION_TYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
//		         cs.add(OPERATION_AMOUNT, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
//		         cs.add(OPERATION_BALANCE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
//		         cs.add(OPERATION_ACCOUNTID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
//		         cs.add(OPERATION_DATE, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.MANDATORY);
//
//		         // ------- Add AgentActions
//
//		         // CreateAccount
//		         AgentActionSchema as = new AgentActionSchema(CREATE_ACCOUNT);
//		         add(as, CreateAccount.class);
//		         as.add(CREATE_ACCOUNT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
//
//		         // MakeOperation
//		         add(as = new AgentActionSchema(MAKE_OPERATION), MakeOperation.class);
//		         as.add(MAKE_OPERATION_TYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
//		         as.add(MAKE_OPERATION_AMOUNT, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
//		         as.add(MAKE_OPERATION_ACCOUNTID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
//
//		         // Information
//		         add(as = new AgentActionSchema(INFORMATION), Information.class);
//		         as.add(INFORMATION_TYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
//		         as.add(INFORMATION_ACCOUNTID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
		      }
		      catch (OntologyException oe) {
		         oe.printStackTrace();
		      }
		   }

}