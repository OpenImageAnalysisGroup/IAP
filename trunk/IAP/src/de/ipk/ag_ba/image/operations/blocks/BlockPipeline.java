package de.ipk.ag_ba.image.operations.blocks;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * 
 * @author klukas
 */
public class BlockPipeline {

	private final ArrayList<Class<? extends ImageAnalysisBlockFIS>> blocks = new ArrayList<Class<? extends ImageAnalysisBlockFIS>>();
	private final ImageProcessorOptions options;
	private static int lastPipelineExecutionTimeInSec = -1;

	public BlockPipeline(ImageProcessorOptions options) {
		this.options = options;

	}

	public void add(Class<? extends ImageAnalysisBlockFIS> blockClass) {
		blocks.add(blockClass);
	}

	public static int getLastPipelineExecutionTimeInSec() {
		return lastPipelineExecutionTimeInSec;
	}

	private static ThreadSafeOptions pipelineID = new ThreadSafeOptions();

	public FlexibleMaskAndImageSet execute(FlexibleMaskAndImageSet input,
			FlexibleImageStack debugStack, BlockProperties settings)
			throws InstantiationException, IllegalAccessException,
			InterruptedException {
		System.out.print(".");
		long a = System.currentTimeMillis();
		nullPointerCheck(input, "PIPELINE INPUT ");

		int id = pipelineID.addInt(1);

		int index = 0;
		for (Class<? extends ImageAnalysisBlockFIS> blockClass : blocks) {
			ImageAnalysisBlockFIS block = blockClass.newInstance();
			block.setInputAndOptions(input, options, settings, index++,
					debugStack);
			long ta = System.currentTimeMillis();
			input = block.process();
			long tb = System.currentTimeMillis();
			int seconds = (int) ((tb - ta) / 1000);
			if (!options.getBooleanSetting(Setting.DEBUG_TAKE_TIMES))
				System.out.println("Pipeline " + id + ": finished block "
						+ index + "/" + blocks.size() + ", took " + seconds
						+ " sec., time: " + StopWatch.getNiceTime() + " ("
						+ block.getClass().getSimpleName() + ")");

			block.reset();

			updateBlockStatistics();
		}

		long b = System.currentTimeMillis();
		System.out.println("PIPELINE execution time: " + (b - a) / 1000 + "s");
		lastPipelineExecutionTimeInSec = (int) ((b - a) / 1000);
		updatePipelineStatistics();
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
		synchronized (BlockPipeline.class) {
			pipelineExecutionsWithinCurrentHour++;
		}
	}

	public static int getPipelineExecutionsWithinCurrentHour() {
		return pipelineExecutionsWithinCurrentHour;
	}

	private static int pipelineExecutionsWithinCurrentHour = 0;

	private void nullPointerCheck(FlexibleMaskAndImageSet input, String name) {
		if (input.getImages() != null) {
			if (input.getImages().getVis() == null)
				System.out.println("WARNING: BLOCK " + name
						+ " is NULL image (vis)!");
			if (input.getImages().getFluo() == null)
				System.out.println("WARNING: BLOCK " + name
						+ " is NULL image (fluo)!");
			if (input.getImages().getNir() == null)
				System.out.println("WARNING: BLOCK " + name
						+ " is NULL image (nir)!");
		}
		if (input.getMasks() != null) {
			if (input.getMasks().getVis() == null)
				System.out.println("WARNING: BLOCK " + name
						+ " is NULL image (vis)!");
			if (input.getMasks().getFluo() == null)
				System.out.println("WARNING: BLOCK " + name
						+ " is NULL image (fluo)!");
			if (input.getMasks().getNir() == null)
				System.out.println("WARNING: BLOCK " + name
						+ " is NULL image (nir)!");
		}
	}
}
