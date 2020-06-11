package ontologies;

import jade.content.*;


public class AddAgent implements AgentAction {
// ------------------------------------------------

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