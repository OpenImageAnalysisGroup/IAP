package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.data_transformation.ColumnDescription;
import de.ipk.ag_ba.data_transformation.DataTable;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public final class ActionFilterGroupsCommand extends AbstractNavigationAction {
	private NavigationButton src2;
	private boolean dirty = true;
	private final ThreadSafeOptions groupsDeterminationInProgress = new ThreadSafeOptions();
	private final LinkedHashSet<String> groups = new LinkedHashSet<String>();
	private final LinkedHashSet<String> disabled_groups = new LinkedHashSet<String>();
	private final DataTable data_table;
	private final ExperimentReference experiment;
	private ActionChartingGroupBySettings groupByAction;
	private final String substanceFilter;
	
	public ActionFilterGroupsCommand(String tooltip, DataTable data_table, ExperimentReference experiment, String substanceFilter) {
		super(tooltip);
		this.data_table = data_table;
		this.experiment = experiment;
		this.substanceFilter = substanceFilter;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		determineGroups();
		while (groupsDeterminationInProgress.getBval(0, false))
			Thread.sleep(100);
		ArrayList<Object> params = new ArrayList<Object>();
		LinkedHashMap<String, JCheckBox> group2setting = new LinkedHashMap<>();
		synchronized (groups) {
			for (String g : groups) {
				params.add("");
				JCheckBox cb = new JCheckBox("<html><code>" + extractValues(g));
				cb.setSelected(!disabled_groups.contains(g));
				group2setting.put(g, cb);
				params.add(cb);
			}
		}
		Object[] res = MyInputHelper.getInput("Select the desired groups. For each group one line or bar is drawn in the chart.<br><br>", "Group Selection",
				params.toArray());
		if (res != null) {
			disabled_groups.clear();
			for (String key : group2setting.keySet()) {
				if (!group2setting.get(key).isSelected())
					disabled_groups.add(key);
			}
		}
	}
	
	private String extractValues(String g) {
		StringBuilder res = new StringBuilder();
		for (String v : g.split("//")) {
			if (v.contains(":"))
				v = v.split(":", 2)[1];
			if (res.length() > 0)
				res.append(";");
			res.append(v);
		}
		return res.toString();
	}
	
	private void determineGroups() {
		if (groupByAction == null)
			return;
		if (!dirty)
			return;
		if (groupsDeterminationInProgress.getBval(0, false))
			return;
		dirty = false;
		groupsDeterminationInProgress.setBval(0, true);
		synchronized (groups) {
			groups.clear();
		}
		try {
			BackgroundThreadDispatcher.addTask(() -> {
				try {
					ArrayList<ColumnDescription> relevantColumns = new ArrayList<ColumnDescription>();
					Set<String> groupby = groupByAction.getGroupByColumnIDs();
					for (ColumnDescription cd : data_table.getColumns()) {
						if (groupby.contains(cd.getID()))
							relevantColumns.add(cd);
					}
					
					LinkedHashSet<String> ng = getInstanceValuesForColumns(relevantColumns);
					synchronized (groups) {
						groups.addAll(ng);
					}
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					groupsDeterminationInProgress.setBval(0, false);
				}
			}, "Determine group names");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static LinkedHashSet<String> getInstanceValuesForColumns(ExperimentReference experiment, ColumnDescription relevantColumn, String optSubstanceFilter)
			throws Exception {
		ArrayList<ColumnDescription> relevantColumns = new ArrayList<ColumnDescription>();
		relevantColumns.add(relevantColumn);
		return getInstanceValues(relevantColumns, experiment, optSubstanceFilter);
	}
	
	private LinkedHashSet<String> getInstanceValuesForColumns(ArrayList<ColumnDescription> relevantColumns) throws Exception {
		return getInstanceValues(relevantColumns, experiment, substanceFilter);
	}
	
	private static LinkedHashSet<String> getInstanceValues(ArrayList<ColumnDescription> relevantColumns, ExperimentReference experiment, String substanceFilter)
			throws Exception {
		LinkedHashSet<String> ng = new LinkedHashSet<>();
		if (ColumnDescription.isMeasurementRelevant(relevantColumns)) {
			experiment.visitNumericMeasurements(
					substanceFilter,
					(nmi) -> {
						String groupInstance = ColumnDescription.extractDataString(relevantColumns, nmi);
						if (groupInstance != null && !groupInstance.isEmpty())
							ng.add(groupInstance);
					});
		} else {
			if (ColumnDescription.isSampleRelevant(relevantColumns)) {
				experiment.visitSamples(
						substanceFilter,
						(sample) -> {
							if (sample.size() == 0)
								return;
							String groupInstance = ColumnDescription.extractDataString(relevantColumns, sample);
							if (groupInstance != null && !groupInstance.isEmpty())
								ng.add(groupInstance);
						});
			} else {
				experiment.visitConditions(
						substanceFilter,
						(condition) -> {
							if (condition.size() == 0)
								return;
							String groupInstance = ColumnDescription.extractDataString(relevantColumns, condition);
							if (groupInstance != null && !groupInstance.isEmpty())
								ng.add(groupInstance);
						});
			}
		}
		return ng;
	}
	
	@Override
	public String getDefaultTitle() {
		determineGroups();
		synchronized (groups) {
			return "<html><center><b>&#8667;</b>&nbsp;Select groups&nbsp;<b>&#8667;</b><br><font color='gray'><small>"
					+
					(groupsDeterminationInProgress.getBval(0, false) ? "~ one moment ~<br>determine group set" :
							(disabled_groups.size() > 0 ? (groups.size() - disabled_groups.size()) + "/" : "")
									+ (groups.size() == 1 ? "1 group" : groups.size() + " groups"));
		}
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
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		groups.clear();
		disabled_groups.clear();
	}
	
	public void setGroupByAction(ActionChartingGroupBySettings groupByAction) {
		this.groupByAction = groupByAction;
	}
}