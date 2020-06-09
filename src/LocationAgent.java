import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.Vector;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import ontologies.PerformCFP;
import ontologies.PerformRequests;
import ontologies.TractorOnto;

public class LocationAgent extends Agent {
	private boolean busy = false;
	private String tractorNo;
	private String tractorPosition = null;;
	private String locPortNumber = null;
	private String tractorNumber = "0";
	private String farmNumber = null;
	private String myTime = null;
	
	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = TractorOnto.getInstance();
	
	public void setup() {
		// Register Service
		DFAgentDescription agentDesc = new DFAgentDescription();
		ServiceDescription serviceDesc = new ServiceDescription();
		serviceDesc.setType("LocationFetcher");
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
				
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("L");
		farmNumber = no[1];
		locPortNumber = "910" + no[1];
				
		System.out.println("Agent " + getLocalName() + " waiting for CFP...");
	  	MessageTemplate template = MessageTemplate.and(
	  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
	  		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
	  		
			addBehaviour(new ContractNetResponder(this, template) {
				protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
//					System.out.println("Agent " + getLocalName( ) + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());
					
					ContentElement content;
					try {
						content = getContentManager().extractContent(cfp);
						PerformCFP pc = (PerformCFP) content;
						tractorNo = pc.getTractorId();
					} catch (CodecException | OntologyException e) {
						System.out.println("Error extracting content for location message.");
						e.printStackTrace();
					}
					
					String proposal = LastTime(Integer.parseInt(tractorNo)); // return last time tractor was on farm
					if (proposal != null) {
						// We provide a proposal
//						System.out.println("Agent " + getLocalName() + ": Proposing " + proposal);
						PerformCFP pc = new PerformCFP();
						
						ACLMessage propose = cfp.createReply();
						propose.setPerformative(ACLMessage.PROPOSE);
						
						pc.setTractorId(tractorNo);
						pc.setTimeStamp(proposal);
						pc.setFarmNumber(farmNumber); 
						// THEN NEED TO CONFIRM THAT THE RETURNED MESSAGE IS RECEIVED CORRECTLY
						// ALSO NEED TO CHECK THE TYPE OF VARIABLE BEING PASSED BY THE LASTTIME FUNCTION
						// INTO PROPOSAL
						
						try {
							getContentManager().fillContent(propose, pc);
						} catch (CodecException | OntologyException e) {
							System.out.println("Error filling content for proposal message.");
							e.printStackTrace();
						}
						
						return propose;
					}
					else {
						// We refuse to provide a proposal
//						System.out.println("Agent " + getLocalName() + ": Nothing to propose");
						throw new RefuseException("evaluation-failed");
					}
				}
				
				protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
//					System.out.println("Agent " + getLocalName() + ": Proposal accepted");
//						System.out.println("Agent " + getLocalName() + ": Action successfully performed");
						ACLMessage inform = accept.createReply();
						
						PerformCFP pc = new PerformCFP();
						
						pc.setFarmLocation(tractorPosition);
						pc.setFarmNumber(farmNumber);
						pc.setTimeStamp(myTime);
						pc.setTractorId(tractorNumber);
						
						inform.setPerformative(ACLMessage.INFORM);
						
						try {
							getContentManager().fillContent(inform, pc);
						} catch (CodecException | OntologyException e) {
							System.out.println("Error filling content for inform message.");
							e.printStackTrace();
						}
						
						return inform;
				}
				
				protected void handleRejectProposal(ACLMessage reject) {
					System.out.println("Agent "+getLocalName()+": Proposal rejected");
				}
			} );
	  }
	
	protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
		
	private String LastTime(int myTract) {
		String currentData = null;
		String sensor = null;
		String newestTime = "0";
		
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("L");
		
		for(int j = 1; j < 3; j++) {
			  for(int i = 1; i < 4; i++) {
					sensor = "farm" + no[1] + "_p" + Integer.toString(j) + Integer.toString(i);
//					System.out.println("Agent " + getLocalName() + " Querying Farm: " + sensor);
					try {
						currentData = FetchLocation(sensor);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String[] dataArr = currentData.split("_");
					
					if(!"none".equals(dataArr[2]) && !"none".equals(dataArr[3])) {
						String[] farm = dataArr[0].split("m");
//						farmNumber = farm[1];
						
						String[] pos = dataArr[1].split("p");
//						tractorPosition = pos[1];
						
						String[] tractor = dataArr[2].split("r");
//						tractorNumber = tractor[2];
						
						myTime = dataArr[3];
						
//						System.out.println(farmNumber);
//						System.out.println(tractorPosition);
//						System.out.println(tractorNumber);
//						System.out.println(myTime);
						if(Integer.parseInt(myTime) > Integer.parseInt(newestTime)) {
							newestTime = myTime;
							tractorNumber = tractor[2];
							tractorPosition = pos[1];
							farmNumber = farm[1];
						}
					} 
			  }
		}
		
		if(Integer.parseInt(tractorNumber) == myTract) {
			return newestTime;
		} else {
			return null;
		}
    }
	
	private String FetchLocation(String farmCode) throws NumberFormatException, UnknownHostException, IOException {
		busy = true;
		String dataReceived = null;
		DataOutputStream outToServer;
		
		//setup socket
		Socket socket = new Socket("localhost", Integer.parseInt(locPortNumber));
					  
		//send message over socket
		outToServer = new DataOutputStream(socket.getOutputStream());
		byte[] outByteString1 = farmCode.getBytes("UTF-8");
		outToServer.write(outByteString1);
		//read replied message from socket
		byte[] inByteString = new byte[500] ;
		int numOfBytes = socket.getInputStream().read(inByteString);
		dataReceived = new String(inByteString, 0, numOfBytes, "UTF-8");
					  
		//				  System.out.println("Agent " + getLocalName() + ": FetchLocation Received: " + dataReceived);
					  
		//close connection
		socket.close();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		busy = false;
		return dataReceived;
	}

}
