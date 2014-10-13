package de.ipk.ag_ba.commands.mongodb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

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
			exRef.m = m;
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
		ArrayList<File> fileList = OpenFileDialogService.getFiles(new String[] { "jpg", "jpeg", "png", "tif", "tiff" }, "JPEG, PNG or TIFF Images");
		if (fileList == null)
			return;
		if (fileList.isEmpty())
			return;
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		final File parentFolder = fileList.iterator().next().getParentFile();
		
		TableData tableData;
		
		boolean foundAnnoFile;
		
		File annotationFile = new File(parentFolder.getAbsoluteFile() + File.pathSeparator + "annotation.xlsx");
		if (!annotationFile.exists()) {
			tableData = new TableData();
			foundAnnoFile = false;
		} else {
			tableData = TableData.getTableData(annotationFile, true);
			foundAnnoFile = true;
		}
		
		HashMap<Integer, String> colNum2headerText = new HashMap<>();
		for (int row = 1; row < tableData.getMaximumRow(); row++) {
			for (int col = 1; col < tableData.getMaximumCol(); col++) {
				Object v = tableData.getCellData(col, row, null);
				if (v == null)
					continue;
				if (row == 1) {
					colNum2headerText.put(col, v + "");
				} else
					if (col == 1) {
						
					}
			}
		}
		
		HashMap<ExperimentDataAnnotation, GregorianCalendar> eda2day = new HashMap<ExperimentDataAnnotation, GregorianCalendar>();
		GregorianCalendar first = null;
		GregorianCalendar last = null;
		int fileIDX = 0;
		for (File f : fileList) {
			ExperimentDataAnnotation eda = new ExperimentDataAnnotation();
			
			// anno.put(f, eda);
			// eda.setExpname(hs(parentFolder.getName()));
			// eda.setCondspecies(hs(c.getSpecies()));
			// eda.setCondgenotype(hs(c.getGenotype()));
			// eda.setCondtreatment(hs(c.getTreatment()));
			// eda.setCondvariety(hs(c.getVariety()));
			// eda.setReplicateIDs(hi(c.getConditionId()));
			String fn = f.getName();
			if (fn.contains("."))
				fn = fn.substring(0, fn.lastIndexOf("."));
			eda.setQualityIDs(hs(fn));
			LinkedHashSet<Integer> replIDs = new LinkedHashSet<Integer>();
			fileIDX++;
			replIDs.add(fileIDX);
			eda.setReplicateIDs(replIDs);
			long creationTime = f.lastModified();
			// eda.setSampleFineTimepoint(creationTime);
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(creationTime);
			eda2day.put(eda, gc);
			if (first == null)
				first = gc;
			if (last == null)
				last = gc;
			if (gc.after(last))
				last = gc;
			if (gc.before(first))
				first = gc;
		}
		final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
		int fD = first.get(GregorianCalendar.DAY_OF_MONTH);
		int fM = first.get(GregorianCalendar.MONTH);
		int fY = first.get(GregorianCalendar.YEAR);
		int lD = last.get(GregorianCalendar.DAY_OF_MONTH);
		int lM = last.get(GregorianCalendar.MONTH);
		int lY = last.get(GregorianCalendar.YEAR);
		
		HashMap<Integer, ExperimentDataAnnotation> anno = null;
		for (ExperimentDataAnnotation eda : anno.values()) {
			if (last == null || first == null || !eda2day.containsKey(eda))
				continue;
			LinkedHashSet<String> substances = new LinkedHashSet<String>();
			substances.add("vis.top");
			substances.add("vis.side");
			substances.add("fluo.top");
			substances.add("fluo.side");
			substances.add("nir.top");
			substances.add("nir.side");
			substances.add("ir.top");
			substances.add("ir.side");
			
			eda.setSubstances(substances);
			eda.setExpname(hs(parentFolder.getName()));
			eda.setExpcoord(hs(SystemAnalysis.getUserName()));
			try {
				eda.setExpsrc(hs(SystemAnalysis.getUserName() + "@" + SystemAnalysis.getLocalHost().getCanonicalHostName() +
						":" + parentFolder.getName()));
			} catch (Exception e) {
				eda.setExpsrc(hs(SystemAnalysis.getUserName() + "@localhost:" + parentFolder.getName()));
				
			}
			
			eda.setExpstartdate(hs(nn(fD) + "/" + nn(fM) + "/" + nn(fY)));
			eda.setExpimportdate(hs(nn(lD) + "/" + nn(lM) + "/" + nn(lY)));
			GregorianCalendar g = eda2day.get(eda);
			long tS = first.getTime().getTime();
			long tM = g.getTime().getTime();
			long diff = 1 + (tM - tS) / MILLISECONDS_IN_DAY;
			eda.setSamptimepoint(hs(diff + ""));
			eda.setSamptimeunit(hs("day"));
		}
		
		String fn = "";
		TableData td = TableData.getTableData(new File(fn));
		/*
		 * for (ExperimentInterface mdl : il.process(fileList, null)) {
		 * if (mdl != null && resultProcessor != null) {
		 * resultProcessor.setExperimenData(mdl);
		 * resultProcessor.run();
		 * }
		 * }
		 */
	}
	
	private LinkedHashSet<String> hs(String string) {
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		res.add(string);
		return res;
	}
	
	private LinkedHashSet<Integer> hi(Integer i) {
		LinkedHashSet<Integer> res = new LinkedHashSet<Integer>();
		res.add(i);
		return res;
	}
	
	private String nn(int n) {
		String res = n + "";
		if (res.length() < 2)
			return "0" + res;
		else
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