package de.ipk.ag_ba.commands.experiment.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.OpenFileDialogService;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class ActionGenerateFieldInfoFromWeight extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReferenceInterface experiment;
	private NavigationButton src;
	
	public ActionGenerateFieldInfoFromWeight() {
		super("Generate placement field information");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		try {
			ExperimentInterface res = experiment.getData();
			TreeMap<Integer, TreeMap<Long, String>> day2time2plant = new TreeMap<>();
			boolean resOK = false;
			for (SubstanceInterface s : res) {
				if (s.getName().equals("weight_before")) {
					for (ConditionInterface ci : s) {
						for (SampleInterface sai : ci) {
							Long time = sai.getSampleFineTimeOrRowId();
							if (time != null) {
								for (NumericMeasurementInterface nmi : sai) {
									if (nmi.getQualityAnnotation() != null) {
										if (!day2time2plant.containsKey(sai.getTime()))
											day2time2plant.put(sai.getTime(), new TreeMap<>());
										day2time2plant.get(sai.getTime()).put(time, nmi.getQualityAnnotation());
									}
								}
							}
						}
					}
					resOK = true;
					break;
				}
			}
			if (!resOK)
				throw new RuntimeException("Could not find 'weight_before' data!");
			
			TextFile t = new TextFile();
			for (Integer day : day2time2plant.keySet()) {
				int idx = 0;
				for (String plantID : day2time2plant.get(day).values()) {
					t.add(day + ";" + (idx++) + "\t" + plantID);
				}
			}
			File target = OpenFileDialogService.getSaveFile(new String[] { ".csv" }, "Result Table (.csv)");
			if (target != null) {
				t.write(target);
				AttributeHelper.showInFileBrowser(target.getParent(), target.getName());
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Placement field generated!");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Format-Justify-Fill-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Generate Placement Table";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experiment = experimentReference;
	}
}