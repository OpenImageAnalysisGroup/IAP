package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;

import javax.swing.JComponent;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.DataChartComponentWindow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

public final class ActionCreatePlotCommand extends AbstractNavigationAction implements DirtyNotificationSupport {
	/**
	 * 
	 */
	private final ActionFxCreateDataChart actionFxCreateDataChart;
	private NavigationButton src2;
	private JComponent chartGUI;
	private int valueCount = -1;
	private int substanceCount;
	private int conditionCount;
	
	public ActionCreatePlotCommand(ActionFxCreateDataChart actionFxCreateDataChart, String tooltip) {
		super(tooltip);
		this.actionFxCreateDataChart = actionFxCreateDataChart;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		status.setCurrentStatusText1("Create dataset for plotting");
		ExperimentInterface expf = actionFxCreateDataChart.experimentWithSingleSubstance;
		DataChartComponentWindow dccw = new DataChartComponentWindow(expf);
		this.chartGUI = dccw.getGUI();
	}
	
	@Override
	public String getDefaultTitle() {
		if (valueCount < 0)
			try {
				updateValueCount();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		return "<html><center>Create plot<br><small><font color='gray'>" + valueCount + " value(s)<br>"
				+ conditionCount + " conditions<br>"
				+ substanceCount + " substance(s)</font></small></center>";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getHistogramIcon();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(chartGUI);
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
	public void setDirty(boolean b) throws Exception {
		if (b)
			updateValueCount();
	}
	
	private void updateValueCount() throws Exception {
		ExperimentInterface ee = actionFxCreateDataChart.experimentWithSingleSubstance;
		this.valueCount = Substance3D.countMeasurementValues2(ee, MeasurementNodeType.OMICS);
		this.conditionCount = 0;
		for (SubstanceInterface si : ee)
			conditionCount += si.size();
		this.substanceCount = ee.size();
	}
}