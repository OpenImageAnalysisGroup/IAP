package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public final class ActionNormalisationCommand extends AbstractNavigationAction implements DirtyNotificationSupport, ExperimentTransformation {
	/**
	 * 
	 */
	private SystemOptions set;
	private final ExperimentTransformationPipeline pipeline;
	private final ChartSettings settingsLocal;
	private final ChartSettings settingsGlobal;
	
	public ActionNormalisationCommand(String tooltip, ExperimentTransformationPipeline pipeline, ChartSettings settingsLocal, ChartSettings settingsGlobal) {
		super(tooltip);
		this.pipeline = pipeline;
		this.settingsLocal = settingsLocal;
		this.settingsGlobal = settingsGlobal;
		this.set = !settingsLocal.getUseLocalSettings() ? settingsGlobal.getSettings() : settingsLocal.getSettings();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		set = !settingsLocal.getUseLocalSettings() ? settingsGlobal.getSettings() : settingsLocal.getSettings();
		
		JCheckBox cbSetMeanToZero = new JCheckBox("Set Mean to Zero");
		cbSetMeanToZero.setSelected(set.getBoolean("Charting", "Normalisation//Set Mean to Zero", false));
		
		JCheckBox cbStdDevToUnit = new JCheckBox("Set StdDev to 1");
		cbStdDevToUnit.setSelected(set.getBoolean("Charting", "Normalisation//Set StdDev to 1", false));
		
		JCheckBox cbRangeToUnit = new JCheckBox("Set Range to 1");
		cbRangeToUnit.setSelected(set.getBoolean("Charting", "Normalisation//Set Range to 1", false));
		
		Object[] sa = new Object[4 + 4];
		int idx = 0;
		sa[idx++] = "";
		sa[idx++] = cbSetMeanToZero;
		sa[idx++] = "";
		sa[idx++] = cbStdDevToUnit;
		sa[idx++] = "";
		sa[idx++] = cbRangeToUnit;
		sa[idx++] = "<html>&nbsp;";
		sa[idx++] = new JLabel();
		sa[idx++] = "Local/global";
		JCheckBox cbPersistentChange = new JCheckBox("Apply change persistent with experiment");
		cbPersistentChange.setSelected(!settingsLocal.getUseLocalSettings());
		if (!settingsLocal.isSavePossible()) {
			cbPersistentChange.setEnabled(false);
			cbPersistentChange.setText("<html>" + cbPersistentChange.getText() +
					"<br>(disabled, experiment loaded from read-only location)");
		}
		
		sa[idx++] = cbPersistentChange;
		
		Object[] ur = MyInputHelper.getInput("Normalise input data:<br><br>", "Normalise Data", sa);
		if (ur != null) {
			// apply settings
			boolean global = cbPersistentChange.isSelected();
			if (global)
				settingsLocal.setUseLocalSettings(false);
			else
				settingsLocal.setUseLocalSettings(true);
			set = global ? settingsGlobal.getSettings() : settingsLocal.getSettings();
			set.setBoolean("Charting", "Normalisation//Set Mean to Zero", cbSetMeanToZero.isSelected());
			set.setBoolean("Charting", "Normalisation//Set StdDev to 1", cbStdDevToUnit.isSelected());
			set.setBoolean("Charting", "Normalisation//Set Range to 1", cbRangeToUnit.isSelected());
			pipeline.setDirty(this);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		boolean normalizeSetMeanToZero = set.getBoolean("Charting", "Normalisation//Set Mean to Zero", false);
		boolean normalizeVarianceToUnit = set.getBoolean("Charting", "Normalisation//Set Variance to 1", false);
		boolean normalizeRangeToUnit = set.getBoolean("Charting", "Normalisation//Set Range to 1", false);
		
		String ops = "";
		if (normalizeSetMeanToZero)
			ops = "<font color='gray'><small>set mean to zero</small></font>";
		if (normalizeVarianceToUnit) {
			if (!ops.isEmpty())
				ops = ops + "<br>";
			ops += "<font color='gray'><small>set variance to 1</small></font>";
		} else
			if (normalizeRangeToUnit) {
				if (!ops.isEmpty())
					ops = ops + "<br>";
				ops += "<font color='gray'><small>set range to 1</small></font>";
			}
		if (ops.isEmpty())
			ops = "<font color='gray'><small>not performed</small></font>";
		
		return "<html><center><b>&#8667;</b>&nbsp;Normalisation&nbsp;<b>&#8667;</b><br>"
				+ ops + "</center>";
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
	public ExperimentInterface transform(ExperimentInterface input) {
		input = input.clone();
		boolean normalizeSetMeanToZero = set.getBoolean("Charting", "Normalisation//Set Mean to Zero", false);
		boolean normalizeStdDevToUnit = set.getBoolean("Charting", "Normalisation//Set StdDev to 1", false);
		boolean normalizeRangeToUnit = set.getBoolean("Charting", "Normalisation//Set Range to 1", false);
		
		if (normalizeSetMeanToZero || normalizeStdDevToUnit || normalizeRangeToUnit)
			for (SubstanceInterface si : input) {
				double meanFromFirstPass = Double.NaN;
				for (boolean firstPass : new boolean[] { true, false }) {
					if (!normalizeStdDevToUnit && firstPass)
						continue; // only for std dev calculation two passes are needed
					double min = Double.MAX_VALUE;
					double max = -Double.MAX_VALUE;
					double squaredMeanDiffSum = 0d;
					double sum = 0;
					long n = 0;
					for (ConditionInterface c : si)
						for (SampleInterface s : c)
							for (NumericMeasurementInterface md : s) {
								double val = md.getValue();
								if (Double.isFinite(val)) {
									if (val < min)
										min = val;
									if (val > max)
										max = val;
									sum += val;
									n++;
									if (normalizeStdDevToUnit && !firstPass) {
										squaredMeanDiffSum += (val - meanFromFirstPass) * (val - meanFromFirstPass);
									}
								}
							}
					if (n > 0) {
						double mean = sum / n;
						if (firstPass) {
							meanFromFirstPass = mean;
						} else {
							double stdDev = Double.NaN;
							if (normalizeStdDevToUnit)
								stdDev = Math.sqrt(1 / (n - 1) * squaredMeanDiffSum);
							for (ConditionInterface c : si)
								for (SampleInterface s : c)
									for (NumericMeasurementInterface md : s) {
										double val = md.getValue();
										if (Double.isFinite(val)) {
											if (normalizeSetMeanToZero && !normalizeRangeToUnit)
												md.setValue(val - mean);
											if (normalizeStdDevToUnit) {
												val = val - mean;
												val = val / stdDev;
												if (!normalizeSetMeanToZero)
													val = val + mean;
												md.setValue(val);
											} else
												if (normalizeRangeToUnit) {
													val = val - mean;
													val = val / (max - min);
													if (!normalizeSetMeanToZero)
														val = val + mean;
													md.setValue(val);
												}
										}
									}
						}
					}
				}
			}
		
		return input;
	}
	
	@Override
	public void updateStatus() throws Exception {
		//
	}
}