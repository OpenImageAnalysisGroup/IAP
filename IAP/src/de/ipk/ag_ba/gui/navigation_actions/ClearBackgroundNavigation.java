package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.ImageAnalysis;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ClearBackgroundNavigation extends AbstractExperimentAnalysisNavigation {
	private final double epsilon;
	private final double epsilon2;

	public ClearBackgroundNavigation(String login, double epsilon, double epsilon2, String pass,
			ExperimentReference experiment) {
		super(login, pass, experiment);
		this.epsilon = epsilon;
		this.epsilon2 = epsilon2;
	}

	@Override
	public void performActionCalculateResults(final NavigationButton src) throws Exception {
		super.performActionCalculateResults(src);

		ExperimentInterface res = experiment.getData();

		// src.title = src.title + ": processing";

		ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();

		for (SubstanceInterface m : res) {
			Substance3D m3 = (Substance3D) m;
			for (ConditionInterface s : m3) {
				Condition3D s3 = (Condition3D) s;
				for (SampleInterface sd : s3) {
					Sample3D sd3 = (Sample3D) sd;
					for (Measurement md : sd3.getAllMeasurements()) {
						if (md instanceof ImageData) {
							ImageData i = (ImageData) md;
							workload.add(i);
						}
					}
				}
			}
		}

		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setInt(1);
		PhenotypeAnalysisTask task = new PhenotypeAnalysisTask(epsilon, epsilon2, ImageAnalysis.isSaveInDatabase());
		task.setInput(workload, login, pass);
		task.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1, status);
		// src.title = src.title.split("\\:")[0];
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<html><h1>TODO</h1>");
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
}