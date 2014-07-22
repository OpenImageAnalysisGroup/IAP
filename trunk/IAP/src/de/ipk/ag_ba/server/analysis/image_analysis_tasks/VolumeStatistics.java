package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
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
	public ExperimentInterface getOutput() {
		Experiment res = new Experiment();
		for (NumericMeasurementInterface nmi : output) {
			Substance3D.addAndMerge(res, new MappingData3DPath(nmi, false).getSubstance(), false);
		}
		output.clear();
		return res;
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
