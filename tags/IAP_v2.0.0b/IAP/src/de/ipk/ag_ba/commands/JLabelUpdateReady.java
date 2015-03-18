package de.ipk.ag_ba.commands;

import javax.swing.JLabel;

/**
 * @author Christian Klukas
 */
public abstract class JLabelUpdateReady extends JLabel {
	
	public JLabelUpdateReady() {
		super("<html>Initializing...");
	}
	
	public abstract void update();
}
