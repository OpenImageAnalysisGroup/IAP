package de.ipk.ag_ba.commands.control;

import java.util.ArrayList;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class SelectTimeScaleAction extends AbstractNavigationAction {
	
	private Integer manualTimeValue;
	private long startTime;
	
	public SelectTimeScaleAction(String tooltip) {
		super(tooltip);
	}
	
	public SelectTimeScaleAction(ExperimentReferenceInterface experimentReference) {
		this("Change time scale");
		
		startTime = experimentReference.getExperiment().getHeader().getStartdate().getTime();
		
		int lastTimePoint = 1;
		for (SubstanceInterface si : experimentReference.getExperiment()) {
			for (ConditionInterface ci : si) {
				for (SampleInterface sai : ci) {
					lastTimePoint = sai.getTime();
				}
			}
			break;
		}
		manualTimeValue = lastTimePoint;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] res = MyInputHelper.getInput("Specify time or auto-time calculation mode:", "Time scale",
				new Object[] {
						"Determine time automatically", autoTimeMode(),
						"Start with 1 instead of 0 (auto)", startWith1(),
						"Time unit@@@" + getUnit(), getTimeUnits(),
						"Time value (manual mode)", getTimeValue()
				});
		if (res != null) {
			SystemOptions.getInstance().setBoolean("Capture", "Time//Automatic time", (Boolean) res[0]);
			SystemOptions.getInstance().setBoolean("Capture", "Time//Start with 1 instead of 0", (Boolean) res[1]);
			SystemOptions.getInstance().setStringRadioSelection("Capture", "Time//Time unit", getTimeUnits(), (String) res[2]);
			manualTimeValue = (Integer) res[3];
		}
	}
	
	private boolean startWith1() {
		return SystemOptions.getInstance().getBoolean("Capture", "Time//Start with 1 instead of 0", false);
	}
	
	private String[] getTimeUnits() {
		ArrayList<String> res = getTimeUnitsList();
		return res.toArray(new String[res.size()]);
	}
	
	private ArrayList<String> getTimeUnitsList() {
		ArrayList<String> res = new ArrayList<>();
		res.add("day");
		res.add("hour");
		res.add("min");
		res.add("sec");
		return res;
	}
	
	private boolean autoTimeMode() {
		return SystemOptions.getInstance().getBoolean("Capture", "Time//Automatic time", true);
	}
	
	public String getUnit() {
		return SystemOptions.getInstance().getStringRadioSelection("Capture", "Time//Time unit", getTimeUnitsList(), "day", true);
	}
	
	public int getTimeValue() {
		if (autoTimeMode()) {
			int secondsPerValue = 1;
			switch (getUnit()) {
				case "day":
					secondsPerValue = 24 * 60 * 60;
					break;
				case "hour":
					secondsPerValue = 60 * 60;
					break;
				case "min":
					secondsPerValue = 60;
					break;
				case "sec":
					secondsPerValue = 1;
					break;
			}
			long timeDiffInSeconds = (System.currentTimeMillis() - startTime) / 1000;
			return (int) (timeDiffInSeconds / secondsPerValue + (startWith1() ? 1 : 0));
		} else {
			return manualTimeValue;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		return getUnit() + " " + getTimeValue();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Appointment-Soon-64_flipped.png";
	}
}
