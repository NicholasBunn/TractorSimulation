import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

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
import ontologies.RemoveAgent;
import ontologies.SystemOnto;
import ontologies.TractorOnto;

public class ProgramGUI extends Agent {
	
	private Object tractorItem;
	private Object farmItem;
	private int windowX = 600;
	private int windowY = 200;
	private String title = "TEST";
	
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
			            System.out.println(tractorItem + " Selected");
					}
				}
				
				if (event.getStateChange() == ItemEvent.DESELECTED) {
					System.out.println(tractorItem + "Deselected");
					// End display of current information
				}
			}
		});
				
		JButton at = new JButton("Add Tractor");
		at.setBounds(10, 45, 130, 25);
		at.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send request to PC to create tractor
			}			
		});
			
		JButton rt = new JButton("Remove Tractor");
		rt.setBounds(10, 80, 130, 25);
		rt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send request to PC to kill current tractor agent
				// PC should take care of killing the corresponding fuel agent too
				// Prompt with an "Are you sure you would like to kill Tractor ...?"
				int killCheck = JOptionPane.showConfirmDialog(null, "Are you sure you would like to remove tractor " + tractorItem + "?");
		        // 0=yes, 1=no, 2=cancel
				if (killCheck == 0) {
					KillAgentMessage((String) tractorItem, "TractorAgent");
					tractorList.removeItem(tractorItem);
				} else {}
			}			
		});
						
		JLabel farmtractorQuery = new JLabel("Active Farms:");
		farmtractorQuery.setBounds(300, -30, 300, 100);	
		
		JTextArea farmArea = new JTextArea();  
	    farmArea.setBounds(300, 45, 82, 90);	
		
		JButton af = new JButton("Add Farm");
		af.setBounds(415, 45, 130, 25);
		af.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				farmArea.append("F1\n");
				// Send a message to the PC to create a new farm
			}			
		});
		
		JComboBox<String> farmList = new JComboBox<String>();
		farmList.setBounds(415, 10, 90, 20);
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
		
		JButton rf = new JButton("Remove Farm");
		rf.setBounds(415, 80, 130, 25);
		rf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send a message to the PC to kill the selected farm
				int killCheck = JOptionPane.showConfirmDialog(null, "Are you sure you would like to remove farm " + farmItem + "?");
		        // 0=yes, 1=no, 2=cancel
				if (killCheck == 0) {
					KillAgentMessage((String) farmItem, null);
					farmList.removeItem(farmItem);
				} else {}
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
			  						System.out.println("Tractor: " + sd.getName() + " found.");
			  						tractorList.addItem(sd.getName());
			  					} 
//				  					else if (sd.get) {
//				  						
//				  					} 
			  					else if (sd.getType().equals("LocationFetcher")) {
			  						System.out.println("Farm: " + sd.getName() + " found.");
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
			
		programFrame.add(farmList);
		programFrame.add(rf);
		programFrame.add(af);
		programFrame.add(farmtractorQuery);
//				programFrame.add(farmList);
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
}
