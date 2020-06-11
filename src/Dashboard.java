import java.awt.Color;
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

import jade.core.AID;
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
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.util.leap.ArrayList;

public class Dashboard extends Agent {
	// Startup variables
	private int startupFarmCount;
	private int startupTractorCount;
	
	private static JLabel farmQ;
	private static JLabel tractorQ;
	protected static JTextField farmInput;
	protected static JTextField tractorInput;
	
	// Program variables
	private ArrayList tractAct  = new ArrayList();
	private int tractorHis;
	private ArrayList farmAct = new ArrayList();
	private int farmHis;
	private Object tractorItem;
	private Object farmItem;
	private String fuelItem;
	
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
				if(farmInput.getText() == "" ) {
					startupFarmCount = 0;
				} else {
					try {
						startupFarmCount = Integer.parseInt(farmInput.getText());
	//					System.out.println("Farms: " + farmCount);
					} catch (NumberFormatException fiErr) {
						fiErr.printStackTrace();
					}
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
				if(tractorInput.getText() == "" ) {
					startupTractorCount = 0;
				} else {
					try {
						startupTractorCount = Integer.parseInt(tractorInput.getText());
	//					System.out.println("Tractors: " + tractorCount);
					} catch (NumberFormatException tiErr) {
						tiErr.printStackTrace();
					}
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
				if((startupTractorCount > 0) & (startupFarmCount > 0)) {
					int i = 0;
					while(i < startupFarmCount) {
						String farmName = "L" + (i+1);
						CreateAgent(farmName, "LocationAgent");
						farmAct.add(farmName);
						i++;
					}
					
					int j = 0;
					while(j < startupTractorCount) {
						String tractorName = "T" + (j+1);
						CreateAgent(tractorName, "TractorAgent");
						tractAct.add(tractorName);
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
		});
		
		tractorInput.addFocusListener(new FocusListener(){
	        @Override
	        public void focusGained(FocusEvent e){
				tractorInput.setForeground(Color.black);
	            tractorInput.setText("");
	        }

			@Override
			public void focusLost(FocusEvent e) {
			}
	    });
		
		farmInput.addFocusListener(new FocusListener(){
	        @Override
	        public void focusGained(FocusEvent e){
				farmInput.setForeground(Color.black);
	            farmInput.setText("");
	        }

			@Override
			public void focusLost(FocusEvent e) {
			}
	    });
		
		// Try adding enter keylistener
		
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

		JLabel query = new JLabel("Querying Tractor:");
		query.setBounds(10, -30, 300, 100);	
		
        JComboBox<String> tractorList = new JComboBox<String>();
		tractorList.setBounds(125, 10, 90, 20);
        tractorList.addItem("None");
        
        DFAgentDescription[] DFResult = null;
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("DataAggregator");
        dfd.addServices(sd);
        
		try {
			DFResult = DFService.search(this, dfd);
			tractorHis = DFResult.length;
			if((DFResult != null) && (DFResult.length > 0)) {
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
                tractorItem = event.getItem();

                if (event.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println(tractorItem + "Selected");
                }

                if (event.getStateChange() == ItemEvent.DESELECTED) {
                	System.out.println(tractorItem + "Deselected");
                }
            }
		});
		
		
		JButton at = new JButton("Add Tractor");
		at.setBounds(10, 45, 120, 25);
		at.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tractorHis++;
				String tracName = "T" + tractorHis;
				CreateAgent(tracName, "TractorAgent");
				tractAct.add(tracName);
				tractorList.addItem(tracName);
				System.out.println(tractAct);
			}			
		});
		
		JButton rt = new JButton("Remove Tractor");
		rt.setBounds(10, 80, 120, 25);
		rt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				KillAgent(new AID((String) tractorItem, AID.ISLOCALNAME));
				
				// Kill corresponding fuel agent
				String fuelItems[] = ((String) tractorItem).split("T");
				fuelItem = "F" + fuelItems[1];
				KillAgent(new AID(fuelItem, AID.ISLOCALNAME));

				tractAct.remove(tractorItem);
				tractorList.removeItem(tractorItem);
			}			
		});
				
		JLabel farmQuery = new JLabel("Querying Farm:");
		farmQuery.setBounds(230, -30, 300, 100);	
		
        JComboBox<String> farmList = new JComboBox<String>();
		farmList.setBounds(345, 10, 90, 20);
        farmList.addItem("None");
        
        DFAgentDescription[] farmDFResult = null;
		DFAgentDescription dfdf = new DFAgentDescription();
        ServiceDescription sdf = new ServiceDescription();
        sdf.setType("LocationFetcher");
        dfdf.addServices(sdf);
        
		try {
			farmDFResult = DFService.search(this, dfd);
			farmHis = farmDFResult.length;
			if((farmDFResult != null) && (farmDFResult.length > 0)) {
				for(int i = 0; i < farmDFResult.length; i++) {
					String farmDFString = farmDFResult[i].getName().getName();
		  			String[] farmDFName = farmDFString.split("@");
		  			farmList.addItem(farmDFName[0]);
				}
			}
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		farmList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
                JComboBox farmList = (JComboBox) event.getSource();

                // The item affected by the event.
                farmItem = event.getItem();

                if (event.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println(farmItem + "Selected");
                }

                if (event.getStateChange() == ItemEvent.DESELECTED) {
                	System.out.println(farmItem + "Deselected");
                }
            }
		});
		
		
		JButton af = new JButton("Add Farm");
		af.setBounds(230, 45, 120, 25);
		af.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				farmHis++;
				String farmName = "L" + farmHis;
				CreateAgent(farmName, "LocationAgent");
				farmAct.add(farmName);
				farmList.addItem(farmName);
				System.out.println(farmAct);
			}			
		});
		
		JButton rf = new JButton("Remove Farm");
		rf.setBounds(230, 80, 120, 25);
		rf.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				KillAgent(new AID((String) farmItem, AID.ISLOCALNAME));
				farmAct.remove(farmItem);
				farmList.removeItem(farmItem);
			}			
		});
		
		mainFrame.add(rf);
		mainFrame.add(af);
		mainFrame.add(farmQuery);
		mainFrame.add(farmList);
		mainFrame.add(rt);
		mainFrame.add(at);
		mainFrame.add(query);
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

		ActionExecutor<CreateAgent, Void> aec = new ActionExecutor<CreateAgent, Void>(ca, JADEManagementOntology.getInstance(), getAMS()) {
			@Override
			public int onEnd() {
				int ret = super.onEnd();
				if (getExitCode() == OutcomeManager.OK) {
					// Creation successful
					System.out.println("Agent " + name + " successfully created");
				} else {
					// Something went wrong
					System.out.println("Agent creation error. " + getErrorMsg());
				}
				
				return ret;
			}
		};
		addBehaviour(aec);
	}
	
	public void KillAgent(AID name) {
		KillAgent ka = new KillAgent();
		ka.setAgent(name);
	
		ActionExecutor<KillAgent, Void> aek = new ActionExecutor<KillAgent, Void>(ka, JADEManagementOntology.getInstance(), getAMS()) {
			@Override
			public int onEnd() {
				int ret = super.onEnd();
				if (getExitCode() == OutcomeManager.OK) {
					// Creation successful
					System.out.println("Agent " + name + " successfully killed");
				} else {
					// Something went wrong
					System.out.println("Agent killing error. " + getErrorMsg());
				}
				
				return ret;
			}
		};
		addBehaviour(aek);
		}
}
