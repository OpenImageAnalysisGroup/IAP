package de.ipk.ag_ba.image.operations.blocks;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ObjectRef;
import org.SystemAnalysis;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * A list of image analysis "blocks" ({@link ImageAnalysisBlockFIS}) which may
 * be executed one after another.
 * 
 * @author klukas
 */
public class BlockPipeline {
	
	private final ArrayList<Class<? extends ImageAnalysisBlockFIS>> blocks = new ArrayList<Class<? extends ImageAnalysisBlockFIS>>();
	private static int lastPipelineExecutionTimeInSec = -1;
	
	/**
	 * Adds a image analysis block to the pipeline (needs to implement {@link ImageAnalysisBlockFIS}).
	 */
	public void add(Class<? extends ImageAnalysisBlockFIS> blockClass) {
		blocks.add(blockClass);
	}
	
	public void remove(Class<?> class1) {
		blocks.remove(class1);
	}
	
	/**
	 * @return The execution time for the last pipeline execution time. This is
	 *         a static method, if several pipelines are executed in parallel,
	 *         it returns the execution time of the globally last finished
	 *         pipeline.
	 */
	public static int getLastPipelineExecutionTimeInSec() {
		return lastPipelineExecutionTimeInSec;
	}
	
	private static ThreadSafeOptions pipelineID = new ThreadSafeOptions();
	
	private static long lastOutput = 0;
	
	public FlexibleMaskAndImageSet execute(ImageProcessorOptions options,
			FlexibleMaskAndImageSet input, FlexibleImageStack debugStack,
			BlockResultSet settings,
			BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		
		long a = System.currentTimeMillis();
		
		int id = pipelineID.addInt(1);
		
		int index = 0;
		boolean blockProgressOutput = false;
		
		if (status != null)
			status.setCurrentStatusValueFine(0);
		
		for (Class<? extends ImageAnalysisBlockFIS> blockClass : blocks) {
			if (status != null && status.wantsToStop())
				break;
			
			ImageAnalysisBlockFIS block = null;
			try {
				block = blockClass.newInstance();
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ "> ERROR: COULD NOT INSTANCIATE BLOCK OF CLASS "
						+ blockClass.getCanonicalName() + "!");
				throw e;
			}
			block.setInputAndOptions(input, options, settings, index++,
					debugStack);
			
			long ta = System.currentTimeMillis();
			int n = input.getImageCount();
			FlexibleMaskAndImageSet input2 = block.process();
			if (n - input.getImageCount() > 0) {
				System.out.println();
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">WARNING: BLOCK " + block.getClass().getSimpleName()
						+ " HAS SET "
						+ (n - input.getImageCount() + " IMAGE(S) TO NULL!"));
				System.out.println("IN: " + input);
				System.out.println("OUT: " + input2);
			}
			input = input2;
			long tb = System.currentTimeMillis();
			
			int seconds = (int) ((tb - ta) / 1000);
			
			int mseconds = (int) (tb - ta);
			
			// if (!options.getBooleanSetting(Setting.DEBUG_TAKE_TIMES))
			if (blockProgressOutput)
				if (seconds >= 10)
					System.out.println("Pipeline " + id + ": finished block "
							+ index + "/" + blocks.size() + ", took " + seconds
							+ " sec., " + mseconds + " ms, time: "
							+ StopWatch.getNiceTime() + " ("
							+ block.getClass().getSimpleName() + ")");
			
			updateBlockStatistics(1);
			
			if (status != null) {
				status.setCurrentStatusValueFine(100d * (index / (double) blocks
						.size()));
				status.setCurrentStatusText2("Finished " + index + "/"
						+ blocks.size());// + "<br>" +"" +
												// filter(blockClass.getSimpleName()));
				// status.setCurrentStatusText1(block.getClass().getSimpleName());
				if (status.wantsToStop())
					break;
			};
		}
		
		long b = System.currentTimeMillis();
		
		if (status != null) {
			status.setCurrentStatusValueFine(100d * (index / (double) blocks
					.size()));
			// status.setCurrentStatusText1("Pipeline finished");
			status.setCurrentStatusText1("T=" + ((b - a) / 1000) + "s");
		}
		// System.out.print("PET: " + (b - a) / 1000 + "s ");
		if (pipelineExecutionsWithinCurrentHour % 5 == 0) {
			String s5performance = "";
			long now = System.currentTimeMillis();
			if (lastOutput > 0) {
				s5performance = ""
						+ (now - lastOutput) + " ms, ";
			}
			System.out.println();
			System.out
					.print(SystemAnalysis.getCurrentTime()
							+ ">INFO: "
							+ s5performance
							+ pipelineExecutionsWithinCurrentHour
							+ " p.e., "
							+ blockExecutionWithinLastMinute
							+ " bl/m, "
							+ SystemAnalysis.getUsedMemoryInMB()
							+ "/"
							+ SystemAnalysis.getMemoryMB()
							+ " MB ("
							+ (int) (100d * SystemAnalysis.getUsedMemoryInMB() / SystemAnalysis
									.getMemoryMB()) + "%) || ");
			lastOutput = now;
		}
		lastPipelineExecutionTimeInSec = (int) ((b - a) / 1000);
		updatePipelineStatistics();
		return input;
	}
	
	private void updateBlockStatistics(int nBlocks) {
		Calendar calendar = new GregorianCalendar();
		int minute = calendar.get(Calendar.MINUTE);
		synchronized (BlockPipeline.class) {
			blockExecutionsWithinCurrentMinute += nBlocks;
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
	
	/**
	 * The given image set is analyzed by a image pipeline upon users choice.
	 * The debug image stack (result of pipeline) will be shown to the user.
	 * 
	 * @param m
	 * @param match
	 *           Image set to be analyzed.
	 */
	public static void debugTryAnalysis(
			final ExperimentInterface e,
			final Collection<NumericMeasurementInterface> input,
			final MongoDB m, AbstractPhenotypingTask analysisTask) {
		final AbstractPhenotypingTask analysisTaskFinal = analysisTask;
		final LinkedHashSet<Sample3D> samples = new LinkedHashSet<Sample3D>();
		HashMap<Sample3D, Sample3D> old2newSample = new HashMap<Sample3D, Sample3D>();
		for (NumericMeasurementInterface nmi : input) {
			if (!old2newSample.containsKey(nmi.getParentSample())) {
				Sample3D s3dNewSample = (Sample3D) nmi.getParentSample().clone(
						nmi.getParentSample().getParentCondition());
				s3dNewSample.clear();
				old2newSample.put((Sample3D) nmi.getParentSample(),
						s3dNewSample);
				samples.add(s3dNewSample);
			}
			old2newSample.get(nmi.getParentSample()).add(
					nmi.clone(old2newSample.get(nmi.getParentSample())));
		}
		
		analysisTaskFinal.setInput(
				AbstractPhenotypingTask.getWateringInfo(e),
				samples, input, m, 0, 1);
		
		final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				analysisTaskFinal.getName(), analysisTaskFinal.getTaskDescription());
		final Runnable backgroundTask = new Runnable() {
			@Override
			public void run() {
				analysisTaskFinal.debugOverrideAndEnableDebugStackStorage(true);
				try {
					analysisTaskFinal.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1,
							status);
					Collection<NumericMeasurementInterface> out = analysisTaskFinal.getOutput();
					if (out != null)
						for (NumericMeasurementInterface nmi : out) {
							if (nmi instanceof NumericMeasurement) {
								NumericMeasurement nm = (NumericMeasurement) nmi;
								String sub = nm.getParentSample().getParentCondition().getParentSubstance().getName();
								if (!sub.contains("histogram"))
									System.out.println("> "
											+ nm.getQualityAnnotation() + " // day: " + nm.getParentSample().getTime() + " // condition: "
											+ nm.getParentSample().getParentCondition().getConditionName() + " // "
											+ sub + ": " + nm.getValue());
							}
						}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		final ObjectRef finishSwingTaskRef = new ObjectRef();
		Runnable finishSwingTask = new Runnable() {
			@Override
			public void run() {
				if (analysisTaskFinal.getForcedDebugStackStorageResult() == null
						|| analysisTaskFinal.getForcedDebugStackStorageResult().isEmpty()) {
					MainFrame.showMessageDialog(
							"No pipeline results available! (" + input.size()
									+ " images input)", "Error");
				} else {
					int idx = 1;
					int nn = analysisTaskFinal.getForcedDebugStackStorageResult().size();
					for (FlexibleImageStack fis : analysisTaskFinal
							.getForcedDebugStackStorageResult()) {
						fis.print(analysisTaskFinal.getName() + " // Result " + idx + "/"
								+ nn, new Runnable() {
							@Override
							public void run() {
								analysisTaskFinal.setInput(AbstractPhenotypingTask.getWateringInfo(e), samples, input, m, 0, 1);
								BackgroundTaskHelper.issueSimpleTaskInWindow(
										analysisTaskFinal.getName(), "Analyze...",
										backgroundTask,
										(Runnable) finishSwingTaskRef
												.getObject(), status, false,
										true);
							}
						}, "Re-run Analysis (debug)");
						idx++;
					}
				}
			}
		};
		finishSwingTaskRef.setObject(finishSwingTask);
		BackgroundTaskHelper.issueSimpleTaskInWindow(analysisTaskFinal.getName(),
				"Analyze...", backgroundTask, finishSwingTask, status, false,
				true);
	}
	
	public TreeMap<Long, BlockResultSet> postProcessPipelineResultsForAllAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, BlockResultSet>> allResultsForSnapshot,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws InstantiationException, IllegalAccessException,
			InterruptedException {
		TreeMap<Long, BlockResultSet> summaryResult = new TreeMap<Long, BlockResultSet>();
		int index = 0;
		for (Class<? extends ImageAnalysisBlockFIS> blockClass : blocks) {
			ImageAnalysisBlockFIS block = blockClass.newInstance();
			block.setInputAndOptions(null, null, null, index++, null);
			block.postProcessResultsForAllTimesAndAngles(
					plandID2time2waterData,
					inSample, inImages,
					allResultsForSnapshot, summaryResult, optStatus);
		}
		
		return summaryResult;
	}
	
}
