/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.TreeMap;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk.ag_ba.rmi_server.task_management.CloundManagerNavigationAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class MongoExperimentsNavigationAction extends AbstractNavigationAction {

	private NavigationButton src;
	private ArrayList<ExperimentHeaderInterface> experimentList;
	private final MongoDB m;

	public MongoExperimentsNavigationAction(MongoDB m) {
		super("Access " + m.getDisplayName());
		this.m = m;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewActionSet()
	 */
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();

		NavigationAction analyzeAction = new UploadImagesToCloud(true);

		NavigationButton analyzeEntity = new NavigationButton(analyzeAction, "Add Files", "img/ext/user-desktop.png",
							"img/ext/user-desktop.png", src.getGUIsetting());
		res.add(analyzeEntity);

		// gruppe => user => experiment

		if (experimentList == null) {
			res.add(Other.getServerStatusEntity(true, "Error Status", src.getGUIsetting()));
		} else {
			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> experiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			ArrayList<ExperimentHeaderInterface> trashed = new ArrayList<ExperimentHeaderInterface>();
			for (ExperimentHeaderInterface eh : experimentList) {
				String group = eh.getImportusergroup();
				if (group == null || group.length() == 0)
					group = "[no group]";
				String user = eh.getImportusername();
				if (user == null || user.length() == 0)
					user = "[no user]";
				if (eh.inTrash()) {
					trashed.add(eh);
					continue;
				}
				if (!experiments.containsKey(group))
					experiments.put(group, new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
				if (!experiments.get(group).containsKey(user))
					experiments.get(group).put(user, new ArrayList<ExperimentHeaderInterface>());
				experiments.get(group).get(user).add(eh);
			}

			res.add(new NavigationButton(new CloundManagerNavigationAction(m), src.getGUIsetting()));

			res.add(Other.getCalendarEntity(experiments, m, src.getGUIsetting()));

			for (String group : experiments.keySet()) {
				res.add(new NavigationButton(createMongoGroupNavigationAction(group, experiments.get(group)), src
									.getGUIsetting()));
			}

			if (trashed.size() > 0) {
				res.add(new NavigationButton(getTrashedExperimentsAction(trashed, m), src.getGUIsetting()));
			}
		}
		return res;
	}

	private NavigationAction getTrashedExperimentsAction(final ArrayList<ExperimentHeaderInterface> trashed, final MongoDB m) {
		NavigationAction res = new AbstractNavigationAction("Show content of trash can") {

			private NavigationButton src;

			@Override
			public String getDefaultImage() {
				return "img/ext/trash-delete2.png";
			}

			@Override
			public String getDefaultTitle() {
				return "Trash";
			}

			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				this.src = src;
			}

			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
				actions.add(Trash.getTrashEntity(trashed, DeletionCommand.EMPTY_TRASH_DELETE_ALL_TRASHED_IN_LIST,
									src.getGUIsetting(), m));
				for (ExperimentHeaderInterface exp : trashed)
					actions.add(getMongoExperimentButton(exp, src.getGUIsetting(), m));
				return actions;
			}
		};
		return res;
	}

	private NavigationAction createMongoGroupNavigationAction(final String group,
						final TreeMap<String, ArrayList<ExperimentHeaderInterface>> user2exp) {
		NavigationAction groupNav = new AbstractNavigationAction("Show User-Group Folder") {
			private NavigationButton src;

			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
				for (String user : user2exp.keySet()) {
					res.add(new NavigationButton(createMongoUserNavigationAction(user, user2exp.get(user)), src
										.getGUIsetting()));
				}
				return res;
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
			}

			@Override
			public String getDefaultImage() {
				return "img/ext/network-workgroup.png";
			}

			@Override
			public String getDefaultNavigationImage() {
				return "img/ext/network-workgroup-power.png";
			}

			@Override
			public String getDefaultTitle() {
				return group;
			}

		};
		return groupNav;
	}

	protected NavigationAction createMongoUserNavigationAction(final String user,
						final ArrayList<ExperimentHeaderInterface> experiments) {
		NavigationAction userNav = new AbstractNavigationAction("Show user folder") {
			private NavigationButton src;

			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
				for (ExperimentHeaderInterface exp : experiments) {
					res.add(getMongoExperimentButton(exp, src.getGUIsetting(), m));
				}
				return res;
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
			}

			@Override
			public String getDefaultImage() {
				return "img/ext/folder-remote.png";
			}

			@Override
			public String getDefaultNavigationImage() {
				return "img/ext/folder-remote-open.png";
			}

			@Override
			public String getDefaultTitle() {
				return user;
			}

		};
		return userNav;
	}

	public static NavigationButton getMongoExperimentButton(ExperimentHeaderInterface ei, GUIsetting guiSetting, MongoDB m) {
		NavigationAction action = new MongoOrLemnaTecExperimentNavigationAction(ei, m);
		NavigationButton exp = new NavigationButton(action, guiSetting);
		exp.setToolTipText("<html><table>" + "<tr><td>Experiment</td><td>" + ei.getExperimentname() + "</td></tr>"
							+ "<tr><td>Type</td><td>" + ei.getExperimentType() + "</td></tr>" + "<tr><td>Owner</td><td>"
							+ ei.getImportusername() + "</td></tr>" + "<tr><td>Import Time</td><td>" + ei.getImportdate()
							+ "</td></tr>" + "<tr><td>Remark</td><td>" + ei.getRemark() + "</td></tr>");
		return exp;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewNavigationSet(java.util.ArrayList)
	 */
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #performActionCalculateResults
	 * (de.ipk_gatersleben.ag_ba.graffiti.plugins.gui
	 * .navigation_model.NavigationGraphicalEntity)
	 */
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		status.setCurrentStatusText1("Establishing Connection");
		experimentList = m.getExperimentList();
		status.setCurrentStatusText1("");
	}
}
