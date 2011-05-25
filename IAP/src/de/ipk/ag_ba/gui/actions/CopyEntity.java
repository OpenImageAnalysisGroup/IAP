/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.actions;

import de.ipk.ag_ba.gui.actions.analysis.AbstractExperimentAnalysisNavigation;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class CopyEntity extends AbstractExperimentAnalysisNavigation {
	
	private boolean active;
	
	public CopyEntity(MongoDB m, ExperimentReference experiment) {
		super(m, experiment);
	}
	
	@Override
	public String getDefaultTooltip() {
		ExperimentHeaderInterface ei = experiment.getHeader();
		return "<html>Copy Dataset:" +
				"<html><table>" + "<tr><td>Experiment</td><td>" + ei.getExperimentName() + "</td></tr>"
				+ "<tr><td>Type</td><td>" + ei.getExperimentType() + "</td></tr>" + "<tr><td>Owner</td><td>"
				+ ei.getImportusername() + "</td></tr>" + "<tr><td>Import Time</td><td>" + ei.getImportdate()
				+ "</td></tr>" + "<tr><td>Remark</td><td>" + ei.getRemark() + "</td></tr>";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] sel = null;
		if (MongoDB.getMongos().size() > 1) {
			sel = MyInputHelper.getInput("Select the database-target:", "Target Selection", new Object[] {
							"Target", MongoDB.getMongos()
			});
		} else
			sel = new Object[] { MongoDB.getMongos().iterator().next() };
		
		if (sel == null)
			return;
		
		super.performActionCalculateResults(src);
		
		this.m = (MongoDB) sel[0];
		
		try {
			active = true;
			m.saveExperiment(experiment.getData(m), status);
		} finally {
			active = false;
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (active) {
			if (System.currentTimeMillis() % 1000 < 500)
				return "img/ext/transfer2.png";
			else
				return "img/ext/transfer22.png";
		} else
			
			return "img/ext/transfer2.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return getDefaultImage();
	}
	
	@Override
	public String getDefaultTitle() {
		return "Copy " + experiment.getExperimentName();
	}
	
}
