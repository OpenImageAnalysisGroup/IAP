package iap.blocks.data_structures;

import iap.pipelines.ImageProcessorOptions;
import info.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public abstract class AbstractImageAnalysisBlockFIS implements ImageAnalysisBlockFIS {
	
	private FlexibleImageStack debugStack;
	protected ImageProcessorOptions options;
	private FlexibleMaskAndImageSet input;
	private BlockResultSet properties;
	private int blockPositionInPipeline;
	
	public AbstractImageAnalysisBlockFIS() {
		// empty
	}
	
	protected boolean getBoolean(String setting, boolean defaultValue) {
		return options != null ? options.getBooleanSetting(this, setting, defaultValue) : defaultValue;
	}
	
	protected boolean getBoolean(ImageAnalysisBlockFIS block, String setting, boolean defaultValue) {
		return options.getBooleanSetting(block, setting, defaultValue);
	}
	
	protected int getInt(String setting, int defaultValue) {
		return options.getIntSetting(this, setting, defaultValue);
	}
	
	protected double getDouble(String setting, double defaultValue) {
		return options.getDoubleSetting(this, setting, defaultValue);
	}
	
	protected String getString(String setting, String defaultValue) {
		return options.getStringSetting(this, setting, defaultValue);
	}
	
	protected Integer[] getIntArray(String setting, Integer[] defaultValue) {
		return options.getIntArraySetting(this, setting, defaultValue);
	}
	
	@Override
	public void setInputAndOptions(FlexibleMaskAndImageSet input, ImageProcessorOptions options, BlockResultSet properties,
			int blockPositionInPipeline,
			FlexibleImageStack debugStack) {
		this.input = input;
		this.options = options;
		this.properties = properties;
		this.blockPositionInPipeline = blockPositionInPipeline;
		this.debugStack = debugStack;
	}
	
	@Override
	public final FlexibleMaskAndImageSet process() throws InterruptedException {
		StopWatch w = debugStart(this.getClass().getSimpleName());
		FlexibleMaskAndImageSet res = run();
		debugEnd(w);
		return res;
	}
	
	protected abstract FlexibleMaskAndImageSet run() throws InterruptedException;
	
	protected StopWatch debugStart(String task) {
		if (debugStack != null && isChangingImages())
			debugStack.addImage("Input for " + task, input().getOverviewImage(
					SystemOptions.getInstance().getInteger("IAP", "Debug-Overview-Image-Width", 1680)
					), task);
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Stop-Block-Exection-Times", true)) {
			if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Display-Each-Step", false))
				if (input().masks() != null)
					input().masks().fluo().print("Mask-Input for step: " + task);
				else
					input().images().fluo().print("Image-Input for step: " + task);
			return new StopWatch("phytochamberTopImageProcessor: " + task);
		} else
			return null;
	}
	
	protected boolean isChangingImages() {
		return true;
	}
	
	protected void debugEnd(StopWatch w) {
		if (w != null) {
			w.printTime(10);
		}
	}
	
	public FlexibleMaskAndImageSet input() {
		return input;
	}
	
	protected BlockResultSet getProperties() {
		return properties;
	}
	
	protected int getBlockPosition() {
		return blockPositionInPipeline;
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InterruptedException {
		// If needed, process the results in allResultsForSnapshot, and add the new data to summaryResult
	}
	
	protected void reportError(Error error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: ERROR IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
	}
	
	protected void reportError(Exception error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: EXCEPTION IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
	}
	
	@Override
	public Parameter[] getParameters() {
		// empty
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// empty
	}
	
	protected String getRemarkSetting(String remarkID, String defaultReturn) {
		ImageData info = input() != null && input.images() != null ? input().images().getVisInfo() : null;
		if (info == null)
			info = input() != null && input.images() != null ? input().images().getFluoInfo() : null;
		if (info == null)
			info = input() != null && input.images() != null ? input().images().getNirInfo() : null;
		if (info == null)
			return defaultReturn;
		else {
			try {
				String rem = info.getParentSample().getParentCondition().getExperimentHeader().getRemark();
				if (rem != null)
					for (String r : rem.split("//")) {
						r = r.trim();
						if (r.startsWith(remarkID) && r.contains(":"))
							return r.split(":", 2)[1].trim();
					}
				return defaultReturn;
			} catch (Exception e) {
				System.err.println(SystemAnalysis.getCurrentTime() + ">Error processing remark information:");
				e.printStackTrace();
				return defaultReturn;
			}
		}
	}
	
	protected void calculateRelativeValues(TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult, int blockPosition,
			String[] desiredProperties) {
		final double timeForOneDayD = 1000 * 60 * 60 * 24d;
		HashMap<String, TreeMap<String, Long>> prop2config2lastHeightAndWidthTime = new HashMap<String, TreeMap<String, Long>>();
		HashMap<String, TreeMap<String, Double>> prop2config2lastHeightAndWidth = new HashMap<String, TreeMap<String, Double>>();
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (!time2summaryResult.containsKey(time))
				time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
			HashMap<Integer, BlockResultSet> summaryResultArray = time2summaryResult.get(time);
			for (String key : allResultsForSnapshot.keySet()) {
				for (Integer tray : summaryResultArray.keySet()) {
					BlockResultSet summaryResult = summaryResultArray.get(tray);
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
					for (String property : desiredProperties) {
						ArrayList<BlockPropertyValue> sr = rt.getPropertiesExactMatch(property);
						for (BlockPropertyValue v : sr) {
							if (v.getValue() != null) {
								if (!prop2config2lastHeightAndWidth.containsKey(property))
									prop2config2lastHeightAndWidth.put(property, new TreeMap<String, Double>());
								if (!prop2config2lastHeightAndWidthTime.containsKey(property))
									prop2config2lastHeightAndWidthTime.put(property, new TreeMap<String, Long>());
								
								Double lastPropertyValue = prop2config2lastHeightAndWidth.get(property).get(key);
								if (lastPropertyValue != null && lastPropertyValue > 0 && prop2config2lastHeightAndWidth.get(property).containsKey(key) &&
										time - prop2config2lastHeightAndWidthTime.get(property).get(key) > 0) {
									double currentPropertyValue = v.getValue().doubleValue();
									double ratio = currentPropertyValue / lastPropertyValue;
									double days = (time - prop2config2lastHeightAndWidthTime.get(property).get(key)) / timeForOneDayD;
									double ratioPerDay = Math.pow(ratio, 1d / days);
									summaryResult.setNumericProperty(blockPosition, property + ".relative", ratioPerDay, "relative/day");
								}
								
								double width = v.getValue().doubleValue();
								prop2config2lastHeightAndWidthTime.get(property).put(key, time);
								prop2config2lastHeightAndWidth.get(property).put(key, width);
							}
						}
					}
				}
			}
		}
	}
}
