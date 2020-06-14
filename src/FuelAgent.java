import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import ontologies.SystemOnto;
import ontologies.FuelRequest;
import ontologies.PerformRequests;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;

// Implement as fuel consumption agent for tractor 2
public class FuelAgent extends Agent {
	private boolean busy = false;
	
	private String mySens;
	
	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = SystemOnto.getInstance();
	
	public void setup() {
		// Register service with the DF
		DFAgentDescription agentDesc = new DFAgentDescription();
		ServiceDescription serviceDesc = new ServiceDescription();
		serviceDesc.setType("FuelFetcher");
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
				
		System.out.println("Agent " + getLocalName() + " waiting for requests...");
		
		// Receive ACL Messages
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				try {
					ContentElement content = getContentManager().extractContent(request);	// Extract content from message
					PerformRequests pr = (PerformRequests) content;							// Cast content to PerformRequest ontology
					FuelRequest fr = pr.getFuelId();										// Extract FuelRequest ontology message structure
					mySens = fr.getTractorId();
				} catch (CodecException | OntologyException e) {
					System.out.println("Error extracting content for fuel message request.");
					e.printStackTrace();
				}
				
				if (!busy) {
					// If the Agent is not currently busy, agree to perform the action
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				}
				else {
					// Refuse to perform the action if Agent is busy
					System.out.println("Agent " + getLocalName() + ": Refuse");
					throw new RefuseException("check-failed");
				}
			}
			
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				PerformRequests pr = new PerformRequests();
				FuelRequest fr = new FuelRequest();
				
				String returnString = FetchConsumption(mySens); 							// Collect the current consumption data
				
				// If there is available information for the consumption, reply with the consumption
				if (returnString != null) {
					ACLMessage inform = request.createReply();
					inform.setLanguage(xmlCodec.getName());
					inform.setOntology(ontology.getName());
					inform.setPerformative(ACLMessage.INFORM);
					
					fr.setTractorId(mySens);
					fr.setTractorConsumption(returnString);
					
					pr.setFuelId(fr);
					
					try {
						getContentManager().fillContent(inform, pr);
					} catch (CodecException | OntologyException e) {
						System.out.println("Error filling content for fuel message.");
						e.printStackTrace();
					}
					return inform;
				}
				else {
					throw new FailureException("Consumption data returned 'null'.");
				}	
			}
		} );
	}
	
	@Override
	protected void takeDown() 
    {
		// De-register this agent from the DF
		try {
			DFService.deregister(this); 
		} catch (Exception e) {}
    }
	
	// Fetch consumption data from the Erlang simulation
	private String FetchConsumption(String myId) { 
		final String fuelPortNumber = "900" + myId; 
		
		busy = true;
		String dataReceived = null;
		DataOutputStream outToServer;
				
		try { 
			String userString = "request";
			  
			// Setup socket
			Socket socket = new Socket("localhost", Integer.parseInt(fuelPortNumber));
			  
			// Send message over socket
			outToServer = new DataOutputStream(socket.getOutputStream());
			byte[] outByteString = userString.getBytes("UTF-8");
			outToServer.write(outByteString);
			
			// Read replied message from socket
			byte[] inByteString = new byte[500] ;
			int numOfBytes = socket.getInputStream().read(inByteString);
			dataReceived = new String(inByteString, 0, numOfBytes, "UTF-8");
			
			// Close connection
			socket.close();
			Thread.sleep(500);
		} catch (IOException e) {
			dataReceived = "No fuel sensor active.";
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		busy = false;
		return dataReceived;
	}
}
