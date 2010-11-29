/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 14, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import info.clearthought.layout.TableLayout;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;

/**
 * @author klukas
 */
public class ExperimentViewer extends JComponent {
	private static final long serialVersionUID = 1L;
	private final Experiment experiment;

	public ExperimentViewer(Experiment statisticsResult) {
		this.experiment = statisticsResult;

		initGui();
	}

	private void initGui() {
		DataTable dataTable = new DataTable(experiment);
		ImageViewer imageViewer = new ImageViewer();
		dataTable.setImageViewer(imageViewer);
		setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, dataTable, imageViewer);
		add(jsp, "0,0");
		validate();
	}

}
