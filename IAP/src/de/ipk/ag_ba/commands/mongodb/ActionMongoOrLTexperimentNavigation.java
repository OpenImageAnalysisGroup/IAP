/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionTrash;
import de.ipk.ag_ba.commands.DeletionCommand;
import de.ipk.ag_ba.commands.experiment.ActionExperimentHistory;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;

/**
 * @author klukas
 */
public class ActionMongoOrLTexperimentNavigation extends
		AbstractNavigationAction implements RunnableWithMappingData {
	private NavigationButton src;
	private final String tt;
	private String displayName;
	private ExperimentReference experimentReference;
	private boolean requestTitleUpdates = true;
	private boolean oldAnalysis;
	private String domainUser;
	
	public ActionMongoOrLTexperimentNavigation(
			ExperimentReference exp) {
		super(
				exp.getHeader().getDatabaseId() != null
						&& exp.getHeader().getDatabaseId().startsWith("lt:") ? "Access imaging system data set"
						: "Access Systems Biology Cloud Data Set");
		
		this.tt = "<html><table>" + "<tr><td>Experiment</td><td>"
				+ exp.getExperimentName() + "</td></tr>"
				+ "<tr><td>Type</td><td>" + exp.getHeader().getExperimentType()
				+ "</td></tr>" + "<tr><td>Owner</td><td>"
				+ exp.getHeader().getImportusername() + "</td></tr>"
				+ "<tr><td>Import Time</td><td>" + exp.getHeader().getImportdate()
				+ "</td></tr>" + "<tr><td>Remark</td><td>" + StringManipulationTools.stringReplace(exp.getHeader().getRemark(), " // ", "<br>")
				+ "</td></tr>";
		
		if (exp.getHeader().getRemark() != null && exp.getHeader().getRemark().contains("IAP image analysis")) {
			if (exp.getHeader().getExperimentType() != null && exp.getHeader().getExperimentType().equals(IAPexperimentTypes.AnalysisResults.toString())) {
				if (!IAPservice.isAnalyzedWithCurrentRelease(exp.getHeader()))
					oldAnalysis = true;
			}
		}
		this.experimentReference = exp;
	}
	
	@Override
	public String getDefaultTooltip() {
		return tt;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		// actions.add(FileManager.getFileManagerEntity(login, pass,
		// ei.experimentName));
		
		ExperimentHeaderInterface header = experimentReference.getHeader();
		
		if (header != null && !header.inTrash()) {
			boolean imageAnalysis = true;
			for (ActionDataProcessing adp : IAPpluginManager.getInstance()
					.getExperimentProcessingActions(experimentReference, imageAnalysis))
				actions.add(new NavigationButton(adp, src.getGUIsetting()));
		}
		if (header.getHistory() != null && !header.getHistory().isEmpty()) {
			actions.add(new NavigationButton(
					new ActionExperimentHistory(header.getHistory(),
							experimentReference.m), src.getGUIsetting()));
		}
		
		if (header != null && header.getDatabaseId() != null
				&& !header.getDatabaseId().startsWith("lt:")
				&& (header.getImportusername() == null
						|| header.getImportusername().equals("tomcat") || header
						.getImportusername().equals(
								SystemAnalysis.getUserName()))
				|| !SystemAnalysis.isHeadless()) {
			// if (experimentReference.m != null)
			if (header.inTrash()) {
				actions.add(ActionTrash.getTrashEntity(header,
						DeletionCommand.UNTRASH, src.getGUIsetting(), experimentReference.m));
				actions.add(ActionTrash.getTrashEntity(header,
						DeletionCommand.DELETE, src.getGUIsetting(), experimentReference.m));
			} else {
				actions.add(ActionTrash.getTrashEntity(header,
						DeletionCommand.TRASH, src.getGUIsetting(), experimentReference.m));
			}
		}
		
		return actions;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(
			ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(
				currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src)
			throws Exception {
		this.src = src;
		requestTitleUpdates = true;
		status.setCurrentStatusValue(0);
		experimentReference.loadDataInBackground(status);
		experimentReference.runAsDataBecomesAvailable(new Runnable() {
			@Override
			public void run() {
				status.setCurrentStatusValue(100);
				ActionMongoOrLTexperimentNavigation.this.requestTitleUpdates = false;
			}
		});
	}
	
	@Override
	public String getDefaultImage() {
		if (oldAnalysis)
			return "img/ext/gpl2/Gnome-Window-Close-48.png";
		ExperimentHeaderInterface header = experimentReference.getHeader();
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().contains("APH_"))
			return "img/ext/phyto.png";
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().contains("CGH_"))
			return "img/maisMultipleScaled.png";
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().contains("BGH_"))
			return "img/000Grad_3.png";
		else {
			if (header.getExperimentType() != null
					&& header.getExperimentType().equals(
							IAPexperimentTypes.Phytochamber))
				return "img/ext/phyto.png";
			if (header.getExperimentType() != null
					&& header.getExperimentType().equals(
							IAPexperimentTypes.MaizeGreenhouse))
				return "img/maisMultipleScaled.png";
			if (header.getExperimentType() != null
					&& header.getExperimentType().equals(
							IAPexperimentTypes.BarleyGreenhouse))
				return "img/000Grad_3.png";
			return "img/ext/image-x-generic-off.png";
		}
	}
	
	@Override
	public String getDefaultNavigationImage() {
		ExperimentHeaderInterface header = experimentReference.getHeader();
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().contains("APH_"))
			return "img/ext/phyto.png";
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().contains("CGH_"))
			return "img/maisMultipleScaled.png";
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().contains("BGH_"))
			return "img/000Grad_3.png";
		else {
			if (header.getExperimentType() != null
					&& header.getExperimentType().equals(
							IAPexperimentTypes.Phytochamber))
				return "img/ext/phyto.png";
			if (header.getExperimentType() != null
					&& header.getExperimentType().equals(
							IAPexperimentTypes.MaizeGreenhouse))
				return "img/maisMultipleScaled.png";
			if (header.getExperimentType() != null
					&& header.getExperimentType().equals(
							IAPexperimentTypes.BarleyGreenhouse))
				return "img/000Grad_3.png";
			return "img/ext/image-x-generic.png";
		}
	}
	
	@Override
	public String getDefaultTitle() {
		ExperimentHeaderInterface header = experimentReference.getHeader();
		long t = header.getStorageTime() != null && header.getStorageTime().getTime() > 0 ?
				header.getStorageTime().getTime() :
				(header.getImportdate() != null ? header.getImportdate().getTime() :
						0);
		long startTime = t;
		String add = "updated";
		if (header.getStorageTime() != null &&
				(header.getExperimentType() != null &&
				header.getExperimentType().equals(IAPexperimentTypes.AnalysisResults.toString()))) {
			if (header.getImportdate() != null)
				add = "input age " + SystemAnalysis.getWaitTime(
						System.currentTimeMillis() - header.getImportdate().getTime(), 1) + "<br>saved";
			else
				add = "storage time<br>undefined";
			t = header.getStorageTime().getTime();
		}
		String time = SystemAnalysis.getWaitTime(System.currentTimeMillis() - t, 1);
		if (time.trim().equals(""))
			System.out.println("ERRR");
		String age = "<br><small><font color='gray'>" + add + " " + time + " ago</font></small>";
		if (startTime == 0)
			age = "<br><small><font color='gray'>(start or end time could not be properly processed)</font></small>";
		if (displayName != null)
			return "<html><center>" + displayName + (oldAnalysis ? "<br>(analysed with old release)" : "") + age;
		else
			return "<html><center>" + header.getExperimentName() + (oldAnalysis ? "<br>(analysed with old release)" : "") + age;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ExperimentHeaderInfoPanel ip = new ExperimentHeaderInfoPanel(true, experimentReference);
		return new MainPanelComponent(ip, true);
	}
	
	public ExperimentInterface getExperimentReference() {
		return experimentReference.getExperiment();
	}
	
	public void setLogin(String domainUser) {
		this.domainUser = domainUser;
	}
	
	public void setOverrideTitle(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public void setExperimenData(ExperimentInterface doc) {
		experimentReference = new ExperimentReference(doc.getHeader());
		experimentReference.setExperimentData(doc);
	}
	
	@Override
	public void run() {
		// empty
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return requestTitleUpdates;
	}
}
