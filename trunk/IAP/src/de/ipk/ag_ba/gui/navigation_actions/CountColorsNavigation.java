package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.ColorHueStatistics;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class CountColorsNavigation extends AbstractExperimentAnalysisNavigation {
	private final int colorCount;
	private NavigationButton src = null;
	
	public CountColorsNavigation(MongoDB m, int colorCount, ExperimentReference experiment) {
		super(m, experiment);
		this.colorCount = colorCount;
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) throws Exception {
		this.src = src;
		
		ExperimentInterface res = experiment.getData(m);
		
		// src.title = src.title + ": processing";
		
		ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();
		
		for (SubstanceInterface m : res) {
			Substance3D m3 = (Substance3D) m;
			for (ConditionInterface s : m3) {
				Condition3D s3 = (Condition3D) s;
				for (SampleInterface sd : s3) {
					Sample3D sd3 = (Sample3D) sd;
					for (Measurement md : sd3.getMeasurements(MeasurementNodeType.IMAGE)) {
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
		ColorHueStatistics task = new ColorHueStatistics(colorCount);
		task.setInput(workload, m);
		task.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1, status);
		// src.title = src.title.split("\\:")[0];
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
}