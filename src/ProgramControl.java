import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.behaviours.ActionExecutor;
import jade.core.behaviours.OutcomeManager;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import ontologies.AddAgent;
import ontologies.PerformRequests;
import ontologies.RemoveAgent;
import ontologies.SystemOnto;
import ontologies.TractorOnto;

public class ProgramControl extends Agent {

	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = SystemOnto.getInstance();
	
	public void setup() {
		// Register Service
		DFAgentDescription agentDesc = new DFAgentDescription();
		ServiceDescription serviceDesc = new ServiceDescription();
		serviceDesc.setType("ProgramController");
		serviceDesc.setName(getLocalName());
		agentDesc.setName(getAID());
		agentDesc.addServices(serviceDesc);
		try {
			DFService.register(this, agentDesc);
		} catch (FIPAException e) {
			e.printStackTrace();
			System.out.print("Error registering " + getLocalName() + " to DF");
		}
		
		// Register language and ontology
		getContentManager().registerLanguage(xmlCodec);
		getContentManager().registerOntology(ontology);
				
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				return null;
			}
			
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				ACLMessage inform = request.createReply();
				inform.setLanguage(xmlCodec.getName());
				inform.setOntology(ontology.getName());
				inform.setPerformative(ACLMessage.INFORM);
				try {
					ContentElement content = getContentManager().extractContent(request);
								
					if (request.getOntology() == "Tractor-Ontology") {
						// DO SOMETHING TO HANDLE TRACTOR QUERY
					} else if(request.getOntology() == "System-Ontology") {
						if (content.getClass().getName() == "ontologies.AddAgent") {
							AddAgent addContent = (AddAgent) content;
							CreateAgent(addContent.getName(), addContent.getType());
						} else if(content.getClass().getName() == "ontologies.RemoveAgent") {
							RemoveAgent killContent = (RemoveAgent) content;
							// Check if the agent being killed is a tractor agent and,
							// if so, kill its corresponding fuel management agent too
							if ((killContent.getType() != null) && (killContent.getType().equals("TractorAgent"))) {
								KillAgent(new AID(killContent.getName(), AID.ISLOCALNAME));
								String fName[] = killContent.getName().split("T");
								KillAgent(new AID(("F" + fName[1]), AID.ISLOCALNAME));
							} else {
								KillAgent(new AID(killContent.getName(), AID.ISLOCALNAME));
							}
						}
					}
				} catch (CodecException | OntologyException e) {
					System.out.println("Error extracting content for create message.");
					e.printStackTrace();
				}
				return inform;
			}
		} );
	}
	
	public void CreateAgent(String name, String type) {
		
		CreateAgent ca = new CreateAgent();
		ca.setAgentName(name);
		ca.setClassName(type);
		ca.setContainer(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
		ca.setInitialCredentials(null);
		ca.setOwner(null);

		ActionExecutor<CreateAgent, Void> aec = new ActionExecutor<CreateAgent, Void>(ca, JADEManagementOntology.getInstance(), getAMS()) {
			@Override
			public int onEnd() {
				int ret = super.onEnd();
				if (getExitCode() == OutcomeManager.OK) {
					// Creation successful
					System.out.println("Agent " + name + " successfully created");
				} else {
					// Something went wrong
					System.out.println("Agent creation error. " + getErrorMsg());
				}
				
				return ret;
			}
		};
		addBehaviour(aec);
	}
	
	public void KillAgent(AID name) {
		KillAgent ka = new KillAgent();
		ka.setAgent(name);
	
		ActionExecutor<KillAgent, Void> aek = new ActionExecutor<KillAgent, Void>(ka, JADEManagementOntology.getInstance(), getAMS()) {
			@Override
			public int onEnd() {
				int ret = super.onEnd();
				if (getExitCode() == OutcomeManager.OK) {
					// Creation successful
					System.out.println("Agent " + name + " successfully killed");
				} else {
					// Something went wrong
					System.out.println("Agent killing error. " + getErrorMsg());
				}
				
				return ret;
			}
		};
		addBehaviour(aek);
		}
}