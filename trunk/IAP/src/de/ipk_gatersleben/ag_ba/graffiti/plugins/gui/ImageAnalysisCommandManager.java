/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.CloudUploadEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.FileManagerExp;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.ThreeDreconstructionAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.ThreeDsegmentationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public class ImageAnalysisCommandManager {

	public static Collection<NavigationGraphicalEntity> getCommands(String login, String pass,
			ExperimentReference experimentReference) {
		return getCommands(login, pass, experimentReference, true);
	}

	public static Collection<NavigationGraphicalEntity> getCommands(String login, String pass,
			ExperimentReference experimentReference, boolean analysis) {

		ArrayList<NavigationGraphicalEntity> actions = new ArrayList<NavigationGraphicalEntity>();

		actions.add(FileManagerExp.getFileManagerEntity(login, pass, experimentReference));

		try {
			if (experimentReference.getData().getHeader().getExcelfileid().startsWith("lemnatec:"))
				actions.add(new NavigationGraphicalEntity(new CloudUploadEntity(login, pass, experimentReference)));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}

		// "img/PoweredMongoDBgreenLeaf.png")); // PoweredMongoDBgreen.png"));

		// actions.add(new NavigationGraphicalEntity(
		// new ClearBackgroundNavigation(login, 15, 25, pass,
		// experimentReference), "Clear Background",
		// "img/colorhistogram.png"));
		// actions.add(new NavigationGraphicalEntity(new
		// CountColorsNavigation(login, pass, 40, experimentReference),
		// "Hue Historam", "img/colorhistogram.png"));
		if (analysis) {
			actions.add(ImageAnalysis.getPhenotypingEntity(login, pass, experimentReference, 10, 15));
			actions.add(ThreeDreconstructionAction.getThreeDreconstructionTaskEntity(login, pass, experimentReference,
					"3-D Reconstruction", 15, 25));
			actions.add(ThreeDsegmentationAction.getThreeDsegmentationTaskEntity(login, pass, experimentReference,
					"3-D Segmentation", 15, 25));
		}
		return actions;
	}
}
