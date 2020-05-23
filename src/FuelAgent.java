//import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.net.Socket;

import jade.core.Agent;
//import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
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
				
		System.out.println("Agent "+getLocalName()+" waiting for requests...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
//				System.out.println("Agent " + getLocalName() + ": Request received from "+request.getSender().getName() + ". " + "Action is "+request.getContent());
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
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("check-failed");
				}
			}
			
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				String returnString = FetchConsumption();
				if (returnString != null) {
//					System.out.println("Agent " + getLocalName() + ": " + "Action successfully performed");
					ACLMessage inform = request.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					inform.setContent(returnString);
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
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
	
	private String FetchConsumption() { // MUST TAKE PORT NUMBER AS INPUT?
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("F");
		final String fuelPortNumber = "900" + no[1]; 
		
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

