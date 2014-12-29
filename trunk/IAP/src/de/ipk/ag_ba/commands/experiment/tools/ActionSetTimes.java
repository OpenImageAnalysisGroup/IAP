package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JLabel;

import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

/**
 * @author klukas
 */
public class ActionSetTimes extends AbstractNavigationAction implements ActionDataProcessing {
	private static final String SECONDS_WITHIN_1 = "Seconds within 1 ";
	private MongoDB m;
	private ExperimentReferenceInterface experiment;
	private NavigationButton src;
	private String summaryHTML = "";
	private Date referenceDay1;
	private String timeUnit;
	private final HashMap<String, Integer> experimentTimeUnit2seconds = new HashMap<String, Integer>();
	private int secondsPerNewTimeUnit;
	private String[] paramDescs;
	
	public ActionSetTimes() {
		super("Set experiment days according to reference date");
	}
	
	@Override
	public ParameterOptions getParameters() {
		Calendar c = Calendar.getInstance();
		c.setTime(experiment.getHeader().getStartdate());
		LinkedList<Object> params = new LinkedList<>();
		for (Object o : new Object[] {
				"Start Day", c.get(Calendar.DAY_OF_MONTH),
				"Start Month", c.get(Calendar.MONTH) + 1,
				"Start Year", c.get(Calendar.YEAR),
				"New Time Unit", "day",
				"Base Time Span per New Time Unit (seconds)", getSecondsWithinTimeUnit("day")
		}) {
			params.add(o);
		}
		boolean first = true;
		HashSet<String> knownTimeUnits = new HashSet<String>();
		try {
			for (SubstanceInterface si : experiment.getData())
				for (ConditionInterface ci : si)
					for (SampleInterface sai : ci) {
						String tu = sai.getTimeUnit();
						if (tu == null || tu.isEmpty())
							tu = "(no time unit)";
						if (!knownTimeUnits.contains(tu)) {
							knownTimeUnits.add(tu);
							if (first) {
								params.add("<html><br><font color='gray'>Currently used time units:");
								params.add(new JLabel());
							}
							first = false;
							params.add(SECONDS_WITHIN_1 + tu);
							params.add(getSecondsWithinTimeUnit(tu));
						}
					}
		} catch (Exception e) {
			params.add("<html><font color='red'>Warning/Error:");
			params.add(new JLabel("" + e.getMessage()));
		}
		
		paramDescs = new String[params.size() / 2];
		int i = 0;
		boolean isDesc = true;
		for (Object p : params) {
			if (isDesc)
				paramDescs[i++] = (String) p;
			isDesc = !isDesc;
		}
		
		return new ParameterOptions("Please specify the reference time point (i.e. day 1) and the desired time unit:",
				params.toArray());
	}
	
	private Integer getSecondsWithinTimeUnit(String tu) {
		tu = tu.toLowerCase();
		int defaultSpan = -1;
		if (tu.equals("day"))
			defaultSpan = 24 * 60 * 60;
		if (tu.equals("das"))
			defaultSpan = 24 * 60 * 60;
		return SystemOptions.getInstance().getInteger("Time Units", SECONDS_WITHIN_1 + tu, defaultSpan);
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		Calendar c = Calendar.getInstance();
		c.setTime(experiment.getHeader().getStartdate());
		for (int i = 0; i < parameters.length; i += 1) {
			switch (i) {
				case 0:
					c.set(Calendar.DAY_OF_MONTH, (Integer) parameters[i]);
					break;
				case 1:
					c.set(Calendar.MONTH, (Integer) parameters[i] - 1);
					break;
				case 2:
					c.set(Calendar.YEAR, (Integer) parameters[i]);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					this.referenceDay1 = c.getTime();
					break;
				case 3:
					this.timeUnit = (String) parameters[i];
					break;
				case 4:
					this.secondsPerNewTimeUnit = (Integer) parameters[i];
					SystemOptions.getInstance().setInteger("Time Units", SECONDS_WITHIN_1 + timeUnit.toLowerCase(), secondsPerNewTimeUnit);
					break;
				default:
					String settingsName = paramDescs[i];
					Object settingsValue = parameters[i];
					if (settingsName.startsWith(SECONDS_WITHIN_1)) {
						SystemOptions.getInstance().setInteger("Time Units", settingsName.toLowerCase(), (Integer) settingsValue);
						settingsName = settingsName.substring(SECONDS_WITHIN_1.length());
						experimentTimeUnit2seconds.put(settingsName, (Integer) settingsValue);
					}
					break;
			}
		}
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		getStatusProvider().setCurrentStatusText1("Get Data...");
		LinkedHashSet<String> warnings = new LinkedHashSet<String>();
		try {
			ExperimentInterface res = experiment.getData(false, getStatusProvider());
			getStatusProvider().setCurrentStatusText1("Process Time Data...");
			Date sd = referenceDay1;
			for (SubstanceInterface si : res)
				for (ConditionInterface ci : si)
					for (SampleInterface sai : ci) {
						Long ft = null;
						if (sai instanceof Sample3D) {
							Sample3D s3 = (Sample3D) sai;
							Long fineTime = s3.getSampleFineTimeOrRowId();
							if (fineTime != null)
								ft = fineTime;
							else
								ft = getFineTime(sd, s3.getTime(), s3.getTimeUnit(), warnings);
						} else
							ft = getFineTime(sd, sai.getTime(), sai.getTimeUnit(), warnings);
						if (ft != null) {
							long diffInSeconds = (ft - sd.getTime()) / 1000;
							int newTimeSpanValue = (int) (diffInSeconds / secondsPerNewTimeUnit);
							String newTimeUnit = timeUnit;
							sai.setTime(newTimeSpanValue);
							sai.setTimeUnit(newTimeUnit);
						}
					}
			if (warnings.isEmpty())
				summaryHTML = "Processing finished.";
			else
				summaryHTML = "Processing finished with warnings: " + StringManipulationTools.getStringList(warnings, ", ");
			getStatusProvider().setCurrentStatusText1("Processing finished");
		} catch (Exception e) {
			getStatusProvider().setCurrentStatusText1("Processing error:");
			getStatusProvider().setCurrentStatusText2(e.getMessage());
			summaryHTML = "Could not process data. Error: " + e.getMessage();
			e.printStackTrace();
		}
	}
	
	private Long getFineTime(Date startdate, int time, String timeUnit, LinkedHashSet<String> warnings) {
		Integer secondsWithinTimeUnit = experimentTimeUnit2seconds.get(timeUnit);
		if (secondsWithinTimeUnit != null) {
			return startdate.getTime() + time * secondsWithinTimeUnit * 1000;
		}
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(summaryHTML);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-open-recent.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Shift Experiment Days";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.m = experimentReference.getM();
		this.experiment = experimentReference;
	}
}