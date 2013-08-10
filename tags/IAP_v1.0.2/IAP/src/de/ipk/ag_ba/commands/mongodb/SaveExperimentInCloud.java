package de.ipk.ag_ba.commands.mongodb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.load_lt.TableDataHeadingRow;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableDataStringRow;
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
	
	private final ArrayList<String> messages = new ArrayList<String>();
	
	private MongoDB m;
	
	public SaveExperimentInCloud(boolean storeInMongo) {
		super("Upload data set to the IAP Systems Biology Cloud database service");
		this.storeInMongo = storeInMongo;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<b>Messages:</b><br><br>" + StringManipulationTools.getStringList(messages, "<br>"));
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
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
					exRef.m = m;
					for (ActionDataProcessing adp : IAPpluginManager.getInstance().getExperimentProcessingActions(exRef, true))
						res.add(new NavigationButton(adp, src.getGUIsetting()));
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
	
	private void processData(RunnableWithMappingData resultProcessor, ImageLoader il, ArrayList<File> fileList,
			AnnotationFromGraphFileNameProvider provider) {
		if (fileList != null)
			for (ExperimentInterface mdl : il.process(fileList, provider)) {
				if (mdl != null && resultProcessor != null) {
					resultProcessor.setExperimenData(mdl);
					resultProcessor.run();
				}
			}
	}
	
	private void prepareDataSetFromFileList(RunnableWithMappingData resultProcessor) throws Exception {
		ImageLoader il = new ImageLoader();
		ArrayList<File> fileList = OpenFileDialogService.getFiles(il.getValidExtensions(), "Images");
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
		final HashMap<String, Condition> metaInfo = scanForMetadata(parentFolder);
		AnnotationFromGraphFileNameProvider provider = null;
		if (metaInfo != null)
			provider = new AnnotationFromGraphFileNameProvider(null, null) {
				
				@Override
				public HashMap<File, ExperimentDataAnnotation> getAnnotations(Collection<File> files) {
					GregorianCalendar first = null;
					GregorianCalendar last = null;
					HashMap<File, ExperimentDataAnnotation> anno = new HashMap<File, ExperimentDataAnnotation>();
					for (File f : files) {
						Condition c = metaInfo.get(f.getName());
						if (c == null) {
							c = new Condition(null);
							c.setGenotype(f.getName());
						}
						ExperimentDataAnnotation eda = new ExperimentDataAnnotation();
						eda.setExpname(hs(parentFolder.getName()));
						
						eda.setCondspecies(hs(c.getSpecies()));
						eda.setCondgenotype(hs(c.getGenotype()));
						eda.setCondtreatment(hs(c.getTreatment()));
						eda.setCondvariety(hs(c.getVariety()));
						
						eda.setReplicateIDs(hi(c.getConditionId()));
						
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
					HashMap<ExperimentDataAnnotation, GregorianCalendar> eda2day = new HashMap<ExperimentDataAnnotation, GregorianCalendar>();
					final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
					int fD = first.get(GregorianCalendar.DAY_OF_MONTH);
					int fM = first.get(GregorianCalendar.MONTH);
					int fY = first.get(GregorianCalendar.YEAR);
					int lD = last.get(GregorianCalendar.DAY_OF_MONTH);
					int lM = last.get(GregorianCalendar.MONTH);
					int lY = last.get(GregorianCalendar.YEAR);
					
					for (ExperimentDataAnnotation eda : anno.values()) {
						if (last == null || first == null || !eda2day.containsKey(eda))
							continue;
						
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
					return anno;
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
			};
		processData(resultProcessor, il, fileList, provider);
	}
	
	private HashMap<String, Condition> scanForMetadata(File folder) throws IOException {
		HashMap<String, Condition> optIdTag2condition = null;
		boolean foundMetadata = false;
		HashMap<String, ArrayList<TableDataStringRow>> metadata = new HashMap<String, ArrayList<TableDataStringRow>>();
		HashMap<String, TableDataHeadingRow> metadataHeading = new HashMap<String, TableDataHeadingRow>();
		String inp = folder.getCanonicalPath();
		for (File f : folder.listFiles()) {
			String snapshotDirName = f.getName();
			if (snapshotDirName.endsWith(".csv") || snapshotDirName.endsWith(".xlsx") || snapshotDirName.endsWith(".xls")) {
				foundMetadata = true;
				String fn = inp + File.separator + snapshotDirName;
				TableData td = TableData.getTableData(new File(fn));
				if (metadata.get(fn) == null)
					metadata.put(fn, new ArrayList<TableDataStringRow>());
				ArrayList<TableDataStringRow> lines = td.getRowsAsStringValues();
				if (lines.size() > 0) {
					TableDataStringRow firstLine = lines.get(0);
					boolean foundHeadingIndicator = false;
					String headingIndicator = SystemOptions.getInstance().getString("File Import", "Meta-Data-Heading-Indicator", "Genotype");
					for (String v : firstLine.getValues()) {
						if (v != null && v.equalsIgnoreCase(headingIndicator)) {
							foundHeadingIndicator = true;
							break;
						}
					}
					if (foundHeadingIndicator) {
						lines.remove(0);
						metadataHeading.put(fn, new TableDataHeadingRow(firstLine.getMap()));
					}
				}
				metadata.get(fn).addAll(lines);
			}
		}
		if (!foundMetadata || metadata == null) {
			messages.add("<b>Found no meta-data files (files ending with file extension name .csv, .xlsx or .xls) in the selected folder!</b>");
			messages.add("Check the 'File Import'-Settings for the meta-data assignment of column-values!");
			messages.add("Modify the settings and repeat the loading, in case data is not assigned to the pre-defined fields as desired.");
		} else {
			// process metadata
			optIdTag2condition = new HashMap<String, Condition>();
			for (String fn : metadata.keySet()) {
				TableDataHeadingRow heading = metadataHeading.get(fn);
				if (heading == null)
					heading = new TableDataHeadingRow(null); // default heading info
				ArrayList<TableDataStringRow> md = metadata.get(fn);
				LTdataExchange.extendId2ConditionList(optIdTag2condition, heading, md, false);
			}
		}
		return optIdTag2condition;
	}
	
	@Override
	public String getDefaultTitle() {
		if (storeInMongo)
			return "Add files";
		else
			return "Load Files";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/user-desktop.png";
	}
	
	public void setMongoDB(MongoDB m) {
		this.m = m;
	}
}