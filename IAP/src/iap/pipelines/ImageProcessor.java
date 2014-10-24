package iap.pipelines;

import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.OptionsGenerator;

public interface ImageProcessor {
	
	public void execute(OptionsGenerator options)
			throws Exception;
	
	/**
	 * @return mapping from tray to analysis results
	 */
	public abstract HashMap<String, BlockResultSet> getNumericResults();
	
	public abstract BlockPipeline getPipeline(ImageProcessorOptionsAndResults options);
	
	public abstract void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public abstract BackgroundTaskStatusProviderSupportingExternalCall getStatus();
	
	public abstract TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> postProcessPlantResults(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ImageProcessorOptionsAndResults options)
			throws Exception;
	
	public abstract void setValidTrays(int[] debugValidTrays);
}