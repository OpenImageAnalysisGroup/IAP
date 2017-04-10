package de.ipk.ag_ba.commands.control;

import javax.swing.JPanel;

import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

public class CapturePanel extends JPanel {
	private ExperimentReferenceInterface experimentReference;
	
	public CapturePanel(ExperimentReferenceInterface experimentReference) {
		this.experimentReference = experimentReference;
		// setLayout(TableLayout.);
	}
	
}
