import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import ontologies.AddAgent;
import ontologies.PerformRequests;
import ontologies.RemoveAgent;
import ontologies.RetrieveData;
import ontologies.SystemOnto;
import ontologies.Tractor;
import ontologies.TractorOnto;

public class ProgramGUI extends Agent {
	
	private Object tractorItem;
	private Object farmItem;
	
	private boolean tractorRemoved = false;
	private boolean farmRemoved = false;
	
	private int windowX = 600;
	private int windowY = 275;
	private int tractorCount = 0;
	private int farmCount = 0;

	private String title = "Program Dashboard";
	
	private String TractorID;
	private String tractorName;
	private String consumption;
	private String currentFarm;
	private String currentLocation;

	private Codec xmlCodec = new XMLCodec();
	private Ontology systemOntology = SystemOnto.getInstance();
	private Ontology tractorOntology = TractorOnto.getInstance();

	protected void setup() {
		
		// Register language and ontology
		getContentManager().registerLanguage(xmlCodec);
		getContentManager().registerOntology(systemOntology);
		getContentManager().registerOntology(tractorOntology);
		
		JFrame programFrame = new JFrame(title);
		
		JLabel tractorQuery = new JLabel("Querying Tractor:");
		tractorQuery.setBounds(10, -30, 300, 100);	
		
		JTextArea infoArea=new JTextArea();  
	    infoArea.setBounds(86, 115, 130, 100); 
	    
		JComboBox<String> tractorList = new JComboBox<String>();
		tractorList.setBounds(125, 10, 90, 20);
		tractorList.addItem("None");

		DFAgentDescription[] DFResult = null;
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("DataAggregator");
		dfd.addServices(sd);
				
		// Identify which tractor is currently being queried
		tractorList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				JComboBox tractorList = (JComboBox) event.getSource();
				
				// The item affected by the event.
				tractorItem = event.getItem();

				if (event.getStateChange() == ItemEvent.SELECTED) {
		            if (event.getItem() != "None") {
		            	infoArea.selectAll();
		            	infoArea.replaceSelection("");
			            System.out.println(tractorItem + " Selected");
			            SendRequestMessage((String) tractorItem);
			            try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            infoArea.append("Tractor: " + TractorID + "\n");
			            infoArea.append("Fuel Consumption: " + consumption + "\n");
			            infoArea.append("Current Farm: " + currentFarm + "\n");
			            infoArea.append("Current Location: " + currentLocation + "\n");
					}
				}
				
//				if (event.getStateChange() == ItemEvent.DESELECTED) {
//					System.out.println(tractorItem + "Deselected");
//					// End display of current information
//				}
			}
		});
				
		JButton at = new JButton("Add Tractor");
		at.setBounds(86, 45, 130, 25);
		at.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send request to PC to create tractor
				if (tractorRemoved) {
	                ImageIcon tractorIcon = new ImageIcon(ProgramGUI.class.getResource("/TractorIcon.png"));
					int reLaunchCheck = JOptionPane.showConfirmDialog(null, "Are you re-commissioning an existing tractor?", "Tractor Re-Commission Check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, tractorIcon);
					// 0=yes, 1=no, 2=cancel
					String tractorName = new String();
					if (reLaunchCheck == 0) {
						tractorName = (String) JOptionPane.showInputDialog(null, "What is the ID of the tractor being re-commissioned? [T_]", "Tractor ID Query", JOptionPane.QUESTION_MESSAGE, tractorIcon, null, null);
						System.out.println(tractorName);
						tractorCount--;
						CreateAgentMessage(tractorName, "TractorAgent");
					} else {
						tractorCount++;
						tractorName = "T" + Integer.toString(tractorCount);
						tractorCount--;
						CreateAgentMessage(tractorName, "TractorAgent");
					}
				} else {
					tractorCount++;
					String tractorName = "T" + Integer.toString(tractorCount);
					tractorCount--;
					CreateAgentMessage(tractorName, "TractorAgent");
				}
				
			}			
		});
			
		JButton rt = new JButton("Remove Tractor");
		rt.setBounds(86, 80, 130, 25);
		rt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send request to PC to kill current tractor agent
				// PC should take care of killing the corresponding fuel agent too
				// Prompt with an "Are you sure you would like to kill Tractor ...?"
				if (tractorItem != null) {
	                ImageIcon tractorIcon = new ImageIcon(ProgramGUI.class.getResource("/TractorIcon.png"));
					int killCheck = JOptionPane.showConfirmDialog(null, "Are you sure you would like to remove tractor " + tractorItem + "?", "Confirm Tractor Removal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, tractorIcon);
			        // 0=yes, 1=no, 2=cancel
					if (killCheck == 0) {
						KillAgentMessage((String) tractorItem, "TractorAgent");
						tractorRemoved = true;
						tractorList.removeItem(tractorItem);
					} else {}
				} else {
					JOptionPane.showMessageDialog(null, "Please select a tractor to remove.");
				}
				
			}			
		});
						
		JLabel farmQuery = new JLabel("Active Farms:");
		farmQuery.setBounds(360, -30, 300, 100);	
		
	    JComboBox<String> farmList = new JComboBox<String>();
		farmList.setBounds(475, 10, 90, 20);
        farmList.addItem("None");
        
		// Identify which farm is currently being queried
		farmList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				// The item affected by the event.
				if (event.getStateChange() == ItemEvent.SELECTED) {
					if (event.getItem() != "None") {
						farmItem = event.getItem();
			            System.out.println(farmItem + " Selected");
					}
		        }
			}
		});
		
		JButton af = new JButton("Add Farm");
		af.setBounds(415, 45, 130, 25);
		af.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send a message to the PC to create a new farm
				if (farmRemoved) {
	                ImageIcon farmIcon = new ImageIcon(ProgramGUI.class.getResource("/FarmIcon.png"));
					int reLaunchCheck2 = JOptionPane.showConfirmDialog(null, "Are you re-commissioning an existing farm?", "Farm Re-Commission Check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, farmIcon);
					// 0=yes, 1=no, 2=cancel
					String farmName = new String();
					if (reLaunchCheck2 == 0) {
						farmName = (String) JOptionPane.showInputDialog(null, "What is the ID of the farm being re-commissioned? [L_]", "Farm ID Query", JOptionPane.QUESTION_MESSAGE, farmIcon, null, null);
						farmCount--;
						CreateAgentMessage(farmName, "LocationAgent");
					} else {
						farmCount++;
						farmName = "L" + Integer.toString(farmCount);
						farmCount--;
						CreateAgentMessage(farmName, "LocationAgent");
					}
				} else {
					farmCount++;
					String farmName = "L" + Integer.toString(farmCount);
					farmCount--;
					CreateAgentMessage(farmName, "LocationAgent");
				}
			}			
		});
				
		JButton rf = new JButton("Remove Farm");
		rf.setBounds(415, 80, 130, 25);
		rf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send a message to the PC to kill the selected farm
				if (farmItem != null) {

	                ImageIcon farmIcon = new ImageIcon(ProgramGUI.class.getResource("/FarmIcon.png"));
					int killCheck = JOptionPane.showConfirmDialog(null, "Are you sure you would like to remove farm " + farmItem + "?", "Confirm Farm Removal", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, farmIcon);
			        // 0=yes, 1=no, 2=cancel
					if (killCheck == 0) {
						KillAgentMessage((String) farmItem, null);
						farmRemoved = true;
						farmList.removeItem(farmItem);
					} else {}
				} else {
					JOptionPane.showMessageDialog(null, "Please select a farm to remove.");
				}
			}			
		});
			
		// Subscribe to the DF to be notified whenever an agent is created or killed
		DFAgentDescription tractorTemplate = new DFAgentDescription();
		ServiceDescription tractorTemplateSd = new ServiceDescription();
		tractorTemplate.addServices(tractorTemplateSd);
		
		// STILL NEED TO DISTINGUISH BETWEEN REGISTER AND DEREGISTER!
		addBehaviour(new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), tractorTemplate, null)) {
			protected void handleInform(ACLMessage inform) {
//		 		System.out.println("Agent " + getLocalName() + ": Notification received from DF");
		 		try {
		 			DFAgentDescription[] tractorResults = DFService.decodeNotification(inform.getContent());
//		 			System.out.println(inform.getContent());
					if (tractorResults.length > 0) {
					 	for (int i = 0; i < tractorResults.length; ++i) {
			  				DFAgentDescription dfd = tractorResults[i];
			  				AID provider = dfd.getName();
			  				Iterator it = dfd.getAllServices();
			  				while (it.hasNext()) {
			  					ServiceDescription sd = (ServiceDescription) it.next();
			  					if ((sd.getType().equals("DataAggregator"))) {
//			  						System.out.println("Tractor: " + sd.getName() + " found.");
			  						tractorCount++;
//			  						System.out.println("Total tractor count: " + tractorCount);
			  						tractorList.addItem(sd.getName());
			  					} 
//				  					else if (sd.get) {
//				  						
//				  					} 
			  					else if (sd.getType().equals("LocationFetcher")) {
//			  						System.out.println("Farm: " + sd.getName() + " found.");
			  						farmCount++;
//			  						System.out.println("Total farm count: " + farmCount);
			  						farmList.addItem(sd.getName());
			  					}
			  				}
			  			}
			  		}	
		  			System.out.println();
			  	}
			  	catch (FIPAException fe) {
			  		fe.printStackTrace();
			  	}
			}
		} );
			
	    programFrame.add(infoArea);
		programFrame.add(rf);
		programFrame.add(af);
		programFrame.add(farmQuery);
		programFrame.add(farmList);
		programFrame.add(rt);
		programFrame.add(at);
		programFrame.add(tractorQuery);
		programFrame.add(tractorList);
		
		programFrame.setSize(windowX, windowY);
		programFrame.setLayout(null);
		programFrame.setVisible(true);
		programFrame.setLocationRelativeTo(null); // Set location to middle on screen
		programFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void CreateAgentMessage(String name, String type) {
		AddAgent aa = new AddAgent();
		aa.setName(name);
		aa.setType(type);
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(xmlCodec.getName()); 
		msg.setOntology(systemOntology.getName());
		msg.addReceiver(new AID("PC", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
		try {
			getContentManager().fillContent(msg, aa);
		} catch(CodecException | OntologyException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new AchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {
				
			}
			protected void handleRefuse(ACLMessage refuse) {}
			protected void handleFailure(ACLMessage failure) {
			if (failure.getSender().equals(myAgent.getAMS())) {
				// FAILURE notification from the JADE runtime: the receiver
				// does not exist
				System.out.println("Responder does not exist");
				}
			else {}
			}
		});
	}
	
	private void KillAgentMessage(String name, String type) {
		RemoveAgent rr = new RemoveAgent();
		rr.setName(name);
		if (type != null) {
			rr.setType(type);
		} else {}
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(xmlCodec.getName()); 
		msg.setOntology(systemOntology.getName());
		msg.addReceiver(new AID("PC", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
		try {
			getContentManager().fillContent(msg, rr);
		} catch(CodecException | OntologyException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new AchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {}
			protected void handleRefuse(ACLMessage refuse) {}
			protected void handleFailure(ACLMessage failure) {
			if (failure.getSender().equals(myAgent.getAMS())) {
				// FAILURE notification from the JADE runtime: the receiver
				// does not exist
				System.out.println("Responder does not exist");
				}
			else {}
			}
		});
	}
	
	private void SendRequestMessage(String tractorID) {
		
		PerformRequests pr = new PerformRequests();
		pr.setTractorId(tractorID);
		
		// Fill the REQUEST message
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(xmlCodec.getName()); 
		msg.setOntology(tractorOntology.getName());
		msg.addReceiver(new AID(tractorID, AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
//		msg.setReplyByDate(new Date(System.currentTimeMillis() + replyBy)); 
		try {
			getContentManager().fillContent(msg, pr);
		} catch (CodecException | OntologyException e) {
			System.out.println("Error filling content for request message.");
			e.printStackTrace();
		}
				
		addBehaviour(new AchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {
//				System.out.println("Agent " + getLocalName() + ": " + "Agent "+inform.getSender().getName() + " successfully performed the requested action" + " with the result: " + inform.getContent());
				ContentElement content;
				try {
					content = getContentManager().extractContent(inform);
					RetrieveData rd = (RetrieveData) content;
					TractorID = rd.getId();
					tractorName = rd.getName();
					consumption = rd.getConsumption();
					currentFarm = rd.getFarmNumber();
					currentLocation = rd.getFarmLocation();
//					System.out.println("Tractor ID: " + TractorID);
//					System.out.println("Tractor name: " + tractorName);
//					System.out.println("Tractor consumption: " + consumption);
//					System.out.println("Tractor location: " + currentFarm + " " + currentLocation);

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

}
