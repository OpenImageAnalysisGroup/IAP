package de.ipk.ag_ba.server.analysis;

import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;

/**
 * @author klukas
 */
public interface ImageAnalysisTask {
	
	public String getTaskDescription();
	
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
						BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public String getName();
	
	public ImageAnalysisType[] getInputTypes();
	
	public ImageAnalysisType[] getOutputTypes();
	
	public void setInput(Collection<NumericMeasurementInterface> input, MongoDB m, int workOnSubset, int numberOfSubsets);
	
	public Collection<NumericMeasurementInterface> getOutput();
}