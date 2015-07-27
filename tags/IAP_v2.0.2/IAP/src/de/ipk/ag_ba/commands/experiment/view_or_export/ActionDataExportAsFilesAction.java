/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.view_or_export;

import java.io.File;
import java.util.ArrayList;

import org.OpenFileDialogService;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
@Deprecated
public class ActionDataExportAsFilesAction extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private MongoDB m;
	private ExperimentReferenceInterface experimentReference;
	private File targetDirectory;
	private String errorMessage;
	
	public ActionDataExportAsFilesAction(String tooltip) {
		super(tooltip);
	}
	
	public ActionDataExportAsFilesAction(MongoDB m, ExperimentReferenceInterface experimentReference) {
		this("Export the experiment to a directory on the file system");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export Experiment to Folder";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveAsArchive();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.errorMessage = null;
		status.setCurrentStatusText1("Load Experiment");
		ExperimentInterface experiment = experimentReference.getData();
		
		String fsinfo = "";
		
		if (!SystemAnalysis.isHeadless()) {
			this.targetDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Directory");
			if (targetDirectory == null)
				return;
		} else {
			if (targetDirectory == null)
				throw new UnsupportedOperationException("This command can't be executed in this environment.");
		}
		status.setCurrentStatusText1("Data Export@MSG:Operation initiated..." + fsinfo);
		Thread.sleep(1000);
		status.setCurrentStatusText1("Start export");
		status.setCurrentStatusValue(-1);
		
		// check if storage location is already registered, if yes, enable it, if it is not enabled, and use it
		// if not known, register new VFS storage location
		// issue copy action to this location
		// if storage location is newly registered, disable storage location after export, otherwise, leave it enabled if it was enabled before,
		// otherwise also disable it
		
		status.setCurrentStatusText1("Operation finished");
		status.setCurrentStatusText2("");
		status.setCurrentStatusValue(100);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (errorMessage == null)
			errorMessage = "";
		else {
			errorMessage = " " + errorMessage + "";
		}
		if (targetDirectory == null)
			return new MainPanelComponent("No output directory has been selected." + errorMessage);
		else {
			if (errorMessage.trim().length() > 0)
				return new MainPanelComponent("Output incomplete. Error: " + errorMessage);
			else
				return new MainPanelComponent("Files have been exported to directory " + targetDirectory.getAbsolutePath() + ".");
		}
	}
	
	public ExperimentReferenceInterface getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
	
	long startTime;
	File ff;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		targetDirectory = null;
		System.out.println();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Current directory is " + (new File("").getAbsolutePath()));
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command requires specification of an empty output directory name.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: If a part of the specified path is not existing, it will be created.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">READY: PLEASE ENTER DIRECTORY STRUCTURE (ENTER NOTHING TO CANCEL OPERATION):");
		String outputDir = SystemAnalysis.getCommandLineInput();
		if (outputDir == null || outputDir.trim().isEmpty())
			return false;
		else {
			File f = new File(outputDir);
			if (!f.exists()) {
				if (!f.mkdirs()) {
					System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Could not create directory structure (" + f.getAbsolutePath() + ")");
					System.out.println();
					return false;
				}
			}
			if (!f.isDirectory()) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Output specifies a file instead of a directory (" + f.getAbsolutePath() + ")");
				System.out.println();
				return false;
			}
			String[] fl = f.list();
			if (fl.length > 0) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">WARNING: Output directory contains " + fl.length + " files or directories.");
				System.out.println();
			}
			
			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Output to " + f.getAbsolutePath());
			targetDirectory = f;
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
				"Processing time " + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000));
	}
}
