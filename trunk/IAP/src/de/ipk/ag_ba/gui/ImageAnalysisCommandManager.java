/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionCopyToMongo;
import de.ipk.ag_ba.commands.ActionDataExport;
import de.ipk.ag_ba.commands.ActionDataExportAsFilesAction;
import de.ipk.ag_ba.commands.ActionDataExportTar;
import de.ipk.ag_ba.commands.ActionFileManager;
import de.ipk.ag_ba.commands.ActionNumericDataReport;
import de.ipk.ag_ba.commands.ActionNumericDataReportSetup;
import de.ipk.ag_ba.commands.ActionPerformanceTest;
import de.ipk.ag_ba.commands.CloudIoTestAction;
import de.ipk.ag_ba.commands.analysis.ActionThreeDreconstruction;
import de.ipk.ag_ba.commands.analysis.ActionThreeDsegmentation;
import de.ipk.ag_ba.commands.hsm.ActionDataExportToHsmFolder;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ImageAnalysisCommandManager {
	
	public static Collection<NavigationButton> getCommands(MongoDB m,
			ExperimentReference experimentReference, GUIsetting guiSetting) {
		return getCommands(m, experimentReference, true, guiSetting);
	}
	
	public static Collection<NavigationButton> getCommands(final MongoDB m,
			final ExperimentReference experimentReference, boolean analysis, final GUIsetting guiSetting) {
		
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		
		actions.add(ActionFileManager.getFileManagerEntity(m, experimentReference, guiSetting));
		
		// "img/PoweredMongoDBgreenLeaf.png")); // PoweredMongoDBgreen.png"));
		
		// actions.add(new NavigationGraphicalEntity(
		// new ClearBackgroundNavigation(login, 15, 25, pass,
		// experimentReference), "Clear Background",
		// "img/colorhistogram.png"));
		// actions.add(new NavigationGraphicalEntity(new
		// CountColorsNavigation(m, 40, experimentReference),
		// "Hue Histogram", "img/colorhistogram.png"));
		
		NavigationAction defaultAction = new AbstractNavigationAction("Create Report Files") {
			
			private NavigationButton src;
			
			TreeSet<String> cs = new TreeSet<String>();
			TreeSet<String> ss = new TreeSet<String>();
			TreeSet<String> gs = new TreeSet<String>();
			TreeSet<String> vs = new TreeSet<String>();
			TreeSet<String> ts = new TreeSet<String>();
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				this.src = src;
				
				ExperimentInterface e = experimentReference.getData(m, false);
				for (SubstanceInterface si : e) {
					for (ConditionInterface ci : si) {
						String condition = ci.getConditionName();
						String species = ci.getSpecies();
						String genotype = ci.getGenotype();
						String variety = ci.getVariety();
						String treatment = ci.getTreatment();
						
						if (condition != null)
							cs.add(condition);
						if (species != null)
							ss.add(species);
						if (genotype != null)
							gs.add(genotype);
						if (variety != null)
							vs.add(variety);
						if (treatment != null)
							ts.add(treatment);
					}
				}
				
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				ArrayList<String> htmlTextPanels = new ArrayList<String>();
				// htmlTextPanels.add(getList("Conditions", cs));
				htmlTextPanels.add(getList("Species", ss));
				htmlTextPanels.add(getList("Genotypes", gs));
				htmlTextPanels.add(getList("Varieties", vs));
				htmlTextPanels.add(getList("Treatments", ts));
				return new MainPanelComponent(htmlTextPanels);
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
				res.add(src);
				return res;
			}
			
			@Override
			public String getDefaultTitle() {
				return "Data Report";
			}
			
			@Override
			public String getDefaultTooltip() {
				return super.getDefaultTooltip();
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-Emblem-Documents-64.png";
				// return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
				
				// actions.add(new NavigationButton(new ActionNumericDataReport(m, experimentReference), guiSetting));
				
				actions.add(new NavigationButton(new ActionNumericDataReportSetup(m, experimentReference, true, new String[] {
						"none", "none" }, true),
						guiSetting));
				actions.add(new NavigationButton(new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
						"Condition", "none" },
						false),
						guiSetting));
				actions.add(new NavigationButton(new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
						"Genotype", "none" },
						false),
						guiSetting));
				actions.add(new NavigationButton(new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
						"Treatment", "none" },
						false),
						guiSetting));
				actions.add(new NavigationButton(
						new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
								"Variety", "none" }, false),
						guiSetting));
				actions.add(new NavigationButton(
						new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
								"Variety", "Treatment" }, false),
						guiSetting));
				actions.add(new NavigationButton(new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
						"Species", "none" },
						false),
						guiSetting));
				actions.add(new NavigationButton(new ActionNumericDataReportSetup(m, experimentReference, false, new String[] {
						"Species", "Treatment" },
						false),
						guiSetting));
				return actions;
			}
		};
		actions.add(new NavigationButton(defaultAction, guiSetting));
		
		actions.add(new NavigationButton(new ActionNumericDataReport(m, experimentReference), guiSetting));
		
		String hsmf = IAPmain.getHSMfolder();
		if (hsmf != null)
			actions.add(new NavigationButton(new ActionDataExportToHsmFolder(m, experimentReference, hsmf), guiSetting));
		
		actions.add(new NavigationButton(new ActionDataExport(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionDataExportTar(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionDataExportAsFilesAction(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionPerformanceTest(m, experimentReference), guiSetting));
		if (analysis) {
			// NavigationAction performanceTestAction = new PerformanceTestAction(m, experimentReference);
			// NavigationButton performanceTestButton = new NavigationButton(performanceTestAction, guiSetting);
			// actions.add(performanceTestButton);
			
			boolean showTestActions = false;
			
			if (showTestActions)
				actions.add(new NavigationButton(new CloudIoTestAction(m, experimentReference), guiSetting));
			
			actions.add(ImageAnalysis.getPhytochamberEntity(m, experimentReference, 10, 15, guiSetting));
			actions.add(ImageAnalysis.getPhytochamberEntityBlueRubber(m, experimentReference, 10, 15, guiSetting));
			
			actions.add(ImageAnalysis.getMaizeEntity(m, experimentReference, 10, 15, guiSetting));
			actions.add(ImageAnalysis.getBarleyEntity(m, experimentReference, 10, 15, guiSetting));
			
			actions.add(ImageAnalysis.getMaize3dEntity(m, experimentReference, 10, 15, guiSetting));
			
			actions.add(ActionThreeDreconstruction.getThreeDreconstructionTaskEntity(m, experimentReference,
					"3-D Reconstruction", 15, 25, guiSetting));
			actions.add(ActionThreeDsegmentation.getThreeDsegmentationTaskEntity(m, experimentReference,
					"3-D Segmentation", 15, 25, guiSetting));
		}
		
		try {
			// if (experimentReference.getData(m).getHeader().getExcelfileid().startsWith("lemnatec:"))
			// if (!analysis)
			actions.add(new NavigationButton(new ActionCopyToMongo(m, experimentReference), guiSetting));
		} catch (Exception e) {
			// empty
		}
		
		return actions;
	}
	
	protected static String getList(String heading, TreeSet<String> cs) {
		StringBuilder res = new StringBuilder();
		res.append(heading + "<ul>");
		if (cs.size() == 0)
			res.append("<li>[NOT SPECIFIED]");
		else
			for (String c : cs)
				res.append("<li>" + c);
		res.append("</ul>");
		return res.toString();
	}
}
