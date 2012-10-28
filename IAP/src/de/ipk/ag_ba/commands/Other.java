/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 23, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.SystemOptions;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.lemnatec.ActionLemnaCamBarleyGH;
import de.ipk.ag_ba.commands.lemnatec.ActionLemnaCamMaizeGH;
import de.ipk.ag_ba.commands.mongodb.ActionMassCopyHistory;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BackupSupport;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk.ag_ba.server.task_management.MassCopySupport;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessor;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class Other {
	
	public static ArrayList<NavigationButton> getProcessExperimentDataWithVantedEntities(final MongoDB m, final ExperimentReference experimentName,
			GUIsetting guIsetting) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		
		ArrayList<AbstractExperimentDataProcessor> validProcessors = new ArrayList<AbstractExperimentDataProcessor>();
		ArrayList<AbstractExperimentDataProcessor> optIgnoredProcessors = null;
		for (ExperimentDataProcessor ep : ExperimentDataProcessingManager.getExperimentDataProcessors())
			// check if ep is not ignored
			if (optIgnoredProcessors == null || !optIgnoredProcessors.contains(ep.getClass())) {
				validProcessors.add((AbstractExperimentDataProcessor) ep);
			}
		
		for (Object o : validProcessors) {
			final AbstractExperimentDataProcessor pp = (AbstractExperimentDataProcessor) o;
			NavigationAction action = new AbstractNavigationAction("Analyze Data") {
				MainPanelComponent mpc;
				private NavigationButton src;
				
				@Override
				public void performActionCalculateResults(NavigationButton src) {
					this.src = src;
					try {
						ExperimentInterface ed = experimentName.getData(m);
						Collection<NumericMeasurementInterface> md = Substance3D.getAllMeasurements(ed);
						ed = MappingData3DPath.merge(md, true);
						if (ed != null) {
							SupplementaryFilePanelMongoDB optSupplementaryPanel = new SupplementaryFilePanelMongoDB(m, ed,
									experimentName.getExperimentName());
							ExperimentDataProcessingManager.getInstance().processData(ed, pp, null,
									optSupplementaryPanel, null);
							JComponent gui = IAPmain.showVANTED(true);
							gui.setBorder(BorderFactory.createLoweredBevelBorder());
							mpc = new MainPanelComponent(gui);
							
						}
					} catch (Exception err) {
						ErrorMsg.addErrorMessage(err);
					}
				}
				
				@Override
				public MainPanelComponent getResultMainPanel() {
					return mpc;
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewNavigationSet(
						ArrayList<NavigationButton> currentSet) {
					ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
					res.add(src);
					return res;
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewActionSet() {
					return new ArrayList<NavigationButton>();
				}
				
				@Override
				public boolean getProvidesActions() {
					return false;
				}
			};
			NavigationButton ne = new NavigationButton(action, pp.getShortName(), "img/vanted1_0.png",
					guIsetting);
			
			ImageIcon i = pp.getIcon();
			if (i != null) {
				i = new ImageIcon(GravistoService.getScaledImage(i.getImage(), -48, 48));
				ne.setIcon(i, "img/vanted1_0.png");
			}
			
			result.add(ne);
		}
		
		return result;
	}
	
	public static NavigationButton getServerStatusEntity(final boolean includeLemnaTecStatus,
			GUIsetting guIsetting) {
		return getServerStatusEntity(includeLemnaTecStatus, "System Status", guIsetting);
	}
	
	public static NavigationButton getServerStatusEntity(final boolean includeLemnaTecStatus, String title,
			GUIsetting guIsetting) {
		NavigationAction serverStatusAction = new AbstractNavigationAction("Check service availability") {
			private NavigationButton src;
			private final HashMap<String, ArrayList<String>> infoset = new HashMap<String, ArrayList<String>>();
			ArrayList<NavigationButton> resultNavigationButtons = new ArrayList<NavigationButton>();
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				this.src = src;
				infoset.clear();
				resultNavigationButtons.clear();
				
				// BackgroundTaskHelper.isTaskWithGivenReferenceRunning(referenceObject)
				
				if (SystemOptions.getInstance().getBoolean("GRID-COMPUTING", "remote_execution", true)) {
					ArrayList<NavigationAction> cloudHostList = new ArrayList<NavigationAction>();
					for (MongoDB m : MongoDB.getMongos()) {
						try {
							m.batchGetWorkTasksScheduledForStart(0);
							CloundManagerNavigationAction cmna = new CloundManagerNavigationAction(m,
									null,
									true);
							try {
								cmna.performActionCalculateResults(src);
								for (NavigationButton o : cmna.getResultNewActionSet())
									cloudHostList.add(o.getAction());
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e) {
							System.out.println(m.getDatabaseName() + " is not accessible!");
						}
					}
					
					if (cloudHostList.size() > 0) {
						ActionFolder cloudHosts = new ActionFolder(
								"Cloud Hosts", "Show overview of cloud computing hosts",
								cloudHostList.toArray(new NavigationAction[] {}), src.getGUIsetting());
						resultNavigationButtons.add(new NavigationButton(cloudHosts, src.getGUIsetting()));
					}
				}
				
				boolean showLTstorageTimeCheckIcon = SystemOptions.getInstance().getBoolean("LemnaTec-DB", "system_status_show_storage_time_check_icon", false);
				if (showLTstorageTimeCheckIcon)
					resultNavigationButtons.add(new NavigationButton(new CheckLtTimesAction(null), src.getGUIsetting()));
				
				boolean showLT = SystemOptions.getInstance().getBoolean("LemnaTec-DB", "show_icon", false);
				if (showLT)
					resultNavigationButtons.add(new NavigationButton(new ActionToggleSettingDefaultIsFalse(
							null, null,
							"Enable or disable the automated backup of LT data sets to the HSM file system",
							"Automatic Backup to HSM",
							"ARCHIVE|auto_daily_backup"), src.getGUIsetting()));
				
				if (showLT)
					resultNavigationButtons.add(new NavigationButton(new ActionBackupHistory("Show full backup history"), src.getGUIsetting()));
				
				BackgroundTaskStatusProviderSupportingExternalCall copyToHsmStatus = MassCopySupport.getInstance().getStatusProvider();
				Runnable startActionMassCopy = new Runnable() {
					@Override
					public void run() {
						final ThreadSafeOptions tso = new ThreadSafeOptions();
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									MassCopySupport.getInstance().performMassCopy(tso.getBval(0, false));
									tso.setBval(0, !tso.getBval(0, false));
								} catch (InterruptedException e) {
									e.printStackTrace();
									MongoDB.saveSystemErrorMessage("Mass Copy Execution Error", e);
								}
							}
						}, "mass copy sync");
						t.start();
					}
				};
				
				if (MongoDB.getMongos().size() > 0) {
					
					resultNavigationButtons.add(new NavigationButton(new ActionToggleSettingDefaultIsFalse(copyToHsmStatus,
							startActionMassCopy,
							"Enable or disable the automated copy of LT data sets to the MongoDB DBs",
							"Automatic DB-Copy",
							"GRID-STORAGE|auto_daily_fetch"), src.getGUIsetting()));
					
					resultNavigationButtons.add(new NavigationButton(new ActionMassCopyHistory("Show DB-Copy history"), src.getGUIsetting()));
				}
				if (includeLemnaTecStatus) {
					resultNavigationButtons.add(ActionLemnaCamMaizeGH.getLemnaCamButton(src.getGUIsetting()));
					
					resultNavigationButtons.add(ActionLemnaCamBarleyGH.getLemnaCamButton(src.getGUIsetting()));
					// if (!IAPservice.isReachable("http://lemnacam.ipk-gatersleben.de"))
					// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
					
					// if (!IAPservice.isReachable("http://ba-10.ipk-gatersleben.de"))
					// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				}
				
				boolean simpleIcons = true;
				
				String pc = IAPimages.getNetworkPConline();
				String pcOff = IAPimages.getNetworkPCoffline();
				
				// boolean rLocal = IAPservice.isReachable("localhost");
				// resultNavigationButtons.add(new NavigationButton(new ActionPortScan("localhost",
				// simpleIcons ? "img/ext/computer.png" : "img/ext/computer.png"), src
				// .getGUIsetting()));
				// if (!rLocal)
				// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				boolean checkServerAvailability = false;
				if (checkServerAvailability) {
					boolean rBA13 = IAPservice.isReachable("ba-13.ipk-gatersleben.de");
					if (!rBA13) {
						resultNavigationButtons.add(new NavigationButton(new ActionPortScan("BA-13",
								simpleIcons ? "img/ext/network-server.png" : "img/ext/dellR810_3.png"), src
								.getGUIsetting()));
						resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
					}
					
					boolean rBA24 = IAPservice.isReachable("ba-24.ipk-gatersleben.de");
					if (!rBA24) {
						resultNavigationButtons.add(new NavigationButton(new ActionPortScan("BA-24",
								simpleIcons ? (rBA24 ? pc : pcOff) : "img/ext/macPro.png"), src
								.getGUIsetting()));
						resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
					}
					
					boolean rLemnaDB = IAPservice.isReachable("lemna-db.ipk-gatersleben.de");
					if (!rLemnaDB) {
						resultNavigationButtons.add(new NavigationButton(new ActionPortScan("lemna-db",
								simpleIcons ? "img/ext/network-server.png" : "img/ext/dellR810_3.png"), src
								.getGUIsetting()));
						resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
					}
				}
				
				// boolean rBA03 = IAPservice.isReachable("ba-03.ipk-gatersleben.de");
				// resultNavigationButtons.add(new NavigationButton(new ActionPortScan("BA-03",
				// simpleIcons ? (rBA03 ? pc : pcOff) : "img/ext/delT7500.png"), src
				// .getGUIsetting()));
				// if (!rBA03)
				// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				// boolean rNW04 = IAPservice.isReachable("nw-04.ipk-gatersleben.de");
				// resultNavigationButtons.add(new NavigationButton(new PortScanAction("NW-04",
				// simpleIcons ? (rNW04 ? pc : pcOff) : "img/ext/pc.png"), src
				// .getGUIsetting()));
				// if (!rNW04)
				// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(
					ArrayList<NavigationButton> currentSet) {
				currentSet.add(src);
				return currentSet;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return resultNavigationButtons;
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				ArrayList<String> htmlTextPanels = new ArrayList<String>();
				htmlTextPanels.add(BackupSupport.getInstance().getHistory(4,
						"" +
								"<p>Backup-Status:<br><br><ul>",
						"<li>", "", ""));
				
				htmlTextPanels.add(MassCopySupport.getInstance().getHistory(4,
						"" +
								"<p>Automatic Copy-Status:<br><br><ul>",
						"<li>", "", ""));
				
				htmlTextPanels.add(SystemAnalysisExt.getStatus("<p>System-Status:<br><br><ul>", "<li>", "", ""));
				
				htmlTextPanels.add(SystemAnalysisExt.getStorageStatus("<p>Storage-Status:<br><br><ul>", "<li>", "", ""));
				
				return new MainPanelComponent(htmlTextPanels);
			}
			
		};
		NavigationButton serverStatusEntity = new NavigationButton(serverStatusAction, title,
				IAPimages.getCheckstatus(),
				guIsetting);
		return serverStatusEntity;
	}
	
	public static NavigationButton getCalendarEntity(
			final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final MongoDB m, GUIsetting guiSettings) {
		
		final ObjectRef refCalEnt = new ObjectRef();
		final ObjectRef refCalGui = new ObjectRef();
		
		NavigationAction calendarAction = new ActionCalendar(refCalEnt, refCalGui, group2ei, m);
		/*
		 * new AbstractNavigationAction("Review or modify experiment plan calendar") {
		 * NavigationButton src;
		 * @Override
		 * public void performActionCalculateResults(NavigationButton src) {
		 * this.src = src;
		 * }
		 * @Override
		 * public ArrayList<NavigationButton> getResultNewNavigationSet(
		 * ArrayList<NavigationButton> currentSet) {
		 * ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		 * res.addAll(currentSet);
		 * res.add(src);
		 * return res;
		 * }
		 * @Override
		 * public ArrayList<NavigationButton> getResultNewActionSet() {
		 * ArrayList<NavigationButton> res = getExperimentNavigationActions(DBEtype.Omics, group2ei, m,
		 * refCalEnt, refCalGui, src.getGUIsetting());
		 * return res;
		 * }
		 * @Override
		 * public MainPanelComponent getResultMainPanel() {
		 * // TreeMap<String, Collection<ExperimentInfo>> group2ei, Calendar2
		 * // action
		 * Calendar calGui = new Calendar(group2ei, (Calendar2) refCalEnt.getObject());
		 * refCalGui.setObject(calGui);
		 * int b = 10;
		 * calGui.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
		 * return new MainPanelComponent(calGui);
		 * }
		 * };
		 */
		Calendar2 calendarEntity = new Calendar2("Calendar", "img/ext/calendar48.png", calendarAction, guiSettings);
		calendarEntity.setShowSpecificDay(true);
		refCalEnt.setObject(calendarEntity);
		return calendarEntity;
	}
	
	// private SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
	
	private static void checkServerAvailabilityByPing(HashMap<String, ArrayList<String>> infoset, String name,
			String role, String host) {
		infoset.put(name, new ArrayList<String>());
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			boolean reachable = address.isReachable(1000);
			if (!reachable)
				throw new Exception("Host is not reachable within time limit of one second.");
			infoset.get(name).add("<h2>" + role + "</h2><hr><br>" + "" + "The " + role + " is powered on.");
			infoset.get(name).add("<br><b>Status result check: OK</b>");
			
		} catch (Exception e1) {
			infoset.get(name).add(
					"<h2>" + role + "</h2><hr><br>" + "" + "The " + role + " was not reachable within time limits.<br>"
							+ "The cause may be internet connectivity problems or server side<br>"
							+ "problems which may take some time to be corrected.<br><br>"
							+ "The availability of this server is monitored automatically.<br>"
							+ "Effort will be put on improving reliability of the service.<br>");
			infoset.get(name).add("Error-Details: " + e1.toString());
			infoset.get(name).add("<br><b>Status result check: ERROR</b>");
			
		}
	}
}
