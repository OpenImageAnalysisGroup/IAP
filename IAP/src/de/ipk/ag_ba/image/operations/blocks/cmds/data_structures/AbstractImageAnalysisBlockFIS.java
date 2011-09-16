package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import info.StopWatch;

import java.util.TreeMap;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public abstract class AbstractImageAnalysisBlockFIS implements ImageAnalysisBlockFIS {
	
	private FlexibleImageStack debugStack;
	protected ImageProcessorOptions options;
	private FlexibleMaskAndImageSet input;
	private BlockProperties properties;
	private int blockPositionInPipeline;
	
	public AbstractImageAnalysisBlockFIS() {
		// empty
	}
	
	@Override
	public void setInputAndOptions(FlexibleMaskAndImageSet input, ImageProcessorOptions options, BlockProperties properties,
			int blockPositionInPipeline,
			FlexibleImageStack debugStack) {
		this.input = input;
		this.options = options;
		this.properties = properties;
		this.blockPositionInPipeline = blockPositionInPipeline;
		this.debugStack = debugStack;
	}
	
	@Override
	public void reset() {
		input = null;
		options = null;
		properties = null;
		debugStack = null;
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
			debugStack.addImage("Input for " + task, getInput().getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
		if (options.getBooleanSetting(Setting.DEBUG_TAKE_TIMES)) {
			if (options.getBooleanSetting(Setting.IS_DEBUG_PRINT_EACH_STEP))
				if (getInput().getMasks() != null)
					getInput().getMasks().getFluo().print("Mask-Input for step: " + task);
				else
					getInput().getImages().getFluo().print("Image-Input for step: " + task);
			return new StopWatch("phytochamberTopImageProcessor: " + task);
		} else
			return null;
	}
	
	protected boolean isChangingImages() {
		return true;
	}
	
	protected void debugEnd(StopWatch w) {
		if (w != null) {
			w.printTime();
		}
	}
	
	public FlexibleMaskAndImageSet getInput() {
		return input;
	}
	
	@Override
	public BlockProperties getProperties() {
		return properties;
	}
	
	@Override
	public int getBlockPosition() {
		return blockPositionInPipeline;
	}
	
	@Override
	public void postProcessResultsForAllAngles(Sample3D inSample,
			TreeMap<Double, ImageData> inImages,
			TreeMap<Double, BlockProperties> allResultsForSnapshot,
			BlockProperties summaryResult) {
		// If needed, process the results in allResultsForSnapshot, and add the new data to summaryResult
	}
	
	protected void reportError(Error error, String errorMessage) {
		System.err.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: ERROR IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
	}
	
	protected void reportError(Exception error, String errorMessage) {
		System.err.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: EXCEPTION IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
	}
}
