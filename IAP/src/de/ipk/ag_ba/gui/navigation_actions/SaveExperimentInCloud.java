package de.ipk.ag_ba.gui.navigation_actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.OpenExcelFileDialogService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.AnnotationFromGraphFileNameProvider;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.ImageLoader;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.MyScanner;

/**
 * @author klukas
 */
public class SaveExperimentInCloud extends AbstractNavigationAction {
	private NavigationButton src;
	
	ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	
	private final boolean storeInMongo;
	
	private Experiment newExperiment;
	
	private MongoDB m;
	
	SaveExperimentInCloud(boolean storeInMongo) {
		super("Upload data set to the IAP Systems Biology Cloud database service");
		this.storeInMongo = storeInMongo;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		
		Object[] sel = MyInputHelper.getInput("Select the database-target:", "Target Selection", new Object[] {
							"Target", MongoDB.getMongos()
		});
		
		if (sel == null)
			return;
		
		this.m = (MongoDB) sel[0];
		
		this.src = src;
		this.newExperiment = null;
		res.clear();
		try {
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					prepareDataSetFromFileList(new RunnableWithMappingData() {
						@Override
						public void run() {
						}
						
						@Override
						public void setExperimenData(ExperimentInterface experiment) {
							tso.setParam(0, experiment);
						}
					});
				}
			});
			Experiment experiment = (Experiment) tso.getParam(0, null);
			if (experiment != null) {
				newExperiment = experiment;
				// Collection<Experiment> experiments = experiment.split();
				// if (experiments.size() > 1)
				// ErrorMsg.addErrorMessage("Unexpected internal error: More than one experiment in dataset!");
				// for (Experiment e : experiments) {
				try {
					if (storeInMongo) {
						newExperiment.getHeader().setExperimenttype("Phenotyping");
						newExperiment.getHeader().setImportusergroup("Shared");
						newExperiment.getHeader().setImportusername(SystemAnalysis.getUserName());
						System.out.println(newExperiment.toString());
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
				// break;
				// }
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		if (newExperiment != null) {
			res.add(src);
			res.add(MongoExperimentsNavigationAction.getMongoExperimentButton(newExperiment.getHeader(),
								src.getGUIsetting(), m));
		}
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return res;
	}
	
	private static void processData(RunnableWithMappingData resultProcessor, ImageLoader il, ArrayList<File> fileList,
						AnnotationFromGraphFileNameProvider provider) {
		for (ExperimentInterface mdl : il.process(fileList, provider)) {
			if (mdl != null) {
				if (resultProcessor != null) {
					resultProcessor.setExperimenData(mdl);
					resultProcessor.run();
				}
			}
		}
	}
	
	public static void prepareDataSetFromFileList(RunnableWithMappingData resultProcessor) {
		ImageLoader il = new ImageLoader();
		ArrayList<File> fileList = OpenFileDialogService.getFiles(il.getValidExtensions(), "Images");
		if (fileList == null)
			return;
		Collections.sort(fileList, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		String providedFileNameFormat = null;
		HashMap<Integer, Condition> replicateNumber2conditionTemplate = null;
		
		Object[] lm = MyInputHelper.getInput(
							"[Yes;No]Load meta data (mapping from plant ID to condition annotation) from file?<br><br>"
												+ "The provided table needs to contain at least two columns,<br>"
												+ "the first needs to contain the plant ID, the following columns,<br>"
												+ "need to contain annotation information about experiment conditions<br>"
												+ "such as species, genotype or treatment.<br><br>", "Load Meta Data?", new Object[] {});
		if (lm != null) {
			// yes answered
			// load meta data file
			File excelFile = OpenExcelFileDialogService.getExcelFile();
			if (excelFile != null && excelFile.canRead()) {
				TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
				if (myData.getMaximumCol() < 2) {
					MainFrame.getInstance().showMessageDialog("File needs to contain at least two columns!");
					return;
				} else {
					List<Object> inp = new LinkedList<Object>();
					inp.add("Column 1 (" + myData.getSampleValues(false, 1, 3, ", ", "-") + ")");
					ArrayList<String> dummy = new ArrayList<String>();
					dummy.add("Plant ID / Replicate ID");
					inp.add(dummy);
					for (int col = 2; col <= myData.getMaximumCol(); col++) {
						inp.add("Column " + col + " (" + myData.getSampleValues(false, col, 3, ", ", "-") + ")");
						ArrayList<ConditionInfo> val = Condition.ConditionInfo.getList();
						inp.add(val);
					}
					Object[] columnTypes = MyInputHelper
										.getInput(
															"<html>"
																				+ "Please specify the column contents:<br><br>"
																				+ "<b>Warning: Currently only the fields Species, Genotype and Treatment are processed correctly!</b><br><hl><br>",
															"Meta Data / Condition Annotation", inp.toArray());
					if (columnTypes != null) {
						replicateNumber2conditionTemplate = new HashMap<Integer, Condition>();
						for (int row = 1; row <= myData.getMaximumRow(); row++) {
							try {
								Integer replicateID = ((Double) myData.getCellData(1, row, null)).intValue();
								if (replicateID == null) {
									ErrorMsg.addErrorMessage("No valid replicate ID in column 1 of row " + row + "!");
								} else {
									Condition c = new Condition(null);
									for (int col = 2; col <= columnTypes.length; col++) {
										ConditionInfo ci = (ConditionInfo) columnTypes[col - 1];
										String value = myData.getUnicodeStringCellData(col, row);
										if (value != null && value.length() > 0)
											c.setField(ci, value.trim());
									}
									if (replicateNumber2conditionTemplate.containsKey(replicateID))
										ErrorMsg.addErrorMessage("Duplicate annotation information definition for replicate ID "
															+ replicateID + " in row " + row + " - ignored!");
									else
										replicateNumber2conditionTemplate.put(replicateID, c);
								}
							} catch (Exception err) {
								ErrorMsg.addErrorMessage("Can't process data from meta data file in row " + row + "!");
							}
						}
					} else
						return;
				}
			}
		}
		
		MyScanner[] replicateIDs = AnnotationFromGraphFileNameProvider.getFileNameInfos(fileList, providedFileNameFormat,
							replicateNumber2conditionTemplate);
		HashSet<String> detectedConditions = getConditions(replicateIDs);
		if (replicateIDs == null || replicateIDs.length == 0) {
			processData(resultProcessor, il, fileList, null);
		} else
			if (detectedConditions.size() > 0) {
				HashMap<String, MyScanner> filename2scanner = new HashMap<String, MyScanner>();
				for (MyScanner s : replicateIDs) {
					filename2scanner.put(s.getFileName(), s);
				}
				HashMap<Integer, Condition> replId2ConditionInfo = new HashMap<Integer, Condition>();
				for (MyScanner rId : filename2scanner.values())
					replId2ConditionInfo.put(rId.getReplicateID(), getCondition(rId));
				AnnotationFromGraphFileNameProvider provider = new AnnotationFromGraphFileNameProvider(replId2ConditionInfo,
									filename2scanner);
				processData(resultProcessor, il, fileList, provider);
			} else {
				Object[] con = MyInputHelper.getInput("Experiment condition specification:", "Number of conditions?",
									new Object[] { "Number of conditions", 1 });
				if (con != null) {
					Integer conditionCount = (Integer) con[0];
					ArrayList<Object> paramList = new ArrayList<Object>();
					int first = lowestOrHighestReplicateID(true, replicateIDs);
					int last = lowestOrHighestReplicateID(false, replicateIDs);
					for (int n = 0; n < conditionCount; n++) {
						paramList.add("Specify details of condition " + (n + 1) + ".<br>"
											+ "The dataset contains replicate IDs ranging from " + first + " to " + last + ".");
						paramList.add("Condition " + (n + 1));
						ArrayList<Object> ppp = new ArrayList<Object>();
						ppp.add("First replicate ID");
						ppp.add(first);
						ppp.add("Last replicate ID");
						ppp.add(last);
						ppp.add("Species");
						ppp.add("");
						ppp.add("Genotype");
						ppp.add("");
						ppp.add("Treatment");
						ppp.add("");
						
						paramList.add(ppp);
					}
					
					ArrayList<ArrayList<Object>> input = MyInputHelper.getMultipleInput(paramList.toArray());
					if (input != null) {
						HashMap<Integer, Condition> replId2ConditionInfo = new HashMap<Integer, Condition>();
						for (int n = 0; n < conditionCount; n++) {
							ArrayList<Object> res = input.get(n);
							int i = 0;
							Integer fId = (Integer) res.get(i++);
							Integer lId = (Integer) res.get(i++);
							
							Condition c = new Condition(null);
							c.setSpecies((String) res.get(i++));
							c.setGenotype((String) res.get(i++));
							c.setTreatment((String) res.get(i++));
							
							int min = min(fId, lId);
							int max = max(fId, lId);
							
							for (int r = min; r <= max; r++)
								replId2ConditionInfo.put(r, c);
						}
						HashMap<String, MyScanner> rrr = new HashMap<String, MyScanner>();
						for (MyScanner s : replicateIDs) {
							rrr.put(s.getFileName(), s);
						}
						AnnotationFromGraphFileNameProvider provider = new AnnotationFromGraphFileNameProvider(
											replId2ConditionInfo, rrr);
						processData(resultProcessor, il, fileList, provider);
					}
				}
			}
	}
	
	/**
	 * @param rId
	 * @return
	 */
	private static Condition getCondition(MyScanner rId) {
		Condition c = new Condition(null);
		c.setSpecies("Arabidopsis");
		c.setGenotype(rId.getCondition());
		return c;
	}
	
	private static HashSet<String> getConditions(MyScanner[] replicateIDs) {
		HashSet<String> res = new HashSet<String>();
		for (MyScanner s : replicateIDs) {
			String cond = s.getCondition();
			if ((cond == null || cond.length() == 0) && s.getConditionTemplate() != null)
				cond = s.getConditionTemplate().getName();
			if (cond != null && cond.length() > 0)
				res.add(cond);
		}
		return res;
	}
	
	private static int lowestOrHighestReplicateID(boolean lowestTrue, MyScanner[] replicateIDs) {
		int res;
		if (lowestTrue) {
			// find lowest replicate ID
			res = Integer.MAX_VALUE;
			for (MyScanner s : replicateIDs) {
				if (s.getReplicateID() < res)
					res = s.getReplicateID();
			}
		} else {
			// find highest replicate ID
			res = Integer.MIN_VALUE;
			for (MyScanner s : replicateIDs) {
				if (s.getReplicateID() > res)
					res = s.getReplicateID();
			}
		}
		return res;
	}
	
	private static int max(int fId, int lId) {
		return fId > lId ? fId : lId;
	}
	
	private static int min(int fId, int lId) {
		return fId < lId ? fId : lId;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Add Files";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/user-desktop.png";
	}
	
}