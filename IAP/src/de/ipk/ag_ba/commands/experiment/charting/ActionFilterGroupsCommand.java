package de.ipk.ag_ba.commands.experiment.charting;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.FolderPanel;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.data_transformation.ColumnDescription;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

public final class ActionFilterGroupsCommand extends AbstractNavigationAction implements DirtyNotificationSupport, ExperimentTransformation {
	private NavigationButton src2;
	private boolean dirty = true;
	private final ThreadSafeOptions groupsDeterminationInProgress = new ThreadSafeOptions();
	private final LinkedHashSet<String> groups = new LinkedHashSet<String>();
	private final LinkedHashSet<String> disabled_groups = new LinkedHashSet<String>();
	private final ExperimentTransformationPipeline pipeline;
	private boolean updateConditionNumbers = false;
	
	public ActionFilterGroupsCommand(String tooltip, ExperimentTransformationPipeline pipeline) {
		super(tooltip);
		this.pipeline = pipeline;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		determineGroups();
		while (groupsDeterminationInProgress.getBval(0, false))
			Thread.sleep(100);
		ArrayList<Object> params = new ArrayList<Object>();
		LinkedHashMap<String, JCheckBox> group2setting = new LinkedHashMap<>();
		
		params.add("");
		params.add(TableLayout.getSplit(new JButton(new AbstractAction("Invert Selection") {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JCheckBox cb : group2setting.values())
					cb.setSelected(!cb.isSelected());
				BackgroundTaskHelper.executeLaterOnSwingTask(150, () -> {
					FolderPanel.performDialogResize(group2setting.values().iterator().next());
				});
			}
		}), null, TableLayout.PREFERRED, TableLayout.FILL));
		params.add("");
		params.add(new JLabel("<html>&nbsp;"));
		
		synchronized (groups) {
			TreeSet<String> instances = new TreeSet<>();
			for (String g : groups)
				instances.add(g);
			for (String g : instances) {
				params.add("");
				JCheckBox cb = new JCheckBox("<html><code>" + extractValues(g));
				cb.setSelected(!disabled_groups.contains(g));
				group2setting.put(g, cb);
				params.add(cb);
			}
		}
		params.add("");
		params.add(new JLabel("<html>&nbsp;"));
		JCheckBox renumberConditions = new JCheckBox("Update Condition Numbers");
		renumberConditions.setSelected(updateConditionNumbers);
		params.add("");
		params.add(renumberConditions);
		Object[] res = MyInputHelper.getInput("Select the desired groups. For each group one line or bar is drawn in the chart.<br><br>", "Group Selection",
				params.toArray());
		if (res != null) {
			this.updateConditionNumbers = renumberConditions.isSelected();
			disabled_groups.clear();
			for (String key : group2setting.keySet()) {
				if (!group2setting.get(key).isSelected())
					disabled_groups.add(key);
			}
			pipeline.setDirty(this);
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
		if (!dirty)
			return;
		if (groupsDeterminationInProgress.getBval(0, false))
			return;
		dirty = false;
		groupScanNeeded = false;
		groupsDeterminationInProgress.setBval(0, true);
		synchronized (groups) {
			disabled_groups.clear();
			groups.clear();
		}
		try {
			BackgroundThreadDispatcher.addTask(() -> {
				try {
					ArrayList<ColumnDescription> relevantColumns = new ArrayList<ColumnDescription>();
					relevantColumns.add(new ColumnDescription("condition.name", "Species", true));
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
	
	public static LinkedHashSet<String> getInstanceValuesForColumns(ExperimentInterface experiment, ColumnDescription relevantColumn)
			throws Exception {
		ArrayList<ColumnDescription> relevantColumns = new ArrayList<ColumnDescription>();
		relevantColumns.add(relevantColumn);
		return getInstanceValues(relevantColumns, experiment, null);
	}
	
	private LinkedHashSet<String> getInstanceValuesForColumns(ArrayList<ColumnDescription> relevantColumns) throws Exception {
		return getInstanceValues(relevantColumns, pipeline.getInput(this), null);
	}
	
	private static LinkedHashSet<String> getInstanceValues(ArrayList<ColumnDescription> relevantColumns, ExperimentInterface experiment,
			String substanceFilter)
			throws Exception {
		LinkedHashSet<String> ng = new LinkedHashSet<>();
		if (ColumnDescription.isMeasurementRelevant(relevantColumns)) {
			((Experiment) experiment).visitNumericMeasurements(
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
									+ (groups.size() == 1 ? "1 group" : groups.size() + " groups"))
					+ "</small></font></center>";
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
	
	@Override
	public ExperimentInterface transform(ExperimentInterface input) {
		ArrayList<ColumnDescription> relevantColumns = new ArrayList<ColumnDescription>();
		
		relevantColumns.add(new ColumnDescription("condition.name", "Species", true));
		
		ArrayList<MappingData3DPath> pathObjects = new ArrayList<MappingData3DPath>();
		for (MappingData3DPath po : MappingData3DPath.get(input, true)) {
			if (ColumnDescription.isMeasurementRelevant(relevantColumns)) {
				NumericMeasurementInterface nmi = po.getMeasurement();
				String groupInstance = ColumnDescription.extractDataString(relevantColumns, nmi);
				if (groupInstance != null && !groupInstance.isEmpty()) {
					if (!disabled_groups.contains(groupInstance))
						pathObjects.add(po);
				}
			} else {
				if (ColumnDescription.isSampleRelevant(relevantColumns)) {
					SampleInterface sample = po.getSampleData();
					if (sample.size() == 0)
						continue;
					String groupInstance = ColumnDescription.extractDataString(relevantColumns, sample);
					if (groupInstance != null && !groupInstance.isEmpty()) {
						if (!disabled_groups.contains(groupInstance))
							pathObjects.add(po);
					}
				} else {
					ConditionInterface condition = po.getConditionData();
					if (condition.size() == 0)
						continue;
					String groupInstance = ColumnDescription.extractDataString(relevantColumns, condition);
					if (groupInstance != null && !groupInstance.isEmpty()) {
						if (!disabled_groups.contains(groupInstance))
							pathObjects.add(po);
					}
				}
			}
		}
		
		ExperimentInterface result = MappingData3DPath.merge(pathObjects, false);
		if (updateConditionNumbers)
			result.numberConditions();
		return result;
	}
	
	boolean groupScanNeeded = false;
	
	@Override
	public void updateStatus() throws Exception {
		if (groupScanNeeded) {
			groupScanNeeded = false;
			determineGroups();
			while (groupsDeterminationInProgress.getBval(0, false)) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public void rescanGroups() {
		dirty = true;
		groupScanNeeded = true;
	}
}