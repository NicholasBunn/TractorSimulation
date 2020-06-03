
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
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
	
	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = TractorOnto.getInstance();
	
//	private String instanceNumber = getLocalName();
//	//private String instanceNumber = "T1";
//	private String[] no = instanceNumber.split("T");
//	private String fuelSensor = "F" + no[1];
//	private String locSensor = "L" + no[1];
//	private final String fuelPortNumber = "900" + no[1]; // Which port is this tractor's data accessed on?
//	private final String locPortNumber = "910" + no[1]; // Need to implement for every farm.
	private String currentConsumption = "No value available yet";
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
		} catch (FIPAException e) {
			e.printStackTrace();
			System.out.print("Error registering " + getLocalName() + " to DF");
		}
		
		// Register language and ontology
		getContentManager().registerLanguage(xmlCodec);
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
		
		// Ticker behaviour, set to execute every second. The methods executed in this behaviour 
		// firstly update the data and then write the current values as well as the time stamp
		// to the respective file.
		addBehaviour(new TickerBehaviour(this, 1000) { // SHOULD THIS BE IN SETUP()?
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
       try { DFService.deregister(this); }
       catch (Exception e) {}
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
		msg.setLanguage(xmlCodec.getName()); //NEED TO UPDATE THIS
		msg.setOntology(ontology.getName());
		msg.addReceiver(new AID((String) fuelSensor, AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000)); // We want to receive a reply within 10 secs
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
		//String instanceNumber = getLocalName();
		//String[] no = instanceNumber.split("T");
		//String locSensor = "L" + no[1];
		
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
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				// We want to receive a reply in 10 secs
				msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				msg.setContent("ontology-ish");
				
				addBehaviour(new ContractNetInitiator(this, msg) {
					
					protected void handlePropose(ACLMessage propose, Vector v) {
//						System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
					}
					
					protected void handleRefuse(ACLMessage refuse) {
//						System.out.println("Agent " + refuse.getSender().getName() + " refused");
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
								int proposal = Integer.parseInt(msg.getContent());
								if (proposal > bestProposal) {
									bestProposal = proposal;
									bestProposer = msg.getSender();
//									System.out.println("Tractor " + getLocalName() + " accepting proposal from " + bestProposer);
									accept = reply;
								}
							}
						}
						// Accept the proposal of the best proposer
						if (accept != null) {
//							System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
							accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						}						
					}
					
					protected void handleInform(ACLMessage inform) {
//						System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
						currentLocation = inform.getContent();
//						System.out.println(currentLocation);
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
		        String[] header = { "Time", "Consumption", "Location" }; 
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
	        	String[] data1 = { strDateTime, currentConsumption, currentLocation }; 
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