package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.concurrent.Callable;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.actions.ImagePreProcessor;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.CutImagePreprocessor;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author klukas
 */
public class PhenotypeAnalysisTask extends AbstractImageAnalysisTask {
	
	public static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 255);
	public static final int BACKGROUND_COLORint = BACKGROUND_COLOR.getRGB();
	
	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	private double epsilonA;
	private double epsilonB;
	
	private final DatabaseTarget storeResultInDatabase;
	
	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();
	
	public PhenotypeAnalysisTask(DatabaseTarget storeResultInDatabase) {
		this.storeResultInDatabase = storeResultInDatabase;
	}
	
	public PhenotypeAnalysisTask(double epsilonA, double epsilonB, DatabaseTarget storeResultInDatabase) {
		this.epsilonA = epsilonA;
		this.epsilonB = epsilonB;
		this.storeResultInDatabase = storeResultInDatabase;
	}
	
	@Override
	public void setInput(Collection<NumericMeasurementInterface> input, MongoDB m, int workLoadIndex, int workLoadSize) {
		this.input = input;
	}
	
	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}
	
	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE, ImageAnalysisType.MEASUREMENT };
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Plants Phenotype";
	}
	
	@Override
	public void performAnalysis(final int maximumThreadCountParallelImages, final int maximumThreadCountOnImageLevel,
						final BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		status.setCurrentStatusValue(0);
		output = new ArrayList<NumericMeasurementInterface>();
		ArrayList<ImageData> workload = new ArrayList<ImageData>();
		for (Measurement md : input)
			if (md instanceof ImageData) {
				workload.add((ImageData) md);
			}
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		
		final Stack<Callable<Integer>> jobs = new Stack<Callable<Integer>>();
		
		for (Measurement md : workload) {
			if (md instanceof ImageData) {
				final ImageData id = (ImageData) md;
				jobs.add(new Callable<Integer>() {
					@Override
					public Integer call() throws Exception {
						
						LoadedImage limg = null;
						if (id != null) {
							if (id instanceof LoadedImage) {
								limg = (LoadedImage) id;
							} else {
								try {
									limg = IOmodule.loadImageFromFileOrMongo(id, true, true);
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
							BlockClearBackground.clearBackgroundAndInterpretImage(limg, maximumThreadCountOnImageLevel,
													storeResultInDatabase, status, true, output, preProcessors, epsilonA,
													epsilonB);
							limg = null;
							tso.addInt(1);
							status.setCurrentStatusValueFine(100d * tso.getInt() / wl);
							status.setCurrentStatusText1("Image " + tso.getInt() + "/" + wl);
						}
						
						return tso.getInt();
					}
				});
			}
		}
		int idx = 1, maxJob = jobs.size();
		ArrayList<MyThread> threads = new ArrayList<MyThread>();
		while (!jobs.empty()) {
			final Callable<Integer> call = jobs.pop();
			threads.add(BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					try {
						call.call();
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}, getName() + " job " + (idx++) + "/" + maxJob, -1));
		}
		try {
			BackgroundThreadDispatcher.waitFor(threads);
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		
		status.setCurrentStatusValueFine(100d);
		input = null;
	}
	
	public static Geometry detectGeometry(int w, int h, int[] rgbArray, int iBackgroundFill, LoadedImage limg) {
		
		int left = w;
		int right = 0;
		int top = h;
		
		double cutline = 1.0; // 0.95
		
		for (int x = 0; x < w; x++)
			for (int y = h - 1; y > 0; y--) {
				int o = x + y * w;
				
				if (y > h * cutline) {
					rgbArray[o] = iBackgroundFill;
					continue;
				}
				if (rgbArray[o] == iBackgroundFill)
					continue;
				
				if (rgbArray[o] != iBackgroundFill) {
					if (x < left)
						left = x;
					if (x > right)
						right = x;
					if (y < top)
						top = y;
				}
			}
		
		long filled = 0;
		for (int x = 0; x < w; x++) {
			for (int y = h - 1; y > 0; y--) {
				int o = x + y * w;
				if (rgbArray[o] != iBackgroundFill) {
					filled++;
				}
			}
		}
		
		return new Geometry(top, left, right, filled);
	}
	
	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		Collection<NumericMeasurementInterface> result = output;
		output = null;
		return result;
	}
	
	public static String getNameStatic() {
		return "Phenotype Analysis";
	}
	
	@Override
	public String getName() {
		return getNameStatic();
	}
	
	public void addPreprocessor(CutImagePreprocessor pre) {
		preProcessors.add(pre);
	}
	
	public static LoadedImage clearBackground(LoadedImage image, int maximumThreadCount) throws InterruptedException {
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		BlockClearBackground.clearBackgroundAndInterpretImage(image, maximumThreadCount, null, null, false, output, null, 2.5d, 5d);
		LoadedImage res = (LoadedImage) output.iterator().next();
		return res;
	}
}
