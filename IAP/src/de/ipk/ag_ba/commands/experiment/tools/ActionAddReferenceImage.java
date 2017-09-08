package de.ipk.ag_ba.commands.experiment.tools;

import java.io.File;
import java.util.ArrayList;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementVisitor;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

/**
 * @author klukas
 */
public class ActionAddReferenceImage extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReferenceInterface experiment;
	private NavigationButton src;
	ArrayList<String> resultText = new ArrayList<String>();
	
	public ActionAddReferenceImage() {
		super("Add a single reference image to all images");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		resultText.clear();
		try {
			getStatusProvider().setCurrentStatusText1("Select reference image file...");
			ArrayList<File> fn = OpenFileDialogService.getFiles(new String[] { "jpg", "jpeg", "png", "tif", "tiff" }, "JPEG, PNG or TIFF Images");
			if (fn != null) {
				ExperimentInterface exp = experiment.getData(false, getStatusProvider());
				ThreadSafeOptions tsoIdx = new ThreadSafeOptions();
				tsoIdx.setInt(0);
				tsoIdx.setLong(fn.size());
				exp.visitNumericMeasurements(null, new NumericMeasurementVisitor() {
					@Override
					public void visit(NumericMeasurementInterface nmi) {
						if (nmi instanceof BinaryMeasurement) {
							BinaryMeasurement bm = (BinaryMeasurement) nmi;
							tsoIdx.addInt(1);
							if (tsoIdx.getInt() == tsoIdx.getLong())
								tsoIdx.setInt(0);
							bm.setLabelURL(FileSystemHandler.getURL(fn.get(tsoIdx.getInt())));
						}
					}
				});
				resultText.add("If desired, save experiment to make changes permanent. The following reference image(s) have been assigned to the dataset:");
				resultText.add(org.StringManipulationTools.getStringList(fn, ", "));
			} else {
				ExperimentInterface exp = experiment.getData(false, getStatusProvider());
				exp.visitNumericMeasurements(null, new NumericMeasurementVisitor() {
					@Override
					public void visit(NumericMeasurementInterface nmi) {
						if (nmi instanceof BinaryMeasurement) {
							BinaryMeasurement bm = (BinaryMeasurement) nmi;
							bm.setLabelURL(null);
						}
					}
				});
				resultText.add(
						"Reference images (if available) have been removed from the loaded dataset (in memory). Carefully consider if you want to save this change. If you copy the dataset or save it later from the loaded state, changes will be applied to disk, too.");
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		getStatusProvider().setCurrentStatusText1("Processing finished");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		// res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton("Save Changes", new ActionCopyToMongo(experiment.getM(), experiment, true), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(resultText);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Go-Jump-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Add/remove reference images";
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