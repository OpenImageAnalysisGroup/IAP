package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.analysis.AbstractExperimentAnalysisNavigation;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ColorHueStatistics;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class CountColorsNavigation extends AbstractExperimentAnalysisNavigation {
	private final int colorCount;
	private NavigationButton src = null;
	private int workLoadIndex;
	private int workloadSize;
	
	public CountColorsNavigation(MongoDB m, int colorCount, ExperimentReference experiment) {
		super(m, experiment);
		this.colorCount = colorCount;
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) throws Exception {
		this.src = src;
		
		ExperimentInterface res = experiment.getData(m);
		
		// src.title = src.title + ": processing";
		
		ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
		
		for (SubstanceInterface m : res) {
			Substance3D m3 = (Substance3D) m;
			for (ConditionInterface s : m3) {
				Condition3D s3 = (Condition3D) s;
				for (SampleInterface sd : s3) {
					if (sd instanceof Sample3D)
						workload.add((Sample3D) sd);
				}
			}
		}
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setInt(1);
		ColorHueStatistics task = new ColorHueStatistics(colorCount);
		task.setInput(
				AbstractPhenotypingTask.getWateringInfo(res),
				workload, null, m, workLoadIndex, workloadSize);
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