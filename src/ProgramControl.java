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
import ontologies.AddAgentConcept;
import ontologies.RemoveAgent;
import ontologies.RemoveAgentConcept;
import ontologies.SystemOnto;

public class ProgramControl extends Agent {

	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = SystemOnto.getInstance();
	
	public void setup() {
		// Register Service with the DF
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
				
		// Receive create and kill requests from GUIs
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

				ContentElement content;
				try {
					content = getContentManager().extractContent(request);
					
					if (content.getClass().getName() == "ontologies.AddAgent") {
						AddAgent addContent = (AddAgent) content;
						AddAgentConcept ac = addContent.getAgent();
						CreateAgent(ac.getName(), ac.getType());
					} else if(content.getClass().getName() == "ontologies.RemoveAgent") {
						RemoveAgent killContent = (RemoveAgent) content;
						RemoveAgentConcept rc = killContent.getAgent();
						// Check if the agent being killed is a tractor agent and,
						// if so, kill its corresponding fuel management agent too
						System.out.println("PG " + rc.getType());
						if ((rc.getType() != null) && (rc.getType().equals("TractorAgent"))) {
							KillAgent(new AID(rc.getName(), AID.ISLOCALNAME));
							String fName[] = rc.getName().split("T");
							KillAgent(new AID(("F" + fName[1]), AID.ISLOCALNAME));
						} else {
							KillAgent(new AID(rc.getName(), AID.ISLOCALNAME));
						}
					}
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
				return inform;
			}
		} );
	}
	
	// Method to create an agent
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
	
	// Method to kill an agent
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