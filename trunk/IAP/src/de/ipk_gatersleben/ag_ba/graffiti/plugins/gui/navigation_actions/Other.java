/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 23, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import info.clearthought.layout.TableLayout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.calendar.Calendar;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.enums.DBEtype;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.DateUtils;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.MyExperimentInfoPanel;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart.AIPmain;
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
 * 
 */
public class Other {

	public static ArrayList<NavigationGraphicalEntity> getProcessExperimentDataWithVantedEntities(final String login,
			final String pass, final ExperimentReference experimentName) {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>();

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
				@Override
				public void performActionCalculateResults(NavigationGraphicalEntity src) {
					try {
						if (experimentName.getData() != null) {
							SupplementaryFilePanelMongoDB optSupplementaryPanel = new SupplementaryFilePanelMongoDB(login,
									pass, experimentName.getData(), experimentName.getExperimentName());
							ExperimentDataProcessingManager.getInstance().processData(experimentName.getData(), pp, null,
									optSupplementaryPanel, null);
							AIPmain.showVANTED();
						} else {
							// final Document doc =
							// CallDBE2WebService.getExperiment(login, pass,
							// experimentName
							// .getExperimentName());
							//
							// Runnable rOK = new Runnable() {
							// public void run() {
							// SupplementaryFilePanel optSupplementaryPanel = new
							// SupplementaryFilePanel(login, pass, doc,
							// experimentName.getExperimentName());
							// Experiment experiment =
							// Experiment.getExperimentFromDOM(doc);
							// ExperimentDataProcessingManager.getInstance().processData(experiment,
							// pp, null,
							// optSupplementaryPanel, null);
							// }
							// };
							// AIPmain.showVANTED();
							// DBE.downloadPrimaryData(experimentName.getExperimentName(),
							// login, pass, doc, rOK);
						}
					} catch (Exception err) {
						ErrorMsg.addErrorMessage(err);
					}
				}

				@Override
				public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
						ArrayList<NavigationGraphicalEntity> currentSet) {
					return null;
				}

				@Override
				public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
					return null;
				}
			};
			NavigationGraphicalEntity ne = new NavigationGraphicalEntity(action, pp.getShortName(), "img/vanted1_0.png");

			ne.setProvidesActions(false);

			ImageIcon i = pp.getIcon();
			if (i != null) {
				i = new ImageIcon(GravistoService.getScaledImage(i.getImage(), -48, 48));
				ne.setIcon(i);
			}

			result.add(ne);
		}

		return result;
	}

	public static NavigationGraphicalEntity getServerStatusEntity(final boolean includeLemnaTecStatus) {
		return getServerStatusEntity(includeLemnaTecStatus, "Check Status");
	}

	public static NavigationGraphicalEntity getServerStatusEntity(final boolean includeLemnaTecStatus, String title) {
		NavigationAction serverStatusAction = new AbstractNavigationAction("Check service availability") {
			private NavigationGraphicalEntity src;
			private final HashMap<String, ArrayList<String>> infoset = new HashMap<String, ArrayList<String>>();

			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) {
				this.src = src;
				infoset.clear();

				checkServerAvailabilityByPing(infoset, "NW-04", "IAP (de) Cloud Database Master Server NW-04",
						"nw-04.ipk-gatersleben.de");

				checkServerAvailabilityByPing(infoset, "BA-13", "IAP (de) Cloud Analysis Server BA-13",
						"ba-13.ipk-gatersleben.de");

				checkServerAvailabilityByPing(infoset, "BA-24", "IAP (de) Cloud Analysis Compute Workstation BA-24",
						"ba-24.ipk-gatersleben.de");

				// try {
				// infoset.put("dbe", new ArrayList<String>());
				// infoset.get("dbe").add("<h2>DBE Web Service</h2><hr>");
				// URL url = new URL(CallDBE2WebService.urlString);
				// URLConnection urlConnection = url.openConnection();
				// urlConnection.setConnectTimeout(1000);
				// urlConnection.setReadTimeout(1000);
				//
				// HashSet<String> validHeaders = new HashSet<String>();
				// validHeaders.add("Date");
				// validHeaders.add("Via");
				// validHeaders.add("Server");
				// validHeaders.add("X-Powered-By");
				//
				// Map<String, List<String>> headers =
				// urlConnection.getHeaderFields();
				// Set<Map.Entry<String, List<String>>> entrySet =
				// headers.entrySet();
				// for (Map.Entry<String, List<String>> entry : entrySet) {
				// String headerName = entry.getKey();
				//
				// if (!validHeaders.contains(headerName))
				// continue;
				//
				// StringBuilder sb = new StringBuilder();
				// if (headerName != null)
				// sb.append(headerName + ": ");
				// List<String> headerValues = entry.getValue();
				// for (String value : headerValues) {
				// sb.append(value + " ");
				// }
				// infoset.get("dbe").add(sb.toString());
				// }
				// if (entrySet.size() == 0)
				// throw new
				// Exception("Read timed out: Headers could not be retrieved within time bounds.");
				// infoset.get("dbe").add("<br><b>Status result check: OK</b>");
				// } catch (Exception e) {
				// if (e.toString().indexOf("Read timed out") >= 0)
				// infoset
				// .get("dbe")
				// .add(
				// ""
				// + "DBE2 Web Service was not reachable within time limits.<br>"
				// +
				// "The cause may be internet connectivity problems or server side<br>"
				// + "problems which may take some time to be corrected.<br>"
				// +
				// "Please write an E-Mail to <a href=\"mailto:mehlhorn@ipk-gatersleben.de\">mehlhorn@ipk-gatersleben.de</a> if this<br>"
				// + "problem persists.<br><br>"
				// + "The Web Service availability is monitored automatically.<br>"
				// + "Effort will be put on improving reliability of the service.");
				// else
				// infoset.get("dbe").add(e.toString());
				// infoset.get("dbe").add("<br><b>Status result check: ERROR</b>");
				// }

				try {
					infoset.put("vanted", new ArrayList<String>());
					infoset.get("vanted").add("<h2>http://vanted.ipk-gatersleben.de</h2><hr>");
					URL url = new URL("http://vanted.ipk-gatersleben.de");
					URLConnection urlConnection = url.openConnection();
					urlConnection.setConnectTimeout(1000);
					urlConnection.setReadTimeout(1000);
					Map<String, List<String>> headers = urlConnection.getHeaderFields();
					Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();

					HashSet<String> validHeaders = new HashSet<String>();
					validHeaders.add("Date");
					validHeaders.add("Via");
					validHeaders.add("Server");
					validHeaders.add("X-Powered-By");

					for (Map.Entry<String, List<String>> entry : entrySet) {
						String headerName = entry.getKey();

						if (!validHeaders.contains(headerName))
							continue;

						StringBuilder sb = new StringBuilder();
						if (headerName != null)
							sb.append(headerName + ": ");
						List<String> headerValues = entry.getValue();
						for (String value : headerValues) {
							sb.append(value + " ");
						}
						infoset.get("vanted").add(sb.toString());
					}
					InputStream inputStream = urlConnection.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					String line = bufferedReader.readLine();
					while (line != null) {
						// infoset.get("vanted").add(line);
						line = bufferedReader.readLine();
					}
					bufferedReader.close();
					infoset.get("vanted").add("<br><b>Status result check: OK</b>");

				} catch (Exception e) {
					if (e.toString().indexOf("Read timed out") >= 0)
						infoset.get("vanted").add(
								"" + "The VANTED Web Site was not reachable within time limits.<br>"
										+ "The cause may be internet connectivity problems or server side<br>"
										+ "problems which may take some time to be corrected.<br><br>"
										+ "The Web Server availability is monitored automatically.<br>"
										+ "Effort will be put on improving reliability of the service.");
					else
						infoset.get("vanted").add(e.toString());
					infoset.get("vanted").add("<br><b>Status result check: ERROR</b>");
				}
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
					ArrayList<NavigationGraphicalEntity> currentSet) {
				currentSet.add(src);
				return currentSet;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
				if (includeLemnaTecStatus)
					res.add(LemnaCam.getLemnaCamButton());
				return res;
			}

			@Override
			public MainPanelComponent getResultMainPanel() {
				ArrayList<String> values = new ArrayList<String>();
				for (String key : infoset.keySet()) {
					String s = "<html>" + StringManipulationTools.getStringList(infoset.get(key), "<br>");
					values.add(s);
				}
				return new MainPanelComponent(values);
			}

		};
		NavigationGraphicalEntity serverStatusEntity = new NavigationGraphicalEntity(serverStatusAction, title,
				"img/ext/network-server-status.png");
		return serverStatusEntity;
	}

	public static NavigationGraphicalEntity getCalendarEntity(
			final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final String l,
			final String p) {

		final ObjectRef refCalEnt = new ObjectRef();
		final ObjectRef refCalGui = new ObjectRef();

		NavigationAction calendarAction = new AbstractNavigationAction("Review or modify experiment plan calendar") {
			NavigationGraphicalEntity src;

			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) {
				this.src = src;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
					ArrayList<NavigationGraphicalEntity> currentSet) {
				ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
				res.addAll(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				ArrayList<NavigationGraphicalEntity> res = getExperimentNavigationActions(DBEtype.Omics, group2ei, l, p,
						refCalEnt, refCalGui);
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

		Calendar2 calendarEntity = new Calendar2("Calendar", "img/ext/calendar48.png", calendarAction);
		calendarEntity.setShowSpecificDay(true);
		refCalEnt.setObject(calendarEntity);
		return calendarEntity;
	}

	// private SimpleDateFormat sdf = new SimpleDateFormat("MMMM");

	protected static NavigationGraphicalEntity getCalendarNavigationEntitiy(final boolean nextMonth,
			final ObjectRef refCalEnt, final ObjectRef refCalGui,
			final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final String l,
			final String p) {
		// GregorianCalendar c = new GregorianCalendar();
		// c.setTime(((Calendar) refCalGui.getObject()).getCalendar().getTime());
		// if (nextMonth)
		// c.add(GregorianCalendar.MONTH, 1);
		// else
		// c. add(GregorianCalendar.MONTH, -1);
		// String m = sdf.format(c.getTime());
		NavigationGraphicalEntity nav = new NavigationGraphicalEntity(new AbstractNavigationAction("Select month") {
			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) {
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(
					ArrayList<NavigationGraphicalEntity> currentSet) {
				return currentSet;
			}

			@Override
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				((Calendar2) refCalEnt.getObject()).setShowSpecificDay(false);
				Calendar c = (Calendar) refCalGui.getObject();
				if (nextMonth)
					c.getCalendar().add(GregorianCalendar.MONTH, 1);
				else
					c.getCalendar().add(GregorianCalendar.MONTH, -1);
				c.updateGUI(false);
				ArrayList<NavigationGraphicalEntity> res = getExperimentNavigationActions(DBEtype.Phenotyping, group2ei, l,
						p, refCalEnt, refCalGui);
				return res;
			}
		}, nextMonth ? "Next" : "Previous", nextMonth ? "img/large_right.png" : "img/large_left.png");
		return nav;
	}

	private static ArrayList<NavigationGraphicalEntity> getExperimentNavigationActions(DBEtype dbeType,
			final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final String l,
			final String p, final ObjectRef refCalEnt, final ObjectRef refCalGui) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
		res.add(getCalendarNavigationEntitiy(false, refCalEnt, refCalGui, group2ei, l, p));
		res.add(getCalendarNavigationEntitiy(true, refCalEnt, refCalGui, group2ei, l, p));

		// image-loading.png

		NavigationAction scheduleExperimentAction = new AbstractNavigationAction("Schedule a new experiment") {

			private NavigationGraphicalEntity src;

			@Override
			public void performActionCalculateResults(NavigationGraphicalEntity src) {
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
			public MainPanelComponent getResultMainPanel() {
				ExperimentHeaderInterface ei = new ExperimentHeader();
				ei.setExperimentname("Planned Experiment");
				ei.setExperimenttype("Phenomics");
				ei.setCoordinator(SystemAnalysis.getUserName());
				ei.setImportusername(SystemAnalysis.getUserName());
				ei.setStartdate(new Date());
				ei.setImportdate(new Date());
				final MyExperimentInfoPanel info = new MyExperimentInfoPanel();
				info.setExperimentInfo(l, p, ei, true, null);

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
			public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
				return new ArrayList<NavigationGraphicalEntity>();
			}
		};

		if (!l.equals("internet")) { // dbeType == DBEtype.Phenotyping &&
			NavigationGraphicalEntity scheduleExperiment = new NavigationGraphicalEntity(scheduleExperimentAction,
					"Schedule Experiment", "img/ext/image-loading.png");
			res.add(scheduleExperiment);
		}

		Calendar2 calEnt = (Calendar2) refCalEnt.getObject();
		String dayInfo = DateUtils.getDayInfo(calEnt.getCalendar());
		String monthInfo = DateUtils.getMonthInfo(calEnt.getCalendar());
		for (String k : group2ei.keySet()) {
			for (Collection<ExperimentHeaderInterface> eil : group2ei.get(k).values()) {
				for (ExperimentHeaderInterface ei : eil) {
					if (calEnt.isShowSpecificDay()) {
						String dayA = DateUtils.getDayInfo(ei.getStartdate());
						String dayB = DateUtils.getDayInfo(ei.getImportdate());
						if (dayA.equals(dayInfo) || dayB.equals(dayInfo)) {
							NavigationGraphicalEntity exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei);
							res.add(exp);
						} else {
							if (calEnt.getCalendar().getTime().after(ei.getStartdate())
									&& calEnt.getCalendar().getTime().before(ei.getImportdate())) {
								NavigationGraphicalEntity exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei);
								res.add(exp);
							}
						}
					} else {
						String mA = DateUtils.getMonthInfo(ei.getStartdate());
						String mB = DateUtils.getMonthInfo(ei.getImportdate());
						if (mA.equals(monthInfo) || mB.equals(monthInfo)) {
							NavigationGraphicalEntity exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei);
							res.add(exp);
						} else {
							if (calEnt.getCalendar().getTime().after(ei.getStartdate())
									&& calEnt.getCalendar().getTime().before(ei.getImportdate())) {
								NavigationGraphicalEntity exp = MongoExperimentsNavigationAction.getMongoExperimentButton(ei);
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
