/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

/**
 * @author klukas
 */
public class CloudUploadEntity extends AbstractExperimentAnalysisNavigation {
	
	private boolean active;
	
	public CloudUploadEntity(MongoDB m, ExperimentReference experiment) {
		super(m, experiment);
	}
	
	@Override
	public String getDefaultTooltip() {
		return "Upload data set to IAP Cloud";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] sel = MyInputHelper.getInput("Select the database-target:", "Target Selection", new Object[] {
							"Target", MongoDB.getMongos()
		});
		
		if (sel == null)
			return;
		
		this.m = (MongoDB) sel[0];
		super.performActionCalculateResults(src);
		
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
		return "Copy";
	}
	
}
