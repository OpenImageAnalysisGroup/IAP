package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public final class ActionFilterOutliersCommand extends AbstractNavigationAction implements ExperimentTransformation {
	/**
	 * 
	 */
	private final ActionFxCreateDataChart actionFxCreateDataChart;
	private NavigationButton src2;
	private SystemOptions set;
	private final DirtyNotificationSupport[] dirtyNotification;
	
	public ActionFilterOutliersCommand(ActionFxCreateDataChart actionFxCreateDataChart, String tooltip,
			DirtyNotificationSupport... dirtyNotification) {
		super(tooltip);
		this.actionFxCreateDataChart = actionFxCreateDataChart;
		this.dirtyNotification = dirtyNotification;
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
			if (dirtyNotification != null)
				for (DirtyNotificationSupport dns : dirtyNotification)
					if (dns == null)
						throw new RuntimeException("internal error: dns variable is null");
					else
						dns.setDirty(true);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		boolean ro = set.getBoolean("Charting", "Filter outliers//Ignore defined outliers", true);
		return "<html><center><b>&nbsp;</b>&nbsp;Outlier&nbsp;<b>&#8667;</b><br><font color='gray'><small>"
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
	
	public static ExperimentInterface tryGetFilteredDataset(ExperimentReferenceInterface experiment, String groupFilter2,
			BackgroundTaskStatusProviderSupportingExternalCall sp) throws Exception {
		if (experiment != null && groupFilter2 != null)
			for (SubstanceInterface si : experiment.getData()) {
				if (groupFilter2.equals(si.getName())) {
					ExperimentInterface ne = Experiment.copyAndExtractSubtanceInclusiveData(si);
					return ne;
				}
			}
		return null;
	}
	
	@Override
	public ExperimentInterface transform(ExperimentInterface input) {
		boolean removeOutliers = set.getBoolean("Charting", "Filter outliers//Ignore defined outliers", true);
		if (removeOutliers) {
			ExperimentInterface eclone = input.clone();
			IAPservice.removeOutliers(eclone);
			return eclone;
		} else
			return input;
	}
}