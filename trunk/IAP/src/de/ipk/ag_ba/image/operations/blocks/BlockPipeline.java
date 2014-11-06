package de.ipk.ag_ba.image.operations.blocks;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.postprocessing.WellProcessing;
import iap.blocks.preprocessing.WellProcessor;
import iap.pipelines.ImageProcessorOptionsAndResults;
import info.StopWatch;
import info.clearthought.layout.TableLayout;

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
import java.util.stream.IntStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import de.ipk.ag_ba.commands.BlockMonitoringResult;
import de.ipk.ag_ba.commands.PipelineMonitoringResult;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.OptionsGenerator;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * A list of image analysis "blocks" ({@link ImageAnalysisBlock}) which may
 * be executed one after another.
 * 
 * @author klukas
 */
public class BlockPipeline {
	
	private static final HashMap<BlockPipeline, PipelineMonitoringResult> monitoringResultHashMap = new HashMap<BlockPipeline, PipelineMonitoringResult>();
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
	
	public void execute(final OptionsGenerator og,
			final HashMap<String, BlockResultSet> blockResults,
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
				int n = inst.getDefinedWellCount(og.getOptions());
				if (n > 0)
					executionTrayCount = n;
			}
		}
		final int executionTrayCountFF = executionTrayCount;
		final ObjectRef exception = new ObjectRef();
		if (status != null)
			status.setCurrentStatusValue(0);
		boolean quick = true;
		if (quick) {
			BackgroundThreadDispatcher.process(IntStream.range(0, executionTrayCount), (currentWell) -> {
				if (debugValidTrays != null && !debugValidTrays.contains(currentWell))
					return;
				ImageProcessorOptionsAndResults options = og.getOptions();
				options.setWellCnt(currentWell, executionTrayCountFF);
				ImageStack ds = options.forceDebugStack ? new ImageStack() : null;
				try {
					String wellName = WellProcessing.getWellID(currentWell, executionTrayCountFF,
							options.getCameraPosition(), options.getCameraAngle(), options);
					BlockResultSet res = executeInnerCall(wellName, currentWell, executionTrayCountFF, options,
							ds, status, og.getImageSet(), og.getMaskSet());
					if (options.forceDebugStack)
						synchronized (options.forcedDebugStacks) {
							ds.setWell(options.getWellIdx(), options.getWellCnt());
							options.forcedDebugStacks.add(ds);
						}
					res.clearStoredPostprocessors();
					res.clearNotUsedResults();
					synchronized (blockResults) {
						blockResults.put(wellName, res);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
					exception.setObject(e);
				}
			}, (Thread t, Throwable e) -> {
				ErrorMsg.addErrorMessage(new Exception(e));
				exception.setObject(e);
			});
		} else {
			if (status != null)
				status.setCurrentStatusValue(0);
			for (int currentWell = 0; currentWell < executionTrayCount; currentWell++) {
				if (debugValidTrays != null && !debugValidTrays.contains(currentWell))
					continue;
				ImageProcessorOptionsAndResults options = og.getOptions();
				options.setWellCnt(currentWell, executionTrayCount);
				ImageStack ds = options.forceDebugStack ? new ImageStack() : null;
				try {
					String wellName = WellProcessing.getWellID(currentWell, executionTrayCount,
							options.getCameraPosition(), options.getCameraAngle(), options);
					BlockResultSet res = executeInnerCall(wellName, currentWell, executionTrayCount, options,
							ds, status, og.getImageSet(), og.getMaskSet());
					if (options.forceDebugStack)
						synchronized (options.forcedDebugStacks) {
							ds.setWell(options.getWellIdx(), options.getWellCnt());
							options.forcedDebugStacks.add(ds);
						}
					res.clearStoredPostprocessors();
					res.clearNotUsedResults();
					synchronized (blockResults) {
						blockResults.put(wellName, res);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
					exception.setObject(e);
				}
			}
		}
		
		if (exception.getObject() != null)
			throw ((Exception) exception.getObject());
	}
	
	private BlockResultSet executeInnerCall(String wellName, int well, int executionWellCount,
			ImageProcessorOptionsAndResults options, ImageStack debugStack,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			ImageSet inputSet, ImageSet maskSet)
			throws Exception {
		BlockResultSet results = new BlockResults(options.getCameraAngle());
		long a = System.currentTimeMillis();
		
		int id = pipelineID.addInt(1);
		
		int index = 0;
		boolean blockProgressOutput = true;
		
		boolean debug = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "Debug-Pipeline-Execution", false);
		int tPrintBlockTime = SystemOptions.getInstance().getInteger("Pipeline-Debugging", "Info-Print-Block-Execution-Time", 30);
		
		double progressOfThisWell = 100d / executionWellCount;
		int nBlocks = blocks.size();
		
		MaskAndImageSet workset = new MaskAndImageSet(inputSet, maskSet != null ? maskSet : inputSet.copy());
		int blockCount = blocks.size();
		for (Class<? extends ImageAnalysisBlock> blockClass : blocks) {
			if (status != null && status.wantsToStop()) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Break requested. Stopping pipeline execution flow.");
				break;
			}
			
			ImageAnalysisBlock block = null;
			try {
				block = blockClass.newInstance();
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: COULD NOT INSTANCIATE BLOCK OF CLASS "
						+ blockClass.getCanonicalName() + "!");
				throw e;
			}
			
			block.setInputAndOptions(wellName, workset, options, results, index++,
					debugStack);
			
			long ta = System.currentTimeMillis();
			workset = block.process();
			long tb = System.currentTimeMillis();
			
			if (System.currentTimeMillis() < pipelineMonitoringMaxValidTimePoint) {
				// save block results
				boolean firstSave = true;
				PipelineMonitoringResult currentPipelineMonitoringResult = null;
				synchronized (BlockPipeline.monitoringResultHashMap) {
					if (!BlockPipeline.monitoringResultHashMap.containsKey(this)) {
						String plantID = workset.images().getAnyInfo().getQualityAnnotation();
						Long snapshotTime = workset.images().getAnyInfo().getParentSample().getSampleFineTimeOrRowId();
						BlockPipeline.monitoringResultHashMap.put(this, new PipelineMonitoringResult(plantID, snapshotTime));
					}
					currentPipelineMonitoringResult = BlockPipeline.monitoringResultHashMap.get(this);
				}
				synchronized (currentPipelineMonitoringResult) {
					if (block.isChangingImages() && currentPipelineMonitoringResult.isEmpty() || (index == blockCount && firstSave)) {
						if (index == blockCount && firstSave)
							firstSave = false;
						currentPipelineMonitoringResult
								.addBlockResult(new BlockMonitoringResult(workset, pipelineMonitoringResultImageSize, block.getName(), tb - ta));
					}
				}
				if (index == blockCount) {
					// last block in list
					lastPipelineMonitoringResult = currentPipelineMonitoringResult;
				}
			}
			
			int seconds = (int) ((tb - ta) / 1000);
			
			int mseconds = (int) (tb - ta);
			
			if (blockProgressOutput)
				if (seconds >= (debug ? 0 : tPrintBlockTime))
					System.out.println(SystemAnalysis.lineSeparator
							+ SystemAnalysis.getCurrentTime() + ">INFO Pipeline " + id + ": finished block "
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
		int ndiv = 20;
		if (pipelineExecutionsWithinCurrentHour % ndiv == 0) {
			String s5performance = "";
			long now = System.currentTimeMillis();
			if (lastOutput > 0) {
				s5performance = ""
						+ (now - lastOutput) + " ms, ";
			}
			double mu = 100d * SystemAnalysis.getUsedMemoryInMB() / SystemAnalysis
					.getMemoryMB();
			String ss = (pipelineExecutionsWithinCurrentHour > 0 ? "" : "") + SystemAnalysis.lineSeparator + SystemAnalysis.getCurrentTime()
					+ ">INFO: "
					// + s5performance
					+ pipelineExecutionsWithinCurrentHour
					+ " p.e., "
					+ blockExecutionWithinLastMinute
					+ " bl/m"
					+ ", CPU Load " + StringManipulationTools.formatNumber(
							SystemAnalysisExt.getRealSystemCpuLoad(), "##.#")
					+ ", MEM " + SystemAnalysis.getUsedMemoryInMB()
					+ "/"
					+ SystemAnalysis.getMemoryMB()
					+ " MB";
			int len = ss.length();
			System.out
					.print(ss + StringManipulationTools.getString(5 - (len % 5) - 1, " ")
							+ " "
							+ StringManipulationTools.formatNumberAddZeroInFront((long) mu, 2) + "% "
							+ StringManipulationTools.getString(mu / 10, (mu > 80 ? "!" : "#"))
							+ StringManipulationTools.getString(10 - mu / 10 - 1, "-")
							+ " || ");
			lastOutput = now;
		}
		if (pipelineExecutionsWithinCurrentHour / ndiv % (100 / ndiv) == 0)
			System.out.print("-");
		else
			System.out.print("#");
		if ((pipelineExecutionsWithinCurrentHour + 1) % ndiv != 0
				&& (pipelineExecutionsWithinCurrentHour + 1) % 5 == 0)
			System.out.print(":");
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
	private static PipelineMonitoringResult lastPipelineMonitoringResult;
	private static int pipelineMonitoringResultImageSize;
	private static long pipelineMonitoringMaxValidTimePoint;
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
		
		BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				analysisTaskFinal.getName(), analysisTaskFinal.getTaskDescription());
		Runnable backgroundTask = new Runnable() {
			@Override
			public void run() {
				analysisTaskFinal.debugOverrideAndEnableDebugStackStorage(true);
				try {
					analysisTaskFinal.performAnalysis(status);
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
									+ " images used as input)", "Error");
				} else {
					for (ImageStack fisArr : analysisTaskFinal.getForcedDebugStackStorageResult()) {
						if (fisArr == null) {
							continue;
						}
						ImageStack fis = fisArr;
						final int idxF = fisArr.getWell();
						final int idxCntF = fisArr.getWellCnt();
						NumericMeasurementInterface iii = input.iterator().next();
						
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
									mnp.getNewWindowListener(ac, true).actionPerformed(arg0);
								}
							});
						} else
							System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No INI-I/O-Provider given for window.");
						
						analysisTaskFinal.setInput(
								AbstractPhenotypingTask.getWateringInfo(e),
								samples, input, er.m, idxF, idxCntF);
						
						BackgroundTaskStatusProviderSupportingExternalCall statusInnerCall = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
								analysisTaskFinal.getName(), analysisTaskFinal.getTaskDescription());
						Runnable backgroundTaskInnerCall = new Runnable() {
							@Override
							public void run() {
								analysisTaskFinal.debugOverrideAndEnableDebugStackStorage(true);
								try {
									analysisTaskFinal.performAnalysis(statusInnerCall);
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
						
						String wellInfo = (idxF + 1) + "/" + idxCntF;
						if (analysisTaskFinal.debugGetValidTrays() != null)
							wellInfo = StringManipulationTools.getStringList(IAPservice.add(analysisTaskFinal.debugGetValidTrays(), 1), ",");
						
						final String wif = wellInfo;
						final JButton osf = openSettingsButton;
						ObjectRef or = new ObjectRef();
						ObjectRef stack = new ObjectRef();
						Runnable prepare = new Runnable() {
							@Override
							public void run() {
								if (fis.getStack() != null)
									stack.setObject(fis.getStack().duplicate());
							}
						};
						Runnable reopen = new Runnable() {
							@Override
							public void run() {
								if (stack.getObject() != null)
									fis.setStack((ij.ImageStack) stack.getObject());
								fis.show(iii.getQualityAnnotation() + " / " + iii.getParentSample().getSampleTime() + " / " + analysisTaskFinal.getName() + " / Well "
										+ wif,
										new Runnable() {
											@Override
											public void run() {
												analysisTaskFinal.debugSetValidTrays(new int[] { idxF });
												analysisTaskFinal.setInput(AbstractPhenotypingTask.getWateringInfo(e), samples, input, er.m, 0, 1);
												BackgroundTaskHelper.issueSimpleTaskInWindow(
														analysisTaskFinal.getName(), "Repeat Snapshot Analysis...",
														backgroundTaskInnerCall,
														(Runnable) finishSwingTaskRef.getObject(), statusInnerCall, false,
														true);
											}
										}, "Repeat Analysis",
										TableLayout.get3Split(osf, null, new JButton(getCloseAction(prepare, (Runnable) (or.getObject()))), TableLayout.PREFERRED, 5,
												TableLayout.PREFERRED),
										tsoCurrentImageDisplayPage);
							}
						};
						or.setObject(reopen);
						
						reopen.run();
					}
				}
			}
		};
		finishSwingTaskRef.setObject(finishSwingTask);
		analysisTaskFinal.setInput(
				AbstractPhenotypingTask.getWateringInfo(e),
				samples, input, er.m, 0, 1);
		BackgroundTaskHelper.issueSimpleTaskInWindow(analysisTaskFinal.getName(),
				"Analyze...", backgroundTask, finishSwingTask, status, false,
				true);
	}
	
	protected static Action getCloseAction(Runnable runnablePrepare, Runnable runnablePost) {
		
		Action action2 = new AbstractAction("Close Additional Image Windows", new ImageIcon(IAPimages.getImage("img/close_frame.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (IAPservice.getIAPimageWindowCount() > 1) {
					runnablePrepare.run();
					IAPservice.closeAllImageJimageWindows();
					runnablePost.run();
				} else {
					MainFrame.showMessageDialog("No additional windows are open!", "Information");
				}
			}
			
			@Override
			public boolean isEnabled() {
				return true;
			}
			
		};
		
		return action2;
	}
	
	public TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> postProcessPipelineResultsForAllAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, ImageProcessorOptionsAndResults options)
			throws InstantiationException, IllegalAccessException,
			InterruptedException {
		TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> summaryResult =
				new TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>>();
		int index = 0;
		for (Class<? extends ImageAnalysisBlock> blockClass : blocks) {
			ImageAnalysisBlock block = blockClass.newInstance();
			block.setInputAndOptions(null, null, options, null, index++, null);
			block.postProcessResultsForAllTimesAndAngles(
					plandID2time2waterData,
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
	
	public static void activateBlockResultMonitoring(int imageSize, int validTime) {
		pipelineMonitoringResultImageSize = imageSize;
		pipelineMonitoringMaxValidTimePoint = System.currentTimeMillis() + validTime;
	}
	
	public static PipelineMonitoringResult getLastPipelineMonitoringResults() {
		if (System.currentTimeMillis() >= pipelineMonitoringMaxValidTimePoint || lastPipelineMonitoringResult == null) {
			lastPipelineMonitoringResult = null;
			synchronized (monitoringResultHashMap) {
				monitoringResultHashMap.clear();
			}
			return null;
		}
		return lastPipelineMonitoringResult;
	}
}
