/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
/*
 * Created on Jul 7, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;

/**
 * @author klukas
 */
public class AnnotationFromGraphFileNameProvider extends AbstractExperimentDataProcessor {
	
	private final HashMap<Integer, Condition> replId2ConditionInfo;
	private final HashMap<String, FileNameScanner> fileName2scanner;
	
	public AnnotationFromGraphFileNameProvider(HashMap<Integer, Condition> replId2ConditionInfo,
			HashMap<String, FileNameScanner> fileName2scanner) {
		this.replId2ConditionInfo = replId2ConditionInfo;
		this.fileName2scanner = fileName2scanner;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	@Override
	public String getName() {
		return "Annotation from graph file name";
	}
	
	@Override
	public HashMap<File, ExperimentDataAnnotation> getAnnotations(Collection<File> files) {
		HashMap<File, ExperimentDataAnnotation> fileName2anno = new HashMap<File, ExperimentDataAnnotation>();
		String parentFolder = null;
		
		GregorianCalendar first = null;
		GregorianCalendar last = null;
		HashMap<ExperimentDataAnnotation, GregorianCalendar> eda2day = new HashMap<ExperimentDataAnnotation, GregorianCalendar>();
		String parentFolderName = null;
		for (File f : files) {
			if (parentFolder == null) {
				parentFolder = f.getParent();
				parentFolderName = parentFolder;
				if (parentFolder != null) {
					
					if (parentFolder.indexOf(File.separator) >= 0)
						parentFolder = parentFolder.substring(parentFolder.lastIndexOf(File.separator)
								+ File.separator.length());
				}
			}
			String fn = f.getName();
			FileNameScanner s = null;
			try {
				s = fileName2scanner.get(fn);
				System.out.println(fn);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				System.err.println("Problematic file name: " + fn);
			}
			if (s != null) {
				try {
					// 001
					int replicateID = s.getReplicateID();
					int year = s.getDateYear();
					int month = s.getDateMonth();
					int day = s.getDateDay();
					GregorianCalendar gc = new GregorianCalendar(year, month, day);
					if (first == null)
						first = gc;
					if (last == null)
						last = gc;
					if (gc.after(last))
						last = gc;
					if (gc.before(first))
						first = gc;
					
					ExperimentDataAnnotation eda = new ExperimentDataAnnotation();
					eda2day.put(eda, gc);
					eda.setExpname(hs(parentFolder));
					eda.setExpcoord(hs(SystemAnalysis.getUserName()));
					try {
						eda.setExpsrc(hs(SystemAnalysis.getUserName() + "@" + SystemAnalysis.getLocalHost().getCanonicalHostName() +
								":" + parentFolderName));
					} catch (Exception e) {
						eda.setExpsrc(hs(SystemAnalysis.getUserName() + "@localhost:" + parentFolderName));
						
					}
					if (replId2ConditionInfo != null) {
						Condition c = replId2ConditionInfo.get(replicateID);
						if (c != null) {
							eda.setCondspecies(hs(c.getSpecies()));
							eda.setCondgenotype(hs(c.getGenotype()));
							eda.setCondtreatment(hs(c.getTreatment()));
							eda.setCondvariety(hs(c.getVariety()));
						}
					}
					
					if (s.getConditionTemplate() != null) {
						Condition template = s.getConditionTemplate();
						if (template.getConditionName() != null
								&& !template.getSpecies().equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
							eda.setCondspecies(hs(template.getSpecies()));
						if (template.getGenotype() != null
								&& !template.getGenotype().equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
							eda.setCondgenotype(hs(template.getGenotype()));
						if (template.getTreatment() != null
								&& !template.getTreatment().equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
							eda.setCondtreatment(hs(template.getTreatment()));
						if (template.getVariety() != null
								&& !template.getVariety().equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
							eda.setCondtreatment(hs(template.getVariety()));
					}
					
					eda.setReplicateIDs(hs(replicateID));
					eda.setSubstances(hs(s.getSubstance()));
					eda.setPositions(hs(s.getRotation()));
					eda.setPositionUnits(hs("degree"));
					eda.setSubstances(hs(s.getSubstance()));
					
					fileName2anno.put(f, eda);
				} catch (Exception e) {
					System.err.println("no annotation for " + fn);
				}
			}
		}
		
		final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
		
		for (ExperimentDataAnnotation eda : fileName2anno.values()) {
			if (last == null || first == null || !eda2day.containsKey(eda))
				continue;
			int fD = first.get(GregorianCalendar.DAY_OF_MONTH);
			int fM = first.get(GregorianCalendar.MONTH);
			int fY = first.get(GregorianCalendar.YEAR);
			int lD = last.get(GregorianCalendar.DAY_OF_MONTH);
			int lM = last.get(GregorianCalendar.MONTH);
			int lY = last.get(GregorianCalendar.YEAR);
			eda.setExpstartdate(hs(nn(fD) + "/" + nn(fM) + "/" + nn(fY)));
			eda.setExpimportdate(hs(nn(lD) + "/" + nn(lM) + "/" + nn(lY)));
			GregorianCalendar g = eda2day.get(eda);
			long tS = first.getTime().getTime();
			long tM = g.getTime().getTime();
			long diff = 1 + (tM - tS) / MILLISECONDS_IN_DAY;
			eda.setSamptimepoint(hs(diff + ""));
			eda.setSamptimeunit(hs("day"));
		}
		return fileName2anno;
	}
	
	private String nn(int n) {
		String res = n + "";
		if (res.length() < 2)
			return "0" + res;
		else
			return res;
	}
	
	private LinkedHashSet<Integer> hs(int i) {
		LinkedHashSet<Integer> res = new LinkedHashSet<Integer>();
		res.add(i);
		return res;
	}
	
	private LinkedHashSet<Double> hs(double d) {
		LinkedHashSet<Double> res = new LinkedHashSet<Double>();
		res.add(d);
		return res;
	}
	
	private LinkedHashSet<String> hs(String string) {
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		res.add(string);
		return res;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.
	 * AbstractExperimentDataProcessor#processData()
	 */
	@Override
	protected void processData() {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.
	 * AbstractExperimentDataProcessor
	 * #setExperimentData(de.ipk_gatersleben.ag_nw.
	 * graffiti.plugins.gui.editing_tools.script_helper.Experiment)
	 */
	@Override
	public void setExperimentData(ExperimentInterface mappingData) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.graffiti.plugin.algorithm.EditorAlgorithm#activeForView(org.graffiti
	 * .plugin.view.View)
	 */
	@Override
	public boolean activeForView(View v) {
		return false;
	}
	
	public static FileNameScanner[] getFileNameInfos(ArrayList<File> fileList, String optProvidedFileNameFormat,
			HashMap<Integer, Condition> optReplicate2conditionInfo) {
		try {
			ArrayList<FileNameScanner> result = new ArrayList<FileNameScanner>();
			for (File f : fileList) {
				String fn = f.getName();
				String[] elements = fn.split("_");
				/*
				 * H13, H31, WT sind die 3 Genotypen
				 * 01...10 sind die fortlaufenden Nummern, von jedem Genotyp 10
				 * Pflanzen
				 * 1016, 1020,...1385 sind die Carriernummern Zusammen ist das alles
				 * die Pflanzen ID (Bsp.: "H31_10_1384")
				 * RgbTopHL2, Nir_Top... sind die Snapshotnamen *
				 * Der Rest Timestamp, Datum, Uhrzeit
				 */
				// [001, 2010-03-04 16, 24, 03, LT, FLUO, Side, 90Grad.png]
				// 001_2010-03-04 16_24_03_LT_FLUO_Side_90Grad.png
				FileNameScanner s = null;
				if (optProvidedFileNameFormat != null) {
					s = new FileNameScanner(optProvidedFileNameFormat, fn);
				} else
					if (elements != null && elements.length == 1 && fn.endsWith(".jpg")) {
						// process root scan images from GED
						if (StringManipulationTools.count(fn, "-") == 2) {
							// type 2: C1-17-P1256.jpg
							s = new FileNameScanner("V-G-R", fn);
						} else
							if (StringManipulationTools.count(fn, "-") == 3) {
								// type 1: Ep2-C1-3-P2428.jpg
								s = new FileNameScanner("X-V-G-R", fn);
							}
						s.setSpecies("Barley");
					} else
						if (elements != null && elements.length == 1) {
							if (fn.endsWith("Grad.png")) {
								s = new FileNameScanner("A'Grad'", fn);
								s.setCondition("Unspecified");
								s.setSubstance("RgbSide");
							}
						} else
							if (elements != null && elements.length == 8) {
								// 001
								try {
									s = new FileNameScanner("R_D X_X_X_X_S_S_A'Grad'", fn);
								} catch (Exception er) {
									s = new FileNameScanner("G_X_R_S_D_X", fn);
								}
							} else {
								boolean transferGerste = false;
								if (transferGerste) {
									fn = StringManipulationTools.stringReplace(fn, "_Side", "Side");
									fn = StringManipulationTools.stringReplace(fn, "_Top", "Top");
									fn = StringManipulationTools.stringReplace(fn, "RGB", "Rgb");
									fn = StringManipulationTools.stringReplace(fn, "FLUO", "Fluo");
									fn = StringManipulationTools.stringReplace(fn, "NIR", "Nir");
									fn = StringManipulationTools.stringReplace(fn, ".png", "");
									s = new FileNameScanner("R_D X_X_X_X_X_S_A'Grad'", fn);
									/*
									 * G = genotype, R = replicate ID, X = ignore, A =
									 * rotation
									 * (degree), D = date (yyyy-mm-dd), 'some string' = some
									 * string (ignored, but may be used to divide strings)
									 */
								} else {
									// WT_10_1394_RgbTopHL2_2010-06-28_07_43_31.png
									//
									// H13_01_1016_Fluo_Top_2010-06-18_07_40_05.png
									// WT_04_1388_Nir_Top_2010-06-18_07_37_41.png
									// H13_01_1016_FluoSide_90_Grad_HL2_2010-06-25_07_40_02.png
									// H13_02_1020_RgbSide_0_Grad_HL2_2010-06-28_07_44_49.png
									// WT_05_1389_NirSide_0_Grad_HL2_2010-06-28_07_40_16.png
									if (fn.contains("Side_")) {
										// side view
										s = new FileNameScanner("G_X_R_S_A_X_D_X", fn);
									} else
										if (fn.contains("Top_")) {
											// top view
											s = new FileNameScanner("G_X_R_S_S_D_X", fn);
										}
								}
							}
				if (s != null) {
					if (optReplicate2conditionInfo != null && optReplicate2conditionInfo.containsKey(s.getReplicateID())) {
						Condition c = optReplicate2conditionInfo.get(s.getReplicateID());
						s.setConditionTemplate(c);
					}
					
					result.add(s);
				} else {
					System.out.println("No scanner for: " + fn);
				}
			}
			return result.toArray(new FileNameScanner[] {});
		} catch (Exception e) {
			e.printStackTrace();
			return new FileNameScanner[] {};
		}
	}
}
