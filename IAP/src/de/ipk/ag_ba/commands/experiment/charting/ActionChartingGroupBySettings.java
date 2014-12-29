package de.ipk.ag_ba.commands.experiment.charting;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.data_transformation.ColumnDescription;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public final class ActionChartingGroupBySettings extends AbstractNavigationAction {
	/**
	 * 
	 */
	private final ActionFxCreateDataChart actionFxCreateDataChart;
	private NavigationButton src2;
	private final ActionFilterGroupsCommand filterGroupAction;
	private SystemOptions set;
	private final ExperimentReferenceWithFilterSupport experiment;
	private final String substanceFilter;
	
	public ActionChartingGroupBySettings(ActionFxCreateDataChart actionFxCreateDataChart, String tooltip, ActionFilterGroupsCommand filterGroupAction,
			ExperimentReferenceWithFilterSupport experiment, String substanceFilter) {
		super(tooltip);
		this.actionFxCreateDataChart = actionFxCreateDataChart;
		this.filterGroupAction = filterGroupAction;
		this.experiment = experiment;
		this.substanceFilter = substanceFilter;
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		LinkedHashMap<String, LinkedHashMap<String, JCheckBox>> settings = new LinkedHashMap<String, LinkedHashMap<String, JCheckBox>>();
		int items = 0;
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
		for (ColumnDescription col : experiment.getDataTable().getColumns()) {
			if (col.allowGroupBy()) {
				String group = col.getID().split("\\.")[0];
				if (!settings.containsKey(group))
					settings.put(group, new LinkedHashMap<String, JCheckBox>());
				String key = col.getID().split("\\.", 2)[1];
				if (!settings.get(group).containsKey(key)) {
					items++;
					LinkedHashSet<String> instancesPre = ActionFilterGroupsCommand.getInstanceValuesForColumns(experiment, col, substanceFilter);
					LinkedHashSet<String> instances = new LinkedHashSet<String>();
					instancesPre.forEach((s) -> {
						if (s.contains(":"))
							instances.add(s.split(":", 2)[1]);
							else
								instances.add(s);
						});
					String nn = " (" + instances.size() + ")";
					JCheckBox cb = new JCheckBox(col.getIitle() + nn);
					if (instances.size() > 0) {
						cb.setToolTipText(StringManipulationTools.getStringList(instances, ", ", 5, "..."));
					}
					if (instances.size() < 2)
						cb.setText("<html><font color='gray'>" + cb.getText());
					cb.setSelected(set.getBoolean("Charting", "Group by//" + group + "//" + key, false));
					settings.get(group).put(col.getID(), cb);
				}
			}
		}
		Object[] sa = new Object[(items + settings.size()) * 2 + 4];
		int idx = 0;
		for (String group : settings.keySet()) {
			String sss = group.toUpperCase();
			int len = sss.length();
			while (len < 12) {
				sss = sss + "&nbsp;";
				len++;
			}
			JLabel l1 = new JLabel("<html><font bgcolor='#EEEEFF'>&nbsp;<code>" + sss + "&nbsp;");
			sa[idx++] = l1;
			String ddd = "";
			len = ddd.length();
			while (len < 90) {
				ddd = ddd + "&nbsp;";
				len++;
			}
			JLabel l2 = new JLabel("<html><font bgcolor='#FEFEFF'>&nbsp;<code>" + ddd + "&nbsp;");
			sa[idx++] = l2;
			for (String key : settings.get(group).keySet()) {
				sa[idx++] = "";
				sa[idx++] = TableLayout.getSplit(settings.get(group).get(key),
						new JLabel("<html><font color='" + (settings.get(group).get(key).getText().startsWith("<html><font color='gray'>") ? "#999999" : "#884444")
								+ "'>"
								+ settings.get(group).get(key).getToolTipText()), 200,
						TableLayout.PREFERRED);
				settings.get(group).get(key).setToolTipText(null);
			}
		}
		sa[idx++] = "<html>&nbsp;";
		sa[idx++] = new JLabel();
		sa[idx++] = "Local/global";
		JCheckBox cbPersistentChange = new JCheckBox("Apply change persistent with experiment");
		cbPersistentChange.setSelected(!this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings());
		sa[idx++] = cbPersistentChange;
		
		Object[] ur = MyInputHelper.getInput("Metadata columns to group the data:<br><br>", "Group data", sa);
		if (ur != null) {
			// apply settings
			boolean global = cbPersistentChange.isSelected();
			if (global)
				this.actionFxCreateDataChart.settingsLocal.setUseLocalSettings(false);
			else
				this.actionFxCreateDataChart.settingsLocal.setUseLocalSettings(true);
			set = global ? this.actionFxCreateDataChart.settingsGlobal.getSettings() : this.actionFxCreateDataChart.settingsLocal.getSettings();
			for (String group : settings.keySet()) {
				for (String id : settings.get(group).keySet()) {
					JCheckBox cb = settings.get(group).get(id);
					String key = id.split("\\.", 2)[1];
					set.setBoolean("Charting", "Group by//" + group + "//" + key, cb.isSelected());
				}
			}
			filterGroupAction.setDirty(true);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		ArrayList<String> ids = set.getSectionSettings("Charting");
		LinkedHashSet<String> groupby = new LinkedHashSet<String>();
		for (String i : ids) {
			if (i.startsWith("Group by//")) {
				if (set.getBoolean("Charting", i, false))
					groupby.add(i.split("//")[2]);
			}
		}
		String list = StringManipulationTools.getStringList(groupby, ", ");
		if (list.isEmpty())
			list = "no filter &gt; single group";
		return "<html><center>&#8667;</b>&nbsp;Group by&nbsp;<b>&#8667;</b><br><font color='gray'><small>" + list + "</small></font></center>";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-shared.png";// Gnome-View-Sort-Selection-64.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	public Set<String> getGroupByColumnIDs() {
		Set<String> groupby = new LinkedHashSet<String>();
		
		ArrayList<String> ids = set.getSectionSettings("Charting");
		for (String i : ids) {
			if (i.startsWith("Group by//")) {
				if (set.getBoolean("Charting", i, false))
					groupby.add(i.split("//")[1] + "." + i.split("//")[2]);
			}
		}
		
		return groupby;
	}
}