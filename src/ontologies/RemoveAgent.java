package ontologies;

import jade.content.AgentAction;

public class RemoveAgent implements AgentAction {
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
