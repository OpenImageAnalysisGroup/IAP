package de.ipk.ag_ba.image.operations.blocks.cmds;

import info.StopWatch;
import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public abstract class AbstractImageAnalysisBlockFIS implements ImageAnalysisBlockFIS {
	
	private FlexibleImageStack debugStack;
	PhytoTopImageProcessorOptions options;
	private FlexibleMaskAndImageSet input;
	
	public AbstractImageAnalysisBlockFIS() {
		// empty
	}
	
	public void setInputAndOptions(FlexibleMaskAndImageSet input, PhytoTopImageProcessorOptions options, FlexibleImageStack debugStack) {
		this.input = input;
		this.options = options;
		this.debugStack = debugStack;
	}
	
	public void reset() {
		input = null;
		options = null;
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
}
