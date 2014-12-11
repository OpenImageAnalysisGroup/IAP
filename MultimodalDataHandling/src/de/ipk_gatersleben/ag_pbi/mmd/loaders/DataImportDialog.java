/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.FolderPanel;
import org.FolderPanel.Iconsize;
import org.MergeCompareRequirements;
import org.SystemAnalysis;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotationManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

public class DataImportDialog {
	
	/**
	 * Opens a a dialog and lets the user specify the metadata.
	 * 
	 * @param templateLoader
	 */
	public Collection<ExperimentInterface> getExperimentMetadataFromUserByDialog(List<File> files_unfiltered,
			TemplateLoaderMMD templateLoader, final List<ExperimentDataProcessor> optUseTheseAnnotationProviders) {
		
		final TreeSet<File> files = new TreeSet<File>();
		
		if (files_unfiltered != null)
			for (final File f : files_unfiltered) {
				if (!f.exists())
					continue;
				if (f.getAbsolutePath().toLowerCase().endsWith(".hdr")) {
					File f2 = new File(f.getAbsolutePath().replaceAll(".hdr", ".img"));
					if (!f2.exists())
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								MainFrame.showMessageDialog(
										"<html>No *.img file exists for the *.hdr file! Skipping import of<br><i>" + f,
										"Error while importing!");
							}
						});
					else
						files.add(f2); // -> treeset gets rid of doubled analyze data
				} else
					files.add(f);
				
			}
		
		if (files.size() == 0)
			return null;
		
		final ArrayList<ImportDialogFile> idflist = new ArrayList<ImportDialogFile>();
		
		final FolderPanel fp = new FolderPanel("");
		fp.setColumnStyle(TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
		fp.enableSearch(false);
		fp.setShowCondenseButton(false);
		fp.setCondensedState(false);
		fp.setMaximumRowCount(1, true);
		fp.setIconSize(Iconsize.MIDDLE);
		fp.setFrameColor(null, Color.BLACK, 0, 5);
		fp.setBackground(null);
		fp.setOpaque(false);
		
		ArrayList<File> filewrong = new ArrayList<File>();
		int cnt = 0;
		for (File f : files) {
			ImportDialogFile idf = new ImportDialogFile();
			if (idf.initializePanel(f, cnt++, idflist, templateLoader)) {
				idflist.add(idf);
				fp.addGuiComponentRow(null, idf, true);
			} else
				filewrong.add(f);
		}
		
		if (filewrong.size() > 0) {
			String text = "<html>Some files were filtered out because the file extension is wrong or errors occured:<i>";
			for (File f : filewrong)
				text += "<br>" + f.toString();
			MainFrame.showMessageDialogWithScrollBars(text, "Data filtered out");
		}
		
		if (idflist.size() == 0)
			return null;
		
		if (idflist.size() == 1)
			idflist.get(0).setCopyFormDataEnabled(false);
		
		fp.layoutRows();
		fp.addCollapseListenerDialogSizeUpdate();
		
		BackgroundTaskHelper.executeLaterOnSwingTask(50, new Runnable() {
			@Override
			public void run() {
				fp.dialogSizeUpdate();
			}
		});
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Requesting Annotations", "...");
		BackgroundTaskHelper.issueSimpleTask("Requesting Annotations", "...", new Runnable() {
			@Override
			public void run() {
				HashMap<File, ExperimentDataAnnotation> res = ExperimentDataAnnotationManager.getInstance()
						.getExperimentAnnotation(optUseTheseAnnotationProviders, files);
				tso.setParam(0, res);
			}
		}, new Runnable() {
			@Override
			@SuppressWarnings("unchecked")
			public void run() {
				HashMap<File, ExperimentDataAnnotation> res = (HashMap<File, ExperimentDataAnnotation>) tso.getParam(0,
						null);
				for (ImportDialogFile idf : idflist)
					idf.setAnnotation(res.get(idf.getFile()));
			}
		}, status, 1000);
		
		int n = JOptionPane.showConfirmDialog(MainFrame.getInstance(), fp, "Add Annotation for " + idflist.size()
				+ " File" + (idflist.size() > 1 ? "s" : ""), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (n != 0)
			return null;
		
		ExperimentInterface experiment = new Experiment();
		long fs = 0;
		for (ImportDialogFile idf : idflist) {
			SubstanceInterface md = Substance3D.createnewSubstance(idf.getSubstance());
			
			Condition3D series = (Condition3D) Experiment.getTypeManager().getNewCondition(md);
			series.setSpecies(idf.getSpecies());
			series.setGenotype(idf.getGenotype());
			series.setTreatment(idf.getTreatment());
			series.setExperimentName(idf.getExperimentName());
			series.setExperimentDatabaseOriginId(idf.getExperimentSrc());
			series.setExperimentCoordinator(idf.getCoordinator());
			series.setExperimentStartDate(idf.getStartdate());
			series.setExperimentImportdate(new Date());
			md.add(series);
			
			Sample3D sample = (Sample3D) Experiment.getTypeManager().getNewSample(series);
			sample.setTime(idf.getTime());
			sample.setTimeUnit(idf.getTimeUnit());
			sample.setSampleFineTimeOrRowId(idf.getFineTime());
			sample.setComponent(idf.getComponent());
			sample.setMeasurementtool(idf.getMeasurementtool());
			series.add(sample);
			
			List<NumericMeasurementInterface> e1 = idf.getLoader().addMeasurementsToHierarchy(sample,
					idf.getExperimentName());
			
			if (e1 != null) {
				sample.addAll(e1);
				Substance3D.addAndMergeA(experiment, md, false, null, new MergeCompareRequirements());
				File f = idf.getFile();
				if (f != null && f.exists() && f.canRead()) {
					fs += f.length();
				}
			} else
				ErrorMsg.addErrorMessage("File " + idf.getFile() + " could not be loaded. Ignoring...");
		}
		experiment.getHeader().setNumberOfFiles(idflist.size());
		experiment.getHeader().setSizekb(fs / 1024);
		experiment.getHeader().setImportusername(SystemAnalysis.getUserName());
		return experiment.split();
	}
	
}
