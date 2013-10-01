package de.ipk.ag_ba.commands;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.ObjectRef;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.calendar.Calendar;
import de.ipk.ag_ba.gui.calendar.NavigationButtonCalendar2;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.DateUtils;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;

public class ActionCalendar extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private NavigationButton src;
	private final ObjectRef refCalEnt;
	private final ObjectRef refCalGui;
	private final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei;
	private final MongoDB m;
	
	public ActionCalendar(ObjectRef refCalEnt, ObjectRef refCalGui, TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, MongoDB m) {
		super("Locate experiments in the calendar view");
		this.refCalEnt = refCalEnt;
		this.refCalGui = refCalGui;
		this.group2ei = group2ei;
		this.m = m;
	}
	
	@Override
	public String getDefaultTitle() {
		Calendar c = (Calendar) refCalGui.getObject();
		if (c == null)
			return "Calendar";
		else
			return c.getCalendar().getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.ENGLISH) + " "
					+ c.getCalendar().get(GregorianCalendar.YEAR);
		
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		Calendar calGui = new Calendar(group2ei, (NavigationButtonCalendar2) refCalEnt.getObject());
		refCalGui.setObject(calGui);
		int b = 10;
		calGui.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
		return new MainPanelComponent(calGui);
	}
	
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
			public String getDefaultTitle() {
				Calendar c = (Calendar) refCalGui.getObject();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTimeInMillis(c.getCalendar().getTimeInMillis());
				if (nextMonth)
					gc.add(GregorianCalendar.MONTH, 1);
				else
					gc.add(GregorianCalendar.MONTH, -1);
				return gc.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.ENGLISH);
			}
			
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
				((NavigationButtonCalendar2) refCalEnt.getObject()).setShowSpecificDay(false);
				Calendar c = (Calendar) refCalGui.getObject();
				if (nextMonth)
					c.getCalendar().add(GregorianCalendar.MONTH, 1);
				else
					c.getCalendar().add(GregorianCalendar.MONTH, -1);
				c.updateGUI(false, false);
				ArrayList<NavigationButton> res = getExperimentNavigationActions(AnalysisCommandType.Phenotyping, group2ei, m, refCalEnt, refCalGui, guIsetting);
				return res;
			}
		},
				null, // nextMonth ? "Next" : "Previous",
				nextMonth ? "img/large_right.png" : "img/large_left.png", guIsetting);
		return nav;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = getExperimentNavigationActions(AnalysisCommandType.Omics, group2ei, m,
				refCalEnt, refCalGui, src.getGUIsetting());
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(src);
		return res;
	}
	
	private static ArrayList<NavigationButton> getExperimentNavigationActions(AnalysisCommandType dbeType,
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
				final ExperimentHeaderInfoPanel info = new ExperimentHeaderInfoPanel();
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
		
		if (m != null) { // dbeType ==
			// DBEtype.Phenotyping &&
			NavigationButton scheduleExperiment = new NavigationButton(scheduleExperimentAction,
					m != null ? "Schedule Experiment" : "Create Dataset",
					m != null ? "img/ext/image-loading.png" : "img/ext/gpl2/Gnome-Text-X-Generic-Template-64.png", guIsetting);
			res.add(scheduleExperiment);
		}
		
		NavigationButtonCalendar2 calEnt = (NavigationButtonCalendar2) refCalEnt.getObject();
		String dayInfo = DateUtils.getDayInfo(calEnt.getCalendar());
		String monthInfo = DateUtils.getMonthInfo(calEnt.getCalendar());
		for (String k : group2ei.keySet()) {
			for (Collection<ExperimentHeaderInterface> eil : group2ei.get(k).values()) {
				if (eil != null)
					for (ExperimentHeaderInterface ehi : eil) {
						if (ehi == null || ehi.getStartdate() == null)
							continue;
						ExperimentReference ei = new ExperimentReference(ehi, m);
						if (calEnt.isShowSpecificDay()) {
							String dayA = DateUtils.getDayInfo(ei.getHeader().getStartdate());
							String dayB = DateUtils.getDayInfo(ei.getHeader().getImportdate());
							if (dayA.equals(dayInfo) || dayB.equals(dayInfo)) {
								NavigationButton exp =
										ActionMongoExperimentsNavigation.getMongoExperimentButton(
												ei, guIsetting);
								res.add(exp);
							} else {
								if (calEnt.getCalendar().getTime().after(ei.getHeader().getStartdate())
										&& calEnt.getCalendar().getTime().before(ei.getHeader().getImportdate())) {
									NavigationButton exp =
											ActionMongoExperimentsNavigation.getMongoExperimentButton(
													ei, guIsetting);
									res.add(exp);
								}
							}
						} else {
							String mA = DateUtils.getMonthInfo(ei.getHeader().getStartdate());
							String mB = DateUtils.getMonthInfo(ei.getHeader().getImportdate());
							if (mA.equals(monthInfo) || mB.equals(monthInfo)) {
								NavigationButton exp = ActionMongoExperimentsNavigation.getMongoExperimentButton(ei, guIsetting);
								res.add(exp);
							} else {
								if (calEnt.getCalendar().getTime().after(ei.getHeader().getStartdate())
										&& calEnt.getCalendar().getTime().before(ei.getHeader().getImportdate())) {
									NavigationButton exp = ActionMongoExperimentsNavigation.getMongoExperimentButton(ei, guIsetting);
									res.add(exp);
								}
							}
						}
					}
			}
		}
		return res;
	}
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		NavigationButtonCalendar2 calEnt = (NavigationButtonCalendar2) refCalEnt.getObject();
		if (calEnt != null)
			calEnt.setShowSpecificDay(false);
		return true;
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		// empty
	}
}
