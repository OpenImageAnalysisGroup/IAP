/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.analysis.ActionPhytochamberAnalysis;
import de.ipk.ag_ba.commands.analysis.ActionPhytochamberBlueRubberAnalysis;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.maize.BarleyAnalysisAction;
import de.ipk.ag_ba.gui.navigation_actions.maize.MaizeAnalysisAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.navigation_model.RemoteExecutionWrapperAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;

/**
 * @author klukas
 */
public class ActionAnalyzeAllExperiments extends AbstractNavigationAction implements NavigationAction {
	
	private final MongoDB m;
	private final ArrayList<ExperimentHeaderInterface> experimentList;
	private int n = 0;
	private String names = "";
	
	public ActionAnalyzeAllExperiments(MongoDB m, ArrayList<ExperimentHeaderInterface> experimentList) {
		super("(Re)analyze all experiments");
		this.m = m;
		this.experimentList = ExperimentHeaderService.filterNewest(experimentList);
		this.n = 0;
		ArrayList<String> nn = new ArrayList<String>();
		for (ExperimentHeaderInterface eh : this.experimentList) {
			if (eh.getRemark() != null && eh.getRemark().contains("IAP image analysis"))
				continue;
			AnalysisStatus status = knownAnalysis(eh, this.experimentList);
			
			if (eh.getExperimentType().equals(IAPexperimentTypes.BarleyGreenhouse + "") ||
					eh.getExperimentType().equals(IAPexperimentTypes.MaizeGreenhouse + "") ||
					eh.getExperimentType().equals(IAPexperimentTypes.Phytochamber + "") ||
					eh.getExperimentType().equals(IAPexperimentTypes.PhytochamberBlueRubber + "")) {
				if (status != AnalysisStatus.CURRENT) {
					nn.add(eh.getExperimentName() + " (" + status + ")");
					n++;
				}
			}
		}
		names = StringManipulationTools.getStringList("<li>", nn, "");
	}
	
	private AnalysisStatus knownAnalysis(ExperimentHeaderInterface eh, ArrayList<ExperimentHeaderInterface> experimentList2) {
		if (eh == null || experimentList2 == null || experimentList2.size() == 0 || eh.getDatabaseId() == null || eh.getDatabaseId().length() == 0)
			return AnalysisStatus.NOT_FOUND;
		if (eh.getImportusergroup() != null && eh.getImportusergroup().equals("Analysis Results"))
			return AnalysisStatus.CURRENT;
		AnalysisStatus res = AnalysisStatus.NOT_FOUND;
		String dbID = eh.getDatabaseId();
		for (ExperimentHeaderInterface e : experimentList2) {
			if (e.getOriginDbId() != null && e.getOriginDbId().equals(dbID)) {
				if (e.getRemark().contains(IAPmain.RELEASE_IAP_IMAGE_ANALYSIS))
					return AnalysisStatus.CURRENT;
				else {
					if (e.getImportusergroup() != null && e.getImportusergroup().equals("Temp")) {
						// temporary results are available, so an analysis is probably already running
						return AnalysisStatus.CURRENT;
					} else
						res = AnalysisStatus.NON_CURRENT;
				}
			}
		}
		return res;
	}
	
	private NavigationButton src;
	private String result;
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		return result;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>(currentSet);
		result.add(src);
		return result;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		result = "Internal Error";
		StringBuilder res = new StringBuilder();
		res.append("Experiments:<ul>");
		for (ExperimentHeaderInterface eh : experimentList) {
			if (eh.getRemark() != null && eh.getRemark().contains("IAP image analysis"))
				continue;
			if (knownAnalysis(eh, experimentList) == AnalysisStatus.CURRENT) {
				res.append("<li>Analysis result with current image analysis pipeline (" + IAPmain.RELEASE_IAP_IMAGE_ANALYSIS + ") available for "
						+ eh.getExperimentName());
				continue;
			}
			NavigationAction navigationAction = null;
			System.out.println("Experiment-type: " + eh.getExperimentType());
			if (eh.getExperimentType().equals(IAPexperimentTypes.BarleyGreenhouse + ""))
				navigationAction = new BarleyAnalysisAction(m, new ExperimentReference(eh));
			if (eh.getExperimentType().equals(IAPexperimentTypes.MaizeGreenhouse + ""))
				navigationAction = new MaizeAnalysisAction(m, new ExperimentReference(eh));
			if (eh.getExperimentType().equals(IAPexperimentTypes.Phytochamber + ""))
				navigationAction = new ActionPhytochamberAnalysis(m, new ExperimentReference(eh));
			if (eh.getExperimentType().equals(IAPexperimentTypes.PhytochamberBlueRubber + ""))
				navigationAction = new ActionPhytochamberBlueRubberAnalysis(m, new ExperimentReference(eh));
			
			if (navigationAction != null) {
				RemoteCapableAnalysisAction rca = (RemoteCapableAnalysisAction) navigationAction;
				CloundManagerNavigationAction ra = new CloundManagerNavigationAction(rca.getMongoDB(), null, false);
				navigationAction = new RemoteExecutionWrapperAction(navigationAction,
						new NavigationButton(ra, src.getGUIsetting()));
				navigationAction.performActionCalculateResults(src);
				
				res.append("<li>Analyze " + eh.getExperimentName() + " with analysis method " +
						StringManipulationTools.removeHTMLtags(rca.getDefaultTitle()));
			}
		}
		res.append("</ul>");
		result = res.toString();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<h1>Compute tasks have been scheduled</h1>" + result);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getAnalyzeAll();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getAnalyzeAll();
	}
	
	@Override
	public String getDefaultTooltip() {
		return "<html>Analyze " + n + " experiments" +
				(n > 0 ? "<br><ul>" + names + "</ul>" : "");
	}
	
	@Override
	public String getDefaultTitle() {
		return "Analyze " + n + " experiments";
	}
}
