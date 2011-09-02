package de.ipk.ag_ba.gui.actions;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.ObjectRef;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.calendar.Calendar;
import de.ipk.ag_ba.gui.enums.DBEtype;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.DateUtils;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;

public class ActionCalendar extends AbstractNavigationAction {
	
	private NavigationButton src;
	private final ObjectRef refCalEnt;
	private final ObjectRef refCalGui;
	private final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei;
	private final MongoDB m;
	
	public ActionCalendar(ObjectRef refCalEnt, ObjectRef refCalGui, TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, MongoDB m) {
		super("Review or modify experiment plan calendar");
		this.refCalEnt = refCalEnt;
		this.refCalGui = refCalGui;
		this.group2ei = group2ei;
		this.m = m;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Calendar";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		Calendar calGui = new Calendar(group2ei, (Calendar2) refCalEnt.getObject());
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
				c.updateGUI(false, false);
				ArrayList<NavigationButton> res = getExperimentNavigationActions(DBEtype.Phenotyping, group2ei, m, refCalEnt, refCalGui, guIsetting);
				return res;
			}
		}, nextMonth ? "Next" : "Previous", nextMonth ? "img/large_right.png" : "img/large_left.png", guIsetting);
		return nav;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = getExperimentNavigationActions(DBEtype.Omics, group2ei, m,
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
							NavigationButton exp = ActionMongoExperimentsNavigation.getMongoExperimentButton(ei, guIsetting, m);
							res.add(exp);
						} else {
							if (calEnt.getCalendar().getTime().after(ei.getStartdate())
									&& calEnt.getCalendar().getTime().before(ei.getImportdate())) {
								NavigationButton exp = ActionMongoExperimentsNavigation.getMongoExperimentButton(ei, guIsetting, m);
								res.add(exp);
							}
						}
					} else {
						String mA = DateUtils.getMonthInfo(ei.getStartdate());
						String mB = DateUtils.getMonthInfo(ei.getImportdate());
						if (mA.equals(monthInfo) || mB.equals(monthInfo)) {
							NavigationButton exp = ActionMongoExperimentsNavigation.getMongoExperimentButton(ei, guIsetting, m);
							res.add(exp);
						} else {
							if (calEnt.getCalendar().getTime().after(ei.getStartdate())
									&& calEnt.getCalendar().getTime().before(ei.getImportdate())) {
								NavigationButton exp = ActionMongoExperimentsNavigation.getMongoExperimentButton(ei, guIsetting, m);
								res.add(exp);
							}
						}
					}
				}
			}
		}
		return res;
	}
}
