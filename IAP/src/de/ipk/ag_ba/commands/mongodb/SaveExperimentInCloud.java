package de.ipk.ag_ba.commands.mongodb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.MergeCompareRequirements;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class SaveExperimentInCloud extends AbstractNavigationAction {
	private NavigationButton src;
	
	ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	
	private final boolean storeInMongo;
	
	private Experiment newExperiment;
	
	private final ArrayList<String> messages = new ArrayList<String>();
	
	private MongoDB m;
	
	public SaveExperimentInCloud(boolean storeInMongo) {
		super("Upload data set to the IAP Systems Biology Cloud database service");
		this.storeInMongo = storeInMongo;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		// return new MainPanelComponent("This function will be implementented in an intermediate update.");
		return new MainPanelComponent("<b>Messages:</b><br><br>" + StringManipulationTools.getStringList(messages, "<br>"));
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		messages.clear();
		if (storeInMongo && m == null) {
			Object[] sel = MyInputHelper.getInput("Select the database-target:", "Target Selection", new Object[] {
					"Target", MongoDB.getMongos()
			});
			
			if (sel == null)
				return;
			
			this.m = (MongoDB) sel[0];
		}
		
		this.src = src;
		this.newExperiment = null;
		res.clear();
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					prepareDataSetFromFileList(new RunnableWithMappingData() {
						@Override
						public void run() {
						}
						
						@Override
						public void setExperimenData(ExperimentInterface experiment) {
							tso.setParam(0, experiment);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					ErrorMsg.addErrorMessage(e);
				}
			}
		});
		Experiment experiment = (Experiment) tso.getParam(0, null);
		if (experiment != null) {
			newExperiment = experiment;
			
			if (storeInMongo) {
				m.saveExperiment(newExperiment, status);
			}
			ExperimentReference exRef = new ExperimentReference(newExperiment);
			exRef.setM(m);
			for (ActionDataProcessing adp : IAPpluginManager.getInstance().getExperimentProcessingActions(exRef, true))
				res.add(new NavigationButton(adp, src.getGUIsetting()));
			
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		if (newExperiment != null) {
			res.add(src);
			res.add(ActionMongoExperimentsNavigation.getMongoExperimentButton(
					new ExperimentReference(newExperiment),
					src.getGUIsetting()));
		}
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return res;
	}
	
	private void prepareDataSetFromFileList(RunnableWithMappingData resultProcessor) throws Exception {
		List<File> fileList = OpenFileDialogService.getFiles(new String[] { "jpg", "jpeg", "png", "tif", "tiff" }, "JPEG, PNG or TIFF Images");
		if (fileList == null)
			return;
		if (fileList.isEmpty())
			return;
		fileList = new ArrayList<File>(fileList);
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		final File parentFolder = fileList.iterator().next().getParentFile();
		
		TableData tableData;
		
		boolean foundAnnoFile;
		
		GregorianCalendar first = null;
		GregorianCalendar last = null;
		int fileIDX = 0;
		for (File f : fileList) {
			ExperimentDataAnnotation eda = new ExperimentDataAnnotation();
			String fn = f.getName();
			if (fn.contains("."))
				fn = fn.substring(0, fn.lastIndexOf("."));
			eda.setQualityIDs(hs(fn));
			LinkedHashSet<Integer> replIDs = new LinkedHashSet<Integer>();
			fileIDX++;
			replIDs.add(fileIDX);
			eda.setReplicateIDs(replIDs);
			long creationTime = f.lastModified();
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(creationTime);
			if (first == null)
				first = gc;
			if (last == null)
				last = gc;
			if (gc.after(last))
				last = gc;
			if (gc.before(first))
				first = gc;
		}
		
		File annotationFile = new File(parentFolder.getAbsoluteFile() + File.separator + "fileimport.xlsx");
		if (!annotationFile.exists()) {
			tableData = new TableData();
			foundAnnoFile = false;
			int col = 1;
			int row = 1;
			tableData.addCellDataNG(col++, row, "import file");
			tableData.addCellDataNG(col++, row, "plant ID");
			tableData.addCellDataNG(col++, row, "replicate");
			tableData.addCellDataNG(col++, row, "time");
			tableData.addCellDataNG(col++, row, "time unit");
			tableData.addCellDataNG(col++, row, "imaging time");
			tableData.addCellDataNG(col++, row, "measurement tool");
			tableData.addCellDataNG(col++, row, "camera.position");
			tableData.addCellDataNG(col++, row, "rotation degree");
			tableData.addCellDataNG(col++, row, "species");
			tableData.addCellDataNG(col++, row, "genotype");
			tableData.addCellDataNG(col++, row, "variety");
			tableData.addCellDataNG(col++, row, "growth conditions");
			tableData.addCellDataNG(col++, row, "treatment");
			row++;
			int repl = 0;
			for (File f : fileList) {
				col = 1;
				tableData.addCellDataNG(col++, row, f.getName());
				tableData.addCellDataNG(col++, row, f.getName().split("@", 2)[0]);
				tableData.addCellDataNG(col++, row, repl++);
				tableData.addCellDataNG(col++, row, (int) ((f.lastModified() - first.getTime().getTime()) / (1000 * 60 * 60 * 24)));
				tableData.addCellDataNG(col++, row, "day");
				tableData.addCellDataNG(col++, row, new Date(f.lastModified()));
				tableData.addCellDataNG(col++, row, "tool");
				tableData.addCellDataNG(col++, row, "vis.top");
				tableData.addCellDataNG(col++, row, 0.0);
				tableData.addCellDataNG(col++, row, "Barley");
				tableData.addCellDataNG(col++, row, "geno");
				tableData.addCellDataNG(col++, row, "var");
				tableData.addCellDataNG(col++, row, "gcon");
				tableData.addCellDataNG(col++, row, "treat");
				row++;
			}
			
			tableData.saveToExcelFile("meta-data", annotationFile, status);
			return;
		} else {
			tableData = TableData.getTableData(annotationFile, true);
			foundAnnoFile = true;
		}
		
		Experiment e = new Experiment();
		ExperimentHeader eh = new ExperimentHeader();
		eh.setExperimentname(parentFolder.getName());
		eh.setExperimentType("File Import");
		eh.setCoordinator(SystemAnalysis.getUserName());
		eh.setImportDate(new Date());
		eh.setImportUserName(SystemAnalysis.getUserName());
		eh.setNumberOfFiles(fileList.size());
		eh.setOriginDbId(parentFolder.getCanonicalPath());
		eh.setStartDate(first.getTime());
		eh.setStorageTime(last.getTime());
		long sizekb = 0;
		for (File f : fileList)
			sizekb += f.length();
		eh.setSizekb(sizekb / 1024);
		e.setHeader(eh);
		
		String path = fileList.iterator().next().getParent();
		
		for (int row = 2; row <= tableData.getMaximumRow(); row++) {
			int metaColumn = 1;
			String importFile = tableData.getUnicodeStringCellData(metaColumn++, row);
			String plantId = tableData.getUnicodeStringCellData(metaColumn++, row);
			Integer replicate = ((Double) tableData.getCellData(metaColumn++, row, -1)).intValue();
			Integer time = ((Double) tableData.getCellData(metaColumn++, row, -1)).intValue();
			String timeUnit = tableData.getUnicodeStringCellData(metaColumn++, row);
			Date imagingTime = tableData.getCellDataDateObject(metaColumn++, row, null);
			String measurementTool = tableData.getUnicodeStringCellData(metaColumn++, row);
			String camera = tableData.getUnicodeStringCellData(metaColumn++, row);
			Double rotation = (Double) tableData.getCellData(metaColumn++, row, 0.0);
			String species = tableData.getUnicodeStringCellData(metaColumn++, row);
			String genotype = tableData.getUnicodeStringCellData(metaColumn++, row);
			String varity = tableData.getUnicodeStringCellData(metaColumn++, row);
			String growthConditions = tableData.getUnicodeStringCellData(metaColumn++, row);
			String treatment = tableData.getUnicodeStringCellData(metaColumn++, row);
			
			if (importFile == null || importFile.isEmpty())
				messages.add("No file name specified in column A, row " + (row + 1) + ".");
			File f = new File(path + File.separator + importFile);
			if (!f.exists())
				messages.add("The file '" + importFile + "' specified in column A, row " + (row + 1) + " could not be found.");
			
			Substance3D sub = new Substance3D();
			sub.setName(camera);
			e.add(sub);
			
			Condition3D con = new Condition3D(sub);
			con.setExperimentInfo(eh);
			con.setSpecies(species);
			con.setGenotype(genotype);
			con.setVariety(varity);
			con.setGrowthconditions(growthConditions);
			con.setTreatment(treatment);
			// con.setSequence("");
			sub.add(con);
			
			Sample3D sample = new Sample3D(con);
			con.add(sample);
			sample.setSampleFineTimeOrRowId(imagingTime.getTime());
			sample.setTime(time);
			sample.setTimeUnit(timeUnit);
			sample.setMeasurementtool(measurementTool);
			
			ImageData img = new ImageData(sample);
			img.setURL(FileSystemHandler.getURL(f));
			img.setQualityAnnotation(plantId);
			img.setPosition(rotation);
			img.setReplicateID(replicate);
			
			sample.add(img);
		}
		
		Experiment res = new Experiment();
		res.setHeader(e.getHeader().clone());
		MergeCompareRequirements mcr = new MergeCompareRequirements();
		for (SubstanceInterface s : new ArrayList<SubstanceInterface>(e)) {
			Substance3D.addAndMergeA(res, s, true, BackgroundThreadDispatcher.getRunnableExecutor(), mcr);
		}
		e.clear();
		res.sortSubstances();
		res.sortConditions();
		
		resultProcessor.setExperimenData(res);
		resultProcessor.run();
	}
	
	private LinkedHashSet<String> hs(String string) {
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		res.add(string);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		if (storeInMongo)
			return "Add files";
		else
			return "Create Dataset from Files";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-Photos-64.png";
	}
	
	public void setMongoDB(MongoDB m) {
		this.m = m;
	}
}