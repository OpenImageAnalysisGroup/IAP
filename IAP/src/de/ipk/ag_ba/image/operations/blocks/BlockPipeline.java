package de.ipk.ag_ba.image.operations.blocks;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
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
		long a = System.currentTimeMillis();
		nullPointerCheck(input, "PIPELINE INPUT ");
		
		int id = pipelineID.addInt(1);
		
		int index = 0;
		boolean blockProgressOutput = false;
		for (Class<? extends ImageAnalysisBlockFIS> blockClass : blocks) {
			ImageAnalysisBlockFIS block = blockClass.newInstance();
			block.setInputAndOptions(input, options, settings, index++,
					debugStack);
			long ta = System.currentTimeMillis();
			input = block.process();
			long tb = System.currentTimeMillis();
			int seconds = (int) ((tb - ta) / 1000);
			if (!options.getBooleanSetting(Setting.DEBUG_TAKE_TIMES))
				if (blockProgressOutput)
					System.out.println("Pipeline " + id + ": finished block "
							+ index + "/" + blocks.size() + ", took " + seconds
							+ " sec., time: " + StopWatch.getNiceTime() + " ("
							+ block.getClass().getSimpleName() + ")");
			
			block.reset();
			
			updateBlockStatistics();
		}
		
		long b = System.currentTimeMillis();
		System.out.print("PET: " + (b - a) / 1000 + "s ");
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
	
	/**
	 * The given image set is analyzed by a image pipeline upon users choice. The
	 * debug image stack (result of pipeline) will be shown to the user.
	 * 
	 * @param m
	 * @param match
	 *           Image set to be analyzed.
	 */
	public static void debugTryAnalyze(Collection<NumericMeasurementInterface> input, MongoDB m) {
		final MaizeAnalysisTask mat = new MaizeAnalysisTask();
		mat.setInput(input, m, 0, 1);
		
		final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				mat.getName(),
				mat.getTaskDescription());
		Runnable backgroundTask = new Runnable() {
			@Override
			public void run() {
				mat.debugOverrideAndEnableDebugStackStorage(true);
				mat.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1, status);
			}
		};
		Runnable finishSwingTask = new Runnable() {
			@Override
			public void run() {
				int idx = 1;
				for (FlexibleImageStack fis : mat.getForcedDebugStackStorageResult()) {
					fis.print(mat.getName() + " // Result " + idx);
					idx++;
				}
			}
		};
		BackgroundTaskHelper.issueSimpleTaskInWindow(mat.getName(), "Analyze...",
				backgroundTask,
				finishSwingTask,
				status, false, true);
	}
}
