package rmi_server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import rmi_server.analysis.AbstractImageAnalysisTask;
import rmi_server.analysis.IOmodule;
import rmi_server.analysis.ImageAnalysisType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

/**
 * @author klukas
 * 
 */
public class VolumeStatistics extends AbstractImageAnalysisTask {

	private Collection<NumericMeasurementInterface> input;
	private Collection<NumericMeasurementInterface> output;
	private String login;
	private String pass;

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performImageAnalysis(int, boolean,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	/**
	 * @deprecated Use {@link #performAnalysis(int,int,BackgroundTaskStatusProviderSupportingExternalCall)} instead
	 */
	@Override
	public void performAnalysis(int maximumThreadCount, BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCount, 1, status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performImageAnalysis(int, boolean,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel, BackgroundTaskStatusProviderSupportingExternalCall status) {

		output = new ArrayList<NumericMeasurementInterface>();

		int background = new Color(PhenotypeAnalysisTask.BACKGROUND_COLOR.getRed(),
				PhenotypeAnalysisTask.BACKGROUND_COLOR.getBlue(), PhenotypeAnalysisTask.BACKGROUND_COLOR.getRed(), 0)
				.getRGB();
		long filled = 0, voxels = 0;
		for (Measurement md : input) {
			if ((md instanceof VolumeData) && !(md instanceof LoadedVolume)) {
				// load volume
				LoadedVolume lv = IOmodule.loadVolumeFromMongo((VolumeData) md, login, pass);
				// LoadedVolume lv = IOmodule.loadVolumeFromDBE((VolumeData) md,
				// login, pass);
				md = lv;
			}
			if (md instanceof LoadedVolume) {
				LoadedVolume lv = (LoadedVolume) md;
				byte[] cube = lv.getVolume();
				for (int i = 0; i < cube.length; i++) {
					if (cube[i] != 0)
						filled++;
					voxels++;
					i += 3;
				}

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
	public void setInput(Collection<NumericMeasurementInterface> input, String login, String pass) {
		this.input = input;
		this.login = login;
		this.pass = pass;
	}

}
