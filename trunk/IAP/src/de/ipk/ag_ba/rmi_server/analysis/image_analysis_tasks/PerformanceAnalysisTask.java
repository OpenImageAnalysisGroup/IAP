package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.navigation_actions.CutImagePreprocessor;
import de.ipk.ag_ba.rmi_server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.IOmodule;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;

/**
 * @author klukas
 * 
 */
public class PerformanceAnalysisTask extends AbstractImageAnalysisTask {

	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();

	private String login, pass;

	public PerformanceAnalysisTask() {
		// empty
	}

	public void setInput(Collection<NumericMeasurementInterface> input, String login, String pass) {
		this.input = input;
		this.login = login;
		this.pass = pass;
	}

	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}

	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}

	@Override
	public String getTaskDescription() {
		return "Test input read performance";
	}

	@Override
	public void performAnalysis(final int maximumThreadCountParallelImages,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {

		status.setCurrentStatusValue(0);
		output = new ArrayList<NumericMeasurementInterface>();
		ArrayList<ImageData> workload = new ArrayList<ImageData>();
		for (Measurement md : input)
			if (md instanceof ImageData) {
				workload.add((ImageData) md);
			}

		final ThreadSafeOptions tsoLA = new ThreadSafeOptions();
		ExecutorService run = Executors.newFixedThreadPool(maximumThreadCountParallelImages, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoLA.addInt(1);
					i = tsoLA.getInt();
				}
				t.setName("Performance read test (" + i + ")");
				return t;
			}
		});

		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		final ThreadSafeOptions tsoRead = new ThreadSafeOptions();
		tsoRead.setInt(0);
		final ThreadSafeOptions tsoTime = new ThreadSafeOptions();
		tsoTime.setParam(0, System.currentTimeMillis());
		for (Measurement md : workload) {
			if (md instanceof ImageData) {
				final ImageData id = (ImageData) md;
				run.submit(new Runnable() {
					@Override
					public void run() {
						if (id != null) {
							try {
								// limg = IOmodule.loadImageFromFileOrMongo(id,
								// login, pass);
								long start = System.currentTimeMillis();
								byte[] imgData = IOmodule.loadImageContentFromFileOrMongo(id, login, pass);
								long end = System.currentTimeMillis();
								tsoRead.addInt(imgData.length);
								{
									NumericMeasurement m = new NumericMeasurement(id, "read length", id.getParentSample()
											.getParentCondition().getExperimentName()
											+ " (" + getName() + ")");
									setAnno(maximumThreadCountParallelImages, m);
									m.setValue(imgData.length);
									m.setUnit("bytes");
									output.add(m);
								}
								{
									NumericMeasurement m = new NumericMeasurement(id, "read performance (single image)", id
											.getParentSample().getParentCondition().getExperimentName()
											+ " (" + getName() + ")");
									setAnno(maximumThreadCountParallelImages, m);
									m.setValue(imgData.length / 1024d / 1024d / (end - start) * 1000);
									m.setUnit("MB/s");
									output.add(m);
								}
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
							tso.addInt(1);
							long time = System.currentTimeMillis();
							long startTime = (Long) tsoTime.getParam(0, null);
							if (time - startTime > 5000) {
								double mbs = tsoRead.getInt() / 1024d / 1024d / 5d;
								status.setCurrentStatusValueFine(100d * tso.getInt() / wl);
								status.setCurrentStatusText1("Image " + tso.getInt() + "/" + wl + ", " + (int) mbs + " MB/s ("
										+ maximumThreadCountParallelImages + " threads)");
								{
									NumericMeasurement m = new NumericMeasurement(id, "read performance (5 sek.)", id
											.getParentSample().getParentCondition().getExperimentName()
											+ " (" + getName() + ")");
									setAnno(maximumThreadCountParallelImages, m);
									m.setValue(mbs);
									m.setUnit("MB/s");
									output.add(m);
								}
								tsoTime.setParam(0, time);
								tsoRead.setInt(0);
							}
						}
					}

					private void setAnno(final int maximumThreadCountParallelImages, NumericMeasurement m) {
						m.getParentSample().setTime(-1);
						m.getParentSample().setTimeUnit("-1");
						m.getParentSample().getParentCondition().setSpecies(maximumThreadCountParallelImages + " threads");
						m.getParentSample().getParentCondition().setGenotype("");
						m.getParentSample().getParentCondition().setVariety("");
						m.getParentSample().getParentCondition().setTreatment("");
						m.getParentSample().getParentCondition().setSequence("");
					}
				});
			}
		}

		run.shutdown();
		try {
			run.awaitTermination(365, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}

		status.setCurrentStatusValueFine(100d);
		input = null;
	}

	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		Collection<NumericMeasurementInterface> result = output;
		output = null;
		return result;
	}

	@Override
	public String getName() {
		return "Performance Test";
	}

	public void addPreprocessor(CutImagePreprocessor pre) {
		// empty
	}

	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCountParallelImages, status);
	}
}
