package ontologies;

import jade.content.*;


public class PerformCFP implements AgentAction {
// ------------------------------------------------

   private LocationRequest tractorId;

   public LocationRequest getTractorId() {
      return tractorId;
   }

   public void setTractorId(LocationRequest tractor) {
      this.tractorId = tractor;
   }

}