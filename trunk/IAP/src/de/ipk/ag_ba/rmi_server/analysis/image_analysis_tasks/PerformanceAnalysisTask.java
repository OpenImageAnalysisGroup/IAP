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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

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
		final ThreadSafeOptions tsoBytesRead = new ThreadSafeOptions();
		tsoBytesRead.setLong(0);
		final ThreadSafeOptions tsoStartTime = new ThreadSafeOptions();
		tsoStartTime.setLong(System.currentTimeMillis());
		final ThreadSafeOptions tsoCurrentSecond = new ThreadSafeOptions();
		tsoCurrentSecond.setInt(0);
		for (Measurement md : workload) {
			if (md instanceof ImageData) {
				final ImageData id = (ImageData) md;
				run.submit(new Runnable() {
					@Override
					public void run() {
						if (id != null) {
							try {
								long start = System.currentTimeMillis();
								byte[] imgData = IOmodule.loadImageContentFromFileOrMongo(id, login, pass);
								long end = System.currentTimeMillis();
								tsoBytesRead.addLong(imgData.length);
								{
									NumericMeasurement m = new NumericMeasurement(id, "image size", id.getParentSample()
														.getParentCondition().getExperimentName()
														+ " (" + getName() + ")");
									setAnno(maximumThreadCountParallelImages, m);
									m.setValue(((int) (imgData.length / 1024d * 10d)) / 10d);
									m.setUnit("KB");
									output.add(m);
								}
								{
									NumericMeasurement m = new NumericMeasurement(id, "read speed (single image)", id
														.getParentSample().getParentCondition().getExperimentName()
														+ " (" + getName() + ")");
									setAnno(maximumThreadCountParallelImages, m);
									m.setValue(((int) (imgData.length * 10d / 1024d / 1024d / (end - start) * 1000)) / 10d);
									m.setUnit("MB/s");
									m.getParentSample().setTime((int) ((end - tsoStartTime.getLong()) / 1000l));
									m.getParentSample().setTimeUnit("sec");
									if (end - start > 0)
										output.add(m);
								}
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
							tso.addInt(1);
							{
								long time = System.currentTimeMillis();
								int currentSecond = (int) (time / 2000 % 2);
								if (tsoCurrentSecond.getInt() != currentSecond) {
									tsoCurrentSecond.setInt(currentSecond);
									double mbs = tsoBytesRead.getLong() / 1024d / 1024d * 1000d
														/ (time - tsoStartTime.getLong());
									status.setCurrentStatusValueFine(100d * tso.getInt() / wl);
									status.setCurrentStatusText1("Image " + tso.getInt() + "/" + wl + ", " + (int) mbs
														+ " MB/s (" + maximumThreadCountParallelImages + " threads)");
									{
										NumericMeasurement m = new NumericMeasurement(id, "read speed", id.getParentSample()
															.getParentCondition().getExperimentName()
															+ " (" + getName() + ")");
										setAnno(maximumThreadCountParallelImages, m);
										m.setValue(mbs);
										m.setUnit("MB/s");
										m.setReplicateID(1);
										m.getParentSample().setTime((int) ((time - tsoStartTime.getLong()) / 1000l));
										m.getParentSample().setTimeUnit("sec");
										output.add(m);
									}
								}
							}
						}
					}

					private void setAnno(final int maximumThreadCountParallelImages, NumericMeasurement m) {
						m.getParentSample().setTime(-1);
						m.getParentSample().setTimeUnit("-1");
						m.getParentSample().getParentCondition().setVariety("");
						m.getParentSample().getParentCondition().setTreatment("");
						m.getParentSample().getParentCondition().setSequence("");
						m.getParentSample().getParentCondition().setSpecies("Performance Test");
						m.getParentSample().getParentCondition().setGenotype(maximumThreadCountParallelImages + " threads");
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
