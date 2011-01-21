package de.ipk.ag_ba.image.operations.blocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.cmds.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class BlockPipeline {
	
	private final ArrayList<Class<? extends ImageAnalysisBlockFIS>> blocks = new ArrayList<Class<? extends ImageAnalysisBlockFIS>>();
	private final PhytoTopImageProcessorOptions options;
	private static int lastPipelineExecutionTimeInSec = -1;
	
	public BlockPipeline(PhytoTopImageProcessorOptions options) {
		this.options = options;
		
	}
	
	public void add(Class<? extends ImageAnalysisBlockFIS> blockClass) {
		blocks.add(blockClass);
	}
	
	public static int getLastPipelineExecutionTimeInSec() {
		return lastPipelineExecutionTimeInSec;
	}
	
	public FlexibleMaskAndImageSet execute(FlexibleMaskAndImageSet input, FlexibleImageStack debugStack, BlockProperties settings)
			throws InstantiationException, IllegalAccessException {
		// System.out.println("Execute BLOCK pipeline...");
		System.out.print(".");
		long a = System.currentTimeMillis();
		nullPointerCheck(input, "PIPELINE INPUT ");
		
		// BlockProperties settings = new BlockPropertiesImpl();
		
		int index = 0;
		for (Class<? extends ImageAnalysisBlockFIS> blockClass : blocks) {
			ImageAnalysisBlockFIS block = blockClass.newInstance();
			
			block.setInputAndOptions(input, options, settings, index++, debugStack);
			
			// nullPointerCheck(input, "INPUT for " + blockClass.getSimpleName());
			
			input = block.process();
			
			// nullPointerCheck(input, "OUTPUT of " + blockClass.getSimpleName());
			
			block.reset();
			
			updateBlockStatistics();
		}
		long b = System.currentTimeMillis();
		System.out.println("PIPELINE execution time: " + (b - a) / 1000 + "s");
		lastPipelineExecutionTimeInSec = (int) ((b - a) / 1000);
		updatePipelineStatistics();
		// if (settings.getNumberOfBlocksWithPropertyResults() > 0)
		// System.out.println("Results:\n" + settings.toString());
		return input;
	}
	
	private void updateBlockStatistics() {
		Calendar calendar = new GregorianCalendar();
		int minute = calendar.get(Calendar.MINUTE);
		synchronized (BlockPipeline.class) {
			blockExecutionsWithinCurrentMinute++;
			if (currentMinuteB != minute) {
				blockExecutionWithinLastMinute = blockExecutionsWithinCurrentMinute;
				blockExecutionsWithinCurrentMinute = 0;
				currentMinuteB = minute;
			}
		}
	}
	
	public static int getBlockExecutionsWithinLastMinute() {
		return blockExecutionWithinLastMinute;
	}
	
	private static int blockExecutionWithinLastMinute = 0;
	private static int blockExecutionsWithinCurrentMinute = 0;
	private static int currentMinuteB = -1;
	
	private void updatePipelineStatistics() {
		Calendar calendar = new GregorianCalendar();
		int minute5 = calendar.get(Calendar.MINUTE) / 5;
		synchronized (BlockPipeline.class) {
			pipelineExecutionsWithinCurrent5Minutes++;
			if (current5MinuteP != minute5) {
				pipelineExecutionWithinLast5Minutes = pipelineExecutionsWithinCurrent5Minutes;
				pipelineExecutionsWithinCurrent5Minutes = 0;
				current5MinuteP = minute5;
			}
		}
	}
	
	public static int getPipelineExecutionsWithinLast5Minutes() {
		return pipelineExecutionWithinLast5Minutes;
	}
	
	private static int pipelineExecutionWithinLast5Minutes = 0;
	private static int pipelineExecutionsWithinCurrent5Minutes = 0;
	private static int current5MinuteP = -1;
	
	private void nullPointerCheck(FlexibleMaskAndImageSet input, String name) {
		if (input.getImages() != null) {
			if (input.getImages().getVis() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (vis)!");
			if (input.getImages().getFluo() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (fluo)!");
			if (input.getImages().getNir() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (nir)!");
		}
		if (input.getMasks() != null) {
			if (input.getMasks().getVis() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (vis)!");
			if (input.getMasks().getFluo() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (fluo)!");
			if (input.getMasks().getNir() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (nir)!");
		}
	}
}
