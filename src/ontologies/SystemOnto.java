package ontologies;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
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
			   // Add AgentActions
			   
			   // AddAgent
			   AgentActionSchema as = new AgentActionSchema(ADD_AGENT);
			   add(as, AddAgent.class);
			   as.add(ADD_AGENT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   as.add(ADD_AGENT_TYPE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   
			   add(as = new AgentActionSchema(REMOVE_AGENT), RemoveAgent.class);
			   as.add(REMOVE_AGENT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			   as.add(REMOVE_AGENT_TYPE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			   
		   } catch (OntologyException oe) {
			   oe.printStackTrace();
		   }
	   }  
}
