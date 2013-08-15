/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;
import java.util.TreeSet;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ActionCopyListOfExperimentsToMongo extends AbstractNavigationAction {
	
	private boolean active;
	private final boolean saveAnnotation;
	private MongoDB m;
	private final ArrayList<ExperimentReference> experimentlist;
	private String task = "";
	private boolean ignoreOutliers;
	
	public ActionCopyListOfExperimentsToMongo(MongoDB m, ArrayList<ExperimentReference> experiment) {
		super("Copy a list of experiments to this storage location");
		this.m = m;
		this.experimentlist = new ArrayList<ExperimentReference>();
		experimentlist.addAll(experiment);
		saveAnnotation = false;
	}
	
	public ActionCopyListOfExperimentsToMongo(MongoDB m, ArrayList<ExperimentReference> experiment, boolean annotationSave, boolean ignoreOutliers) {
		super("Copy a list of experiments to this storage location");
		this.m = m;
		this.ignoreOutliers = ignoreOutliers;
		this.experimentlist = new ArrayList<ExperimentReference>();
		experimentlist.addAll(experiment);
		this.saveAnnotation = annotationSave;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] sel = null;
		if (m != null)
			sel = new Object[] { m };
		else {
			if (MongoDB.getMongos().size() > 1) {
				sel = MyInputHelper.getInput(
						"Select the database-target:",
						"Target Selection", new Object[] { "Target", MongoDB.getMongos() });
			} else
				sel = new Object[] { MongoDB.getMongos().iterator().next() };
		}
		if (sel == null)
			return;
		
		this.m = (MongoDB) sel[0];
		
		ArrayList<ExperimentReference> expList = new ArrayList<ExperimentReference>();
		boolean filter = experimentlist.size() > 1;
		if (!filter) {
			expList.addAll(experimentlist);
		} else {
			ArrayList<Object> params = new ArrayList<Object>();
			TreeSet<String> dbNames = new TreeSet<String>();
			for (ExperimentReference experiment : experimentlist) {
				try {
					String sourceDB = experiment.getHeader().getOriginDbId().split(":")[1];
					if (sourceDB != null && !sourceDB.isEmpty())
						dbNames.add(sourceDB);
				} catch (Exception e) {
					// ignore
					dbNames.add("[no source DB]");
				}
			}
			for (String dbName : dbNames) {
				params.add(dbName);
				params.add(Boolean.FALSE);
			}
			Object[] res = MyInputHelper.getInput("Select the valid source database(s)", "Filter Experiment List", params.toArray());
			if (res == null)
				return;
			int idx = 0;
			ArrayList<String> dbNamesArr = new ArrayList<String>(dbNames);
			for (Object o : res) {
				Boolean i = (Boolean) o;
				String dbName = dbNamesArr.get(idx);
				System.out.println("?Accept " + dbName + ": " + i);
				if (i) {
					for (ExperimentReference experiment : experimentlist) {
						String sourceDB;
						try {
							sourceDB = experiment.getHeader().getOriginDbId().split(":")[1];
						} catch (Exception e) {
							sourceDB = "[no source DB]";
						}
						if (sourceDB != null && !sourceDB.isEmpty() && sourceDB.equals(dbName)) {
							System.out.println("Accept " + experiment.getExperimentName() + " from " + sourceDB);
							expList.add(experiment);
						}
					}
				}
				idx++;
			}
		}
		try {
			active = true;
			int i = 0, n = expList.size();
			for (ExperimentReference experiment : expList) {
				i++;
				status.setCurrentStatusText1("Load " + experiment.getExperimentName());
				task = "Load " + experiment.getExperimentName() + " (" + i + "/" + n + ")";
				ExperimentInterface exp = experiment.getData(status); // .clone();
				if (ignoreOutliers) {
					status.setCurrentStatusText1("Clone Experiment");
					exp = exp.clone();
					status.setCurrentStatusText1("Process Outliers");
					IAPservice.removeOutliers(exp);
				}
				exp.getHeader().setOriginDbId(exp.getHeader().getDatabaseId() + "");
				status.setCurrentStatusText1("Copy " + exp.getName());
				task = "Copy " + exp.getName() + " (" + i + "/" + n + ")";
				m.saveExperiment(exp, status);
			}
			task = "Copy finished " + i + "/" + n + "";
		} finally {
			active = false;
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (saveAnnotation)
			return "img/ext/gpl2/Gnome-Document-Save-64.png";
		if (active) {
			if (System.currentTimeMillis() % 1000 < 500)
				return "img/ext/transfer2.png";
			else
				return "img/ext/transfer22.png";
		} else
			return "img/ext/transfer2.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return getDefaultImage();
	}
	
	@Override
	public String getDefaultTitle() {
		if (m == null)
			return "INTERNAL ERROR: target db is NULL";
		else
			return (task.isEmpty() ? "" : "") +
					"To " + m.getDisplayName() + (task.isEmpty() ? "" : "<br>" + task);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}
