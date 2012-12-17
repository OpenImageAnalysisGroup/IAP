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

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ActionAnalysisCommandList;
import de.ipk.ag_ba.commands.experiment.ActionCopyCommandList;
import de.ipk.ag_ba.commands.experiment.ActionCopyToClipboard;
import de.ipk.ag_ba.commands.experiment.ActionDataReport;
import de.ipk.ag_ba.commands.experiment.ActionFileManager;
import de.ipk.ag_ba.commands.experiment.ActionToolList;
import de.ipk.ag_ba.commands.experiment.process.ActionNumericDataReport;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;

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
		
		if (analysis) {
			actions.add(new NavigationButton(
					new ActionAnalysisCommandList(
							"Analysis Tasks", m, experimentReference), guiSetting));
		}
		
		actions.add(new NavigationButton(getDataReportAction(m, experimentReference, guiSetting), guiSetting));
		
		if (IAPmain.getRunMode() == IAPrunMode.WEB)
			actions.add(new NavigationButton(new ActionNumericDataReport(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionCopyCommandList(m, experimentReference, guiSetting), guiSetting));
		
		actions.add(new NavigationButton(
				new ActionToolList(
						"Performance test and sort substances tool commands",
						m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(new ActionCopyToClipboard(m, experimentReference), guiSetting));
		return actions;
	}
	
	private static AbstractNavigationAction getDataReportAction(final MongoDB m, final ExperimentReference experimentReference, final GUIsetting guiSetting) {
		return new ActionDataReport("Create Report Files", experimentReference, m);
	}
	
	public static String getList(String heading, TreeSet<String> cs) {
		StringBuilder res = new StringBuilder();
		res.append(heading + " (" + cs.size() + ")" + "<ul>");
		if (cs.size() == 0)
			res.append("<li>[NOT SPECIFIED]");
		else {
			int n = 0;
			for (String c : cs) {
				res.append("<li>" + StringManipulationTools.stringReplace(c, ";", ";<br>"));
				n++;
				if (cs.size() > 15 && n >= 15) {
					res.append("<li>...");
					break;
				}
			}
		}
		res.append("</ul>");
		return res.toString();
	}
}
