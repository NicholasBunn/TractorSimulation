package ontologies;

import jade.content.*;


public class PerformCFP implements AgentAction {
// ------------------------------------------------

   private String farmNumber;
   private String farmLocation;
   private String tractorId;
   private String timeStamp;

   public String getFarmNumber() {
     return farmNumber;
   }
   
   public String getFarmLocation() {
	   return farmLocation;
   }

   public String getTractorId() {
      return tractorId;
   }
   
   public String getTimeStamp() {
	   return timeStamp;
   }

   public void setFarmNumber(String farmNumber) {
      this.farmNumber = farmNumber;
   }
   
   public void setFarmLocation(String farmLocation) {
	   this.farmLocation = farmLocation;
   }

   public void setTractorId(String tractorId) {
      this.tractorId = tractorId;
   }
   
   public void setTimeStamp(String timeStamp) {
	   this.timeStamp = timeStamp;
   }
}