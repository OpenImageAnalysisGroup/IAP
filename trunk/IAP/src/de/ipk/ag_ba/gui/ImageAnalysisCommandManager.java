/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.gui.navigation_actions.CloudIoTestAction;
import de.ipk.ag_ba.gui.navigation_actions.CopyEntity;
import de.ipk.ag_ba.gui.navigation_actions.DataExportAction;
import de.ipk.ag_ba.gui.navigation_actions.DataExportAsFilesAction;
import de.ipk.ag_ba.gui.navigation_actions.DataExportTarAction;
import de.ipk.ag_ba.gui.navigation_actions.FileManagerAction;
import de.ipk.ag_ba.gui.navigation_actions.NumericDataReportAction;
import de.ipk.ag_ba.gui.navigation_actions.ThreeDreconstructionAction;
import de.ipk.ag_ba.gui.navigation_actions.ThreeDsegmentationAction;
import de.ipk.ag_ba.gui.navigation_actions.hsm.DataExportToHsmFolderAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
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
		
		// "img/PoweredMongoDBgreenLeaf.png")); // PoweredMongoDBgreen.png"));
		
		// actions.add(new NavigationGraphicalEntity(
		// new ClearBackgroundNavigation(login, 15, 25, pass,
		// experimentReference), "Clear Background",
		// "img/colorhistogram.png"));
		// actions.add(new NavigationGraphicalEntity(new
		// CountColorsNavigation(m, 40, experimentReference),
		// "Hue Historam", "img/colorhistogram.png"));
		
		if (GraphicsEnvironment.isHeadless())
			actions.add(new NavigationButton(new NumericDataReportAction(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new DataExportToHsmFolderAction(m, experimentReference, IAPmain.getHSMfolder()), guiSetting));
		
		actions.add(new NavigationButton(new DataExportAction(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new DataExportTarAction(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new DataExportAsFilesAction(m, experimentReference), guiSetting));
		
		if (analysis) {
			// NavigationAction performanceTestAction = new PerformanceTestAction(m, experimentReference);
			// NavigationButton performanceTestButton = new NavigationButton(performanceTestAction, guiSetting);
			// actions.add(performanceTestButton);
			
			boolean showTestActions = false;
			
			if (showTestActions)
				actions.add(new NavigationButton(new CloudIoTestAction(m, experimentReference), guiSetting));
			
			actions.add(ImageAnalysis.getPhenotypingEntity(m, experimentReference, 2.5, 5, guiSetting));
			actions.add(ImageAnalysis.getPhytochamberEntity(m, experimentReference, 10, 15, guiSetting));
			
			actions.add(ThreeDreconstructionAction.getThreeDreconstructionTaskEntity(m, experimentReference,
								"3-D Reconstruction", 15, 25, guiSetting));
			actions.add(ThreeDsegmentationAction.getThreeDsegmentationTaskEntity(m, experimentReference,
								"3-D Segmentation", 15, 25, guiSetting));
		}
		
		try {
			// if (experimentReference.getData(m).getHeader().getExcelfileid().startsWith("lemnatec:"))
			// if (!analysis)
			actions.add(new NavigationButton(new CopyEntity(m, experimentReference), guiSetting));
		} catch (Exception e) {
			// empty
		}
		
		return actions;
	}
}
