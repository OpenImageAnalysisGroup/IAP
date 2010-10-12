/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.mongo;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.ImageAnalysisCommandManager;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.AbstractNavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.DeletionCommand;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Other;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Trash;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.MyExperimentInfoPanel;
import de.ipk_gatersleben.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 * 
 */
public class MongoOrLemnaTecExperimentNavigationAction extends AbstractNavigationAction {
	private final ExperimentHeaderInterface header;
	private NavigationGraphicalEntity src;
	private ExperimentInterface experiment;

	public MongoOrLemnaTecExperimentNavigationAction(ExperimentHeaderInterface ei) {
		super(ei.getExcelfileid().startsWith("lemnatec:") ? "Access LemnaTec-DB data set"
				: "Access Systems Biology Cloud Data Set");
		header = ei;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> actions = new ArrayList<NavigationGraphicalEntity>();
		// actions.add(FileManager.getFileManagerEntity(login, pass,
		// ei.experimentName));

		if (header != null && !header.getExcelfileid().startsWith("lemnatec:")
				&& (header.getImportusername() == null || header.getImportusername().equals(SystemAnalysis.getUserName()))) {
			if (header.inTrash()) {
				actions.add(Trash.getTrashEntity(header, DeletionCommand.UNTRASH));
				actions.add(Trash.getTrashEntity(header, DeletionCommand.DELETE));
			} else
				actions.add(Trash.getTrashEntity(header, DeletionCommand.TRASH));
		}
		boolean add = true;
		if (header != null && header.inTrash())
			add = false;
		else {
			add = true;
		}
		if (add) {
			getDefaultActions(actions, experiment, header, true);
		}
		return actions;
	}

	public static void getDefaultActions(ArrayList<NavigationGraphicalEntity> actions, ExperimentInterface experiment,
			ExperimentHeaderInterface header, boolean imageAnalysis) {
		if (experiment == null)
			return;
		try {
			if (imageAnalysis)
				for (NavigationGraphicalEntity ne : ImageAnalysisCommandManager.getCommands(SystemAnalysis.getUserName(),
						null, new ExperimentReference(experiment)))
					actions.add(ne);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		for (NavigationGraphicalEntity ne : Other.getProcessExperimentDataWithVantedEntities(null, null,
				new ExperimentReference(experiment))) {
			if (ne.getTitle().contains("Put data")) {
				ne.setTitle("View in VANTED");
				actions.add(ne);
			}
		}
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		this.src = src;
		if (header.getExcelfileid().startsWith("lemnatec:"))
			experiment = new LemnaTecDataExchange().getExperiment(header);
		else
			experiment = new MongoDB().getExperiment(header);
	}

	@Override
	public String getDefaultImage() {
		if (header.getExcelfileid().startsWith("lemnatec:"))
			return "img/000Grad_3-gray.png";
		else
			return "img/ext/image-x-generic-off.png";
	}

	@Override
	public String getDefaultNavigationImage() {
		if (header.getExcelfileid().startsWith("lemnatec:"))
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
		ip.setExperimentInfo(SystemAnalysis.getUserName(), null, header, true, experiment);
		return new MainPanelComponent(ip, true);
	}
}
