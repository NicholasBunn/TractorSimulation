import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.Agent;
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
import jade.proto.ContractNetResponder;
import ontologies.LocationRequest;
import ontologies.PerformCFP;
import ontologies.SystemOnto;

public class LocationAgent extends Agent {	
	private String tractorNo;
	private String tractorPosition = null;;
	private String locPortNumber = null;
	private String tractorNumber = "0";
	private String farmNumber = null;
	private String myTime = null;
	
	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = SystemOnto.getInstance();
	
	public void setup() {
		// Register Service with DF
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
				
		// Set Agent name and port number
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
					
					ContentElement content;
					try {
						content = getContentManager().extractContent(cfp);
						PerformCFP pr = (PerformCFP) content;
						LocationRequest lr = pr.getTractorId();
						tractorNo = lr.getTractorId();
					} catch (CodecException | OntologyException e) {
						System.out.println("Error extracting content for location message request.");
						e.printStackTrace();
					}
					
					String proposal = LastTime(Integer.parseInt(tractorNo)); // Return last time tractor was on farm
					
					// If a proposal exists, send it to the relevant tractor agent
					if (proposal != null) {
						ACLMessage propose = cfp.createReply();
						propose.setPerformative(ACLMessage.PROPOSE);
						
						PerformCFP pc = new PerformCFP();
						LocationRequest lr = new LocationRequest();
						
						lr.setTractorId(tractorNo);
						lr.setTimeStamp(proposal);
						lr.setTractorFarm(farmNumber);
						
						pc.setTractorId(lr);
						
						try {
							getContentManager().fillContent(propose, pc);
						} catch (CodecException | OntologyException e) {
							System.out.println("Error filling content for proposal message.");
							e.printStackTrace();
						}
						
						return propose;
					} else {
						// We refuse to provide a proposal
						throw new RefuseException("evaluation-failed");
					}
				}
				
				protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
						ACLMessage inform = accept.createReply();
						inform.setPerformative(ACLMessage.INFORM);

						PerformCFP pc = new PerformCFP();
						LocationRequest lr = new LocationRequest();
						
						lr.setTractorLocation(tractorPosition);
						lr.setTractorFarm(farmNumber);
						lr.setTimeStamp(myTime);
						lr.setTractorId(tractorNumber);
						
						pc.setTractorId(lr);
												
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
	
	@Override
	protected void takeDown() 
    {
       try { 
    	   DFService.deregister(this); 
       } catch (Exception e) {}
    }
		
	// Fetch the most recent time the requested tractor was picked up
	private String LastTime(int myTract) {
		String currentData = null;
		String sensor = null;
		String newestTime = "0";
		
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("L");
		
		// Iterate through the different locations on the requested farm for the most recent tractor available at the relevant location
		for(int j = 1; j < 3; j++) {
			  for(int i = 1; i < 4; i++) {
					sensor = "farm" + no[1] + "_p" + Integer.toString(j) + Integer.toString(i);
					
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
					
					// Check that the available information is valid
					if((!"No location sensors avaialable on this farm.".equals(currentData)) && (!"none".equals(dataArr[2])) && (!"none".equals(dataArr[3]))) {
						String[] farm = dataArr[0].split("m");
						
						String[] pos = dataArr[1].split("p");
						
						String[] tractor = dataArr[2].split("r");
						
						myTime = dataArr[3];
						
						if(Integer.parseInt(myTime) > Integer.parseInt(newestTime)) {
							newestTime = myTime;
							tractorNumber = tractor[2];
							tractorPosition = pos[1];
						}
					} 
			  }
		}
		
		// If the most recent available tractor is the queried tractor, return the newest time as the proposal
		if(Integer.parseInt(tractorNumber) == myTract) {
//			System.out.println(newestTime + " T" + (String) tractorNumber + " F" + farmNumber + tractorPosition);
			return newestTime;
		} else {
			return null;
		}
    }
	
	// Fetch the most recent location
	private String FetchLocation(String farmCode) throws NumberFormatException, UnknownHostException, IOException {
		String dataReceived = null;
		DataOutputStream outToServer;
		
		try {
			// Setup socket
			Socket socket = new Socket("localhost", Integer.parseInt(locPortNumber));
						  
			// Send message over socket
			outToServer = new DataOutputStream(socket.getOutputStream());
			byte[] outByteString1 = farmCode.getBytes("UTF-8");
			outToServer.write(outByteString1);
			
			// Read replied message from socket
			byte[] inByteString = new byte[500] ;
			int numOfBytes = socket.getInputStream().read(inByteString);
			dataReceived = new String(inByteString, 0, numOfBytes, "UTF-8");
						  						  
			// Close connection
			socket.close();
			Thread.sleep(500);
		} catch (IOException e) {
			dataReceived = "No location sensors avaialable on this farm.";
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return dataReceived;
	}
}
