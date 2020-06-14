import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import ontologies.AddAgent;
import ontologies.AddAgentConcept;
import ontologies.SystemOnto;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;

public class StartupGUI extends Agent {
	
	private int windowX = 600;
	private int windowY = 200;
	private String title = "Startup Dashboard";
	
	private static int startupFarmCount;
	private static int startupTractorCount; 
	
	private static JLabel farmQ;
	private static JLabel tractorQ;
	protected static JTextField farmInput;
	protected static JTextField tractorInput;

	private Codec xmlCodec = new XMLCodec();
	private Ontology ontology = SystemOnto.getInstance();
	
	protected void setup() {
		// Register language and ontology
		getContentManager().registerLanguage(xmlCodec);
		getContentManager().registerOntology(ontology);
		
		JFrame startupFrame = new JFrame(title);
		
		// Text asking for tractor and farm inputs
		farmQ = new JLabel("How many farms are active?");
		farmQ.setBounds(50, 0, 300, 100);		
		tractorQ = new JLabel("How many tractors are active on your farms?");
		tractorQ.setBounds(50, 50, 300, 100);
		
		// Input field for farm numbers
		farmInput = new JTextField();
		farmInput.setBounds(50, 60, 100, 20);
		farmInput.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if(farmInput.getText() == "" ) {
					startupFarmCount = 0;
					farmInput.setText("0");
				} else {
					try {
						startupFarmCount = Integer.parseInt(farmInput.getText());
					} catch (NumberFormatException fiErr) {
						System.out.println("Error reading farm inputs.");
					}
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {}
		});
		
		// Input field for tractor numbers
		tractorInput = new JTextField();
		tractorInput.setBounds(50, 110, 100, 20);
		tractorInput.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if(tractorInput.getText() == "" ) {
					startupTractorCount = 0;
					tractorInput.setText("0");
				} else {
					try {
						startupTractorCount = Integer.parseInt(tractorInput.getText());
					} catch (NumberFormatException tiErr) {
						System.out.println("Error reading tractor inputs.");
					}
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {}	
		});
		
		// Button to deploy the user-described digital shadow model
		JButton d = new JButton("Deploy");
		d.setBounds(windowX-140, windowY-90, 100, 30);
		d.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DeployAction();
				startupFrame.dispose();
				CreateAgentMessage("PG", "ProgramGUI");
				doDelete();
			}
		});
		
		// Set enter key to deploy the user-described digital shadow model
		KeyListener deployListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar()==KeyEvent.VK_ENTER) {
					DeployAction();
					startupFrame.dispose();
					CreateAgentMessage("PG", "ProgramGUI");
					doDelete();
				} else {}
			}
			
			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {}
		};
		
		tractorInput.addFocusListener(new FocusListener(){
	        @Override
	        public void focusGained(FocusEvent e){
				tractorInput.setForeground(Color.black);
	            tractorInput.setText("");
	        }
	
			@Override
			public void focusLost(FocusEvent e) {}
	    });
		
		farmInput.addFocusListener(new FocusListener(){
	        @Override
	        public void focusGained(FocusEvent e){
				farmInput.setForeground(Color.black);
	            farmInput.setText("");
	        }
	
			@Override
			public void focusLost(FocusEvent e) {}
	    });
		
		startupFrame.add(d);
		startupFrame.add(farmQ);
		startupFrame.add(tractorQ);
		startupFrame.add(farmInput);
		startupFrame.add(tractorInput);
		
		startupFrame.setSize(windowX, windowY);
		startupFrame.setLayout(null);
		startupFrame.setVisible(true);
		startupFrame.setLocationRelativeTo(null); // Set location to middle on screen
		startupFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	// Iterate through user-specified number of farms and tractors to create them
	private void DeployAction() {
		if((startupTractorCount > 0) & (startupFarmCount > 0)) {
			int i = 0;
			while(i < startupFarmCount) {
				String farmName = "L" + (i+1);
				System.out.println("Creating " + farmName);
				CreateAgentMessage(farmName, "LocationAgent");
				i++;
			}
			
			int j = 0;
			while(j < startupTractorCount) {
				String tractorName = "T" + (j+1);
				CreateAgentMessage(tractorName, "TractorAgent");
				j++;
			}	
		} else if((startupTractorCount <= 0) & (startupFarmCount <= 0)) {
			tractorInput.setForeground(Color.red);
			tractorInput.setText("Invalid Entry!");
			farmInput.setForeground(Color.red);
			farmInput.setText("Invalid Entry!");
		} else if(startupTractorCount <= 0) {
			tractorInput.setForeground(Color.red);
			tractorInput.setText("Invalid Entry!");
		} else if(startupFarmCount <= 0) {
			farmInput.setForeground(Color.red);
			farmInput.setText("Invalid Entry!");
		}
	}
	
	// Send create agent message to the program controller
	private void CreateAgentMessage(String name, String type) {
		AddAgent aa = new AddAgent();
		AddAgentConcept ac = new AddAgentConcept();
		ac.setName(name);
		ac.setType(type);
		
		aa.setAgent(ac);
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(xmlCodec.getName()); 
		msg.setOntology(ontology.getName());
		msg.addReceiver(new AID("PC", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
		
		try {
			getContentManager().fillContent(msg, aa);
		} catch(CodecException | OntologyException e) {
			e.printStackTrace();
		}
		System.out.println(msg);
		
		addBehaviour(new AchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {}
			
			protected void handleRefuse(ACLMessage refuse) {}
			
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					System.out.println("Responder does not exist");
				} else {}
			}
		});
	}
}
