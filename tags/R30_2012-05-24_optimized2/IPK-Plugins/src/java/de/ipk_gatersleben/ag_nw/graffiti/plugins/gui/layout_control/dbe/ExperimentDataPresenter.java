package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import javax.swing.JComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * Receives experimental data and adds it to the GUI (e.g. TabDBE).
 */
public interface ExperimentDataPresenter {
	
	public void processReceivedData(TableData td, String experimentName, ExperimentInterface md, JComponent gui);
	
}