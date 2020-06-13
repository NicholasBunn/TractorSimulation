import jade.core.Agent;
import jade.core.behaviours.ActionExecutor;
import jade.core.behaviours.OutcomeManager;
import jade.core.behaviours.TickerBehaviour;
import jade.content.ContentElement;
import jade.content.lang.*;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
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
import jade.domain.JADEAgentManagement.KillAgent;

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
	
	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = TractorOnto.getInstance();
	
	private String myID;
	private String myName;
	private String currentConsumption = "No value available yet";
	private String currentFarm = "No farm information available yet";
	private String currentLocation = "No location available yet";
	
	private boolean first = false;
	private int nResponders;
	
	protected void setup() {
		// Register Service
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
		
		// Firstly, create a file for this tractor agent to write to if a file does not already exist.
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("T");
		String fileName = "Tractor" + no[1] + ".csv";
		File tractorFile = new File(fileName);
		
		try {
			if(tractorFile.createNewFile()) {
				System.out.println("File created: " + tractorFile.getName());
			} else {
				System.out.println("File already exists.");
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				return null;
			}
			
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				RetrieveData rd = new RetrieveData();
				rd.setId(myID);
				rd.setName(myName);
				rd.setConsumption(currentConsumption);
				rd.setFarmNumber(currentFarm);
				rd.setFarmLocation(currentLocation);

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
		
		// Ticker behaviour, set to execute every second. The methods executed in this behaviour 
		// firstly update the data and then write the current values as well as the time stamp
		// to the respective file.
		addBehaviour(new TickerBehaviour(this, 7000) { // SHOULD THIS BE IN SETUP()?
			@Override
			protected void onTick() {
					SendRequest();
				try {
					SendCFP();
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				WriteToFile(tractorFile);
			}
		} );
	}
	
	protected void takeDown() 
    {
       try { 
    	   // De-register this service from the DF
    	   DFService.deregister(this);
       } catch(Exception e) {}
    }
	
	// Function/method to request consumption data for tractor.
	private void SendRequest() {
		// Create 
		PerformRequests pr = new PerformRequests();
		Tractor tr = new Tractor();
		
		String instanceNumber = getLocalName();
		String[] no = instanceNumber.split("T");
		String fuelSensor = "F" + no[1];
		
		tr.setId(no[1]);
		tr.setName("Tractor " + no[1]);
		pr.setTractorId(no[1]); // Should I be passing an object here? 
		
		// Fill the REQUEST message
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(xmlCodec.getName()); 
		msg.setOntology(ontology.getName());
		msg.addReceiver(new AID((String) fuelSensor, AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + replyBy)); // We want to receive a reply within 10 secs
		try {
			getContentManager().fillContent(msg, pr);
		} catch (CodecException | OntologyException e) {
			System.out.println("Error filling content for fuel message.");
			e.printStackTrace();
		}
				
		addBehaviour(new AchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {
//				System.out.println("Agent " + getLocalName() + ": " + "Agent "+inform.getSender().getName() + " successfully performed the requested action" + " with the result: " + inform.getContent());
				ContentElement content;
				try {
					content = getContentManager().extractContent(inform);
					PerformRequests pr = (PerformRequests) content;
					currentConsumption = pr.getConsumption();
//					System.out.println("Consumption for " + getLocalName() + ": " + currentConsumption);
				} catch (CodecException | OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				currentConsumption = inform.getContent();
//				System.out.println("Agent " +  getLocalName() + ": " + "Response received from " + ". " + inform.getSender().getName() + ". " + "Consumption for Tractor: " + inform.getContent());
			}
			protected void handleRefuse(ACLMessage refuse) {
//				System.out.println("Agent " + getLocalName() + ": " + "Agent "+refuse.getSender().getName()+" refused to perform the requested action");
			}
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
					}
				else {
//					System.out.println("Agent " + getLocalName() + ": " + "Agent "+failure.getSender().getName()+" failed to perform the requested action");
					}
				}
			} );
	}
	

	private void SendCFP() throws FIPAException {
		PerformCFP pc = new PerformCFP();
		Tractor tr = new Tractor();
		//String instanceNumber = getLocalName();
		//String[] no = instanceNumber.split("T");
		//String locSensor = "L" + no[1];
		
		String tractorNo = getLocalName();
		String tracNo[] = tractorNo.split("T");
		tractorNo = tracNo[1];
		
		tr.setId(tractorNo);
		tr.setName("Tractor " + tractorNo);
		pc.setTractorId(tractorNo);
		
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("LocationFetcher");
        dfd.addServices(sd);
        
		DFAgentDescription[] DFResult = DFService.search(this, dfd);
		
	  	if (DFResult != null && DFResult.length > 0) {
//	  		System.out.println("Trying to receive farm information from one out of " + nResponders + " responders.");
	  		
	  		// Fill the CFP message
	  		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
	  		for (int i = 0; i < DFResult.length; ++i) {
	  			String DFString = DFResult[i].getName().getName();
	  			String[] DFName = DFString.split("@");
	  			msg.addReceiver(new AID((String) DFName[0], AID.ISLOCALNAME));
	  		}
	  		//msg.addReceiver(new AID((String) locSensor, AID.ISLOCALNAME));
	  		msg.setLanguage(xmlCodec.getName()); 
			msg.setOntology(ontology.getName());
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
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
							
							try {
								PerformCFP pc = (PerformCFP) getContentManager().extractContent(msg);
								int proposal = Integer.parseInt(pc.getTimeStamp());
//								System.out.println("Received proposal " + proposal);
//								System.out.println("Consumption for " + getLocalName() + ": " + currentConsumption);
								if (proposal > bestProposal) {
									bestProposal = proposal;
									bestProposer = msg.getSender();
//									System.out.println("Tractor " + getLocalName() + " accepting proposal from " + bestProposer);
									accept = reply;
								}
							} catch (CodecException | OntologyException err) {
								// TODO Auto-generated catch block
								err.printStackTrace();
							}							
						}
					}
					// Accept the proposal of the best proposer
					if (accept != null) {
//						System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
						accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					}						
				}
				
				protected void handleInform(ACLMessage inform) {
//					System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
					PerformCFP pc;
					try {
						pc = (PerformCFP) getContentManager().extractContent(inform);
						currentLocation = pc.getFarmLocation();
						currentFarm = pc.getFarmNumber();
//						System.out.println(currentLocation);
					} catch (CodecException | OntologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				protected void handlePropose(ACLMessage propose, Vector v) {
//					System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
				}
					
				protected void handleRefuse(ACLMessage refuse) {
//					System.out.println("Agent " + refuse.getSender().getName() + " refused");
				}
				
				protected void handleFailure(ACLMessage failure) {
					if (failure.getSender().equals(myAgent.getAMS())) {
						// FAILURE notification from the JADE runtime: the receiver
						// does not exist
						System.out.println("Responder does not exist");
					}
					else {
						System.out.println("Agent " + failure.getSender().getName() + " failed");
					}
					// Immediate failure --> we will not receive a response from this agent
					nResponders--;
				}
				
			} );
	 	}
	 	else {
	 		System.out.println("No responder specified.");
	 	}
	}
	
	//Function to write values to file
	private void WriteToFile(File tracFile) {
		try {
			// create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(tracFile, true); 
	  
	        // create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        if (first != true) {
	        	// adding header to csv 
		        String[] header = { "Time", "Consumption", "Farm", "Location" }; 
		        writer.writeNext(header); 
		        first = true;
	        } else {
		        // set date and times
		        Calendar c1 = Calendar.getInstance(); // creating a Calendar object 
		  
		        // creating a date object with specified time. 
		        Date dateTime = c1.getTime(); 
		        
		        // converting date object to string
		        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
	            String strDateTime = dateFormat.format(dateTime);  
            
	        	// add data to csv 
	        	String[] data1 = { strDateTime, currentConsumption, currentFarm, currentLocation }; 
	        	writer.writeNext(data1); 
	        }
	        // closing writer connection 
	        writer.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}