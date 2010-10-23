/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.navigation_actions.CloudUploadEntity;
import de.ipk.ag_ba.gui.navigation_actions.FileManagerExp;
import de.ipk.ag_ba.gui.navigation_actions.ThreeDreconstructionAction;
import de.ipk.ag_ba.gui.navigation_actions.ThreeDsegmentationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public class ImageAnalysisCommandManager {

	public static Collection<NavigationGraphicalEntity> getCommands(String login, String pass,
			ExperimentReference experimentReference, GUIsetting guiSetting) {
		return getCommands(login, pass, experimentReference, true, guiSetting);
	}

	public static Collection<NavigationGraphicalEntity> getCommands(String login, String pass,
			ExperimentReference experimentReference, boolean analysis, GUIsetting guiSettings) {

		ArrayList<NavigationGraphicalEntity> actions = new ArrayList<NavigationGraphicalEntity>();

		actions.add(FileManagerExp.getFileManagerEntity(login, pass, experimentReference, guiSettings));

		try {
			if (experimentReference.getData().getHeader().getExcelfileid().startsWith("lemnatec:"))
				actions.add(new NavigationGraphicalEntity(new CloudUploadEntity(login, pass, experimentReference),
						guiSettings));
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
			actions.add(ImageAnalysis.getPhenotypingEntity(login, pass, experimentReference, 10, 15, guiSettings));
			actions.add(ThreeDreconstructionAction.getThreeDreconstructionTaskEntity(login, pass, experimentReference,
					"3-D Reconstruction", 15, 25, guiSettings));
			actions.add(ThreeDsegmentationAction.getThreeDsegmentationTaskEntity(login, pass, experimentReference,
					"3-D Segmentation", 15, 25, guiSettings));
		}
		return actions;
	}
}
