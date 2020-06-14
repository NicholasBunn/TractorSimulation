package ontologies;

import jade.content.Concept;

public class AddAgentConcept implements Concept {
	private String name;
	   private String type;

	   public String getName() {
	     return name;
	   }
	   
	   public String getType() {
		   return type;
	   }

	   public void setName(String name) {
	      this.name = name;
	   }
	   
	   public void setType(String type) {
		   this.type = type;
	   }
}
