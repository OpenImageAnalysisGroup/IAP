package de.ipk.ag_ba.commands.experiment.charting;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;

public final class ActionTimeRangeCommand extends AbstractNavigationAction implements DirtyNotificationSupport {
	/**
	 * 
	 */
	private final ActionFxCreateDataChart actionFxCreateDataChart;
	private NavigationButton src2;
	private SystemOptions set;
	private final ActionFilterGroupsCommand filterGroupAction;
	private final String substanceFilter;
	
	public ActionTimeRangeCommand(ActionFxCreateDataChart actionFxCreateDataChart, String tooltip, ActionFilterGroupsCommand filterGroupAction,
			String substanceFilter) {
		super(tooltip);
		this.actionFxCreateDataChart = actionFxCreateDataChart;
		this.filterGroupAction = filterGroupAction;
		this.substanceFilter = substanceFilter;
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
		
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		LinkedHashMap<String, JCheckBox> settings = new LinkedHashMap<String, JCheckBox>();
		int items = 0;
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
		
		for (String time : Experiment.getTimes(this.actionFxCreateDataChart.experiment.getData(), substanceFilter)) {
			items++;
			JCheckBox cb = new JCheckBox(time);
			cb.setSelected(set.getBoolean("Charting", "Time range//" + time, true));
			settings.put(time, cb);
		}
		Object[] sa = new Object[(items + settings.size()) * 2 + 4];
		int idx = 0;
		Iterator<String> timeIt = settings.keySet().iterator();
		while (timeIt.hasNext()) {
			sa[idx++] = "";
			String time = timeIt.next();
			if (timeIt.hasNext())
				sa[idx++] = TableLayout.getSplit(settings.get(time), settings.get(timeIt.next()), 100, 100);
			else
				sa[idx++] = settings.get(time);
		}
		sa[idx++] = "<html>&nbsp;";
		sa[idx++] = new JLabel();
		sa[idx++] = "Local/global";
		JCheckBox cbPersistentChange = new JCheckBox("Apply change persistent with experiment");
		cbPersistentChange.setSelected(!this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings());
		sa[idx++] = cbPersistentChange;
		
		Object[] ur = MyInputHelper.getInput("Experiment days:<br><br>", "Time Range", sa);
		if (ur != null) {
			// apply settings
			boolean global = cbPersistentChange.isSelected();
			if (global)
				this.actionFxCreateDataChart.settingsLocal.setUseLocalSettings(false);
			else
				this.actionFxCreateDataChart.settingsLocal.setUseLocalSettings(true);
			for (String time : settings.keySet()) {
				JCheckBox cb = settings.get(time);
				set.setBoolean("Charting", "Time range//" + time, cb.isSelected());
			}
			filterGroupAction.setDirty(true);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		ArrayList<String> ids = set.getSectionSettings("Charting");
		int sel = 0;
		int unsel = 0;
		for (String i : ids) {
			if (i.startsWith("Time range//")) {
				if (set.getBoolean("Charting", i, true))
					sel++;
				else
					unsel++;
			}
		}
		// cb.setSelected(set.getBoolean("Charting", "Time range//" + time, true));
		// settings.put(time, cb);
		// }
		if (unsel == 0)
			return "<html><center><b>&#8667;</b>&nbsp;Time range&nbsp;<b>&#8667;</b><br><font color='gray'><small>no time point unselected";
		else
			return "<html><center><b>&#8667;</b>&nbsp;Time range&nbsp;<b>&#8667;</b><br><font color='gray'><small>" + sel + "/" + (sel + unsel) + " days selected";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Sort-Selection-64 Filter.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public void setDirty(boolean b) {
		// TODO Auto-generated method stub
		
	}
}