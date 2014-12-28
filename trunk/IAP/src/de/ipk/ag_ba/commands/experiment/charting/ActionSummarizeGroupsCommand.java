package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.swing.JCheckBox;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public final class ActionSummarizeGroupsCommand extends AbstractNavigationAction {
	private NavigationButton src2;
	private final LinkedHashSet<String> groups = new LinkedHashSet<String>();
	private final ActionFxCreateDataChart actionFxCreateDataChart;
	private SystemOptions set;
	
	public ActionSummarizeGroupsCommand(String tooltip, ActionFxCreateDataChart actionFxCreateDataChart) {
		super(tooltip);
		this.actionFxCreateDataChart = actionFxCreateDataChart;
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.set = !this.actionFxCreateDataChart.settingsLocal.getUseLocalSettings() ? this.actionFxCreateDataChart.settingsGlobal.getSettings()
				: this.actionFxCreateDataChart.settingsLocal.getSettings();
		
		src2 = src;
		
		boolean calcGrubbsA = set.getBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", false);
		boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
		boolean calcGrubbsB = set.getBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", false);
		boolean calcANOVA = set.getBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", false);
		
		JCheckBox cbPerformGrubbsTestA = new JCheckBox("Perform Grubbs test on group samples to remove outliers");
		cbPerformGrubbsTestA.setSelected(calcGrubbsA);
		JCheckBox cbGroup = new JCheckBox("Merge data into single value for each day and plant ID");
		cbGroup.setSelected(groupByPlantID);
		JCheckBox cbPerformGrubbsTestB = new JCheckBox("Perform Grubbs test on group samples to remove outliers");
		cbPerformGrubbsTestB.setSelected(calcGrubbsB);
		JCheckBox cbANOVA = new JCheckBox("Calculate ANOVA p-Values");
		cbANOVA.setSelected(calcANOVA);
		
		Object[] res = MyInputHelper.getInput("Group data by plant ID prior to plotting?", "Process Data", new Object[] {
				"1.", cbPerformGrubbsTestA,
				"2.", cbGroup,
				"3.", cbPerformGrubbsTestB,
				"4.", cbANOVA,
		});
		if (res != null) {
			set.setBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", cbPerformGrubbsTestA.isSelected());
			set.setBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", cbGroup.isSelected());
			set.setBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", cbPerformGrubbsTestB.isSelected());
			set.setBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", cbANOVA.isSelected());
		}
	}
	
	@Override
	public String getDefaultTitle() {
		synchronized (groups) {
			boolean calcGrubbsA = set.getBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", false);
			boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
			boolean calcGrubbsB = set.getBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", false);
			boolean calcANOVA = set.getBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", false);
			int step = 1;
			if (!calcGrubbsA && !groupByPlantID && !calcGrubbsB && !calcANOVA)
				return "<html><center><b>&#8667;</b>&nbsp;Pass data&nbsp;<b>&#8667;</b><br><font color='gray'><small>no calculations";
			else
				return "<html><center><b>&#8667;</b>&nbsp;Process data"
						+ "&nbsp;<b>&#8667;</b><br><font color='gray'><small>"
						+ (calcGrubbsA ? (step++) + ". outlier removal for technical replicates" : "")
						+ (groupByPlantID ? (calcGrubbsA ? "<br>" : "") + (step++) + ". calculate mean value for plant ID and day"
								+ (calcGrubbsB ? "<br>" + (step++) + ". outlier removal for group replicates" : "")
								: "")
						+ (calcANOVA ? (calcGrubbsA || groupByPlantID || calcGrubbsB ? "<br>" : "") + (step++) + ". calculate ANOVA p-Values" : "");
			// (groupsDeterminationInProgress.getBval(0, false) ? "~ one moment ~<br>determine group set" :
			// (groups.size() == 1 ? "1 step" : groups.size() + " steps"));
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Accessories-Calculator-64.png";
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