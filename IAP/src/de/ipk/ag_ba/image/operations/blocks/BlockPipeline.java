package de.ipk.ag_ba.image.operations.blocks;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.preprocessing.WellProcessor;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.StringAndFlexibleMaskAndImageSet;
import info.StopWatch;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.ActionSettings;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * A list of image analysis "blocks" ({@link ImageAnalysisBlock}) which may
 * be executed one after another.
 * 
 * @author klukas
 */
public class BlockPipeline {
	
	private final ArrayList<Class<? extends ImageAnalysisBlock>> blocks = new ArrayList<Class<? extends ImageAnalysisBlock>>();
	private static int lastPipelineExecutionTimeInSec = -1;
	
	/**
	 * Adds a image analysis block to the pipeline (needs to implement {@link ImageAnalysisBlock}).
	 */
	public void add(Class<? extends ImageAnalysisBlock> blockClass) {
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
	
	public void execute(final ImageProcessorOptionsAndResults options,
			final MaskAndImageSet input,
			final HashMap<Integer, BlockResultSet> blockResults,
			final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		// normally each image is analyzed once (i.e. one plant per image)
		// for arabidopsis trays with 2x3 or 2x4 sections the
		// pipeline will be executed 6 or 12 times per image
		// the load-image-block then needs to cut out image 1/6, 2/6, ...
		// and place the section in the middle of the image for further processing
		int executionTrayCount = 1;
		for (Class<? extends ImageAnalysisBlock> blockClass : blocks) {
			if (blockClass != null && (WellProcessor.class.isAssignableFrom(blockClass))) {
				WellProcessor inst = (WellProcessor) blockClass.newInstance();
				int n = inst.getDefinedWellCount(options);
				if (n > 0)
					executionTrayCount = n;
			}
		}
		options.setWellCnt(executionTrayCount);
		final ObjectRef exception = new ObjectRef();
		if (status != null)
			status.setCurrentStatusValue(0);
		ArrayList<LocalComputeJob> jobs = new ArrayList<LocalComputeJob>();
		for (int idx = 0; idx < executionTrayCount; idx++) {
			if (debugValidTrays != null && !debugValidTrays.contains(idx))
				continue;
			final int well = idx;
			final int wellCnt = executionTrayCount;
			// Runnable r = new Runnable() {
			//
			// @Override
			// public void run() {
			ImageStack ds = options.forceDebugStack ? new ImageStack() : null;
			try {
				BlockResultSet res = executeInnerCall(well, wellCnt, options, new StringAndFlexibleMaskAndImageSet(null, input), ds, status);
				if (options.forceDebugStack)
					synchronized (options.forcedDebugStacks) {
						options.forcedDebugStacks.add(ds);
					}
				res.clearStoredPostprocessors();
				res.clearNotUsedResults();
				synchronized (blockResults) {
					blockResults.put(well, res);
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				exception.setObject(e);
			}
		}
		// };
		// jobs.add(BackgroundThreadDispatcher.addTask(r, "Analyse well " + idx));
		// }
		// BackgroundThreadDispatcher.waitFor(jobs);
		if (exception.getObject() != null)
			throw ((Exception) exception.getObject());
	}
	
	private BlockResultSet executeInnerCall(int well, int executionWellCount,
			ImageProcessorOptionsAndResults options,
			StringAndFlexibleMaskAndImageSet input, ImageStack debugStack,
			BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		BlockResultSet results = new BlockResults(options.getCameraAngle());
		long a = System.currentTimeMillis();
		
		int id = pipelineID.addInt(1);
		
		int index = 0;
		boolean blockProgressOutput = true;
		
		boolean debug = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "Debug-Pipeline-Execution", false);
		int tPrintBlockTime = SystemOptions.getInstance().getInteger("Pipeline-Debugging", "Info-Print-Block-Execution-Time", 30);
		
		if (SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "Print-Block-Analysis-Results", false)) {
			int n = 0;
			
			System.out.println("\n##Blocks##");
			for (Class<? extends ImageAnalysisBlock> blockClass : blocks) {
				System.out.println("Block " + n + "=" + blockClass.getSimpleName());
				n++;
			}
			System.out.println("##Output##");
		}
		
		double progressOfThisWell = 100d / executionWellCount;
		int nBlocks = blocks.size();
		for (Class<? extends ImageAnalysisBlock> blockClass : blocks) {
			if (status != null && status.wantsToStop()) {
				System.out.println("Break requested");
				break;
			}
			
			ImageAnalysisBlock block = null;
			try {
				block = blockClass.newInstance();
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ "> ERROR: COULD NOT INSTANCIATE BLOCK OF CLASS "
						+ blockClass.getCanonicalName() + "!");
				throw e;
			}
			
			block.setInputAndOptions(well, input.getMaskAndImageSet(), options, results, index++,
					debugStack);
			
			long ta = System.currentTimeMillis();
			
			input.setMaskAndImageSet(block.process());
			input.setOptions(block.getClass().getCanonicalName());
			
			long tb = System.currentTimeMillis();
			
			int seconds = (int) ((tb - ta) / 1000);
			
			int mseconds = (int) (tb - ta);
			
			if (blockProgressOutput)
				if (seconds >= (debug ? 0 : tPrintBlockTime))
					System.out.println("Pipeline " + id + ": finished block "
							+ index + "/" + blocks.size() + ", took " + seconds
							+ " sec., " + mseconds + " ms, time: "
							+ StopWatch.getNiceTime() + " ("
							+ block.getClass().getSimpleName() + ")");
			
			updateBlockStatistics(1);
			if (status != null)
				status.setCurrentStatusValueFineAdd(progressOfThisWell / nBlocks);
		}
		
		long b = System.currentTimeMillis();
		
		if (status != null) {
			String s1 = status.getCurrentStatusMessage2();
			String div = "<br>";
			if (s1.contains(div))
				s1 = s1.substring(0, s1.indexOf(div));
			if (s1 != null && !s1.isEmpty())
				s1 = s1 + ", ";
			int n = StringManipulationTools.count(s1, ", ");
			if (n >= 5)
				s1 = s1.substring(s1.indexOf(", ") + ", ".length());
			String vfsSpeed = VirtualFileSystemVFS2.getVFSspeedInfo(div, "");
			status.setCurrentStatusText2(s1 + ((b - a) / 1000) + "s" + vfsSpeed);
		}
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
		return results;
	}
	
	private static long lastBlockUpdate = 0;
	
	public static long getLastBlockUpdateTime() {
		return lastBlockUpdate;
	}
	
	private void updateBlockStatistics(int nBlocks) {
		overallBlockExecutions += nBlocks;
		Calendar calendar = new GregorianCalendar();
		int minute = calendar.get(Calendar.MINUTE);
		lastBlockUpdate = System.currentTimeMillis();
		blockExecutionsWithinCurrentMinute += nBlocks;
		if (currentMinuteB != minute) {
			blockExecutionWithinLastMinute = blockExecutionsWithinCurrentMinute;
			blockExecutionsWithinCurrentMinute = 0;
			currentMinuteB = minute;
		}
	}
	
	public static int getBlockExecutionsWithinLastMinute() {
		return blockExecutionWithinLastMinute;
	}
	
	public static long getBlockExecutionsOverall() {
		return overallBlockExecutions;
	}
	
	private static long overallBlockExecutions = 0;
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
	private HashSet<Integer> debugValidTrays;
	
	/**
	 * The given image set is analyzed by a image pipeline upon users choice.
	 * The debug image stack (result of pipeline) will be shown to the user.
	 * 
	 * @param m
	 * @param match
	 *           Image set to be analyzed.
	 */
	public static void debugTryAnalysis(
			final ExperimentReference er,
			final Collection<NumericMeasurementInterface> input,
			AbstractPhenotypingTask analysisTask) {
		final ExperimentInterface e = er.getExperiment();
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
				samples, input, er.m, 0, 1);
		
		final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				analysisTaskFinal.getName(), analysisTaskFinal.getTaskDescription());
		final Runnable backgroundTask = new Runnable() {
			@Override
			public void run() {
				analysisTaskFinal.debugOverrideAndEnableDebugStackStorage(true);
				try {
					analysisTaskFinal.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1,
							status);
					ExperimentInterface out = analysisTaskFinal.getOutput();
					if (out != null)
						for (NumericMeasurementInterface nmi : Substance3D.getAllMeasurements(out)) {
							if (nmi instanceof NumericMeasurement) {
								NumericMeasurement nm = (NumericMeasurement) nmi;
								String sub = nm.getParentSample().getParentCondition().getParentSubstance().getName();
								if (!sub.contains("histogram"))
									System.out.println("> "
											+ nm.getQualityAnnotation() + " // day: " + nm.getParentSample().getTime() + " // condition: "
											+ nm.getParentSample().getParentCondition().getConditionName() + " // "
											+ sub + ": " + nm.getValue() + " [" + nm.getUnit() + "]");
							}
						}
				} catch (InterruptedException e) {
					MongoDB.saveSystemErrorMessage("BlockPipeline backgroundTask InterruptedException.", e);
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
					final ThreadSafeOptions tsoCurrentImageDisplayPage = new ThreadSafeOptions();
					JButton openSettingsButton = null;
					if (er.getIniIoProvider() != null) {
						openSettingsButton = new JButton("Change Analysis Settings");
						openSettingsButton.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Science-64.png").getScaledInstance(24, 24,
								Image.SCALE_SMOOTH)));
						openSettingsButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								IAPnavigationPanel mnp = new IAPnavigationPanel(PanelTarget.NAVIGATION, null, null);
								ActionSettings ac = new ActionSettings(null, er.getIniIoProvider(),
										"Change analysis settings (experiment " + er.getExperimentName()
												+ ")", "Modify settings");
								ac.setInitialNavigationPath(analysisTaskFinal.getDebugLastSystemOptionStorageGroup());
								ac.setInitialNavigationSubPath((String) tsoCurrentImageDisplayPage.getParam(0, null)); // current image analysis step
								mnp.getNewWindowListener(ac).actionPerformed(arg0);
							}
						});
					}
					int idx = 1;
					int nn = analysisTaskFinal.getForcedDebugStackStorageResult().size();
					for (ImageStack fisArr : analysisTaskFinal.getForcedDebugStackStorageResult()) {
						if (fisArr == null) {
							idx++;
							continue;
						}
						ImageStack fis = fisArr;
						final int idxF = idx - 1;
						fis.show(analysisTaskFinal.getName() + " // Result tray " + idx + "/" + nn, new Runnable() {
							@Override
							public void run() {
								analysisTaskFinal.debugSetValidTrays(new int[] { idxF });
								analysisTaskFinal.setInput(AbstractPhenotypingTask.getWateringInfo(e), samples, input, er.m, 0, 1);
								BackgroundTaskHelper.issueSimpleTaskInWindow(
										analysisTaskFinal.getName(), "Analyze...",
										backgroundTask,
										(Runnable) finishSwingTaskRef.getObject(), status, false,
										true);
							}
						}, "Repeat Analysis", openSettingsButton, tsoCurrentImageDisplayPage);
						idx++;
					}
					idx++;
				}
			}
		};
		finishSwingTaskRef.setObject(finishSwingTask);
		BackgroundTaskHelper.issueSimpleTaskInWindow(analysisTaskFinal.getName(),
				"Analyze...", backgroundTask, finishSwingTask, status, false,
				true);
	}
	
	public TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> postProcessPipelineResultsForAllAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, ImageProcessorOptionsAndResults options)
			throws InstantiationException, IllegalAccessException,
			InterruptedException {
		TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> summaryResult =
				new TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>>();
		int index = 0;
		for (Class<? extends ImageAnalysisBlock> blockClass : blocks) {
			ImageAnalysisBlock block = blockClass.newInstance();
			block.setInputAndOptions(-1, null, options, null, index++, null);
			block.postProcessResultsForAllTimesAndAngles(
					plandID2time2waterData,
					inSample, inImages,
					analysisResults, summaryResult, optStatus,
					null);
		}
		
		return summaryResult;
	}
	
	public void setValidTrays(int[] debugValidTrays) {
		if (debugValidTrays == null)
			return;
		
		this.debugValidTrays = new HashSet<Integer>();
		for (int i : debugValidTrays)
			this.debugValidTrays.add(i);
	}
	
	public int getSize() {
		return blocks.size();
	}
	
	public static void ping() {
		overallBlockExecutions++;
	}
}
