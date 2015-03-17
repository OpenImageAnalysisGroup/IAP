/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.analysis;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.ErrorMsg;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public abstract class AbstractExperimentDataNavigationAction extends AbstractNavigationAction {
	
	protected final ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
	protected ExperimentReferenceInterface experiment;
	
	private NavigationButton src;
	
	public AbstractExperimentDataNavigationAction(ExperimentReferenceInterface experiment) {
		super("Analyse Experiment Data Set");
		this.experiment = experiment;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		actions.clear();
		ExperimentReferenceInterface exp = experiment;
		try {
			exp.setExperimentData(experiment.getData());
			exp.setM(experiment.getM());
			for (ActionDataProcessing adp : IAPpluginManager.getInstance().getExperimentProcessingActions(exp, true))
				actions.add(new NavigationButton(adp, src.getGUIsetting()));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return actions;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		try {
			ExperimentHeaderInfoPanel info = new ExperimentHeaderInfoPanel();
			ExperimentInterface ex = experiment.getData();
			info.setExperimentInfo(experiment.getM(), ex.getHeader(), true, ex);
			JComponent jp = TableLayout.getSplit(info, null, TableLayout.PREFERRED, TableLayout.FILL);
			jp.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			jp = TableLayout.getSplitVertical(jp, null, TableLayout.PREFERRED, TableLayout.FILL);
			jp = TableLayout.getSplitVertical(jp, null, TableLayout.PREFERRED, TableLayout.FILL);
			
			return new MainPanelComponent(jp);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
}
