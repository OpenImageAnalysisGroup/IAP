package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public class VolumeStatistics implements ImageAnalysisTask {
	
	private Collection<Sample3D> input;
	private Collection<NumericMeasurementInterface> output;
	private MongoDB m;
	private int workLoadIndex;
	private int workLoadSize;
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.AbstractImageAnalysisTask
	 * #getInputType()
	 */
	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.COLORED_VOLUME };
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.AbstractImageAnalysisTask
	 * #getResultType()
	 */
	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.MEASUREMENT };
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.AbstractImageAnalysisTask
	 * #getTaskDescription()
	 */
	@Override
	public String getTaskDescription() {
		return "Creates an Voxel-Color-Histogramm (counts number of voxels for different colors)";
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performImageAnalysis(int, boolean,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		output = new ArrayList<NumericMeasurementInterface>();
		
		int background = new Color(ImageOperation.BACKGROUND_COLOR.getRed(),
				ImageOperation.BACKGROUND_COLOR.getBlue(), ImageOperation.BACKGROUND_COLOR.getRed(), 0)
				.getRGB();
		long filled = 0, voxels = 0;
		for (Sample3D ins : input) {
			for (Measurement md : ins) {
				if ((md instanceof VolumeData) && !(md instanceof LoadedVolume)) {
					// load volume
					LoadedVolume lv;
					try {
						lv = IOmodule.loadVolume((VolumeData) md);
						md = lv;
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				if (md instanceof LoadedVolume) {
					LoadedVolume lv = (LoadedVolume) md;
					
					LoadedVolumeExtension lve = new LoadedVolumeExtension(lv);
					voxels = lve.getVoxelCount();
					
					NumericMeasurement m = new NumericMeasurement(lv, "filled (voxel)", md.getParentSample()
							.getParentCondition().getExperimentName()
							+ " (" + getName() + ")");
					m.setValue(filled);
					output.add(m);
					
					m = new NumericMeasurement(lv, "cube volume (voxel)", md.getParentSample().getParentCondition()
							.getExperimentName()
							+ " (" + getName() + ")");
					m.setValue(voxels);
					output.add(m);
					
					m = new NumericMeasurement(lv, "filled (percent)", md.getParentSample().getParentCondition()
							.getExperimentName()
							+ " (" + getName() + ")");
					m.setValue((double) filled / (double) voxels * 100d);
					output.add(m);
				}
			}
		}
		input = null;
	}
	
	@Override
	public String getName() {
		return "Volume Statistic";
	}
	
	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		return output;
	}
	
	@Override
	public void setInput(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			Collection<Sample3D> input,
			Collection<NumericMeasurementInterface> optValidMeasurements,
			MongoDB m, int workLoadIndex, int workLoadSize) {
		this.input = input;
		this.m = m;
		this.workLoadIndex = workLoadIndex;
		this.workLoadSize = workLoadSize;
	}
	
}
