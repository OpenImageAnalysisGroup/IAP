/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import de.ipk.ag_ba.commands.analysis.AbstractExperimentDataNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ActionCopyToMongo extends AbstractExperimentDataNavigationAction {
	
	private boolean active;
	private final boolean saveAnnotation;
	private MongoDB m;
	
	public ActionCopyToMongo(MongoDB m, ExperimentReference experiment) {
		super(experiment);
		this.m = m;
		saveAnnotation = false;
	}
	
	public ActionCopyToMongo(MongoDB m, ExperimentReference experiment, boolean annotationSave) {
		super(experiment);
		this.m = m;
		this.saveAnnotation = annotationSave;
	}
	
	@Override
	public String getDefaultTooltip() {
		ExperimentHeaderInterface ei = experiment.getHeader();
		return "<html><b>Save in database:</b>" +
				"<html><table>" + "<tr><td>Experiment</td><td>" + ei.getExperimentName() + "</td></tr>"
				+ "<tr><td>Type</td><td>" + ei.getExperimentType() + "</td></tr>" + "<tr><td>Owner</td><td>"
				+ ei.getImportusername() + "</td></tr>" + "<tr><td>Import Time</td><td>" + ei.getImportdate()
				+ "</td></tr>" + "<tr><td>Remark</td><td>" + ei.getRemark() + "</td></tr>";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] sel = null;
		if (m != null)
			sel = new Object[] { m };
		else {
			if (MongoDB.getMongos().size() > 1) {
				sel = MyInputHelper.getInput(
						"Select the database-target:",
						"Target Selection", new Object[] { "Target", MongoDB.getMongos() });
			} else
				sel = new Object[] { MongoDB.getMongos().iterator().next() };
		}
		if (sel == null)
			return;
		
		super.performActionCalculateResults(src);
		
		this.m = (MongoDB) sel[0];
		
		try {
			active = true;
			
			ExperimentInterface exp = experiment.getData();
			
			exp.getHeader().setOriginDbId(exp.getHeader().getDatabaseId() + "");
			
			m.saveExperiment(exp, status);
		} finally {
			active = false;
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (saveAnnotation)
			return "img/ext/gpl2/Gnome-Emblem-Downloads-64.png";// Gnome-Document-Save-64.png";
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
		if (m == null)
			return "INTERNAL ERROR: target db is NULL";
		else
			return "Copy to " + m.getDisplayName();
		// return "<html><center>Save in database<br>(" + ActionMongoExperimentsNavigation.getTempdataExperimentName(experiment) + ")";
	}
}
