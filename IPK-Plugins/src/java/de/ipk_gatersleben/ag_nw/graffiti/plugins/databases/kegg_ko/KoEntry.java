/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class KoEntry implements Comparable<KoEntry> {
	private String koentryID = "";
	private String koname = "";
	private String kodefinition = "";
	private String koclasses = "";
	
	private HashMap<String, ArrayList<String>> dbLinkId2Values = new HashMap<String, ArrayList<String>>();
	private HashMap<String, HashSet<String>> dbOrganism2Genes = new HashMap<String, HashSet<String>>();
	
	// _exists tags are by definition always exist for an entry
	private static final String entryTag_exists = "ENTRY";
	private static final String nameTag_exists = "NAME";
	private static final String definitionTag_exists = "DEFINITION";
	private static final String classTag = "CLASS";
	private static final String dblinksTag = "DBLINKS";
	private static final String genesTag = "GENES";
	public static final String endTag_exists = "///";
	
	private String lastDBlinkType = "";
	private String lastOrg = "";
	
	public static boolean isValidKoStart(String line) {
		boolean res = (line != null && line.startsWith(entryTag_exists));
		return res;
	}
	
	@Override
	public String toString() {
		return koentryID + " (" + koname + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		KoEntry objKo = (KoEntry) obj;
		return koentryID.equals(objKo.koentryID);
	}
	
	@Override
	public int hashCode() {
		return koentryID.hashCode();
	}
	
	public void processInputLine(String line) {
		if (line == null || line.length() < 3)
			return;
		
		if (line.startsWith(entryTag_exists)) {
			line = line.replace("KO", "");
			koentryID = line.substring(entryTag_exists.length()).trim();
		} else
			if (line.startsWith(nameTag_exists)) {
				koname = koname + " " + line.substring(nameTag_exists.length()).trim();
				koname = koname.trim();
			} else
				if (line.startsWith(definitionTag_exists)) {
					kodefinition = kodefinition + " " + line.substring(definitionTag_exists.length()).trim();
					kodefinition = kodefinition.trim();
					if (kodefinition.indexOf("[EC:") >= 0) {
						String ec = kodefinition.substring(kodefinition.indexOf("[EC:") + "[EC:".length());
						if (ec.indexOf("]") > 0) {
							ec = ec.substring(0, ec.indexOf("]"));
							if (!dbLinkId2Values.containsKey("EC"))
								dbLinkId2Values.put("EC", new ArrayList<String>());
							dbLinkId2Values.get("EC").add(ec);
						}
					}
				} else
					if (line.startsWith(classTag)) {
						koclasses = koclasses + " " + line.substring(classTag.length()).trim();
						koclasses = koclasses.trim();
					} else
						if (line.startsWith(dblinksTag)) {
							// lastDBlinkType EC, RN, COG, GO, ...
							/*
							 * DBLINKS RN: R00623 R00754 R01036 R01041 R04805 R04880 R06917 R06927 R07105
							 * EC: 1.1.1.1
							 * COG: COG1012 COG1062 COG1064 COG1454
							 * GO: 0004022 0004023 0004024 0004025
							 */
							// remove start string "DBLINKS"
							String dbLinks = line.substring(dblinksTag.length()).trim();
							// identify database identifier (RN, EC, COG, GO, ...)
							String dbId = "";
							if (dbLinks.indexOf(":") > 0) {
								dbId = dbLinks.substring(0, dbLinks.indexOf(":")).trim();
								lastDBlinkType = dbId;
							} else
								dbId = lastDBlinkType;
							// memorize db links information
							if (dbId.length() > 0) {
								String w1 = dbLinks.substring(dbId.length() + 1).trim();
								// process db links
								String[] dblinkvalues = w1.split(" ");
								for (String dbv : dblinkvalues) {
									if (dbv.trim().length() > 0) {
										if (!dbLinkId2Values.containsKey(dbId))
											dbLinkId2Values.put(dbId, new ArrayList<String>());
										dbLinkId2Values.get(dbId).add(dbv.trim());
									}
									
								}
							}
						} else
							if (line.startsWith(genesTag)) {
								/*
								 * GENES HSA: 124(ADH1A) 125(ADH1B) 126(ADH1C) 127(ADH4) 128(ADH5) 130(ADH6)
								 * 131(ADH7) 137872(ADHFE1)
								 * PTR: 461396(ADH1B)
								 * MMU: 11522(Adh1) 11529(Adh7) 11532(Adh5) 26876(Adh4)
								 * RNO: 171178(Adh7) 24172(Adh1) 29646(Adh4)
								 * CFA: 474946(LOC474946)
								 */
								String genes = line.substring(genesTag.length()).trim();
								// identify organism identifier (HSA, MMU, RNO, ATH, ...)
								String org = "";
								if (genes.indexOf(":") > 0) {
									org = genes.substring(0, genes.indexOf(":")).trim();
									if (org.equals(org.toUpperCase()))
										lastOrg = org.toUpperCase();
									else
										org = lastOrg;
								} else
									org = lastOrg;
								
								// memorize gene information
								if (org.length() > 0 && KoService.isSelectedOrganism(org) && ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
									String w1 = "";
									try {
										w1 = genes.substring(org.length() + 1).trim();
									} catch (StringIndexOutOfBoundsException eee) {
										ErrorMsg.addErrorMessage("A problem occured while parsing the KO file: " + w1 + " / " + org + ". " +
															"Please click Yes to inform the developer of VANTED about the problem.");
									}
									// process gene ids
									String[] genevalues = w1.split(" ");
									for (String g : genevalues) {
										if (g.trim().length() > 0) {
											if (!dbOrganism2Genes.containsKey(org))
												dbOrganism2Genes.put(org, new HashSet<String>());
											dbOrganism2Genes.get(org).add(g.trim());
											if (g.contains("(")) {
												// String gP = g.substring(g.indexOf("("));
												g = g.substring(0, g.indexOf("("));
												dbOrganism2Genes.get(org).add(g.trim());
												// if (gP.contains(")")) {
												// gP = gP.substring(0, gP.indexOf(")"));
												// dbOrganism2Genes.get(org).add(gP.trim());
												// }
											}
										}
										
									}
								}
							}
		// all other types are ignored
	}
	
	public boolean isValid() {
		return koentryID != null && koname.length() > 0;
	}
	
	public String getKoID() {
		return koentryID;
	}
	
	public String getKoName() {
		return koname;
	}
	
	public String getKoDefinition() {
		return kodefinition;
	}
	
	// public String getKoClass() { return koclasses; }
	
	public Collection<String> getKoClasses() {
		return splitClassInformation(koclasses);
	}
	
	/***
	 * Metabolism; Carbohydrate Metabolism; Glycolysis / Gluconeogenesis [PATH:ko00010] Metabolism; Lipid Metabolism; Fatty acid metabolism [PATH:ko00071]
	 * Metabolism; Lipid Metabolism; Bile acid biosynthesis [PATH:ko00120] Metabolism; Lipid Metabolism; Glycerolipid metabolism [PATH:ko00561] Metabolism; Amino
	 * Acid Metabolism; Tyrosine metabolism [PATH:ko00350] Metabolism; Xenobiotics Biodegradation and Metabolism; 1- and 2-Methylnaphthalene degradation
	 * [PATH:ko00624] Metabolism; Xenobiotics Biodegradation and Metabolism; Metabolism of xenobiotics by cytochrome P450 [PATH:ko00980] Genetic Information
	 * Processing; Translation; Ribosome [PATH:ko03010] [BR:ko03010] Genetic Information Processing; Translation; Ribosome [PATH:ko03010] [BR:ko03010]
	 * ==>
	 * Metabolism; Carbohydrate Metabolism; Glycolysis / Gluconeogenesis [PATH:ko00010]
	 * Metabolism; Lipid Metabolism; Fatty acid metabolism [PATH:ko00071]
	 * ...
	 * Genetic Information Processing; Translation; Ribosome [PATH:ko03010] [BR:ko03010]
	 */
	private Collection<String> splitClassInformation(String s) {
		ArrayList<String> result = new ArrayList<String>();
		
		// search first "]", but skip "] [",
		// this is the end of the first path definition and add it to the
		// result set
		// remove this definition from the string (start) and process
		// the remaining string as before
		while (s.indexOf("  ") > 0)
			s = StringManipulationTools.stringReplace(s, "  ", " ");
		s = StringManipulationTools.stringReplace(s, "][", "] [");
		s = StringManipulationTools.stringReplace(s, "] [", "§ [");
		while (s.length() > 0) {
			int pA = s.indexOf("]");
			if (pA <= 0) {
				s = StringManipulationTools.stringReplace(s, "§ [", "] ["); // revert change
				result.add(s.trim());
				break;
			} else {
				String path = s.substring(0, s.indexOf("]") + 1);
				path = StringManipulationTools.stringReplace(path, "§ [", "] ["); // revert change
				result.add(path.trim());
				s = s.substring(path.length());
			}
			s = s.trim();
		}
		
		// the result may be split by the ";" characters to gain the hierarchy
		return result;
	}
	
	public Collection<String> getKoDbLinks(String dbLinkId) {
		if (!dbLinkId2Values.containsKey(dbLinkId))
			return new ArrayList<String>();
		else
			return dbLinkId2Values.get(dbLinkId);
	}
	
	public Collection<String> getKoDbLinks() {
		ArrayList<String> result = new ArrayList<String>();
		for (String db : dbLinkId2Values.keySet()) {
			result.addAll(dbLinkId2Values.get(db));
		}
		return result;
	}
	
	public HashSet<String> getGeneIDs(String orgCode) {
		orgCode = orgCode.toUpperCase();
		if (!dbOrganism2Genes.containsKey(orgCode)) {
			return new HashSet<String>();
		} else
			return dbOrganism2Genes.get(orgCode);
	}
	
	public Set<String> getOrganismCodes() {
		return dbOrganism2Genes.keySet();
	}
	
	public boolean hasGeneMapping(String orgCode, String gene) {
		// orgCode = orgCode.toUpperCase();
		HashSet<String> hs = dbOrganism2Genes.get(orgCode);
		if (hs == null || hs.size() <= 0) {
			return false;
		} else
			return hs.contains(gene);
	}
	
	public int compareTo(KoEntry o) {
		return koentryID.compareTo(o.koentryID);
	}
	
	public Set<String> getOrganismCodesForGeneID(String altId) {
		HashSet<String> result = new HashSet<String>();
		for (String o : getOrganismCodes()) {
			if (hasGeneMapping(o, altId))
				result.add(o);
		}
		return result;
	}
}
