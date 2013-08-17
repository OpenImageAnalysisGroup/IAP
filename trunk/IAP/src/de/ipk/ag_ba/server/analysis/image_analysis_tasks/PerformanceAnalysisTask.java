package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class PerformanceAnalysisTask implements ImageAnalysisTask {
	
	private Collection<Sample3D> input = new ArrayList<Sample3D>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	
	private int workLoadIndex;
	
	private final TextFile errors = new TextFile();
	
	public PerformanceAnalysisTask() {
		// empty
	}
	
	@Override
	public void setInput(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			Collection<Sample3D> input,
			Collection<NumericMeasurementInterface> optValidMeasurements, MongoDB m,
			int workLoadIndex, int workLoadSize) {
		this.input = input;
		this.workLoadIndex = workLoadIndex;
	}
	
	@Override
	public String getTaskDescription() {
		return "Test input read performance";
	}
	
	private void performAnalysis(final int maximumThreadCountParallelImages,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {
		synchronized (errors) {
			errors.clear();
			performAnalysisIC(maximumThreadCountParallelImages, status);
			try {
				errors.write(ReleaseInfo.getAppFolderWithFinalSep() + "performance_test_" + System.currentTimeMillis() + ".txt");
			} catch (IOException e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private void performAnalysisIC(final int maximumThreadCountParallelImages,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (workLoadIndex != 0)
			return;
		
		status.setCurrentStatusValue(0);
		output = new ArrayList<NumericMeasurementInterface>();
		ArrayList<ImageData> workload = new ArrayList<ImageData>();
		for (Sample3D ins : input)
			for (Measurement md : ins)
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
		
		final ExecutorService run2 = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoLA.addInt(1);
					i = tsoLA.getInt();
				}
				t.setName("Performance test image load (" + i + ")");
				return t;
			}
		});
		
		final ThreadSafeOptions tsoLoadDataErrorsVISside = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLoadDataErrorsVIStop = new ThreadSafeOptions();
		
		final ThreadSafeOptions tsoLoadDataErrorsFLUOside = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLoadDataErrorsFLUOtop = new ThreadSafeOptions();
		
		final ThreadSafeOptions tsoLoadDataErrorsNIRside = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLoadDataErrorsNIRtop = new ThreadSafeOptions();
		
		final ThreadSafeOptions tsoLoadDataOkVISside = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLoadDataOkVIStop = new ThreadSafeOptions();
		
		final ThreadSafeOptions tsoLoadDataOkFLUOside = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLoadDataOkFLUOtop = new ThreadSafeOptions();
		
		final ThreadSafeOptions tsoLoadDataOkNIRside = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLoadDataOkNIRtop = new ThreadSafeOptions();
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		final ThreadSafeOptions tsoBytesRead = new ThreadSafeOptions();
		tsoBytesRead.setLong(0);
		final ThreadSafeOptions tsoStartTime = new ThreadSafeOptions();
		tsoStartTime.setLong(System.currentTimeMillis());
		final ThreadSafeOptions tsoCurrentSecond = new ThreadSafeOptions();
		tsoCurrentSecond.setInt(0);
		ImageData dummyImageData = null;
		for (Measurement md : workload) {
			if (md instanceof ImageData) {
				if (dummyImageData == null)
					dummyImageData = (ImageData) md;
				final ImageData id = (ImageData) md;
				run.submit(new Runnable() {
					@Override
					public void run() {
						ImageConfiguration icNF = ImageConfiguration.get(id.getSubstanceName());
						if (icNF == ImageConfiguration.Unknown) {
							icNF = ImageConfiguration.get(id.getURL().getFileName());
							System.out.println(SystemAnalysis.getCurrentTime() +
									">INFO: IMAGE CONFIGURATION UNKNOWN (" + id.getSubstanceName() + "), " +
									"GUESSING FROM IMAGE NAME: " + id.getURL() + ", GUESS: " + icNF);
						}
						if (icNF == ImageConfiguration.Unknown) {
							System.out.println(SystemAnalysis.getCurrentTime() +
									">ERROR: INVALID (UNKNOWN) IMAGE CONFIGURATION FOR IMAGE: " + id.getURL());
						}
						final ImageConfiguration ic = icNF;
						if (id != null) {
							byte[] imgDataNF = null;
							try {
								imgDataNF = loadImageContent(maximumThreadCountParallelImages, tsoBytesRead, tsoStartTime, id);
								switch (ic) {
									case FluoSide:
										tsoLoadDataOkFLUOside.addInt(1);
										break;
									case FluoTop:
										tsoLoadDataOkFLUOtop.addInt(1);
										break;
									case NirSide:
										tsoLoadDataOkNIRside.addInt(1);
										break;
									case NirTop:
										tsoLoadDataOkNIRtop.addInt(1);
										break;
									case RgbSide:
										tsoLoadDataOkVISside.addInt(1);
										break;
									case RgbTop:
										tsoLoadDataOkVIStop.addInt(1);
										break;
									case Unknown:
										break;
								}
							} catch (Exception e) {
								System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: DATA LOAD ERROR: " + e.getMessage() + ", IMAGE: " + id.getURL());
								switch (ic) {
									case FluoSide:
										tsoLoadDataErrorsFLUOside.addInt(1);
										break;
									case FluoTop:
										tsoLoadDataErrorsFLUOtop.addInt(1);
										break;
									case NirSide:
										tsoLoadDataErrorsNIRside.addInt(1);
										break;
									case NirTop:
										tsoLoadDataErrorsNIRtop.addInt(1);
										break;
									case RgbSide:
										tsoLoadDataErrorsVISside.addInt(1);
										break;
									case RgbTop:
										tsoLoadDataErrorsVIStop.addInt(1);
										break;
									case Unknown:
										break;
								}
							}
							final byte[] imgData = imgDataNF;
							if (imgData != null) {
								run2.submit(new Runnable() {
									
									@Override
									public void run() {
										try {
											boolean ok = true;
											BufferedImage img = ImageIO.read(new MyByteArrayInputStream(imgData));
											if (img == null) {
												imgReadError("read error, image is NULL", id, ic, tsoLoadDataErrorsFLUOside,
														tsoLoadDataErrorsFLUOtop, tsoLoadDataErrorsNIRside, tsoLoadDataErrorsNIRtop,
														tsoLoadDataErrorsVISside, tsoLoadDataErrorsVIStop);
												ok = false;
											} else {
												if (img.getWidth() < 10 || img.getHeight() < 10) {
													imgReadError("read error, image size is small: " + img.getWidth() + "x" + img.getHeight(), id, ic,
															tsoLoadDataErrorsFLUOside,
															tsoLoadDataErrorsFLUOtop, tsoLoadDataErrorsNIRside, tsoLoadDataErrorsNIRtop,
															tsoLoadDataErrorsVISside, tsoLoadDataErrorsVIStop);
													ok = false;
												}
											}
											if (ok)
												imgReadOk(id, ic, tsoLoadDataOkFLUOside,
														tsoLoadDataOkFLUOtop, tsoLoadDataOkNIRside, tsoLoadDataOkNIRtop,
														tsoLoadDataOkVISside, tsoLoadDataOkVIStop);
										} catch (Exception e2) {
											imgReadError(e2.getMessage(), id, ic, tsoLoadDataErrorsFLUOside,
													tsoLoadDataErrorsFLUOtop, tsoLoadDataErrorsNIRside, tsoLoadDataErrorsNIRtop,
													tsoLoadDataErrorsVISside, tsoLoadDataErrorsVIStop);
										}
									}
								});
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
									long errorCnt =
											tsoLoadDataErrorsFLUOside.getInt() + tsoLoadDataErrorsFLUOside.getLong() +
													tsoLoadDataErrorsFLUOtop.getInt() + tsoLoadDataErrorsFLUOtop.getLong() +
													tsoLoadDataErrorsNIRside.getInt() + tsoLoadDataErrorsNIRside.getLong() +
													tsoLoadDataErrorsNIRtop.getInt() + tsoLoadDataErrorsNIRtop.getLong() +
													tsoLoadDataErrorsVISside.getInt() + tsoLoadDataErrorsVISside.getLong() +
													tsoLoadDataErrorsVIStop.getInt() + tsoLoadDataErrorsVIStop.getLong();
									
									long okCnt =
											tsoLoadDataOkFLUOside.getInt() + tsoLoadDataOkFLUOside.getLong() +
													tsoLoadDataOkFLUOtop.getInt() + tsoLoadDataOkFLUOtop.getLong() +
													tsoLoadDataOkNIRside.getInt() + tsoLoadDataOkNIRside.getLong() +
													tsoLoadDataOkNIRtop.getInt() + tsoLoadDataOkNIRtop.getLong() +
													tsoLoadDataOkVISside.getInt() + tsoLoadDataOkVISside.getLong() +
													tsoLoadDataOkVIStop.getInt() + tsoLoadDataOkVIStop.getLong();
									
									status.setCurrentStatusText1("Image " + tso.getInt() + "/" + wl + ", " + (int) mbs
											+ " MB/s" +
											(maximumThreadCountParallelImages > 1 ?
													" (" + maximumThreadCountParallelImages + " thread(s)" : "") +
											", ok: " + okCnt + ", errors: " + errorCnt + ")");
									{
										NumericMeasurement m = new NumericMeasurement(id, "overall read speed", id.getParentSample()
												.getParentCondition().getExperimentName()
												+ " (" + getName() + ")");
										setAnno(maximumThreadCountParallelImages, m);
										m.setValue(mbs);
										m.setUnit("MB/s");
										m.setReplicateID(1);
										m.getParentSample().setTime((int) ((time - tsoStartTime.getLong()) / 1000l));
										m.getParentSample().setTimeUnit("sec");
										m.setQualityAnnotation(null);
										output.add(m);
									}
								}
							}
						}
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
		
		run2.shutdown();
		try {
			run2.awaitTermination(365, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		store(maximumThreadCountParallelImages, dummyImageData, "vis.side.errors.loading", tsoLoadDataErrorsVISside.getInt(), "images");
		store(maximumThreadCountParallelImages, dummyImageData, "vis.side.errors.content", tsoLoadDataErrorsVISside.getLong(), "images");
		
		store(maximumThreadCountParallelImages, dummyImageData, "fluo.side.errors.loading", tsoLoadDataErrorsFLUOside.getInt(), "images");
		store(maximumThreadCountParallelImages, dummyImageData, "fluo.side.errors.content", tsoLoadDataErrorsFLUOside.getLong(), "images");
		
		store(maximumThreadCountParallelImages, dummyImageData, "nir.side.errors.loading", tsoLoadDataErrorsNIRside.getInt(), "images");
		store(maximumThreadCountParallelImages, dummyImageData, "nir.side.errors.content", tsoLoadDataErrorsNIRside.getLong(), "images");
		
		store(maximumThreadCountParallelImages, dummyImageData, "vis.top.errors.loading", tsoLoadDataErrorsVIStop.getInt(), "images");
		store(maximumThreadCountParallelImages, dummyImageData, "vis.top.errors.content", tsoLoadDataErrorsVIStop.getLong(), "images");
		
		store(maximumThreadCountParallelImages, dummyImageData, "fluo.top.errors.loading", tsoLoadDataErrorsFLUOtop.getInt(), "images");
		store(maximumThreadCountParallelImages, dummyImageData, "fluo.top.errors.content", tsoLoadDataErrorsFLUOtop.getLong(), "images");
		
		store(maximumThreadCountParallelImages, dummyImageData, "nir.top.errors.loading", tsoLoadDataErrorsNIRtop.getInt(), "images");
		store(maximumThreadCountParallelImages, dummyImageData, "nir.top.errors.content", tsoLoadDataErrorsNIRtop.getLong(), "images");
		
		System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR-STAT:");
		System.out.println("CONFIG\t\tGET DATA ERRORS\t\tLOAD DATA ERRORS");
		System.out.println("VIS SIDE\t\t" + tsoLoadDataErrorsVISside.getInt() + "\t\t" + tsoLoadDataErrorsVISside.getLong());
		System.out.println("VIS TOP \t\t" + tsoLoadDataErrorsVIStop.getInt() + "\t\t" + tsoLoadDataErrorsVIStop.getLong());
		System.out.println("FLUO SIDE\t\t" + tsoLoadDataErrorsFLUOside.getInt() + "\t\t" + tsoLoadDataErrorsFLUOside.getLong());
		System.out.println("FLUO TOP\t\t" + tsoLoadDataErrorsFLUOtop.getInt() + "\t\t" + tsoLoadDataErrorsFLUOtop.getLong());
		System.out.println("NIR SIDE\t\t" + tsoLoadDataErrorsNIRside.getInt() + "\t\t" + tsoLoadDataErrorsNIRside.getLong());
		System.out.println("NIR TOP \t\t" + tsoLoadDataErrorsNIRtop.getInt() + "\t\t" + tsoLoadDataErrorsNIRtop.getLong());
		
		errors.add(SystemAnalysis.getCurrentTime() + ">ERROR-STAT:");
		errors.add("CONFIG\t\tGET DATA ERRORS\t\tLOAD DATA ERRORS");
		errors.add("VIS SIDE\t\t" + tsoLoadDataErrorsVISside.getInt() + "\t\t" + tsoLoadDataErrorsVISside.getLong());
		errors.add("VIS TOP \t\t" + tsoLoadDataErrorsVIStop.getInt() + "\t\t" + tsoLoadDataErrorsVIStop.getLong());
		errors.add("FLUO SIDE\t\t" + tsoLoadDataErrorsFLUOside.getInt() + "\t\t" + tsoLoadDataErrorsFLUOside.getLong());
		errors.add("FLUO TOP\t\t" + tsoLoadDataErrorsFLUOtop.getInt() + "\t\t" + tsoLoadDataErrorsFLUOtop.getLong());
		errors.add("NIR SIDE\t\t" + tsoLoadDataErrorsNIRside.getInt() + "\t\t" + tsoLoadDataErrorsNIRside.getLong());
		errors.add("NIR TOP \t\t" + tsoLoadDataErrorsNIRtop.getInt() + "\t\t" + tsoLoadDataErrorsNIRtop.getLong());
		
		errors.add(SystemAnalysis.getCurrentTime() + ">OK-STAT:");
		errors.add("CONFIG\t\tGET DATA OK\t\tLOAD DATA OK");
		errors.add("VIS SIDE\t\t" + tsoLoadDataOkVISside.getInt() + "\t\t" + tsoLoadDataOkVISside.getLong());
		errors.add("VIS TOP \t\t" + tsoLoadDataOkVIStop.getInt() + "\t\t" + tsoLoadDataOkVIStop.getLong());
		errors.add("FLUO SIDE\t\t" + tsoLoadDataOkFLUOside.getInt() + "\t\t" + tsoLoadDataOkFLUOside.getLong());
		errors.add("FLUO TOP\t\t" + tsoLoadDataOkFLUOtop.getInt() + "\t\t" + tsoLoadDataOkFLUOtop.getLong());
		errors.add("NIR SIDE\t\t" + tsoLoadDataOkNIRside.getInt() + "\t\t" + tsoLoadDataOkNIRside.getLong());
		errors.add("NIR TOP \t\t" + tsoLoadDataOkNIRtop.getInt() + "\t\t" + tsoLoadDataOkNIRtop.getLong());
		
		status.setCurrentStatusValueFine(100d);
		input = null;
	}
	
	private void store(final int maximumThreadCountParallelImages, ImageData dummyImageData, String measurement, double value, String unit) {
		NumericMeasurement m = new NumericMeasurement(dummyImageData, measurement, dummyImageData.getParentSample()
				.getParentCondition().getExperimentName()
				+ " (" + getName() + ")");
		setAnno(maximumThreadCountParallelImages, m);
		m.setValue(value);
		m.setUnit(unit);
		m.setReplicateID(1);
		m.getParentSample().setTime(-1);
		m.getParentSample().setTimeUnit("-1");
		output.add(m);
	}
	
	protected void imgReadError(String message, ImageData id, ImageConfiguration ic, ThreadSafeOptions tsoLoadDataErrorsFLUOside,
			ThreadSafeOptions tsoLoadDataErrorsFLUOtop, ThreadSafeOptions tsoLoadDataErrorsNIRside, ThreadSafeOptions tsoLoadDataErrorsNIRtop,
			ThreadSafeOptions tsoLoadDataErrorsVISside, ThreadSafeOptions tsoLoadDataErrorsVIStop) {
		String sss = SystemAnalysis.getCurrentTime() + ">ERROR: CONVERTING IMAGE DATA TO IMAGE: " + message
				+ ", IMAGE: " + id.getURL() + ", TIME " + id.getParentSample().getTime() + " " + id.getParentSample().getTimeUnit() + ", "
				+ id.getParentSample().getSampleTime() + ", " + SystemAnalysis.getCurrentTime(id.getParentSample().getSampleFineTimeOrRowId()) + ", ID "
				+ id.getQualityAnnotation() + ", CONFIG: " + ic;
		System.out.println(sss);
		errors.add(sss);
		switch (ic) {
			case FluoSide:
				tsoLoadDataErrorsFLUOside.addLong(1);
				break;
			case FluoTop:
				tsoLoadDataErrorsFLUOtop.addLong(1);
				break;
			case NirSide:
				tsoLoadDataErrorsNIRside.addLong(1);
				break;
			case NirTop:
				tsoLoadDataErrorsNIRtop.addLong(1);
				break;
			case RgbSide:
				tsoLoadDataErrorsVISside.addLong(1);
				break;
			case RgbTop:
				tsoLoadDataErrorsVIStop.addLong(1);
				break;
			case Unknown:
				break;
		}
	}
	
	protected void imgReadOk(ImageData id, ImageConfiguration ic, ThreadSafeOptions tsoLoadDataOkFLUOside,
			ThreadSafeOptions tsoLoadDataOkFLUOtop, ThreadSafeOptions tsoLoadDataOkNIRside, ThreadSafeOptions tsoLoadDataOkNIRtop,
			ThreadSafeOptions tsoLoadDataOkVISside, ThreadSafeOptions tsoLoadDataOkVIStop) {
		switch (ic) {
			case FluoSide:
				tsoLoadDataOkFLUOside.addLong(1);
				break;
			case FluoTop:
				tsoLoadDataOkFLUOtop.addLong(1);
				break;
			case NirSide:
				tsoLoadDataOkNIRside.addLong(1);
				break;
			case NirTop:
				tsoLoadDataOkNIRtop.addLong(1);
				break;
			case RgbSide:
				tsoLoadDataOkVISside.addLong(1);
				break;
			case RgbTop:
				tsoLoadDataOkVIStop.addLong(1);
				break;
			case Unknown:
				break;
		}
	}
	
	@Override
	public ExperimentInterface getOutput() {
		Experiment res = new Experiment();
		for (NumericMeasurementInterface nmi : output) {
			Substance3D.addAndMerge(res, new MappingData3DPath(nmi, false).getSubstance(), false);
		}
		output.clear();
		return res;
	}
	
	@Override
	public String getName() {
		return "Performance Test";
	}
	
	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCountParallelImages, status);
	}
	
	private byte[] loadImageContent(final int maximumThreadCountParallelImages, final ThreadSafeOptions tsoBytesRead, final ThreadSafeOptions tsoStartTime,
			final ImageData id) throws Exception {
		byte[] imgData = null;
		long start = System.currentTimeMillis();
		imgData = IOmodule.loadImageContent(id);
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
			m.setQualityAnnotation(null);
			if (end - start > 0)
				output.add(m);
		}
		return imgData;
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
	
	@Override
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps) {
		if (unit_test_steps > 0)
			throw new UnsupportedOperationException("ToDo: for this task the unit test info is not utilized.");
	}
	
}
