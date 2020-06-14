package ontologies;

import jade.content.AgentAction;

public class RetrieveData implements AgentAction {
	private Tractor tractor;
	
	public Tractor getTractor() {
		return tractor;
	}
	
	public void setTractor(Tractor tractor) {
		this.tractor = tractor;
	}
}
