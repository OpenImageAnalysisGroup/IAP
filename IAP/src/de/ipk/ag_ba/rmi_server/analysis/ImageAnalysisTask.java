package de.ipk.ag_ba.rmi_server.analysis;

import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;

/**
 * @author klukas
 */
public interface ImageAnalysisTask {

	public String getTaskDescription();

	/**
	 * @deprecated Use {@link #performAnalysis(int,int,BackgroundTaskStatusProviderSupportingExternalCall)} instead
	 */
	public void performAnalysis(int maximumThreadCount, BackgroundTaskStatusProviderSupportingExternalCall status);

	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
						BackgroundTaskStatusProviderSupportingExternalCall status);

	public String getName();

	public ImageAnalysisType[] getInputTypes();

	public ImageAnalysisType[] getOutputTypes();

	public void setInput(Collection<NumericMeasurementInterface> input, String login, String pass);

	public Collection<NumericMeasurementInterface> getOutput();
}