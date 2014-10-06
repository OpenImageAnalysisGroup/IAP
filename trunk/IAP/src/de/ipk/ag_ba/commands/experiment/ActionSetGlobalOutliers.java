package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class ActionSetGlobalOutliers extends AbstractNavigationAction {
	
	private final ExperimentReference experiment;
	
	private final ArrayList<String> res = new ArrayList<String>();
	
	public ActionSetGlobalOutliers(ExperimentReference experiment) {
		super("Enumerates a global outlier list from marked outliers.");
		this.experiment = experiment;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		res.clear();
		String currentOutliers = experiment.getHeader().getGlobalOutlierInfo();
		
		LinkedHashSet<String> outliers = new LinkedHashSet<>();
		if (currentOutliers != null)
			for (String o : currentOutliers.split("//")) {
				res.add("Found existing global outlier definition for " + o.trim() + ".");
				outliers.add(o.trim());
			}
		
		if (res.size() == 0)
			res.add("Found no existing global outlier definitions.");
		
		int resCnt = 0;
		
		for (SubstanceInterface m : experiment.getExperiment()) {
			Substance3D m3 = (Substance3D) m;
			for (ConditionInterface s : m3) {
				Condition3D s3 = (Condition3D) s;
				for (SampleInterface sd : s3) {
					Sample3D sd3 = (Sample3D) sd;
					outlierSearch: for (NumericMeasurementInterface nmi : sd3) {
						if (nmi instanceof ImageData) {
							ImageData id = (ImageData) nmi;
							String o = id.getAnnotationField("outlier");
							if (o != null && o.equals("1")) {
								String pid = id.getQualityAnnotation().trim();
								if (!outliers.contains(pid)) {
									res.add("Added ID " + pid + " to global outlier list.");
									outliers.add(pid);
									resCnt++;
								}
								break outlierSearch;
							}
						}
					}
				}
			}
		}
		
		if (resCnt > 0) {
			experiment.getHeader().setGlobalOutlierInfo(StringManipulationTools.getStringList(outliers, "//"));
			res.add("Updated global outlier list with " + resCnt + " additional IDs (changed in memory, needs to be manually saved if desired)).");
		} else
			res.add("Found no image which has been marked as an outlier. Global outlier list remains unchanged.");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<html>Results:<ul>" + StringManipulationTools.getStringList("<li>", res, ""));
	}
	
	@Override
	public String getDefaultTitle() {
		return "Add Marked Outliers to Global Outlier List";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-User-Desktop-64.png";
	}
	
}
