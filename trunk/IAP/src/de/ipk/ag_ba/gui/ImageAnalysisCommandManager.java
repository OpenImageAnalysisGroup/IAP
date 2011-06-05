/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import java.util.ArrayList;
import java.util.Collection;

import org.SystemAnalysis;

import de.ipk.ag_ba.gui.actions.ActionDataExport;
import de.ipk.ag_ba.gui.actions.ActionDataExportTar;
import de.ipk.ag_ba.gui.actions.ActionFileManager;
import de.ipk.ag_ba.gui.actions.ActionNumericDataReport;
import de.ipk.ag_ba.gui.actions.CloudIoTestAction;
import de.ipk.ag_ba.gui.actions.CopyEntity;
import de.ipk.ag_ba.gui.actions.DataExportAsFilesAction;
import de.ipk.ag_ba.gui.actions.analysis.ActionThreeDreconstruction;
import de.ipk.ag_ba.gui.actions.analysis.ActionThreeDsegmentation;
import de.ipk.ag_ba.gui.actions.hsm.ActionDataExportToHsmFolder;
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
		
		actions.add(ActionFileManager.getFileManagerEntity(m, experimentReference, guiSetting));
		
		// "img/PoweredMongoDBgreenLeaf.png")); // PoweredMongoDBgreen.png"));
		
		// actions.add(new NavigationGraphicalEntity(
		// new ClearBackgroundNavigation(login, 15, 25, pass,
		// experimentReference), "Clear Background",
		// "img/colorhistogram.png"));
		// actions.add(new NavigationGraphicalEntity(new
		// CountColorsNavigation(m, 40, experimentReference),
		// "Hue Historam", "img/colorhistogram.png"));
		
		if (SystemAnalysis.isHeadless())
			actions.add(new NavigationButton(new ActionNumericDataReport(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionDataExportToHsmFolder(m, experimentReference, IAPmain.getHSMfolder()), guiSetting));
		
		actions.add(new NavigationButton(new ActionDataExport(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionDataExportTar(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new DataExportAsFilesAction(m, experimentReference), guiSetting));
		
		if (analysis) {
			// NavigationAction performanceTestAction = new PerformanceTestAction(m, experimentReference);
			// NavigationButton performanceTestButton = new NavigationButton(performanceTestAction, guiSetting);
			// actions.add(performanceTestButton);
			
			boolean showTestActions = false;
			
			if (showTestActions)
				actions.add(new NavigationButton(new CloudIoTestAction(m, experimentReference), guiSetting));
			
			// TODO: Fix and test pipeline, probably the maize pipline structures and classes should be newly adapted
			// actions.add(ImageAnalysis.getPhytochamberEntity(m, experimentReference, 10, 15, guiSetting));
			
			actions.add(ImageAnalysis.getMaizeEntity(m, experimentReference, 10, 15, guiSetting));
			
			actions.add(ActionThreeDreconstruction.getThreeDreconstructionTaskEntity(m, experimentReference,
								"3-D Reconstruction", 15, 25, guiSetting));
			actions.add(ActionThreeDsegmentation.getThreeDsegmentationTaskEntity(m, experimentReference,
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
