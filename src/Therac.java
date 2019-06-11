import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class Therac {
	private char turntablePosition;
	private int energy;
	private boolean active;
	
	// GUI
	private JFrame frame;
	private JTextField turntableTF;
	private JTextField energyTF;
	private JTextField statusTF;
	private JTextField activeTF;
	
	public Therac() {
		createGUI();
		
//		this.setTurntablePosition('L');
//		this.setEnergy(0);
		this.setActive(false);
		
		frame.setVisible(true);
	}
	
	private void createGUI() {
		// Increase font size for presentation, taken from https://stackoverflow.com/questions/1043872/are-there-any-built-in-methods-in-java-to-increase-font-size
		UIManager.put("Label.font", new FontUIResource(new Font("Dialog", Font.PLAIN, ControlPanel.fontSize)));
	    UIManager.put("TextField.font", new FontUIResource(new Font("Dialog", Font.PLAIN, ControlPanel.fontSize)));
		
		int width = 400;
		int height = 200;
		frame = new JFrame("Therac Hardware");
		frame.setLayout(new GridLayout(4, 2));
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel tL = new JLabel("Turntable Position:");
		turntableTF = new JTextField();
		turntableTF.setEditable(false);
		turntableTF.setSize(width/2, height/4);
		
		JLabel eL = new JLabel("Energy:");
		energyTF = new JTextField();
		energyTF.setEditable(false);
		energyTF.setSize(width/2, height/4);
		
		JLabel sL = new JLabel("Status:");
		statusTF = new JTextField();
		statusTF.setEditable(false);
		statusTF.setText("Idle");
		statusTF.setSize(width/2, height/4);
		
		JLabel aL = new JLabel("Active:");
		activeTF = new JTextField();
		activeTF.setEditable(false);
		activeTF.setSize(width/2, height/4);
		
		frame.add(tL);
		frame.add(turntableTF);
		
		frame.add(eL);
		frame.add(energyTF);
		
		frame.add(sL);
		frame.add(statusTF);
		
		frame.add(aL);
		frame.add(activeTF);
	}

	public char getTurntablePosition() {
		return turntablePosition;
	}

	public void setTurntablePosition(char turntablePosition) {
		this.turntablePosition = turntablePosition;
		this.turntableTF.setText("" + turntablePosition);
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
		this.energyTF.setText("" + energy);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		this.activeTF.setText("" + active);
		if (active) {
			statusTF.setText("Beam on");
		}
	}

	public void setMagnet(int i) {
		System.out.print("Setting magnet " + i + " ...");
		statusTF.setText("Setting magnet " + i);
	}
	
	public void finalizeMagnet(int i) {
		System.out.println(" Done!");
		statusTF.setText("Done setting magnet " + i);
	}
	
	public boolean isCollimatorConsistent() {
		// Assume that the collimator is never correct, since we want to show a bug and not create a working system
		return false;
	}
}
