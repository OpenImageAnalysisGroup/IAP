package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.rmi_server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.IOmodule;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.reconstruction3d.GenerationMode;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.reconstruction3d.ModelGenerator;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.reconstruction3d.MyPicture;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.reconstruction3d.TransparencyAnalysis;
import de.ipk.ag_ba.rmi_server.databases.DBTable;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public class ThreeDreconstruction extends AbstractImageAnalysisTask {

	private Collection<NumericMeasurementInterface> output;
	private Collection<NumericMeasurementInterface> input;
	private String login;
	private String pass;

	private int voxelresolution = 200;
	private int widthFactor = 40;

	private LoadedVolume volume;
	private final ArrayList<ImageAnalysisTask> resultProcessors = new ArrayList<ImageAnalysisTask>();
	private HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>> additionalResults = new HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>>();

	private final DatabaseTarget storeResultInDatabase;

	public ThreeDreconstruction(DatabaseTarget storeResultInDatabase) {
		this.storeResultInDatabase = storeResultInDatabase;
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * getTaskDescription()
	 */
	@Override
	public String getTaskDescription() {
		return "Creates a 3-D volume model of the plant out of rotational side-view images.";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.AbstractImageAnalysisTask
	 * #getInputType()
	 */
	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.AbstractImageAnalysisTask
	 * #getResultType()
	 */
	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.COLORED_VOLUME };
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performImageAnalysis(int,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	/**
	 * @deprecated Use {@link #performAnalysis(int,int,BackgroundTaskStatusProviderSupportingExternalCall)} instead
	 */
	@Deprecated
	@Override
	public void performAnalysis(int maximumThreadCount, final BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCount, 1, status);
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performImageAnalysis(int,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
						final BackgroundTaskStatusProviderSupportingExternalCall status) {

		output = new ArrayList<NumericMeasurementInterface>();

		// for each sample
		// for each replicate
		// processAllImages for sample-replicate
		// upload volume file
		// add volume to result set

		LinkedHashSet<Sample3D> samples = new LinkedHashSet<Sample3D>();
		for (Measurement md : input) {
			if (md instanceof ImageData) {
				ImageData li = (ImageData) md;
				Sample3D s3d = (Sample3D) li.getParentSample();
				samples.add(s3d);
			}
		}

		for (Sample3D s3d : samples) {
			ImageConfiguration ic = ImageConfiguration.get(s3d.getParentCondition().getParentSubstance().getName());
			boolean ok = false;
			if (ic == ImageConfiguration.RgbSide)
				ok = true;
			if (ic == ImageConfiguration.FluoSide)
				ok = true;
			if (!ok)
				continue;
			TreeSet<Integer> replicateIDs = new TreeSet<Integer>();
			for (Measurement md : s3d.getMeasurements(MeasurementNodeType.IMAGE)) {
				ImageData id = (ImageData) md;
				replicateIDs.add(id.getReplicateID());
			}
			for (Integer replicateID : replicateIDs) {
				ArrayList<ImageData> imageData = new ArrayList<ImageData>();
				for (Measurement md : s3d.getMeasurements(MeasurementNodeType.IMAGE)) {
					ImageData id = (ImageData) md;
					if (id.getPosition() != null)
						imageData.add(id);
				}
				try {
					if (imageData.size() == 0)
						continue;
					System.out.println("Process sample: " + imageData.size() + " images...");
					processSampleCreateVolume(s3d, imageData, replicateID, maximumThreadCountParallelImages, status);

					for (ImageAnalysisTask resultProcessor : resultProcessors) {
						Collection<NumericMeasurementInterface> inp = new ArrayList<NumericMeasurementInterface>();
						inp.add(volume);
						resultProcessor.setInput(inp, login, login);
						resultProcessor.performAnalysis(maximumThreadCountParallelImages, maximumThreadCountParallelImages,
											status);
						if (additionalResults.get(resultProcessor) == null)
							additionalResults.put(resultProcessor, new ArrayList<NumericMeasurementInterface>());
						additionalResults.get(resultProcessor).addAll(resultProcessor.getOutput());
					}

					if (storeResultInDatabase != null) {
						try {

							String md5 = AttributeHelper.getMD5fromInputStream(volume.getURL().getInputStream());
							volume.getURL().setDetail(md5);
							if (status != null)
								status.setCurrentStatusValue(-1);
							if (status != null)
								status.setCurrentStatusText1("Storing result");

							storeResultInDatabase.saveVolume(volume, s3d, login, pass, DBTable.SAMPLE, null, md5, status);
							if (status != null)
								status.setCurrentStatusValue(100);
							if (status != null)
								status.setCurrentStatusText1("Finished");

							VolumeData volumeInDatabase = new VolumeData(s3d, volume);
							volumeInDatabase.getURL().setPrefix(MongoDBhandler.PREFIX);
							output.add(volumeInDatabase);
						} catch (Exception e) {
							LoadedVolume v = new LoadedVolumeExtension(s3d, null);
							ErrorMsg.addErrorMessage(e);
							output.add(v);
						}
					} else {
						boolean returnFullVolume = false;
						if (!returnFullVolume) {
							VolumeData v = new VolumeData(s3d, volume);
							output.add(v);
						} else {
							output.add(volume);
						}

					}
				} finally {
					volume = null;
				}
				boolean debug = false;
				if (debug)
					break;
			}
		}
		input = null;
	}

	private void processSampleCreateVolume(Sample3D sample, ArrayList<ImageData> loadedImages, int replicateID,
						final int maximumThreadCount, final BackgroundTaskStatusProviderSupportingExternalCall status) {
		GenerationMode modeOfOperation = GenerationMode.COLORED_RGBA;
		double maxPercent = 2; // maximum background color difference

		final boolean doBlur = true;

		try {
			status.setCurrentStatusText1("Analyze input images");
			status.setCurrentStatusText2("Please wait.");
			status.setCurrentStatusValue(-1);

			final ModelGenerator mg = new ModelGenerator(voxelresolution, widthFactor);
			mg.setCameraDistance(1500);
			mg.setCubeSideLength(300, 300, 300);

			final TransparencyAnalysis taNorm = new TransparencyAnalysis(maxPercent / 100d);
			taNorm.addColor(PhenotypeAnalysisTask.BACKGROUND_COLOR);
			taNorm.addColors(new Color(255, 246, 245), new Color(235, 234, 210));

			final TransparencyAnalysis taTop = new TransparencyAnalysis(maxPercent / 100d);
			taTop.addColor(PhenotypeAnalysisTask.BACKGROUND_COLOR);
			// taTop.addColors(new Color(80, 140, 200), new Color(100, 160, 230));
			// taTop.addColors(new Color(0, 0, 0), new Color(50, 50, 50));
			// taTop.addColors(new Color(60, 55, 40), new Color(55, 70, 80));
			// taTop.addColors(new Color(30, 20, 15), new Color(70, 50, 26));
			// taTop.addColors(new Color(70, 66, 50), new Color(90, 80, 60));
			// taTop.addColors(new Color(117, 100, 77), new Color(85, 70, 50));

			final ThreadSafeOptions tsoLA = new ThreadSafeOptions();
			ExecutorService run = Executors.newFixedThreadPool(maximumThreadCount, new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					int i;
					synchronized (tsoLA) {
						tsoLA.addInt(1);
						i = tsoLA.getInt();
					}
					t.setName("Analyse input images (" + i + ")");
					return t;
				}
			});

			status.setCurrentStatusValue(0);
			double workLoad = loadedImages.size();
			final double workloadStep = 100d / workLoad;

			status.setCurrentStatusText1("Processing images (" + (int) workLoad + ")");
			status.setCurrentStatusText2("Please wait.");
			status.setCurrentStatusValue(-1);

			// try {
			// Thread.sleep(20);
			// } catch (InterruptedException e) {
			// // empty
			// }

			final ArrayList<MyPicture> pictures = new ArrayList<MyPicture>();

			double blurfactor = 1.5;
			if (modeOfOperation != GenerationMode.GRAYSCALE_PROBABILITY)
				blurfactor = 1;
			if (!doBlur)
				blurfactor = 0;
			final double bf = blurfactor;
			for (final ImageData image : loadedImages) {
				String fileName = image.getURL().getFileName();
				if (fileName.indexOf("/") >= 0)
					fileName = fileName.substring(fileName.lastIndexOf("/") + "/".length());
				if (fileName.indexOf("\\") >= 0)
					fileName = fileName.substring(fileName.lastIndexOf("\\") + "\\".length());
				final String ffn = fileName;

				run.execute(new Runnable() {
					public void run() {
						MyPicture p = new MyPicture();

						if (image.getPosition() == null)
							ErrorMsg.addErrorMessage("ERROR: no position for image " + image.toString());
						double angle = image.getPosition() / 180 * Math.PI;

						if (Math.abs(angle) < 0.0001) {
							angle = getAngleFromFileName(image.getURL().getFileName());
							if (angle < 0)
								angle = 0;
						}

						{
							LoadedImage limg;
							try {
								limg = IOmodule.loadImageFromFileOrMongo(image, login, pass);

								limg = PhenotypeAnalysisTask.clearBackground(limg, 1, login, pass);

								limg = storeResultInDatabase.saveImage(limg, login, pass);

								if (limg == null) {
									ErrorMsg.addErrorMessage("Could not store processed input image in database target.");
								} else {
									output.add(new ImageData(limg.getParentSample(), limg));
									if (getIsTopFromFileName(ffn)) {
										p.setPictureData(limg.getLoadedImage(), angle, mg, taTop, getIsTopFromFileName(ffn), bf);
									} else
										p.setPictureData(limg.getLoadedImage(), angle, mg, taNorm, getIsTopFromFileName(ffn), bf);
									synchronized (pictures) {
										pictures.add(p);
									}
								}
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}

						if (doBlur)
							status.setCurrentStatusText1("Blur: " + ffn);
						else
							status.setCurrentStatusText1("Image: " + ffn);
						status.setCurrentStatusText2("Angle: " + (image.getPosition().intValue()) + " "
											+ image.getPositionUnit() + ", is top: " + (getIsTopFromFileName(ffn)));
						status.setCurrentStatusValueFineAdd(workloadStep);
					}
				});
			}
			run.shutdown();
			try {
				run.awaitTermination(7, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			} // wait max 7 days for result
			status.setCurrentStatusValue(100);

			mg.setRoundViewImages(pictures);
			if (true)
				mg.calculateModel(status, modeOfOperation, 0);
			else
				mg.calculateModelMotionScan(status);

			volume = new LoadedVolumeExtension(sample, mg.getRGBcubeResult());

			volume.setReplicateID(replicateID);

			volume.setVoxelsizeX(25f * 400 / voxelresolution);
			volume.setVoxelsizeY(25f * 400 / voxelresolution * (100d / widthFactor));
			volume.setVoxelsizeZ(25f * 400 / voxelresolution);

			volume.setDimensionX(voxelresolution);
			volume.setDimensionY(voxelresolution);
			volume.setDimensionZ(voxelresolution);

			if (volume.getURL() == null)
				volume.setURL(new IOurl("loadedvolume", "", ""));
			volume.getURL().setFileName("IAP_reconstruction_" + System.currentTimeMillis() + ".argb_volume");

			volume.setColorDepth(VolumeColorDepth.RGBA.toString());
		} catch (OutOfMemoryError e) {
			MainFrame.showMessageDialog("You have not enough memory! Please select a lower resolution",
								"Out of Memory Error");
		}
	}

	@Override
	public void setInput(Collection<NumericMeasurementInterface> input, String login, String pass) {
		this.input = input;
		this.login = login;
		this.pass = pass;
	}

	private boolean getIsTopFromFileName(String fileName) {
		if (fileName.indexOf("/") >= 0)
			fileName = fileName.substring(fileName.lastIndexOf("/") + "/".length());
		return fileName.toUpperCase().startsWith("TOP");
	}

	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		Collection<NumericMeasurementInterface> res = output;
		output = null;
		return res;
	}

	private double getAngleFromFileName(String fileName) {
		double correction = 0;
		if (getIsTopFromFileName(fileName))
			correction = Math.PI / 2;
		if (fileName.indexOf("/") >= 0)
			fileName = fileName.substring(fileName.lastIndexOf("/") + "/".length());
		if (fileName.indexOf("\\") >= 0)
			fileName = fileName.substring(fileName.lastIndexOf("\\") + "\\".length());
		String numbers = StringManipulationTools.getNumbersFromString(fileName);
		try {
			double d = Double.parseDouble(numbers);
			return d / 180 * Math.PI + correction;
		} catch (Exception e) {
			return -1;
		}
	}

	public void addResultProcessor(ImageAnalysisTask resultProcessor) {
		resultProcessors.add(resultProcessor);
	}

	public HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>> getAdditionalResults() {
		HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>> res = additionalResults;
		additionalResults = new HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>>();
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#getName
	 * ()
	 */
	@Override
	public String getName() {
		return "3D-Reconstruction";
	}

	public void setResolution(int voxelresolution, int widthFactor) {
		this.voxelresolution = voxelresolution;
		this.widthFactor = widthFactor;
	}
}
