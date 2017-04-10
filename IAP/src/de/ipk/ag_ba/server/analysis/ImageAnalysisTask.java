package de.ipk.ag_ba.server.analysis;

import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

/**
 * @author klukas
 */
public interface ImageAnalysisTask {
	
	public String getTaskDescription();
	
	public void performAnalysis(
			BackgroundTaskStatusProviderSupportingExternalCall status) throws InterruptedException;
	
	public String getName();
	
	public void setInput(
			ExperimentHeaderInterface header,
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			Collection<Sample3D> input, Collection<NumericMeasurementInterface> optValidMeasurements,
			MongoDB m, int workOnSubset,
			int numberOfSubsets);
	
	public ExperimentInterface getOutput();
	
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps);
	
	public void setValidSideAngle(int dEBUG_SINGLE_ANGLE1, int dEBUG_SINGLE_ANGLE2, int DEBUG_SINGLE_ANGLE3);
	
}