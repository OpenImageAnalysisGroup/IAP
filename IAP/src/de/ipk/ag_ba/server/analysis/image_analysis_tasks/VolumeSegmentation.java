/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 7, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MergeCompareRequirements;
import org.StringManipulationTools;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ThreeDsegmentationColored;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public class VolumeSegmentation implements ImageAnalysisTask {
	
	private static int somSize = 4;
	protected Collection<Sample3D> input;
	protected MongoDB m;
	protected Collection<NumericMeasurementInterface> output;
	
	private final DatabaseTarget storeResultInDatabase;
	private int workLoadIndex;
	private int workLoadSize;
	private ExperimentHeaderInterface header;
	
	public VolumeSegmentation(DatabaseTarget storeResultInDatabase) {
		this.storeResultInDatabase = storeResultInDatabase;
	}
	
	@Override
	public String getName() {
		return "Segmentation";
	}
	
	@Override
	public ExperimentInterface getOutput() {
		Experiment res = new Experiment();
		for (NumericMeasurementInterface nmi : output) {
			Substance3D.addAndMergeA(res, new MappingData3DPath(nmi, false).getSubstance(), false, BackgroundThreadDispatcher.getRunnableExecutor(),
					new MergeCompareRequirements());
		}
		output.clear();
		return res;
	}
	
	@Override
	public String getTaskDescription() {
		return "Color- and SOM-based Volume Segmentation";
	}
	
	@Override
	public void performAnalysis(
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
			for (Sample3D ins : input) {
				for (NumericMeasurementInterface in : ins) {
					if (in instanceof VolumeData) {
						status.setCurrentStatusText1("Load Volume");
						LoadedVolume volume = IOmodule.loadVolume((VolumeData) in);
						
						volume.getURL().setFileName(volume.getURL().getFileName() + ".labelfield");
						status.setCurrentStatusText1("Segmentation");
						ThreeDsegmentationColored.segment(new LoadedVolumeExtension(volume), somSize, status, volume
								.getDimensionX(), volume.getDimensionY(), volume.getDimensionZ());
						
						if (storeResultInDatabase != null) {
							long bytes = volume.getDimensionX() * volume.getDimensionY() * volume.getDimensionZ() * 4;
							status.setCurrentStatusText1("Generate Stream (" + bytes / 1024 / 1024 + " MB)");
							try {
								status.setCurrentStatusText1("Saving (" + bytes / 1024 / 1024 + " MB)");
								long t1 = System.currentTimeMillis();
								storeResultInDatabase.saveVolume(volume,
										(Sample3D) volume.getParentSample(), m,
										null, status);
								long t2 = System.currentTimeMillis();
								if (t2 > t1)
									System.out.println("Saved Volume ("
											+ StringManipulationTools.formatNumber(bytes / (t2 - t1) * 1000d / 1024 / 1024, "#.##")
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
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			output.clear();
		}
		input = null;
	}
	
	@Override
	public void setInput(
			ExperimentHeaderInterface header,
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			Collection<Sample3D> input,
			Collection<NumericMeasurementInterface> optValidMeasurements,
			MongoDB m, int workLoadIndex, int workLoadSize) {
		this.header = header;
		this.input = input;
		this.m = m;
		this.workLoadIndex = workLoadIndex;
		this.workLoadSize = workLoadSize;
	}
	
	@Override
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps) {
		if (unit_test_steps > 0)
			throw new UnsupportedOperationException("ToDo: for this task the unit test info is not utilized.");
	}
	
	@Override
	public void setValidSideAngle(int dEBUG_SINGLE_ANGLE1, int dEBUG_SINGLE_ANGLE2, int dEBUG_SINGLE_ANGLE3) {
		// TODO Auto-generated method stub
		
	}
	
}