import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.behaviours.ActionExecutor;
import jade.core.behaviours.OutcomeManager;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;

public class Dashboard extends Agent {
	// Setup variables
	
	private int startupFarmCount;
	private int startupTractorCount;
	
	private static JLabel farmQ;
	private static JLabel tractorQ;
	protected static JTextField farmInput;
	protected static JTextField tractorInput;
	
	public void setup() {
		StartupGUI(600, 200, "Startup Dashboard");
	}
	
	public void StartupGUI(int windowX, int windowY, String title) {
		
		JFrame startupFrame = new JFrame(title);
		
		// Label for tractor and farm request text
		farmQ = new JLabel("How many active farms do you have?");
		farmQ.setBounds(50, 0, 300, 100);		
		tractorQ = new JLabel("How many tractors  on your farms?");
		tractorQ.setBounds(50, 50, 300, 100);
		
		// Text field for tractor and farm inputs
		farmInput = new JTextField();
		farmInput.setBounds(50, 60, 100, 20);
		farmInput.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					startupFarmCount = Integer.parseInt(farmInput.getText());
//					System.out.println("Farms: " + farmCount);
				} catch (NumberFormatException fiErr) {
					fiErr.printStackTrace();
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		tractorInput = new JTextField();
		tractorInput.setBounds(50, 110, 100, 20);
		tractorInput.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				try {
					startupTractorCount = Integer.parseInt(tractorInput.getText());
//					System.out.println("Tractors: " + tractorCount);
				} catch (NumberFormatException tiErr) {
					tiErr.printStackTrace();
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}	
		});
		
		// Button to deploy the user-described digital twin model
		JButton d = new JButton("Deploy");
		d.setBounds(windowX-140, windowY-90, 100, 30);
		d.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = 0;
				while(i < startupFarmCount) {
					String farmName = "L" + (i+1);
					CreateAgent(farmName, "LocationAgent");
					i++;
				}
				
				int j = 0;
				while(j < startupTractorCount) {
					String tractorName = "T" + (j+1);
					CreateAgent(tractorName, "TractorAgent");
					j++;
				}
				
				startupFrame.dispose();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ProgramGUI(600, 200, "Program Dashboard");
			}
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
	
	public void ProgramGUI(int windowX, int windowY, String title) {
		
		JFrame mainFrame = new JFrame(title);
		
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("DataAggregator");
        dfd.addServices(sd);
        
        JComboBox<String> tractorList = new JComboBox<String>();
        tractorList.addItem("None");
		tractorList.setBounds(50, 10, 90, 20);
		
        DFAgentDescription[] DFResult = null;
        
		try {
			DFResult = DFService.search(this, dfd);
			if(DFResult != null && DFResult.length > 0) {
				for(int i = 0; i < DFResult.length; i++) {
					String DFString = DFResult[i].getName().getName();
		  			String[] DFName = DFString.split("@");
		  			tractorList.addItem(DFName[0]);
				}
			}
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tractorList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
                JComboBox tractorList = (JComboBox) event.getSource();

                // The item affected by the event.
                Object item = event.getItem();

                if (event.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println(item + "Selected");
                }

                if (event.getStateChange() == ItemEvent.DESELECTED) {
                	System.out.println(item + "Deselected");
                }
            }
		});
		
		mainFrame.add(tractorList);
		
		mainFrame.setSize(windowX, windowY);
		mainFrame.setLayout(null);
		mainFrame.setVisible(true);
		mainFrame.setLocationRelativeTo(null); // Set location to middle on screen
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	

	public void CreateAgent(String name, String type) {
		
		CreateAgent ca = new CreateAgent();
		ca.setAgentName(name);
		ca.setClassName(type);
		ca.setContainer(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
		ca.setInitialCredentials(null);
		ca.setOwner(null);
		
		ActionExecutor<CreateAgent, Void> aef = new ActionExecutor<CreateAgent, Void>(ca, JADEManagementOntology.getInstance(), getAMS()) {
			@Override
			public int onEnd() {
				int ret = super.onEnd();
				if (getExitCode() == OutcomeManager.OK) {
					// Creation successful
					System.out.println("Farm " + name + " successfully created");
				} else {
					// Something went wrong
					System.out.println("Agent creation error. " + getErrorMsg());
				}
				
				return ret;
			}
		};
		addBehaviour(aef);
	}


}
