/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;

/**
 * @author klukas
 * 
 */
public abstract class AbstractExperimentAnalysisNavigation extends AbstractNavigationAction {
	private static final long serialVersionUID = 1L;

	protected String login;
	protected String pass;

	protected final ArrayList<NavigationGraphicalEntity> actions = new ArrayList<NavigationGraphicalEntity>();
	protected ExperimentReference experiment;

	private NavigationGraphicalEntity src;

	public AbstractExperimentAnalysisNavigation(String login, String pass, ExperimentReference experiment) {
		super("Analyse Experiment Data Set");
		this.login = login;
		this.pass = pass;
		this.experiment = experiment;

	}

	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		actions.clear();
		ExperimentReference exp = experiment;
		try {
			exp.setExperimentData(experiment.getData());
			for (NavigationGraphicalEntity ne : ImageAnalysisCommandManager.getCommands(login, pass, exp))
				actions.add(ne);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return actions;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		try {
			MyExperimentInfoPanel info = new MyExperimentInfoPanel();
			info.setExperimentInfo(login, pass, experiment.getData().getHeader(), true, experiment.getData());
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

	public void performActionCalculateResults(final NavigationGraphicalEntity src) throws Exception {
		this.src = src;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}
}
