/*******************************************************************************
 * 
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 7, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import java.util.ArrayList;
import java.util.Collection;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;

import de.ipk.ag_ba.rmi_server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.IOmodule;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.rmi_server.analysis.ThreeDsegmentationColored;
import de.ipk.ag_ba.rmi_server.analysis.VolumeUploadData;
import de.ipk.ag_ba.rmi_server.databases.DBTable;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

/**
 * @author klukas
 * 
 */
public class VolumeSegmentation extends AbstractImageAnalysisTask {

	private static int somSize = 4;
	protected Collection<NumericMeasurementInterface> input;
	protected String login;
	protected String pass;
	protected Collection<NumericMeasurementInterface> output;

	private final DatabaseTarget storeResultInDatabase;

	public VolumeSegmentation(DatabaseTarget storeResultInDatabase) {
		this.storeResultInDatabase = storeResultInDatabase;
	}

	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.COLORED_IMAGE, ImageAnalysisType.GRAY_VOLUME,
							ImageAnalysisType.IMAGE };
	}

	@Override
	public String getName() {
		return "Segmentation";
	}

	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		return output;
	}

	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.COLORED_IMAGE, ImageAnalysisType.GRAY_VOLUME,
							ImageAnalysisType.IMAGE };
	}

	@Override
	public String getTaskDescription() {
		return "Color- and SOM-based Volume Segmentation";
	}

	/**
	 * @deprecated Use
	 *             {@link #performAnalysis(int,int,BackgroundTaskStatusProviderSupportingExternalCall)}
	 *             instead
	 */
	@Deprecated
	@Override
	public void performAnalysis(int maximumThreadCount, BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCount, 1, status);
	}

	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (output != null) {
			input = null;
			return;
		}
		Object[] res = MyInputHelper.getInput("Please specify the maximum number of segments:", "SOM Initialization",
							new Object[] { "SOM-Size", somSize });
		if (res == null)
			return;
		somSize = (Integer) res[0];
		output = new ArrayList<NumericMeasurementInterface>();
		try {
			for (NumericMeasurementInterface in : input) {
				if (in instanceof VolumeData) {
					status.setCurrentStatusText1("Load Volume");
					LoadedVolume volume = IOmodule.loadVolumeFromMongo((VolumeData) in, login, pass);

					volume.getURL().setFileName(volume.getURL().getFileName() + ".labelfield");
					status.setCurrentStatusText1("Segmentation");
					ThreeDsegmentationColored.segment(new LoadedVolumeExtension(volume), somSize, status, volume
										.getDimensionX(), volume.getDimensionY(), volume.getDimensionZ());

					if (storeResultInDatabase != null) {
						long bytes = volume.getDimensionX() * volume.getDimensionY() * volume.getDimensionZ() * 4;
						status.setCurrentStatusText1("Generate Stream (" + bytes / 1024 / 1024 + " MB)");
						VolumeUploadData vud = IOmodule.getThreeDvolumeInputStream(volume);
						try {
							status.setCurrentStatusText1("Saving (" + bytes / 1024 / 1024 + " MB)");
							long t1 = System.currentTimeMillis();
							String md5 = AttributeHelper.getMD5fromInputStream(vud.getStream());
							storeResultInDatabase.saveVolume(volume, (Sample3D) volume.getParentSample(), login, pass,
												DBTable.SAMPLE, vud, null, vud.getLength(), md5, status);
							long t2 = System.currentTimeMillis();
							if (t2 > t1)
								System.out.println("Saved Volume ("
													+ AttributeHelper.formatNumber(bytes / (t2 - t1) * 1000d / 1024 / 1024, "#.##")
													+ " MB/s, if already in DB, save is skipped)");
							else
								System.out.println("Volume saved in 0 ms, saving was not needed or error occured.");
							status.setCurrentStatusText1("Finished");
							VolumeData volumeInDatabase = new VolumeData(volume.getParentSample(), volume);
							output.add(volumeInDatabase);
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					} else {
						output.add(volume);
					}
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			output.clear();
		}
		input = null;
	}

	@Override
	public void setInput(Collection<NumericMeasurementInterface> input, String login, String pass) {
		this.input = input;
		this.login = login;
		this.pass = pass;
	}
}