package ontologies;

import jade.content.*;


public class AddAgent implements AgentAction {
	private AddAgentConcept agent;
	
	public AddAgentConcept getAgent() {
		return agent;
	}
	
	public void setAgent(AddAgentConcept agent) {
		this.agent = agent;
	}
}