import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class ControlPanel {
	
	private Therac therac;
	
	private int tphase;
	private volatile boolean dataEntryComplete;
	
	private String lastEnergy;
	
	private boolean bendingMagnet;
	private final int magnetCount = 3;
	
	private volatile byte class3;
	private volatile int f$mal;
	private volatile boolean set = false;
	
	private final Object f$malLock = new Object();
	
	// GUI
	public static final int fontSize = 20;
	
	private JFrame frame;
	private JTextField turntableTF;
	private JTextField energyTF;
	private JTextField patientTF;
	private JTextField commandTF;
	
	private JFrame monitor;
	private JTextField tphaseTF;
	private JTextField bendingTF;
	private JTextField dataTF;
	private JTextField class3TF;
	private JTextField f$malTF;
	
	public static void main(String[] args) {
		new ControlPanel(new Therac());
	}
	
	public ControlPanel(Therac t) {
		createGUI();
		
		therac = t;
		
		setTphase(1); // DATENT
		setDataEntryComplete(false);
		setClass3((byte) 1);
		
		KeyboardHandler kbh = new KeyboardHandler();
		kbh.start();
		
		Hand hand = new Hand();
		hand.start();
		
		Hkeper hkeper = new Hkeper();
		hkeper.start();
		
		monitor.setVisible(true);
		frame.setVisible(true);
		
		treat();
	}
	
	private void treat() {
		while (true) {
			switch (tphase) {
			case 1:
				datent();
				break;
			case 2:
				System.out.println("Proceed with treatment");
				therac.setActive(true);
				return;
			case 3:
				setupTest();
				break;
			default:
				return;
			}
		}
	}
	
	private void datent() {
		lastEnergy = energyTF.getText();
		// String patient = patientTF.getText();
		
		// Mode energy specified
		if (lastEnergy != null && !lastEnergy.equals("") && !energyTF.isFocusOwner()) {
			lastEnergy = energyTF.getText();
			// get params from table, we just set energy immediately
			try {
				therac.setEnergy(Integer.parseInt(lastEnergy));
			} catch (NumberFormatException e) {
				// Intentionally left blank
				// e.printStackTrace();
			}
			
			// Set other params...
			
			magnet();
			
			// Check for changes to energy, but only if there is editing
			if (!commandTF.isFocusOwner() && !lastEnergy.equals(energyTF.getText())) {
				System.err.println("Energy has changed!");
				return;	// Datent will be rescheduled by treat
			}
			
			// System.out.println("After check");
			
			if (dataEntryComplete) {
				setTphase(3); // Everything ok (really?), go to next phase
			} else {
				// if (reset) { tphase = 0}
			}
		}
	}
	
	private void magnet() {
		setBendingMagnet(true);
		for (int i = 0; i < magnetCount; i++) {
			// Set magnet
			therac.setMagnet(i);
			ptime();
			therac.finalizeMagnet(i);
			
			// Check for changes to energy, but only if there is editing
			if (!commandTF.isFocusOwner() && !lastEnergy.equals(energyTF.getText())) {
				System.err.println("Energy has changed!");
				return;	// Datent will be rescheduled by treat
			}
		}
	}
	
	private void ptime() {
		// Set 8s delay
		long stop = System.currentTimeMillis() + 8000;
		// Busy waiting as in the original code
		while (System.currentTimeMillis() < stop) {
			// Only check when flag is set
			if (bendingMagnet) {
				if (!commandTF.isFocusOwner() && !lastEnergy.equals(energyTF.getText())) {
					System.err.println("Energy has changed!");
					return; // Datent will be rescheduled by treat
				} 
			}
			
			try {
				Thread.sleep(1);	// Sleep for the sake of your CPU
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		setBendingMagnet(false); // Clear flag after FIRST execution, flag is never reset
	}
	
	private void setupTest() {
		setClass3((byte) (class3 + 1));
		// System.out.println("Class3: " + class3);
		// Give Hkeper time to use class3
		try {
			Thread.sleep(10);
			// Pause when f$mal is inconsistent, this makes it easier to meet the right time window to show the bug
			if (class3 == 0) {
				Thread.sleep(4000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Synchronize to prevent the scheduling from producing the bug
		synchronized (f$malLock) {
			if (f$mal == 0 && set) {
				setTphase(2);	// Everything should be consistent ...
			} else if (set) {
				System.err.println("Inconsistent state, rejecting set command");
				set = false;
				commandTF.setText("");
			}
		}
	}
	
	private void createGUI() {
		
		// Increase font size for presentation, taken from https://stackoverflow.com/questions/1043872/are-there-any-built-in-methods-in-java-to-increase-font-size
		UIManager.put("Label.font", new FontUIResource(new Font("Dialog", Font.PLAIN, fontSize)));
	    UIManager.put("TextField.font", new FontUIResource(new Font("Dialog", Font.PLAIN, fontSize)));
		
		int width = 400;
		int height = 200;
		frame = new JFrame("Control Panel");
		frame.setLayout(new GridLayout(4, 2));
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel tL = new JLabel("Turntable Position:");
		turntableTF = new JTextField();
		turntableTF.setSize(width/2, height/4);
		
		JLabel eL = new JLabel("Energy:");
		energyTF = new JTextField();
		energyTF.setSize(width/2, height/4);
		
		JLabel aL = new JLabel("Patient Info:");
		patientTF = new JTextField();
		patientTF.setSize(width/2, height/4);
		
		JLabel cL = new JLabel("Command:");
		commandTF = new JTextField();
		commandTF.setSize(width/2, height/4);
		
		frame.add(tL);
		frame.add(turntableTF);
		
		frame.add(eL);
		frame.add(energyTF);
		
		frame.add(aL);
		frame.add(patientTF);
		
		frame.add(cL);
		frame.add(commandTF);
		
		
		int heightM = 250;
		monitor = new JFrame("Internal Variables");
		monitor.setLayout(new GridLayout(5, 2));
		monitor.setSize(width, heightM);
		monitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel tpL = new JLabel("Tphase:");
		tphaseTF = new JTextField();
		tphaseTF.setEditable(false);
		tphaseTF.setSize(width/2, heightM/5);
		
		JLabel bL = new JLabel("Bending magnet flag:");
		bendingTF = new JTextField();
		bendingTF.setEditable(false);
		bendingTF.setSize(width/2, heightM/5);
		
		JLabel dL = new JLabel("Data entry complete:");
		dataTF = new JTextField();
		dataTF.setEditable(false);
		dataTF.setSize(width/2, heightM/5);
		
		JLabel clL = new JLabel("Class3:");
		class3TF = new JTextField();
		class3TF.setEditable(false);
		class3TF.setSize(width/2, heightM/5);
		
		JLabel fL = new JLabel("F$mal:");
		f$malTF = new JTextField();
		f$malTF.setEditable(false);
		f$malTF.setSize(width/2, heightM/5);
		
		monitor.add(tpL);
		monitor.add(tphaseTF);
		
		monitor.add(bL);
		monitor.add(bendingTF);
		
		monitor.add(dL);
		monitor.add(dataTF);
		
		monitor.add(clL);
		monitor.add(class3TF);
		
		monitor.add(fL);
		monitor.add(f$malTF);
	}
	
	private void setTphase(int tphase) {
		this.tphase = tphase;
		String text = "";
		switch (tphase) {
		case 1:
			text = "DATENT";
			break;
		case 2:
			text = "SETUP_DONE";
			break;
		case 3:
			text = "SETUP_TEST";
			break;
		default:
			break;
		}
		tphaseTF.setText(text);
	}
	
	private void setBendingMagnet(boolean bendingMagnet) {
		this.bendingMagnet = bendingMagnet;
		bendingTF.setText("" + bendingMagnet);
	}
	
	private void setDataEntryComplete(boolean dataEntryComplete) {
		this.dataEntryComplete = dataEntryComplete;
		dataTF.setText("" + dataEntryComplete);
	}

	private void setClass3(byte class3) {
		this.class3 = class3;
		class3TF.setText("" + class3);
	}

	private void setF$mal(int f$mal) {
		this.f$mal = f$mal;
		f$malTF.setText("" + f$mal);
	}



	private class KeyboardHandler extends Thread {
		
		@Override
		public void run() {
			while (!dataEntryComplete) {
				if (commandTF.isFocusOwner()) {
					setDataEntryComplete(true);
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			while (tphase != 2) {
				String command = commandTF.getText();
				if (tphase == 3 && command != null && command.equalsIgnoreCase("set")) {
					set = true;
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Hand extends Thread {
		
		@Override
		public void run() {
			while (tphase == 1) {
				String t = turntableTF.getText();
				if (t != null && !t.equals("")) {	
					therac.setTurntablePosition(t.charAt(0));
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Hkeper extends Thread {
		
		@Override
		public void run() {
			while (tphase != 2) {
				lmtchk();
				try {
					sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void lmtchk() {
			// Synchronize to prevent the scheduling from producing the bug
			synchronized (f$malLock) {
				setF$mal(0);	// initial value has to be 0, since skipping chkcol can only lead to a type II error
				if (class3 != 0) {
					chkcol();
				}
			}
		}
		
		private void chkcol() {
			if (!therac.isCollimatorConsistent()) {
				setF$mal(1);
			}
		}
		
	}
}