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
import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public abstract class AbstractExperimentAnalysisNavigation extends AbstractNavigationAction {
	private static final long serialVersionUID = 1L;
	
	protected MongoDB m;
	
	protected final ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
	protected ExperimentReference experiment;
	
	private NavigationButton src;
	
	public AbstractExperimentAnalysisNavigation(MongoDB m, ExperimentReference experiment) {
		super("Analyse Experiment Data Set");
		this.m = m;
		this.experiment = experiment;
		
	}
	
	public ArrayList<NavigationButton> getResultNewActionSet() {
		actions.clear();
		ExperimentReference exp = experiment;
		try {
			exp.setExperimentData(experiment.getData(m));
			for (NavigationButton ne : ImageAnalysisCommandManager.getCommands(m, exp,
								src.getGUIsetting()))
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
			info.setExperimentInfo(m, experiment.getData(m).getHeader(), true, experiment.getData(m));
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
