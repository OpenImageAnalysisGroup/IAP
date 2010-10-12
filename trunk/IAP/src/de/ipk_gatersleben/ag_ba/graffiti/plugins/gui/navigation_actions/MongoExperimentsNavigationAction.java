/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.util.ArrayList;
import java.util.TreeMap;

import rmi_server.task_management.CloundManagerNavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class MongoExperimentsNavigationAction extends AbstractNavigationAction {

	private NavigationGraphicalEntity src;
	private ArrayList<ExperimentHeader> experimentList;
	private final String login;
	private final String pass;

	public MongoExperimentsNavigationAction(String login, String pass) {
		super("Access IAP Systems Biology Cloud Service");
		this.login = login;
		this.pass = pass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewActionSet()
	 */
	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();

		NavigationAction analyzeAction = new UploadImagesToCloud(true);

		NavigationGraphicalEntity analyzeEntity = new NavigationGraphicalEntity(analyzeAction, "Add Files",
				"img/ext/user-desktop.png", "img/ext/user-desktop.png");
		res.add(analyzeEntity);

		// gruppe => user => experiment

		if (experimentList == null) {
			res.add(Other.getServerStatusEntity(true, "Error Status"));
		} else {
			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> experiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			ArrayList<ExperimentHeaderInterface> trashed = new ArrayList<ExperimentHeaderInterface>();
			for (ExperimentHeader eh : experimentList) {
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

			res.add(new NavigationGraphicalEntity(new CloundManagerNavigationAction(login, pass)));

			res.add(Other.getCalendarEntity(experiments, login, pass));

			for (String group : experiments.keySet()) {
				res.add(new NavigationGraphicalEntity(createMongoGroupNavigationAction(group, experiments.get(group))));
			}

			if (trashed.size() > 0) {
				res.add(new NavigationGraphicalEntity(getTrashedExperimentsAction(trashed)));
			}
		}
		return res;
	}

	private NavigationAction getTrashedExperimentsAction(final ArrayList<ExperimentHeaderInterface> trashed) {
		NavigationAction res = new AbstractNavigationAction("Show content of trash can") {

			private NavigationGraphicalEntity src;

			@Override
			public String getDefaultImage() {
				return "img/ext/trash.png";
			}

			@Override
			public String getDefaultTitle() {
				return "Trash";
			}

			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
				this.src = src;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
					ArrayList<NavigationGraphicalEntity> currentSet) {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				ArrayList<NavigationGraphicalEntity> actions = new ArrayList<NavigationGraphicalEntity>();
				for (ExperimentHeaderInterface exp : trashed)
					actions.add(getMongoExperimentButton(exp));
				return actions;
			}
		};
		return res;
	}

	private NavigationAction createMongoGroupNavigationAction(final String group,
			final TreeMap<String, ArrayList<ExperimentHeaderInterface>> user2exp) {
		NavigationAction groupNav = new AbstractNavigationAction("Show User-Group Folder") {
			private NavigationGraphicalEntity src;

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
				for (String user : user2exp.keySet()) {
					res.add(new NavigationGraphicalEntity(createMongoUserNavigationAction(user, user2exp.get(user))));
				}
				return res;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
					ArrayList<NavigationGraphicalEntity> currentSet) {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
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
			private NavigationGraphicalEntity src;

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
				for (ExperimentHeaderInterface exp : experiments) {
					res.add(getMongoExperimentButton(exp));
				}
				return res;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
					ArrayList<NavigationGraphicalEntity> currentSet) {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
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

	public static NavigationGraphicalEntity getMongoExperimentButton(ExperimentHeaderInterface ei) {
		NavigationAction action = new MongoOrLemnaTecExperimentNavigationAction(ei);
		NavigationGraphicalEntity exp = new NavigationGraphicalEntity(action);
		exp.setToolTipText("<html><table>" + "<tr><td>Experiment</td><td>" + ei.getExperimentname() + "</td></tr>"
				+ "<tr><td>Type</td><td>" + ei.getExperimentType() + "</td></tr>" + "<tr><td>Owner</td><td>"
				+ ei.getImportusername() + "</td></tr>" + "<tr><td>Import Time</td><td>" + ei.getImportdate()
				+ "</td></tr>" + "<tr><td>Remark</td><td>" + ei.getRemark() + "</td></tr>");
		return exp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewNavigationSet(java.util.ArrayList)
	 */
	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #performActionCalculateResults
	 * (de.ipk_gatersleben.ag_ba.graffiti.plugins.gui
	 * .navigation_model.NavigationGraphicalEntity)
	 */
	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		this.src = src;
		status.setCurrentStatusText1("Establishing Connection");
		experimentList = new MongoDB().getExperimentList();
		status.setCurrentStatusText1("");
	}
}
