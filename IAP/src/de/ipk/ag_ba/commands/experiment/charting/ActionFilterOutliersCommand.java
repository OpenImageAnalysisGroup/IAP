package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public final class ActionFilterOutliersCommand extends AbstractNavigationAction {
	/**
	 * 
	 */
	private final ActionFxCreateDataChart actionFxCreateDataChart;
	private NavigationButton src2;
	private SystemOptions set;
	private final ActionFilterGroupsCommand filterGroupAction;
	
	public ActionFilterOutliersCommand(ActionFxCreateDataChart actionFxCreateDataChart, String tooltip, ActionFilterGroupsCommand filterGroupAction) {
		super(tooltip);
		this.actionFxCreateDataChart = actionFxCreateDataChart;
		this.filterGroupAction = filterGroupAction;
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		
		set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
		
		JCheckBox cbIgnoreDefinedOutliers = new JCheckBox("Ignore defined outliers");
		cbIgnoreDefinedOutliers.setSelected(set.getBoolean("Charting", "Filter outliers//Ignore defined outliers", true));
		
		Object[] sa = new Object[2 + 4];
		int idx = 0;
		sa[idx++] = "";
		sa[idx++] = cbIgnoreDefinedOutliers;
		sa[idx++] = "<html>&nbsp;";
		sa[idx++] = new JLabel();
		sa[idx++] = "Local/global";
		JCheckBox cbPersistentChange = new JCheckBox("Apply change persistent with experiment");
		cbPersistentChange.setSelected(!this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings());
		sa[idx++] = cbPersistentChange;
		
		Object[] ur = MyInputHelper.getInput("Filter input data (defined outliers):<br><br>", "Filter Data (Outliers)", sa);
		if (ur != null) {
			// apply settings
			boolean global = cbPersistentChange.isSelected();
			if (global)
				this.actionFxCreateDataChart.settingsLocal.setUseLocalSettings(false);
			else
				this.actionFxCreateDataChart.settingsLocal.setUseLocalSettings(true);
			set = global ? this.actionFxCreateDataChart.settingsGlobal.getSettings() : this.actionFxCreateDataChart.settingsLocal.getSettings();
			set.setBoolean("Charting", "Filter outliers//Ignore defined outliers", cbIgnoreDefinedOutliers.isSelected());
			filterGroupAction.setDirty(true);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		boolean ro = set.getBoolean("Charting", "Filter outliers//Ignore defined outliers", true);
		return "<html><center><b>&#8667;</b>&nbsp;Outlier&nbsp;<b>&#8667;</b><br><font color='gray'><small>"
				+ (ro ? "removing defined outliers" : "defined outliers are not removed");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Sort-Selection-64 Filter.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}