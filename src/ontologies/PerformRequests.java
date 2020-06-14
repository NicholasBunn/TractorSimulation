package ontologies;

import jade.content.*;


public class PerformRequests implements AgentAction {
   private FuelRequest fuel;

   public FuelRequest getFuelId() {
      return fuel;
   }

   public void setFuelId(FuelRequest fuel) {
      this.fuel = fuel;
   }
}