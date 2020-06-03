package ontologies;

import jade.content.*;


public class PerformRequests implements AgentAction {
// ------------------------------------------------

   private String consumption;
   private String tractorId;

   public String getConsumption() {
     return consumption;
   }

   public String getTractorId() {
      return tractorId;
   }

   public void setConsumption(String consumption) {
      this.consumption = consumption;
   }

   public void setTractorId(String tractorId) {
      this.tractorId = tractorId;
   }
}