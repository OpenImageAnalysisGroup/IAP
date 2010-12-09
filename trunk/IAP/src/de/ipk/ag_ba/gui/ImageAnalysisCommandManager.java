/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.CloudIoTestAction;
import de.ipk.ag_ba.gui.navigation_actions.CloudUploadEntity;
import de.ipk.ag_ba.gui.navigation_actions.FileManagerAction;
import de.ipk.ag_ba.gui.navigation_actions.NumericDataReportAction;
import de.ipk.ag_ba.gui.navigation_actions.PerformanceTestAction;
import de.ipk.ag_ba.gui.navigation_actions.ThreeDreconstructionAction;
import de.ipk.ag_ba.gui.navigation_actions.ThreeDsegmentationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ImageAnalysisCommandManager {

	public static Collection<NavigationButton> getCommands(MongoDB m,
						ExperimentReference experimentReference, GUIsetting guiSetting) {
		return getCommands(m, experimentReference, true, guiSetting);
	}

	public static Collection<NavigationButton> getCommands(MongoDB m,
						ExperimentReference experimentReference, boolean analysis, GUIsetting guiSetting) {

		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();

		actions.add(FileManagerAction.getFileManagerEntity(m, experimentReference, guiSetting));

		try {
			if (experimentReference.getData(m).getHeader().getExcelfileid().startsWith("lemnatec:"))
				actions.add(new NavigationButton(new CloudUploadEntity(m, experimentReference), guiSetting));
		} catch (Exception e) {
			// empty
		}

		// "img/PoweredMongoDBgreenLeaf.png")); // PoweredMongoDBgreen.png"));

		// actions.add(new NavigationGraphicalEntity(
		// new ClearBackgroundNavigation(login, 15, 25, pass,
		// experimentReference), "Clear Background",
		// "img/colorhistogram.png"));
		// actions.add(new NavigationGraphicalEntity(new
		// CountColorsNavigation(m, 40, experimentReference),
		// "Hue Historam", "img/colorhistogram.png"));
		if (analysis) {
			NavigationAction performanceTestAction = new PerformanceTestAction(m, experimentReference);
			NavigationButton performanceTestButton = new NavigationButton(performanceTestAction, guiSetting);
			actions.add(performanceTestButton);

			actions.add(new NavigationButton(new CloudIoTestAction(m, experimentReference), guiSetting));

			actions.add(new NavigationButton(new NumericDataReportAction(m, experimentReference), guiSetting));

			actions.add(ImageAnalysis.getPhenotypingEntity(m, experimentReference, 10, 15, guiSetting));
			actions.add(ImageAnalysis.getPhytochamberEntity(m, experimentReference, 10, 15, guiSetting));

			actions.add(ThreeDreconstructionAction.getThreeDreconstructionTaskEntity(m, experimentReference,
								"3-D Reconstruction", 15, 25, guiSetting));
			actions.add(ThreeDsegmentationAction.getThreeDsegmentationTaskEntity(m, experimentReference,
								"3-D Segmentation", 15, 25, guiSetting));
		}
		return actions;
	}
}
