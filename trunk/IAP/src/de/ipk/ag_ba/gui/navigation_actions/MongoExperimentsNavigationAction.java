/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.TreeMap;

import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class MongoExperimentsNavigationAction extends AbstractNavigationAction {
	
	private NavigationButton src;
	private ArrayList<ExperimentHeaderInterface> experimentList;
	private final MongoDB m;
	private final boolean limitToResuls;
	private final boolean limitToData;
	private String currentUser;
	
	public MongoExperimentsNavigationAction(MongoDB m, boolean limitToData, boolean limitToResults) {
		super("Access " + m.getDisplayName());
		this.limitToData = limitToData;
		this.limitToResuls = limitToResults;
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
		
		if (!limitToResuls) {
			res.add(new NavigationButton(new DomainLogoutAction(), src.getGUIsetting()));
			
			NavigationAction saveInCloudAction = new SaveExperimentInCloud(true);
			
			NavigationButton uploadFilesEntity = new NavigationButton(saveInCloudAction, "Add Files", "img/ext/user-desktop.png",
							"img/ext/user-desktop.png", src.getGUIsetting());
			res.add(uploadFilesEntity);
		}
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
				String user = IAPservice.getSetting(IAPoptions.GROUP_BY_COORDINATOR) ? eh.getCoordinator() : eh.getImportusername();
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
			
			if (!limitToResuls)
				if (currentUser == null || !currentUser.equals("public"))
					res.add(new NavigationButton(
							new CloundManagerNavigationAction(m, new MongoExperimentsNavigationAction(m, false, true)), src.getGUIsetting()));
			
			if (!limitToResuls)
				if (currentUser == null || !currentUser.equals("public"))
					res.add(Other.getCalendarEntity(experiments, m, src.getGUIsetting()));
			
			for (String group : experiments.keySet()) {
				if (limitToResuls && !group.toUpperCase().contains("ANALYSIS RESULTS"))
					continue;
				if (limitToData && group.toUpperCase().contains("ANALYSIS RESULTS"))
					continue;
				
				res.add(new NavigationButton(createMongoGroupNavigationAction(group
						+ " (" + experiments.get(group).size() + ")", experiments.get(group)), src
									.getGUIsetting()));
			}
			
			if (!limitToResuls)
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
				if (group.toUpperCase().contains("ANALYSIS RESULTS"))
					return IAPimages.getCloudResult();
				if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)"))
					return IAPimages.getPhytochamber();
				else
					if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)"))
						return IAPimages.getBarleyGreenhouse();
					else
						if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)"))
							return IAPimages.getMaizeGreenhouse();
						else
							return "img/ext/network-workgroup.png";
			}
			
			@Override
			public String getDefaultNavigationImage() {
				if (group.toUpperCase().contains("ANALYSIS RESULTS"))
					return IAPimages.getCloudResultActive();
				if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)"))
					return IAPimages.getPhytochamber();
				else
					if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)"))
						return IAPimages.getBarleyGreenhouse();
					else
						if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)"))
							return IAPimages.getMaizeGreenhouse();
						else
							return "img/ext/network-workgroup-power.png";
			}
			
			@Override
			public String getDefaultTitle() {
				String db = group;
				if (db.startsWith("APH_"))
					return "Phytoch. (20" + db.substring("APH_".length()) + ")";
				else
					if (db.startsWith("CGH_"))
						return "Maize Greenh. (20" + db.substring("CGH_".length()) + ")";
					else
						if (db.startsWith("BGH_"))
							return "Barley Greenh. (20" + db.substring("BGH_".length()) + ")";
						else
							return db;
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
				NavigationButton tb = new NavigationButton(
						new Trash(experiments, DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS, m),
						src.getGUIsetting());
				tb.setRightAligned(true);
				res.add(tb);
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
				return IAPimages.getFolderRemoteClosed();
			}
			
			@Override
			public String getDefaultNavigationImage() {
				return IAPimages.getFolderRemoteOpen();
			}
			
			@Override
			public String getDefaultTitle() {
				return user + " (" + experiments.size() + ")";
			}
			
		};
		return userNav;
	}
	
	public static NavigationButton getMongoExperimentButton(ExperimentHeaderInterface ei, GUIsetting guiSetting, MongoDB m) {
		NavigationAction action = new MongoOrLemnaTecExperimentNavigationAction(ei, m);
		NavigationButton exp = new NavigationButton(action, guiSetting);
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
		experimentList = m.getExperimentList(currentUser);
		status.setCurrentStatusText1("");
	}
	
	public void setLogin(String user) {
		this.currentUser = user;
	}
}
