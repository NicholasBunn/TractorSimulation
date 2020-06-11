//import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.net.Socket;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.Agent;
//import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import ontologies.PerformRequests;
import ontologies.Tractor;
import ontologies.TractorOnto;
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
	private Ontology ontology = TractorOnto.getInstance();
	
	public void setup() {
		// Register Service
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
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
//				System.out.println("Agent " + getLocalName() + ": Request received from " + request.getSender().getName() + ". " + "Action is "+request.getContent());
				try {
					ContentElement content = getContentManager().extractContent(request);
					PerformRequests pr = (PerformRequests) content;
					mySens = pr.getTractorId();
//					System.out.println("Fuel agent id received: " + mySens);
				} catch (CodecException | OntologyException e) {
					// TODO Auto-generated catch block
					System.out.println("Error extracting content for fuel message.");
					e.printStackTrace();
				}
				
				if (!busy) {
					// We agree to perform the action. Note that in the FIPA-Request
					// protocol the AGREE message is optional. Return null if you
					// don't want to send it.
//					System.out.println("Agent "+getLocalName()+": Agree");
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				}
				else {
					// We refuse to perform the action
					System.out.println("Agent " + getLocalName() + ": Refuse");
					throw new RefuseException("check-failed");
				}
			}
			
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				PerformRequests pr = new PerformRequests();
				String returnString = FetchConsumption(mySens);
				if (returnString != null) {
//					System.out.println("Agent " + getLocalName() + ": " + "Action successfully performed");
					ACLMessage inform = request.createReply();
					inform.setLanguage(xmlCodec.getName());
					inform.setOntology(ontology.getName());
					inform.setPerformative(ACLMessage.INFORM);
//					inform.setContent(returnString);
					pr.setTractorId(mySens);
					pr.setConsumption(returnString);
					try {
						getContentManager().fillContent(inform, pr);
					} catch (CodecException | OntologyException e) {
						System.out.println("Error filling content for fuel message.");
						e.printStackTrace();
					}
					return inform;
				}
				else {
//					System.out.println("Agent " + getLocalName() + ": Action failed");
					throw new FailureException("unexpected-error");
				}	
			}
		} );
	}
	
	protected void takeDown() 
    {
       try { 
    	   DFService.deregister(this); 
    	   
       } catch (Exception e) {}
    }
	
	private String FetchConsumption(String myId) { // MUST TAKE PORT NUMBER AS INPUT?
//		String instanceNumber = getLocalName();
//		String[] no = instanceNumber.split("F");
		final String fuelPortNumber = "900" + myId; 
		
		busy = true;
		  String dataReceived = null;
		  DataOutputStream outToServer;
			
//		  System.out.println("Agent " + getLocalName() + ": " + "Client started");
	
		  try { 

			  String userString = "request";
			  //System.out.println("Sending " + userString + " over port " + Port);
			  
			  //setup socket
			  Socket socket = new Socket("localhost", Integer.parseInt(fuelPortNumber));
			  
			  //send message over socket
			  outToServer = new DataOutputStream(socket.getOutputStream());
			  byte[] outByteString = userString.getBytes("UTF-8");
			  outToServer.write(outByteString);
			  
			  //read replied message from socket
			  byte[] inByteString = new byte[500] ;
			  int numOfBytes = socket.getInputStream().read(inByteString);
			  dataReceived = new String(inByteString, 0, numOfBytes, "UTF-8");
			  //System.out.println("Received: " + dataReceived);
			  
			  //close connection
			  socket.close();
			  Thread.sleep(500);
		  }
		  catch (IOException e) {
			  e.printStackTrace();
		  } catch (InterruptedException e) {
			  e.printStackTrace();
		  }
		  
		  busy = false;
		  return dataReceived;
	}
}

