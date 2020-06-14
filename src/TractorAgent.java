import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.content.ContentElement;
import jade.content.lang.*;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import ontologies.*;

public class TractorAgent extends Agent {
	private int replyBy = 7000;
	
	private String faName;
	private String myID;
	private String myName;
	private String currentConsumption = "No value available yet";
	private String currentFarm = "No farm information available yet";
	private String currentLocation = "No location available yet";
	
	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = SystemOnto.getInstance();
	
	private boolean first = false;
	private int nResponders;
	
	protected void setup() {
		// Register Service with the DF
		DFAgentDescription agentDesc = new DFAgentDescription();
		ServiceDescription serviceDesc = new ServiceDescription();
		serviceDesc.setType("DataAggregator");
		serviceDesc.setName(getLocalName());
		agentDesc.setName(getAID());
		agentDesc.addServices(serviceDesc);
		try {
			DFService.register(this, agentDesc);
			System.out.println("Tractor registered with DF");
		} catch (FIPAException e) {
			e.printStackTrace();
			System.out.print("Error registering " + getLocalName() + " to DF");
		}
		
		// Register language and ontology
		getContentManager().registerLanguage(xmlCodec);
	    getContentManager().registerOntology(JADEManagementOntology.getInstance());
	    getContentManager().registerOntology(ontology);
		
		// Firstly, create a file for this tractor agent to write to if it does not already exist.
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("T");
		String fileName = "Tractor" + no[1] + ".csv";
		File tractorFile = new File(fileName);
		
		try {
			if(tractorFile.createNewFile()) {
				System.out.println("File created: " + tractorFile.getName());
			} else {
				System.out.println("File for tractor " + no[1] +"already exists.");
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Set ID and name for this tractor instance
		myID = "T" + no[1];
		myName = "Tractor" + no[1];
		
		// Create corresponding fuel management agent
		ContainerController cc = getContainerController();
		faName = "F" + no[1];
		try {
			AgentController ac;
			ac = cc.createNewAgent(faName, "FuelAgent", null);
			ac.start();
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}		
		
		// Receive requests from the program GUI
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				return null;
			}
			
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				RetrieveData rd = new RetrieveData();
				Tractor tr = new Tractor();
				
				// Set the response information with the current tractor data
				tr.setId(myID);
				tr.setName(myName);
				tr.setConsumption(currentConsumption);
				tr.setFarmNumber(currentFarm);
				tr.setFarmLocation(currentLocation);
				rd.setTractor(tr);
				
				ACLMessage inform = request.createReply();
				inform.setLanguage(xmlCodec.getName());
				inform.setOntology(ontology.getName());
				inform.setPerformative(ACLMessage.INFORM);
				
				try {
					getContentManager().fillContent(inform, rd);
				} catch (CodecException | OntologyException e) {
					System.out.println("Error filling content for data message.");
					e.printStackTrace();
				}
				
				return inform;
			}
		} );
		
		// Ticker behaviour, set to execute every few second. The methods executed in this behaviour 
		// firstly update the data and then write the current values as well as the time stamp
		// to the respective file.
		addBehaviour(new TickerBehaviour(this, 7000) { 
			@Override
			protected void onTick() {
					SendRequest();					// Request most recent fuel consumption data
				try {
					SendCFP();						// Request most recent location of this tractor from the active farms
				} catch (FIPAException e) {
					e.printStackTrace();
				}
				WriteToFile(tractorFile);			// Write the available information to the tractors data file
			}
		} );
	}
	
	@Override
	protected void takeDown() 
    {
       try { 
    	   // De-register this service from the DF
    	   DFService.deregister(this);
       } catch(Exception e) {}
    }
	
	// Method to request consumption data for tractor
	private void SendRequest() {
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("T");
		String fuelSensor = "F" + no[1];
		
		PerformRequests pr = new PerformRequests();
		FuelRequest fr = new FuelRequest();
		
		fr.setTractorId(no[1]);
		pr.setFuelId(fr);
		
		// Fill the REQUEST message
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(xmlCodec.getName()); 
		msg.setOntology(ontology.getName());
		msg.addReceiver(new AID((String) fuelSensor, AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + replyBy));
		
		try {
			getContentManager().fillContent(msg, pr);
		} catch (CodecException | OntologyException e) {
			System.out.println("Error filling content for fuel reply.");
			e.printStackTrace();
		}
				
		addBehaviour(new AchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {
				
				ContentElement content;
				try {
					content = getContentManager().extractContent(inform);
					PerformRequests pr = (PerformRequests) content;
					FuelRequest fr = pr.getFuelId();
					currentConsumption = fr.getTractorConsumption();
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
			protected void handleRefuse(ACLMessage refuse) {}
			
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					System.out.println("Responder does not exist");
				} else {}
			}
		} );
	}
	
	// Method to request proposals from the available location agents
	private void SendCFP() throws FIPAException {
		String tractorNo = getLocalName();
		String tracNo[] = tractorNo.split("T");
		tractorNo = tracNo[1];
		
		PerformCFP pc = new PerformCFP();
		LocationRequest lr = new LocationRequest();
		
		lr.setTractorId(tractorNo);
		pc.setTractorId(lr);
		
		// Collect the available location agents
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("LocationFetcher");
        dfd.addServices(sd);
        
		DFAgentDescription[] DFResult = DFService.search(this, dfd);
		
	  	if (DFResult != null && DFResult.length > 0) {	  		
	  		// Fill the CFP message
	  		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
	  		for (int i = 0; i < DFResult.length; ++i) {
	  			String DFString = DFResult[i].getName().getName();
	  			String[] DFName = DFString.split("@");
	  			msg.addReceiver(new AID((String) DFName[0], AID.ISLOCALNAME));
	  		}
	  		
	  		msg.setLanguage(xmlCodec.getName()); 
			msg.setOntology(ontology.getName());
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			msg.setReplyByDate(new Date(System.currentTimeMillis() + replyBy));
			
			try {
				getContentManager().fillContent(msg, pc);
//				System.out.println(msg);
			} catch (CodecException | OntologyException e) {
				System.out.println("Error filling content for location message.");
				e.printStackTrace();
			}
				
			addBehaviour(new ContractNetInitiator(this, msg) {
				
				protected void handleAllResponses(Vector responses, Vector acceptances) {
					if (responses.size() < nResponders) {
						// Some responder didn't reply within the specified timeout
						System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
					}
					// Evaluate proposals.
					int bestProposal = 0;
					AID bestProposer = null;
					ACLMessage accept = null;
					Enumeration e = responses.elements();
					while (e.hasMoreElements()) {
						ACLMessage msg = (ACLMessage) e.nextElement();
						if (msg.getPerformative() == ACLMessage.PROPOSE) {
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							acceptances.addElement(reply);
							
							ContentElement content;
							try {
								content = getContentManager().extractContent(msg);
								PerformCFP pCFP = (PerformCFP) content;
								LocationRequest lr = pCFP.getTractorId();
								int proposal = Integer.parseInt(lr.getTimeStamp());
								if (proposal > bestProposal) {
									bestProposal = proposal;
									bestProposer = msg.getSender();
									accept = reply;
								}
							} catch (CodecException | OntologyException err) {
								err.printStackTrace();
							}							
						}
					}
					
					// Accept the proposal of the best proposer
					if (accept != null) {
						accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					}						
				}
				
				protected void handleInform(ACLMessage inform) {
					ContentElement content;
					try {
						content = getContentManager().extractContent(inform);
						PerformCFP pCFP = (PerformCFP) content;
						LocationRequest lr = pCFP.getTractorId();
						currentLocation = lr.getTractorLocation();
						currentFarm = lr.getTractorFarm();
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					
				}
				
				protected void handlePropose(ACLMessage propose, Vector v) {}
					
				protected void handleRefuse(ACLMessage refuse) {}
				
				protected void handleFailure(ACLMessage failure) {
					if (failure.getSender().equals(myAgent.getAMS())) {
						System.out.println("Responder does not exist");
					} else {
						System.out.println("Agent " + failure.getSender().getName() + " failed");
					}
					// Immediate failure - we will not receive a response from this agent
					nResponders--; // Decrement the number of available responders
				}
				
			} );
	 	} else {
	 		System.out.println("No responder specified.");
	 	}
	}
	
	//Function to write values to file
	private void WriteToFile(File tracFile) {
		try {
			// Create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(tracFile, true); 
	  
	        // Create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        if (first != true) {
	        	// Add header to csv every time a tractor is created or re-commissioned
		        String[] header = { "Time", "Consumption", "Farm", "Location" }; 
		        writer.writeNext(header); 
		        first = true;
	        } else {
		        // Set date and times
		        Calendar c1 = Calendar.getInstance(); // Create a Calendar object 
		  
		        // Create a date object with specified time. 
		        Date dateTime = c1.getTime(); 
		        
		        // Convert date object to string
		        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
	            String strDateTime = dateFormat.format(dateTime);  
            
	        	// Add data to csv 
	        	String[] data1 = { strDateTime, currentConsumption, currentFarm, currentLocation }; 
	        	writer.writeNext(data1); 
	        }
	        
	        // Close writer connection 
	        writer.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}