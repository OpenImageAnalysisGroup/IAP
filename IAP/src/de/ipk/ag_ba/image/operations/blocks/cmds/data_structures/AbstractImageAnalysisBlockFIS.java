package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import info.StopWatch;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

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
	public final FlexibleMaskAndImageSet process() {
		StopWatch w = debugStart(this.getClass().getSimpleName());
		FlexibleMaskAndImageSet res = run();
		debugEnd(w);
		return res;
	}
	
	protected abstract FlexibleMaskAndImageSet run();
	
	protected StopWatch debugStart(String task) {
		if (debugStack != null)
			debugStack.addImage("Input for " + task, getInput().getOverviewImage(options.getDebugStackWidth()));
		if (options.isDebugTakeTimes()) {
			if (options.isDebugPrintEachStep())
				if (getInput().getMasks() != null)
					getInput().getMasks().getFluo().print("Mask-Input for step: " + task);
				else
					getInput().getImages().getFluo().print("Image-Input for step: " + task);
			return new StopWatch("phytochamberTopImageProcessor: " + task);
		} else
			return null;
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
}
