package de.ipk.ag_ba.commands.mongodb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.AnnotationFromGraphFileNameProvider;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.ImageLoader;

/**
 * @author klukas
 */
public class SaveExperimentInCloud extends AbstractNavigationAction {
	private NavigationButton src;
	
	ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	
	private final boolean storeInMongo;
	
	private Experiment newExperiment;
	
	private MongoDB m;
	
	public SaveExperimentInCloud(boolean storeInMongo) {
		super("Upload data set to the IAP Systems Biology Cloud database service");
		this.storeInMongo = storeInMongo;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
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
		try {
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
				try {
					if (storeInMongo) {
						m.saveExperiment(newExperiment, status);
					}
					ExperimentReference exRef = new ExperimentReference(newExperiment);
					for (NavigationButton ne : ImageAnalysisCommandManager.getCommands(m, exRef,
							src.getGUIsetting()))
						res.add(ne);
				} catch (Exception e1) {
					newExperiment = null;
					ErrorMsg.addErrorMessage(e1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
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
	
	private static void processData(RunnableWithMappingData resultProcessor, ImageLoader il, ArrayList<File> fileList,
			AnnotationFromGraphFileNameProvider provider) {
		if (fileList != null)
			for (ExperimentInterface mdl : il.process(fileList, provider)) {
				if (mdl != null && resultProcessor != null) {
					resultProcessor.setExperimenData(mdl);
					resultProcessor.run();
				}
			}
	}
	
	public static void prepareDataSetFromFileList(RunnableWithMappingData resultProcessor) throws Exception {
		ImageLoader il = new ImageLoader();
		ArrayList<File> fileList = OpenFileDialogService.getFiles(il.getValidExtensions(), "Images");
		if (fileList == null)
			return;
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		processData(resultProcessor, il, fileList, null);
	}
	
	@Override
	public String getDefaultTitle() {
		if (storeInMongo)
			return "Add files";
		else
			return "Load files";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/user-desktop.png";
	}
	
	public void setMongoDB(MongoDB m) {
		this.m = m;
	}
}