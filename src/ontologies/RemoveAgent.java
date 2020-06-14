package ontologies;

import jade.content.AgentAction;

public class RemoveAgent implements AgentAction {
private RemoveAgentConcept agent;
	
	public RemoveAgentConcept getAgent() {
		return agent;
	}
	
	public void setAgent(RemoveAgentConcept agent) {
		this.agent = agent;
	}
}
