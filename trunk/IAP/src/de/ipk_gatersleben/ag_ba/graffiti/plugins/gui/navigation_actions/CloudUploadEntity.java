/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 * 
 */
public class CloudUploadEntity extends AbstractExperimentAnalysisNavigation {

	public CloudUploadEntity(String login, String pass, ExperimentReference experiment) {
		super(login, pass, experiment);
	}

	@Override
	public String getDefaultTooltip() {
		return "Upload data set to IAP Cloud";
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		super.performActionCalculateResults(src);

		new MongoDB().storeExperiment("dbe3", null, null, null, experiment.getData(), status);
	}
}
