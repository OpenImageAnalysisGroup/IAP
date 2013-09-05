/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionTrash;
import de.ipk.ag_ba.commands.DeletionCommand;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.commands.clima.ActionImportClimateData;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;

/**
 * @author klukas
 */
public class ActionMongoExperimentsNavigation extends AbstractNavigationAction {
	
	private NavigationButton src;
	private ArrayList<ExperimentHeaderInterface> experimentList;
	private final MongoDB m;
	private final boolean limitToResuls;
	private final boolean limitToData;
	private String currentUser;
	private int nVis, nAvail = 0;
	private boolean error;
	private String errorMsg;
	
	public ActionMongoExperimentsNavigation(MongoDB m, boolean limitToData, boolean limitToResults) {
		super("Access " + m.getDisplayName());
		this.limitToData = limitToData;
		this.limitToResuls = limitToResults;
		this.m = m;
	}
	
	@Override
	public String getDefaultTitle() {
		if (nAvail > 0)
			return m.getDisplayName() + " (" + nVis + "/" + nAvail + ")";
		else
			return m.getDisplayName();
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
		nVis = 0;
		
		if (error) {
			res.add(Other.getServerStatusEntity("<html><center>Connection Error<br>(" + errorMsg + ")</center>", src.getGUIsetting()));
		} else {
			if (!limitToResuls) {
				if (IAPmain.getRunMode() == IAPrunMode.WEB)
					res.add(new NavigationButton(new ActionDomainLogout(), src.getGUIsetting()));
				
				if (!SystemAnalysis.isHeadless() && IAPoptions.getInstance().getInteger("NEWS", "show_n_items", 0) > 0)
					res.add(new NavigationButton(new AddNewsAction(), src.getGUIsetting()));
				if (!SystemAnalysis.isHeadless()) {
					res.add(new NavigationButton(new ActionMongoDatabaseManagement(
							"Database Mangement", m, experimentList), src.getGUIsetting()));
				} else {
					res.add(new NavigationButton(new ActionMongoDatabaseServerStatus(
							"Show server status information", m, "serverStatus", "Server Status"), src.getGUIsetting()));
					res.add(new NavigationButton(new ActionMongoDatabaseServerStatus(
							"Show database statistics", m, new BasicDBObject("dbstats", 1), "Database Statistics"), src.getGUIsetting()));
				}
				
				if (SystemOptions.getInstance().getBoolean("File Import", "Show Load Files Icon", false)) {
					SaveExperimentInCloud saveInCloudAction = new SaveExperimentInCloud(true);
					saveInCloudAction.setMongoDB(m);
					NavigationButton uploadFilesEntity = new NavigationButton(saveInCloudAction, "Load Files", "img/ext/user-desktop.png",
							"img/ext/user-desktop.png", src.getGUIsetting());
					res.add(uploadFilesEntity);
				}
				res.add(new NavigationButton(new ActionImportClimateData(
						"Import greenhouse temperature data"), src.getGUIsetting()));
			}
			// gruppe => user => experiment
			
			if (experimentList == null) {
				res.add(Other.getServerStatusEntity("Error Status", src.getGUIsetting()));
			} else {
				TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> experiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
				LinkedHashSet<ExperimentHeaderInterface> trashed = new LinkedHashSet<ExperimentHeaderInterface>();
				for (ExperimentHeaderInterface eh : experimentList) {
					String type = IAPoptions.getSetting(IAPoptions.IAPoptionFields.GROUP_BY_EXPERIMENT_TYPE) ? eh.getExperimentType() : eh.getImportusergroup();
					if (type == null || type.length() == 0)
						type = "[no type]";
					String user = IAPoptions.getSetting(IAPoptions.IAPoptionFields.GROUP_BY_COORDINATOR) ? eh.getCoordinator() : eh.getImportusername();
					if (user == null || user.length() == 0)
						user = "[no user]";
					if (eh.inTrash()) {
						trashed.add(eh);
						nVis++;
						continue;
					}
					if (!experiments.containsKey(type))
						experiments.put(type, new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
					if (!experiments.get(type).containsKey(user))
						experiments.get(type).put(user, new ArrayList<ExperimentHeaderInterface>());
					experiments.get(type).get(user).add(eh);
				}
				
				if (!limitToResuls)
					if (currentUser == null || !currentUser.equals("public"))
						if (SystemOptions.getInstance().getBoolean("IAP", "grid_remote_execution", false))
							res.add(new NavigationButton(
									new CloundManagerNavigationAction(m, false),
									src.getGUIsetting()));
				
				if (!limitToResuls)
					if (currentUser == null || !currentUser.equals("public")) {
						NavigationAction scheduleExperimentAction = new AbstractNavigationAction("Schedule a new experiment") {
							
							private NavigationButton src;
							
							@Override
							public void performActionCalculateResults(NavigationButton src) {
								this.src = src;
							}
							
							@Override
							public ArrayList<NavigationButton> getResultNewNavigationSet(
									ArrayList<NavigationButton> currentSet) {
								ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
								res.add(src);
								return res;
							}
							
							@Override
							public MainPanelComponent getResultMainPanel() {
								ExperimentHeaderInterface ei = new ExperimentHeader();
								ei.setExperimentname("Dataset");
								ei.setExperimenttype("Imported Dataset");
								ei.setCoordinator(SystemAnalysis.getUserName());
								ei.setImportusername(SystemAnalysis.getUserName());
								ei.setStartdate(new Date());
								ei.setImportdate(new Date());
								final MyExperimentInfoPanel info = new MyExperimentInfoPanel();
								info.setExperimentInfo(m, ei, true, null);
								
								Substance md = new Substance();
								final Condition experimentInfo = new Condition(md);
								
								info.setSaveAction(new RunnableWithExperimentInfo() {
									@Override
									public void run(ExperimentHeaderInterface newProperties) throws Exception {
										experimentInfo.setExperimentInfo(newProperties);
									}
								});
								JComponent jp = TableLayout.getSplit(info, null, TableLayout.PREFERRED, TableLayout.FILL);
								jp.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
								jp = TableLayout.getSplitVertical(jp, null, TableLayout.PREFERRED, TableLayout.FILL);
								jp = TableLayout.getSplitVertical(jp, null, TableLayout.PREFERRED, TableLayout.FILL);
								return new MainPanelComponent(jp);
							}
							
							@Override
							public ArrayList<NavigationButton> getResultNewActionSet() {
								return new ArrayList<NavigationButton>();
							}
						};
						NavigationButton scheduleExperiment = new NavigationButton(scheduleExperimentAction,
								"Create Dataset",
								"img/ext/gpl2/Gnome-Text-X-Generic-Template-64.png", guiSetting);
						res.add(scheduleExperiment);
						res.add(Other.getCalendarEntity(experiments, m, src.getGUIsetting()));
					}
				
				for (String group : experiments.keySet()) {
					if (limitToResuls && !group.toUpperCase().contains("ANALYSIS RESULTS"))
						continue;
					if (limitToData && group.toUpperCase().contains("ANALYSIS RESULTS"))
						continue;
					
					res.add(new NavigationButton(createMongoGroupNavigationAction(group
							+ " (" + count(experiments.get(group)) + ")", experiments.get(group)), src
							.getGUIsetting()));
				}
				
				if (!limitToResuls)
					if (trashed.size() > 0) {
						res.add(new NavigationButton(getTrashedExperimentsAction(trashed, m), src.getGUIsetting()));
					}
				
				for (TreeMap<String, ArrayList<ExperimentHeaderInterface>> tm : experiments.values())
					for (ArrayList<ExperimentHeaderInterface> al : tm.values())
						nVis += al.size();
			}
		}
		
		return res;
	}
	
	private int count(TreeMap<String, ArrayList<ExperimentHeaderInterface>> treeMap) {
		int res = 0;
		if (treeMap != null) {
			for (ArrayList<ExperimentHeaderInterface> tm : treeMap.values()) {
				res += tm.size();
			}
		}
		return res;
	}
	
	public static NavigationAction getTrashedExperimentsAction(final LinkedHashSet<ExperimentHeaderInterface> trashed, final MongoDB m) {
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
				actions.add(ActionTrash.getTrashEntity(trashed, DeletionCommand.EMPTY_TRASH_DELETE_ALL_TRASHED_IN_LIST,
						src.getGUIsetting(), m));
				actions.add(ActionTrash.getTrashEntity(trashed, DeletionCommand.UNTRASH_ALL,
						src.getGUIsetting(), m));
				for (ExperimentHeaderInterface ehi : trashed) {
					ExperimentReference exp = new ExperimentReference(ehi, m);
					actions.add(getMongoExperimentButton(exp, src.getGUIsetting()));
				}
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
				if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)") || group.startsWith(IAPexperimentTypes.Phytochamber + ""))
					return IAPimages.getPhytochamber();
				else
					if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)")
							|| group.startsWith(IAPexperimentTypes.BarleyGreenhouse + ""))
						return IAPimages.getBarleyGreenhouse();
					else
						if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)")
								|| group.startsWith(IAPexperimentTypes.MaizeGreenhouse + ""))
							return IAPimages.getMaizeGreenhouse();
						else
							if (group.toUpperCase().startsWith("ROOT_") || group.toUpperCase().contains("(ROOT)")
									|| group.startsWith(IAPexperimentTypes.RootWaterScan + ""))
								return IAPimages.getRoots();
							else
								if (group.startsWith(IAPexperimentTypes.LeafImages + ""))
									return IAPimages.getLeafDiseaseImage();
								else
									if (group.startsWith(IAPexperimentTypes.Raps + ""))
										return IAPimages.getRapeseedImage();
									else
										return "img/ext/network-workgroup.png";
			}
			
			@Override
			public String getDefaultNavigationImage() {
				if (group.toUpperCase().contains("ANALYSIS RESULTS"))
					return IAPimages.getCloudResultActive();
				if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)") || group.startsWith(IAPexperimentTypes.Phytochamber + ""))
					return IAPimages.getPhytochamber();
				else
					if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)")
							|| group.startsWith(IAPexperimentTypes.BarleyGreenhouse + ""))
						return IAPimages.getBarleyGreenhouse();
					else
						if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)")
								|| group.startsWith(IAPexperimentTypes.MaizeGreenhouse + ""))
							return IAPimages.getMaizeGreenhouse();
						else
							if (group.toUpperCase().startsWith("ROOT_") || group.toUpperCase().contains("(ROOT)")
									|| group.startsWith(IAPexperimentTypes.RootWaterScan + ""))
								return IAPimages.getRoots();
							else
								if (group.startsWith(IAPexperimentTypes.LeafImages + ""))
									return IAPimages.getLeafDiseaseImage();
								else
									if (group.startsWith(IAPexperimentTypes.Raps + ""))
										return IAPimages.getRapeseedImage();
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
			final Collection<ExperimentHeaderInterface> experiments) {
		NavigationAction userNav = new AbstractNavigationAction("Show user folder") {
			private NavigationButton src;
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<ExperimentHeaderInterface> tempResults = new ArrayList<ExperimentHeaderInterface>();
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
				for (ExperimentHeaderInterface exp : experiments) {
					String n = exp.getExperimentName();
					if (n.replaceAll("§", "").length() == n.length() - 4
							|| n.replaceAll("§", "").length() == n.length() - 3)
						tempResults.add(exp);
					else {
						ExperimentReference e = new ExperimentReference(exp, m);
						res.add(getMongoExperimentButton(e, src.getGUIsetting()));
					}
				}
				if (tempResults.size() > 0)
					res.add(new NavigationButton(createSubFolderActionForTemporaryResults(tempResults), src.getGUIsetting()));
				NavigationButton tb = new NavigationButton(
						new ActionTrash(experiments, DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS, m),
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
	
	protected NavigationAction createSubFolderActionForTemporaryResults(final Collection<ExperimentHeaderInterface> experiments) {
		NavigationAction userNav = new AbstractNavigationAction("Intermediate results of image analysis calculations") {
			private NavigationButton src;
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
				TreeMap<Long, ArrayList<ExperimentHeaderInterface>> time2expList = new TreeMap<Long, ArrayList<ExperimentHeaderInterface>>();
				for (ExperimentHeaderInterface exp : experiments) {
					Long submissionTime = new Date().getTime();
					try {
						String n = exp.getExperimentName();
						String[] nn = n.split("§");
						if (nn[3] != null && nn[3].contains(","))
							nn[3] = nn[3].split(",")[0];
						submissionTime = Long.parseLong(nn[3]);
					} catch (Exception err) {
						System.err.println("ERROR: Problematic experiment name: " + exp.getExperimentName());
					}
					if (!time2expList.containsKey(submissionTime))
						time2expList.put(submissionTime, new ArrayList<ExperimentHeaderInterface>());
					
					time2expList.get(submissionTime).add(exp);
				}
				for (Long time : time2expList.keySet()) {
					String folderName = SystemAnalysis.getCurrentTime(time);
					res.add(new NavigationButton(createSubFolderActionForTemporaryResults2(folderName, time2expList.get(time)), src.getGUIsetting()));
				}
				NavigationButton tb = new NavigationButton(
						new ActionTrash(experiments, DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS, m),
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
			public String getDefaultTitle() {
				return "Temporary Data (" + experiments.size() + ")";
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
			
		};
		return userNav;
	}
	
	protected NavigationAction createSubFolderActionForTemporaryResults2(final String displayName, final Collection<ExperimentHeaderInterface> experiments) {
		NavigationAction userNav = new AbstractNavigationAction("Intermediate results of image analysis calculations") {
			private NavigationButton src;
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
				for (ExperimentHeaderInterface exp : experiments) {
					ExperimentReference e = new ExperimentReference(exp, m);
					res.add(getMongoExperimentButton(e, src.getGUIsetting()));
				}
				NavigationButton tb = new NavigationButton(
						new ActionTrash(experiments, DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS, m),
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
			public String getDefaultTitle() {
				return displayName + " (" + experiments.size() + ")";
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
			
		};
		return userNav;
	}
	
	public static NavigationButton getMongoExperimentButton(ExperimentReference ei, GUIsetting guiSetting) {
		return getMongoExperimentButton(
				ActionMongoExperimentsNavigation.getTempdataExperimentName(ei), ei, guiSetting);
	}
	
	public static NavigationButton getMongoExperimentButton(
			final String displayName,
			ExperimentReference ei, GUIsetting guiSetting) {
		ActionMongoOrLTexperimentNavigation action =
				new ActionMongoOrLTexperimentNavigation(ei);
		action.setOverrideTitle(displayName);
		
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
		final ThreadSafeOptions tso_LastDbResult = new ThreadSafeOptions();
		tso_LastDbResult.setLong(System.currentTimeMillis());
		Runnable r = new Runnable() {
			@Override
			public void run() {
				experimentList = m.getExperimentList(currentUser, status, tso_LastDbResult);
				boolean reSort = false;
				if (reSort)
					if (experimentList != null)
						Collections.sort(experimentList, new Comparator<ExperimentHeaderInterface>() {
							@Override
							public int compare(ExperimentHeaderInterface o1, ExperimentHeaderInterface o2) {
								return o1.getImportdate().compareTo(o2.getImportdate());
							}
						});
			}
		};
		error = false;
		errorMsg = "";
		
		Thread t = new Thread(r, "Read MonogDB Experiment List");
		t.start();
		do {
			Thread.sleep(100);
			long current = System.currentTimeMillis();
			int to = SystemOptions.getInstance().getInteger("GRID-STORAGE", "Data Source Read-Timeout (s)", 240);
			if (current - tso_LastDbResult.getLong() > to * 1000) {
				t.interrupt();
				error = true;
				errorMsg = "time out - " + to + "s";
				break;
			}
		} while (t.isAlive());
		
		nAvail = experimentList != null ? experimentList.size() : 0;
		status.setCurrentStatusText1("");
	}
	
	public void setLogin(String user) {
		this.currentUser = user;
	}
	
	public static String getTempdataExperimentName(ExperimentReference exp) {
		String n = exp.getExperimentName();
		if (n == null || !n.contains("§"))
			return n;
		try {
			String[] nn = n.split("§");
			if (nn[0].indexOf(".") > 0)
				nn[0] = nn[0].substring(nn[0].lastIndexOf(".") + ".".length());
			if (nn[3] != null && nn[3].contains(","))
				nn[3] = nn[3].split(",")[0];
			String start = SystemAnalysis.getCurrentTime(Long.parseLong(nn[3]));
			n = "" + nn[1] + "/" + nn[2] + " <small>" + start + "</small>";
		} catch (Exception err) {
			System.err.println("ERROR: Problematic experiment name: " + exp.getExperimentName());
		}
		return n;
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return "img/ext/network-mongo.png";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/network-mongo-gray.png";
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return false;
	}
}
