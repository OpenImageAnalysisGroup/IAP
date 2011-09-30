package de.ipk.ag_ba.server.analysis;

import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

/**
 * @author klukas
 */
public interface ImageAnalysisTask {
	
	public String getTaskDescription();
	
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
						BackgroundTaskStatusProviderSupportingExternalCall status) throws InterruptedException;
	
	public String getName();
	
	public ImageAnalysisType[] getInputTypes();
	
	public ImageAnalysisType[] getOutputTypes();
	
	public void setInput(Collection<Sample3D> input, Collection<NumericMeasurementInterface> optValidMeasurements,
			MongoDB m, int workOnSubset,
			int numberOfSubsets);
	
	public Collection<NumericMeasurementInterface> getOutput();
}