/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.hsm;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
@Deprecated
public class ActionDataExportToHsmFolder extends AbstractNavigationAction {
	private DataExportHelper dataExchangeHelper;
	
	public ActionDataExportToHsmFolder(MongoDB m,
			ExperimentReference experimentReference, String hsmFolder) {
		super("Save in HSM (" + hsmFolder + ")");
		this.dataExchangeHelper = new DataExportHelper(experimentReference, m, hsmFolder, status);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ParameterOptions getParameters() {
		return new ParameterOptions(
				"<html>"
						+ "This commands copies the experiment and its connected binary data to the<br>"
						+ "HSM (Hierarchical Storage Management), which may be used as a safe<br>"
						+ "backup storage.<br><br>", new Object[] {
						"Copy images", dataExchangeHelper.includeMainImages,
						"Copy reference images", dataExchangeHelper.includeReferenceImages,
						"Copy annotation images", dataExchangeHelper.includeAnnotationImages });
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		super.setParameters(parameters);
		if (parameters != null && parameters.length == 3) {
			int idx = 0;
			dataExchangeHelper.includeMainImages = (Boolean) parameters[idx++];
			dataExchangeHelper.includeReferenceImages = (Boolean) parameters[idx++];
			dataExchangeHelper.includeAnnotationImages = (Boolean) parameters[idx++];
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(
			ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(
				currentSet);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Save in HSM";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveToHsmArchive();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src)
			throws Exception {
		dataExchangeHelper.performCopy();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		String errorMessage = dataExchangeHelper.getErrorMessage();
		if (errorMessage == null)
			errorMessage = "";
		else {
			errorMessage = " " + errorMessage + "";
		}
		
		if (errorMessage.trim().length() > 0)
			return new MainPanelComponent("Output incomplete. Error: "
					+ errorMessage);
		else
			return new MainPanelComponent("The data has been exported (copied "
					+ dataExchangeHelper.getMB() + " MB, " + dataExchangeHelper.getFiles() + " files added, " + dataExchangeHelper.getKnownFiles()
					+ " existing files have been skipped)." + errorMessage);
	}
	
}
