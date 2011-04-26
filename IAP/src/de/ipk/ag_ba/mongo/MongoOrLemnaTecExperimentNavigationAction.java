/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.DeletionCommand;
import de.ipk.ag_ba.gui.navigation_actions.Other;
import de.ipk.ag_ba.gui.navigation_actions.Trash;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 */
public class MongoOrLemnaTecExperimentNavigationAction extends AbstractNavigationAction {
	private final ExperimentHeaderInterface header;
	private NavigationButton src;
	private ExperimentInterface experiment;
	private final MongoDB m;
	private String domainUser;
	private final String tt;
	
	public MongoOrLemnaTecExperimentNavigationAction(ExperimentHeaderInterface ei, MongoDB m) {
		super(ei.getDatabaseId() != null && ei.getDatabaseId().startsWith("lemnatec:") ? "Access LemnaTec-DB data set"
							: "Access Systems Biology Cloud Data Set");
		
		this.tt = "<html><table>" + "<tr><td>Experiment</td><td>" + ei.getExperimentname() + "</td></tr>"
				+ "<tr><td>Type</td><td>" + ei.getExperimentType() + "</td></tr>" + "<tr><td>Owner</td><td>"
				+ ei.getImportusername() + "</td></tr>" + "<tr><td>Import Time</td><td>" + ei.getImportdate()
				+ "</td></tr>" + "<tr><td>Remark</td><td>" + ei.getRemark() + "</td></tr>";
		
		header = ei;
		this.m = m;
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
		
		if (header != null && !header.getDatabaseId().startsWith("lemnatec:")
							&& (header.getImportusername() == null || header.getImportusername().equals(SystemAnalysis.getUserName()))) {
			if (m != null)
				if (header.inTrash()) {
					actions.add(Trash.getTrashEntity(header, DeletionCommand.UNTRASH, src.getGUIsetting(), m));
					actions.add(Trash.getTrashEntity(header, DeletionCommand.DELETE, src.getGUIsetting(), m));
				} else
					actions.add(Trash.getTrashEntity(header, DeletionCommand.TRASH, src.getGUIsetting(), m));
		}
		boolean add = true;
		if (header != null && header.inTrash())
			add = false;
		else {
			add = true;
		}
		if (add) {
			boolean imageAnalysis = m != null;
			getDefaultActions(actions, experiment, header, imageAnalysis, src.getGUIsetting(), m);
		}
		return actions;
	}
	
	public static void getDefaultActions(ArrayList<NavigationButton> actions, ExperimentInterface experiment,
						ExperimentHeaderInterface header, boolean imageAnalysis, GUIsetting guiSetting, MongoDB m) {
		if (experiment == null)
			return;
		try {
			for (NavigationButton ne : ImageAnalysisCommandManager.getCommands(m, new ExperimentReference(experiment), imageAnalysis, guiSetting))
				actions.add(ne);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		if (imageAnalysis)
			for (NavigationButton ne : Other.getProcessExperimentDataWithVantedEntities(m, new ExperimentReference(
							experiment), guiSetting)) {
				if (ne.getTitle().contains("Put data")) {
					ne.setTitle("View in VANTED");
					actions.add(ne);
				}
			}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		if (experiment == null) {
			if (header.getDatabaseId() != null && header.getDatabaseId().startsWith("lemnatec:"))
				experiment = new LemnaTecDataExchange().getExperiment(header, status);
			else
				experiment = m.getExperiment(header, true);
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (header.getDatabaseId() != null && header.getDatabaseId().contains("APH_"))
			return "img/ext/phyto.png";
		if (header.getDatabaseId() != null && header.getDatabaseId().contains("CGH_"))
			return "img/maisMultipleScaled.png";
		if (header.getDatabaseId() != null && header.getDatabaseId().contains("BGH_"))
			return "img/000Grad_3.png";
		else
			return "img/ext/image-x-generic-off.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		if (header.getDatabaseId() != null && header.getDatabaseId().contains("APH_"))
			return "img/ext/phyto.png";
		if (header.getDatabaseId() != null && header.getDatabaseId().contains("CGH_"))
			return "img/maisMultipleScaled.png";
		if (header.getDatabaseId() != null && header.getDatabaseId().contains("BGH_"))
			return "img/000Grad_3.png";
		else
			return "img/ext/image-x-generic.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "" + header.getExperimentname();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
		ip.setExperimentInfo(m, header, true, experiment);
		return new MainPanelComponent(ip, true);
	}
	
	public ExperimentInterface getExperimentReference() {
		return experiment;
	}
	
	public void setLogin(String domainUser) {
		this.domainUser = domainUser;
	}
}
