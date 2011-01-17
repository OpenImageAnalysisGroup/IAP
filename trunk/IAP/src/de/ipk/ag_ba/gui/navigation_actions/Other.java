/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 23, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import info.clearthought.layout.TableLayout;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.ErrorMsg;
import org.ObjectRef;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.calendar.Calendar;
import de.ipk.ag_ba.gui.enums.DBEtype;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.DateUtils;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

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
						if (experimentName.getData(m) != null) {
							SupplementaryFilePanelMongoDB optSupplementaryPanel = new SupplementaryFilePanelMongoDB(m, experimentName.getData(m),
												experimentName.getExperimentName());
							ExperimentDataProcessingManager.getInstance().processData(experimentName.getData(m), pp, null,
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
				ne.setIcon(i);
			}
			
			result.add(ne);
		}
		
		return result;
	}
	
	public static NavigationButton getServerStatusEntity(final boolean includeLemnaTecStatus,
						GUIsetting guIsetting) {
		return getServerStatusEntity(includeLemnaTecStatus, "Check Status", guIsetting);
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
				if (includeLemnaTecStatus) {
					resultNavigationButtons.add(LemnaCam.getLemnaCamButton(src.getGUIsetting()));
					if (!IAPservice.isReachable("http://lemnacam.ipk-gatersleben.de"))
						resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				}
				
				boolean simpleIcons = true;
				
				String pc = "img/ext/network-workgroup-power.png";
				String pcOff = "img/ext/network-workgroup.png";
				
				boolean rLocal = IAPservice.isReachable("localhost");
				resultNavigationButtons.add(new NavigationButton(new CheckAvailabilityAction("localhost",
									simpleIcons ? "img/ext/computer.png" : "img/ext/computer.png"), src
									.getGUIsetting()));
				if (!rLocal)
					resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				boolean rBA13 = IAPservice.isReachable("ba-13.ipk-gatersleben.de");
				resultNavigationButtons.add(new NavigationButton(new CheckAvailabilityAction("BA-13",
									simpleIcons ? "img/ext/network-server.png" : "img/ext/dellR810_3.png"), src
									.getGUIsetting()));
				if (!rBA13)
					resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				boolean rBA24 = IAPservice.isReachable("ba-24.ipk-gatersleben.de");
				resultNavigationButtons.add(new NavigationButton(new CheckAvailabilityAction("BA-24",
									simpleIcons ? (rBA24 ? pc : pcOff) : "img/ext/macPro.png"), src
									.getGUIsetting()));
				if (!rBA24)
					resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				boolean rLemnaDB = IAPservice.isReachable("lemna-db.ipk-gatersleben.de");
				resultNavigationButtons.add(new NavigationButton(new CheckAvailabilityAction("lemna-db",
						simpleIcons ? "img/ext/network-server.png" : "img/ext/dellR810_3.png"), src
									.getGUIsetting()));
				if (!rLemnaDB)
					resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				boolean rBA03 = IAPservice.isReachable("ba-03.ipk-gatersleben.de");
				resultNavigationButtons.add(new NavigationButton(new CheckAvailabilityAction("BA-03",
									simpleIcons ? (rBA03 ? pc : pcOff) : "img/ext/delT7500.png"), src
									.getGUIsetting()));
				if (!rBA03)
					resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
				boolean rNW04 = IAPservice.isReachable("nw-04.ipk-gatersleben.de");
				resultNavigationButtons.add(new NavigationButton(new CheckAvailabilityAction("NW-04",
									simpleIcons ? (rNW04 ? pc : pcOff) : "img/ext/pc.png"), src
									.getGUIsetting()));
				if (!rNW04)
					resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
				
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
				return null;
			}
			
		};
		NavigationButton serverStatusEntity = new NavigationButton(serverStatusAction, title,
							"img/ext/network-server-status.png", guIsetting);
		return serverStatusEntity;
	}
	
	public static NavigationButton getCalendarEntity(
						final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final MongoDB m, GUIsetting guiSettings) {
		
		final ObjectRef refCalEnt = new ObjectRef();
		final ObjectRef refCalGui = new ObjectRef();
		
		NavigationAction calendarAction = new AbstractNavigationAction("Review or modify experiment plan calendar") {
			NavigationButton src;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				this.src = src;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(
								ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
				res.addAll(currentSet);
				res.add(src);
				return res;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> res = getExperimentNavigationActions(DBEtype.Omics, group2ei, m,
									refCalEnt, refCalGui, src.getGUIsetting());
				return res;
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				// TreeMap<String, Collection<ExperimentInfo>> group2ei, Calendar2
				// action
				Calendar calGui = new Calendar(group2ei, (Calendar2) refCalEnt.getObject());
				refCalGui.setObject(calGui);
				int b = 10;
				calGui.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
				return new MainPanelComponent(calGui);
			}
			
		};
		
		Calendar2 calendarEntity = new Calendar2("Calendar", "img/ext/calendar48.png", calendarAction, guiSettings);
		calendarEntity.setShowSpecificDay(true);
		refCalEnt.setObject(calendarEntity);
		return calendarEntity;
	}
	
	// private SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
	
	protected static NavigationButton getCalendarNavigationEntitiy(final boolean nextMonth,
						final ObjectRef refCalEnt, final ObjectRef refCalGui,
						final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final MongoDB m, final GUIsetting guIsetting) {
		// GregorianCalendar c = new GregorianCalendar();
		// c.setTime(((Calendar) refCalGui.getObject()).getCalendar().getTime());
		// if (nextMonth)
		// c.add(GregorianCalendar.MONTH, 1);
		// else
		// c. add(GregorianCalendar.MONTH, -1);
		// String m = sdf.format(c.getTime());
		NavigationButton nav = new NavigationButton(new AbstractNavigationAction("Select month") {
			@Override
			public void performActionCalculateResults(NavigationButton src) {
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(
								ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				((Calendar2) refCalEnt.getObject()).setShowSpecificDay(false);
				Calendar c = (Calendar) refCalGui.getObject();
				if (nextMonth)
					c.getCalendar().add(GregorianCalendar.MONTH, 1);
				else
					c.getCalendar().add(GregorianCalendar.MONTH, -1);
				c.updateGUI(false);
				ArrayList<NavigationButton> res = getExperimentNavigationActions(DBEtype.Phenotyping, group2ei, m, refCalEnt, refCalGui, guIsetting);
				return res;
			}
		}, nextMonth ? "Next" : "Previous", nextMonth ? "img/large_right.png" : "img/large_left.png", guIsetting);
		return nav;
	}
	
	private static ArrayList<NavigationButton> getExperimentNavigationActions(DBEtype dbeType,
						final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final MongoDB m, final ObjectRef refCalEnt,
						final ObjectRef refCalGui, GUIsetting guIsetting) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(getCalendarNavigationEntitiy(false, refCalEnt, refCalGui, group2ei, m, guIsetting));
		res.add(getCalendarNavigationEntitiy(true, refCalEnt, refCalGui, group2ei, m, guIsetting));
		
		// image-loading.png
		
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
				ei.setExperimentname("Planned Experiment");
				ei.setExperimenttype("Phenomics");
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
						
						// Document doc = Experiment.getEmptyDocument(experimentInfo);
						// try {
						// CallDBE2WebService.setExperiment(l, p,
						// info.getUserGroupVisibility(), experimentInfo
						// .getExperimentName(), doc);
						// } catch (Exception e) {
						// MainFrame.showMessageDialogWithScrollBars2(e.getMessage(),
						// "Error");
						// throw e;
						// }
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
		
		if (m != null) { // dbeType ==
			// DBEtype.Phenotyping &&
			NavigationButton scheduleExperiment = new NavigationButton(scheduleExperimentAction,
								"Schedule Experiment", "img/ext/image-loading.png", guIsetting);
			res.add(scheduleExperiment);
		}
		
		Calendar2 calEnt = (Calendar2) refCalEnt.getObject();
		String dayInfo = DateUtils.getDayInfo(calEnt.getCalendar());
		String monthInfo = DateUtils.getMonthInfo(calEnt.getCalendar());
		for (String k : group2ei.keySet()) {
			for (Collection<ExperimentHeaderInterface> eil : group2ei.get(k).values()) {
				for (ExperimentHeaderInterface ei : eil) {
					if (ei.getStartdate() == null)
						continue;
					if (calEnt.isShowSpecificDay()) {
						String dayA = DateUtils.getDayInfo(ei.getStartdate());
						String dayB = DateUtils.getDayInfo(ei.getImportdate());
						if (dayA.equals(dayInfo) || dayB.equals(dayInfo)) {
							NavigationButton exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei, guIsetting, m);
							res.add(exp);
						} else {
							if (calEnt.getCalendar().getTime().after(ei.getStartdate())
												&& calEnt.getCalendar().getTime().before(ei.getImportdate())) {
								NavigationButton exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei, guIsetting, m);
								res.add(exp);
							}
						}
					} else {
						String mA = DateUtils.getMonthInfo(ei.getStartdate());
						String mB = DateUtils.getMonthInfo(ei.getImportdate());
						if (mA.equals(monthInfo) || mB.equals(monthInfo)) {
							NavigationButton exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei, guIsetting, m);
							res.add(exp);
						} else {
							if (calEnt.getCalendar().getTime().after(ei.getStartdate())
												&& calEnt.getCalendar().getTime().before(ei.getImportdate())) {
								NavigationButton exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei, guIsetting, m);
								res.add(exp);
							}
						}
					}
				}
			}
		}
		return res;
	}
	
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
