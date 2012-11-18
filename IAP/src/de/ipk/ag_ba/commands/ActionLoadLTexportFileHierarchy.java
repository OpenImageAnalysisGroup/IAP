package de.ipk.ag_ba.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class ActionLoadLTexportFileHierarchy extends AbstractNavigationAction {
	
	public ActionLoadLTexportFileHierarchy(String tooltip) {
		super(tooltip);
	}
	
	private NavigationButton src;
	private ExperimentReference loaded_experiment = null;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		loaded_experiment = null;
		File inp = OpenFileDialogService.getDirectoryFromUser("Select Input Folder");
		HashSet<String> processedDirectories = new HashSet<String>();
		if (inp != null && inp.isDirectory()) {
			processDir(inp);
		}
		this.src = src;
	}
	
	private void processDir(File inp) {
		System.out.println(SystemAnalysis.getCurrentTime() + ">Scanning input folder '" + inp.getPath() + "'...");
		ArrayList<File> toBeProcessed = new ArrayList<File>();
		for (String fn : inp.list()) {
			File f = new File(fn);
			if (f.isDirectory()) {
				toBeProcessed.add(f);
			} else {
				if (f.getName().equals("info.txt")) {
					try {
						InfoFile fi = new InfoFile(new TextFile(f));
						
					} catch (IOException e) {
						e.printStackTrace();
						ErrorMsg.addErrorMessage("Could not process file " + f.getPath() + ": " + e.getMessage());
					}
					// this a info-file, e.g.:
					/*
					 * Example 1:
					 * IdTag: 000668-LETL
					 * Color: 0
					 * Creator: Bettina
					 * Comment:
					 * Measurement: 0024 calibration wheat
					 * Timestamp: 9/20/2010 2:26:09 PM
					 * Weight before [g]: 5
					 * Weight after [g]: 5
					 * Water amount [ml]: 0
					 */
					/*
					 * Example 2:
					 * Camera label: FLUO SV1
					 * MM pro pixel X: 0
					 * MM pro pixel Y: 0
					 */
				} else {
					// this could be an image file or anything else, now process image files
				}
			}
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (loaded_experiment != null)
			res.add(new NavigationButton(new ActionMongoOrLemnaTecExperimentNavigation(loaded_experiment), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return super.getResultMainPanel();
	}
	
	@Override
	public String getDefaultTitle() {
		return "Load LT File Export";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-System-File-Manager-64.png";
	}
}
