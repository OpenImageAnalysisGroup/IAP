/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JDialog;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.MapNumber;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.MapOrg;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Url;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.gui.KGMLerrorWindow;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class Pathway {
	private final KeggId name;
	private final MapOrg org;
	private final MapNumber number;
	private final String title;
	private final Url image;
	private final Url link;
	private final Collection<Entry> entries;
	private final Collection<Reaction> reactions;
	private final Collection<Relation> relations;
	
	public static void testLoadView() {
		File f = new File("/Users/klukas/kegg/map/vanted_cache_kegg_map00010.xml");
		if (!f.exists())
			f = new File("/home/klukas/kegg/map/vanted_cache_kegg_map04010dme.xml");
		// f = new File("/home/klukas/kegg/map/vanted_cache_kegg_map00010.xml");
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			Pathway p = getPathwayFromKGML(fis);
			testShowPathwayInfo(p);
			Graph g = p.getGraph();
			MainFrame.getInstance().showGraph(g, null);
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public KeggId getName() {
		return name;
	}
	
	public MapOrg getMapOrg() {
		return org;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static void testLoadConvertAll() {
		
		// Tee standard output
		PrintStream out, err;
		try {
			out = new PrintStream(new FileOutputStream("/home/klukas/kegg/batch_out.log"));
			PrintStream tee1 = new TeeStream(System.out, out);
			System.setOut(tee1);
			err = new PrintStream(new FileOutputStream("/home/klukas/kegg/batch_err.log"));
			PrintStream tee2 = new TeeStream(System.err, err);
			System.setErr(tee2);
			System.err.println("Pathway\tError-Sum\tDiff-Entries\tDiff-Reactions\tDiff-Relations\tErrors\tWarnings");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		File d = new File("/home/klukas/kegg/61/map");
		String[] files = d.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		Arrays.sort(files);
		for (String fn : files) {
			File f = new File("/home/klukas/kegg/61/map/" + fn);
			testFile2Pathway2GML2Pathway2GML(f);
		}
	}
	
	public static void testFile2Pathway2GML2Pathway2GML(File f) {
		boolean batch = f != null;
		if (f == null) {
			f = new File("/home/klukas/kegg/61/map/map04010dme.xml");
		}
		if (f == null || !f.exists()) {
			if (f != null)
				System.err.println("FILE NOT FOUND: " + f.getAbsolutePath());
			return;
		}
		if (batch) {
			System.out.println("FILE: " + f.getAbsolutePath());
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			Pathway p1;
			try {
				p1 = getPathwayFromKGML(fis);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				return;
			}
			if (!batch) {
				testShowPathwayInfo(p1);
				System.out.println("RELATIONS A: " + p1.getRelations().size());
			}
			Graph g1 = p1.getGraph();
			g1.setName(g1.getName() + " (file -> pathway -> gml)");
			if (!batch)
				MainFrame.getInstance().showGraph(g1, null);
			Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
			Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
			Pathway p2 = getPathwayFromGraph(g1, warnings, errors, null);
			if (!batch) {
				System.out.println("RELATIONS B: " + p2.getRelations().size());
			}
			for (Gml2PathwayErrorInformation error : errors)
				System.out.println("ERROR: " + error.getError() + " Affected Graph Elements: " + error.getCausingGraphElements() + " Message: "
						+ error.getDescription());
			for (Gml2PathwayWarningInformation warning : warnings)
				System.out.println("WARNING: " + warning.getWarning() + " Affected Graph Element: " + warning.getCausingGraphElement());
			if (!batch)
				testShowPathwayInfo(p2);
			Graph g2 = p2.getGraph();
			g2.setName(g2.getName() + " (file -> pathway -> gml --> pathway --> gml)");
			if (!batch)
				MainFrame.getInstance().showGraph(g2, null);
			int result = testShowPathwayComparison(p1, p2, !batch);
			int diffEntry = p2.getEntries().size() - p1.getEntries().size();
			int diffReac = p2.getReactions().size() - p1.getReactions().size();
			int diffRel = p2.getRelations().size() - p1.getRelations().size();
			System.err.println(f.getName() + "\t" + result + "\t" + diffEntry + "\t" + diffReac + "\t" + diffRel + "\t" + errors.size() + "\t" + warnings.size());
			boolean showRelationCSVinfo = false;
			if (showRelationCSVinfo) {
				// System.out.println("RELATIONS A: "+p1.getRelations().size());
				// System.out.println("RELATIONS B: "+p2.getRelations().size());
				System.out.println("------------ RELATIONS P1 ----------------");
				for (Relation r : p1.getRelations())
					System.out.println(r.getCSVline());
				System.out.println("------------ RELATIONS P2 ----------------");
				for (Relation r : p2.getRelations())
					System.out.println(r.getCSVline());
			}
		} catch (Exception e) { // FileNotFoundException
			System.err.println(f.getName() + "\t" + "" + "\t" + "" + "\t" + "" + "\t" + "" + "\t" + "" + "\t" + "" + "\tERROR:" + e.getMessage());
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static void testGML2Pathway2GML() {
		Graph g = MainFrame.getInstance().getActiveEditorSession().getGraph();
		
		Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
		Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
		
		Pathway p = getPathwayFromGraph(g, warnings, errors, null);
		testShowPathwayInfo(p);
		for (Gml2PathwayErrorInformation error : errors)
			System.err.println("ERROR: " + error.getError() + " Affected Graph Elements: " + error.getCausingGraphElements());
		for (Gml2PathwayWarningInformation warning : warnings)
			System.err.println("WARNING: " + warning.getWarning() + " Affected Graph Element: " + warning.getCausingGraphElement());
		
		Graph g2 = p.getGraph();
		Pathway p2 = getPathwayFromGraph(g2, warnings, errors, null);
		testShowPathwayInfo(p2);
		g2.setName(g2.getName() + " (gml --> pathway --> gml)");
		MainFrame.getInstance().showGraph(g2, null);
	}
	
	public static void testShowPathwayInfo(Pathway p) {
		String entriesDesc = testGetEntryTypes(p.entries);
		BackgroundTaskHelper.showMessage(
				"<html>Pathway: " + p.entries.size() + " entries,<br>" + testGetReactionDetailInfos(p.reactions) + ", " + p.relations.size()
						+ " relations",
				"<html>Entries:<br>" + entriesDesc);
	}
	
	/**
	 * Compare two pathways
	 * 
	 * @param p1
	 *           Pathway 1
	 * @param p2
	 *           Pathway 2
	 * @param showMessage
	 *           If true, the differences will be shown in a task panel
	 * @return Number of differences, 0 if p1 and p2 appear to be the same.
	 */
	public static int testShowPathwayComparison(Pathway p1, Pathway p2, boolean showMessage) {
		int differences = 0;
		HashSet<String> entryIDsA = new HashSet<String>();
		HashSet<String> entryIDsB = new HashSet<String>();
		HashSet<String> reactionIDsA = new HashSet<String>();
		HashSet<String> reactionIDsB = new HashSet<String>();
		HashSet<String> reactionDetailsA = new HashSet<String>();
		HashSet<String> reactionDetailsB = new HashSet<String>();
		HashSet<String> relationDetailsA = new HashSet<String>();
		HashSet<String> relationDetailsB = new HashSet<String>();
		
		for (Entry e : p1.getEntries())
			entryIDsA.add(e.getName().getId());
		for (Entry e : p2.getEntries())
			entryIDsB.add(e.getName().getId());
		for (Reaction r : p1.getReactions())
			reactionIDsA.add(r.getId());
		for (Reaction r : p2.getReactions())
			reactionIDsB.add(r.getId());
		for (Reaction r : p1.getReactions())
			reactionDetailsA.add(r.toStringWithDetails(false, false));
		for (Reaction r : p2.getReactions())
			reactionDetailsB.add(r.toStringWithDetails(false, false));
		for (Relation r : p1.getRelations())
			relationDetailsA.add(r.getType().toString() + " / " + r.toStringWithKeggNames());
		for (Relation r : p2.getRelations())
			relationDetailsB.add(r.getType().toString() + " / " + r.toStringWithKeggNames());
		
		differences += Math.abs(p1.getEntries().size() - p2.getEntries().size());
		differences += Math.abs(entryIDsA.size() - entryIDsB.size());
		differences += Math.abs(p1.getReactions().size() - p2.getReactions().size());
		differences += Math.abs(reactionIDsA.size() - reactionIDsB.size());
		differences += Math.abs(reactionDetailsA.size() - reactionDetailsB.size());
		differences += Math.abs(p1.getRelations().size() - p2.getRelations().size());
		differences += Math.abs(relationDetailsA.size() - relationDetailsB.size());
		
		if (differences == 0)
			return differences;
		
		String lineBreak;
		if (showMessage)
			lineBreak = "<br>";
		else
			lineBreak = "\n";
		
		String missingInB =
				getMissingDetails("Entry IDs", entryIDsA, entryIDsB, lineBreak) +
						getMissingDetails("Reaction IDs", reactionIDsA, reactionIDsB, lineBreak) +
						getMissingDetails("Reaction Details", reactionDetailsA, reactionDetailsB, lineBreak) +
						getMissingDetails("Relation Details", relationDetailsA, relationDetailsB, lineBreak) +
						getMissingDetailCount("Entry/Entries", p1.getEntries().size(), p2.getEntries().size(), lineBreak) +
						getMissingDetailCount("Reaction(s)", p1.getReactions().size(), p2.getReactions().size(), lineBreak) +
						getMissingDetailCount("Relation(s)", p1.getRelations().size(), p2.getRelations().size(), lineBreak);
		String addedInB =
				getAddedDetails("Entry IDs", entryIDsA, entryIDsB, lineBreak) +
						getAddedDetails("Reaction IDs", reactionIDsA, reactionIDsB, lineBreak) +
						getAddedDetails("Reaction Details", reactionDetailsA, reactionDetailsB, lineBreak) +
						getAddedDetails("Relation Details", relationDetailsA, relationDetailsB, lineBreak) +
						getAddedDetailCount("Entry/Entries", p1.getEntries().size(), p2.getEntries().size(), lineBreak) +
						getAddedDetailCount("Reaction(s)", p1.getReactions().size(), p2.getReactions().size(), lineBreak) +
						getAddedDetailCount("Relation(s)", p1.getRelations().size(), p2.getRelations().size(), lineBreak);
		
		if (showMessage) {
			MainFrame.showMessageDialogWithScrollBars("<html><b>Missing in Pathway 2:</b><br>" + missingInB +
					"<hr><b>Added in Pathway 2:</b><br>" + addedInB, "Pathway Comparison");
		} else {
			System.out.println("### Missing in Pathway 2: ###\n" + missingInB +
					"\n### Added in Pathway 2: ###\n" + addedInB);
		}
		return differences;
	}
	
	private static String getMissingDetails(String desc, HashSet<String> infoA, HashSet<String> infoB, String lineBreak) {
		StringBuilder missing = new StringBuilder();
		int miss = 0;
		for (String iA : infoA) {
			if (!infoB.contains(iA)) {
				missing.append(iA + ", ");
				miss++;
			}
		}
		if (missing.length() == 0)
			return ""; // "No differences for "+desc+" "+infoA.size()+" --> "+infoB.size()+lineBreak;
		else {
			return "### " + miss + " missing " + desc + " ###" + lineBreak + missing.toString().substring(0, missing.length() - ", ".length()) + lineBreak;
		}
	}
	
	private static String getMissingDetailCount(String desc, int infoA, int infoB, String lineBreak) {
		if (infoA >= infoB)
			return "";
		else {
			return "### " + Math.abs(infoA - infoB) + " " + desc + " ###" + lineBreak + infoA + " --> " + infoB + lineBreak;
		}
	}
	
	private static String getAddedDetailCount(String desc, int infoA, int infoB, String lineBreak) {
		if (infoA <= infoB)
			return "";
		else {
			return "### " + Math.abs(infoA - infoB) + " added " + desc + " ###" + lineBreak + infoA + " --> " + infoB + lineBreak;
		}
	}
	
	private static String getAddedDetails(String desc, HashSet<String> infoA, HashSet<String> infoB, String lineBreak) {
		StringBuilder added = new StringBuilder();
		int add = 0;
		for (String iB : infoB) {
			if (!infoA.contains(iB)) {
				added.append(iB + ", ");
				add++;
			}
		}
		if (added.length() == 0)
			return ""; // "No differences for "+desc+" ("+infoA.size()+" --> "+infoB.size()+")"+lineBreak;
		else {
			return "### " + add + " added (" + desc + " / " + infoA.size() + " --> " + infoB.size() + ") ###" + lineBreak
					+ added.toString().substring(0, added.length() - ", ".length()) + lineBreak;
		}
	}
	
	public Collection<Entry> getEntries() {
		return entries;
	}
	
	public Collection<Reaction> getReactions() {
		return reactions;
	}
	
	public Collection<Relation> getRelations() {
		return relations;
	}
	
	private static String testGetReactionDetailInfos(Collection<Reaction> reactions) {
		int numSub = 0;
		int numProd = 0;
		for (Reaction r : reactions) {
			numSub += r.getSubstrates().size();
			numProd += r.getSubstrates().size();
		}
		return reactions.size() + " reactions (" + numSub + " subst., " + numProd + " prod.)";
	}
	
	private static String testGetEntryTypes(Collection<Entry> entries) {
		int ortholog = 0, enzyme = 0, gene = 0, genes = 0, compound = 0, map = 0, group = 0; // genes -> group v0.6 -> 0.6.1
		for (Entry e : entries) {
			if (e.getType() == EntryType.ortholog)
				ortholog++;
			if (e.getType() == EntryType.enzyme)
				enzyme++;
			if (e.getType() == EntryType.gene)
				gene++;
			if (e.getType() == EntryType.genes)
				genes++;
			if (e.getType() == EntryType.group)
				group++;
			if (e.getType() == EntryType.compound)
				compound++;
			if (e.getType() == EntryType.map)
				map++;
		}
		return "ortholog: " + ortholog + "<br>" +
				"enzyme: " + enzyme + "<br>" +
				"gene: " + gene + "<br>" +
				"genes: " + genes + "<br>" +
				"group: " + group + "<br>" +
				"compound: " + compound + "<br>" +
				"map: " + map;
	}
	
	public Pathway(
			KeggId name,
			MapOrg org,
			MapNumber number,
			String title,
			Url image,
			Url link,
			Collection<Entry> entries,
			Collection<Reaction> reactions,
			Collection<Relation> relations) {
		// assert name!=null;
		// assert org!=null;
		// assert number!=null;
		
		this.name = name;
		this.org = org;
		this.number = number;
		
		this.title = title;
		this.image = image;
		this.link = link;
		
		this.entries = entries;
		this.reactions = reactions;
		this.relations = relations;
	}
	
	public static Pathway getPathwayFromKGML(Element kgmlRoot) {
		return getPathwayFromKgmlRootElement(kgmlRoot);
	}
	
	public static Pathway getPathwayFromKGML(InputStream in) {
		Pathway result = null;
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Document doc;
			try {
				TextFile tf = new TextFile(in, 0);
				int idx = 0;
				for (String s : tf) {
					if (s.contains("DOCTYPE")) {
						tf.remove(idx);
						break;
					}
					idx++;
				}
				MyByteArrayOutputStream bos = new MyByteArrayOutputStream();
				tf.write(bos);
				MyByteArrayInputStream in2 = new MyByteArrayInputStream(bos.getBuffTrimmed());
				builder.setValidation(false);
				builder.setExpandEntities(false);
				doc = builder.build(in2);
				Element kgmlRoot = doc.getRootElement();
				result = getPathwayFromKgmlRootElement(kgmlRoot);
			} catch (JDOMException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
		}
		return result;
	}
	
	public static Pathway getPathwayFromKGML(Reader in) {
		Pathway result = null;
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Document doc;
			try {
				builder.setValidation(false);
				builder.setExpandEntities(false);
				doc = builder.build(in);
				Element kgmlRoot = doc.getRootElement();
				result = getPathwayFromKgmlRootElement(kgmlRoot);
			} catch (JDOMException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
		}
		return result;
	}
	
	private static Pathway getPathwayFromKgmlRootElement(Element kgmlRoot) {
		String nameAttributeValue = KGMLhelper.getAttributeValue(kgmlRoot, "name", null);
		String orgAttributeValue = KGMLhelper.getAttributeValue(kgmlRoot, "org", null);
		String numberAttributeValue = KGMLhelper.getAttributeValue(kgmlRoot, "number", null);
		String titleAttributeValue = KGMLhelper.getAttributeValue(kgmlRoot, "title", null);
		String imageAttributeValue = KGMLhelper.getAttributeValue(kgmlRoot, "image", null);
		String linkAttributeValue = KGMLhelper.getAttributeValue(kgmlRoot, "link", null);
		
		assert nameAttributeValue != null;
		assert orgAttributeValue != null;
		assert numberAttributeValue != null;
		
		KeggId name = new KeggId(nameAttributeValue);
		MapOrg org = new MapOrg(orgAttributeValue);
		MapNumber number = new MapNumber(numberAttributeValue);
		String title = titleAttributeValue;
		Url image = null;
		if (imageAttributeValue != null)
			image = new Url(imageAttributeValue);
		Url link = null;
		if (linkAttributeValue != null)
			link = new Url(linkAttributeValue);
		
		Collection<Entry> entries = getEntriesFromKgmlRootElement(kgmlRoot, nameAttributeValue);
		Collection<Relation> relations = getRelationsFromKgmlRootElement(kgmlRoot, entries, nameAttributeValue);
		Collection<Reaction> reactions = getReactionsFromKgmlRootElement(kgmlRoot, entries, nameAttributeValue);
		
		Pathway result = new Pathway(name, org, number, title, image, link, entries, reactions, relations);
		name.setReference(result);
		return result;
	}
	
	private static Collection<Entry> getEntriesFromKgmlRootElement(
			Element kgmlRoot, String sourcePathwayId) {
		Collection<Entry> result = new ArrayList<Entry>();
		List<?> entryElements = kgmlRoot.getChildren("entry");
		Collection<IdRef> mapLinksWhichNeedToBeUpdated = new ArrayList<IdRef>();
		Collection<IdRef> componentsWhichNeedToBeUpdated = new ArrayList<IdRef>();
		// System.out.println("XML Entry Count: "+entryElements.size());
		for (Object o : entryElements) {
			Element entryElement = (Element) o;
			try {
				Entry e = Entry.getEntryFromKgmlEntryElement(
						mapLinksWhichNeedToBeUpdated,
						componentsWhichNeedToBeUpdated,
						entryElement,
						sourcePathwayId);
				result.add(e);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		updateReferences(result, mapLinksWhichNeedToBeUpdated, componentsWhichNeedToBeUpdated);
		return result;
	}
	
	private static void updateReferences(Collection<Entry> entries, Collection<IdRef> mapLinksWhichNeedToBeUpdated,
			Collection<IdRef> componentsWhichNeedToBeUpdated) {
		HashMap<String, Entry> id2entry = new HashMap<String, Entry>();
		for (Entry e : entries)
			id2entry.put(e.getId().getValue(), e);
		
		// update map links with corresponding entry reference
		for (IdRef ir : mapLinksWhichNeedToBeUpdated) {
			Entry e = id2entry.get(ir.getValue());
			if (e != null && e.getType() == EntryType.map) {
				ir.setRef(e);
			} else {
				ErrorMsg.addErrorMessage("Map Link reference " + ir.getValue() + " could not processed correctly. " +
						"Corresponding map entry not found (map not found or entry not of type map).");
			}
		}
		// update Entry-Components IdRef values with entry reference
		for (IdRef component : componentsWhichNeedToBeUpdated) {
			String entryId = component.getValue();
			Entry entryRef = id2entry.get(entryId);
			if (entryRef != null) {
				component.setRef(entryRef);
				entryRef.setIsPartOfGroup(true);
			} else {
				ErrorMsg.addErrorMessage("Component reference " + entryId + " could not processed correctly. " +
						"Corresponding entry not found.");
			}
		}
	}
	
	private static Collection<Relation> getRelationsFromKgmlRootElement(
			Element kgmlRoot,
			Collection<Entry> entryElements, String clusterIdForHiddenCompounds) {
		Collection<Relation> result = new ArrayList<Relation>();
		List<?> relationElements = kgmlRoot.getChildren("relation");
		for (Object o : relationElements) {
			Element relationElement = (Element) o;
			Relation r = Relation.getRelationFromKgmlRelationElement(relationElement, entryElements, clusterIdForHiddenCompounds);
			result.add(r);
		}
		return result;
	}
	
	private static Collection<Reaction> getReactionsFromKgmlRootElement(
			Element kgmlRoot,
			Collection<Entry> entryElements,
			String clusterIdForHiddenCompounds) {
		Collection<Reaction> result = new ArrayList<Reaction>();
		HashMap<Reaction, String> hmReactionElementID = new HashMap<Reaction, String>();
		List<?> reactionElements = kgmlRoot.getChildren("reaction");
		for (Object o : reactionElements) {
			Element reactionElement = (Element) o;
			Reaction r = Reaction.getReactionFromKgmlReactionElement(reactionElement, entryElements, clusterIdForHiddenCompounds);
			if (r != null) {
				result.add(r);
				// store reaction element id from KGML file
				if (KGMLhelper.getAttributeValue(reactionElement, "id", null) != null)
					hmReactionElementID.put(r, KGMLhelper.getAttributeValue(reactionElement, "id", null));
			}
		}
		// update corresponding reaction element references in entry elements!!!
		for (Entry e : entryElements) {
			if (e.getReactions() != null)
				for (KeggId reactionRef : e.getReactions()) {
					for (Reaction r : result) {
						if (r.getId().equals(reactionRef.getId())) {
							reactionRef.setReference(r);
						} else
							// in KGML entry one can have an attribute reaction="rn:R####1 rn:R####2"
							// this is stored as two ids (in reactionRef.getId(), two KEGG ids) rn:R####1 and rn:R####2
							// in KGML file the according reaction has an attribute name="rn:R####1 rn:R####2"
							// this is stored as one id (in r.getId()) rn:R####1 rn:R####2
							// equal does not work here
							// this kind of entries and reactions can occur several times
							// compare also entry element id and reaction element id from KGML to get the right reference
							if (r.getId().contains(reactionRef.getId()) &&
									hmReactionElementID.get(r) != null && hmReactionElementID.get(r).equals(e.getId().getValue()))
								reactionRef.setReference(r);
					}
				}
		}
		return result;
	}
	
	/**
	 * Use <code>getPathwayFromKGML</code> to read a KGML document and to create pathway object.
	 * Use this method to create KGML document from a pathway object.
	 * 
	 * @return A JDOM XML Document containing the pathway information of this object.
	 */
	public Document getKgmlDocument() {
		Element pathwayElement = getKgmlPathwayElement();
		Document doc = new Document(pathwayElement);
		return doc;
	}
	
	private Element getKgmlPathwayElement() {
		Element pathwayElement = new Element("pathway");
		KGMLhelper.addNewAttribute(pathwayElement, "name", (name == null ? "" : name.getId()));
		KGMLhelper.addNewAttribute(pathwayElement, "org", (org == null ? "" : org.toString()));
		KGMLhelper.addNewAttribute(pathwayElement, "number", (number == null ? "" : number.toString()));
		KGMLhelper.addNewAttribute(pathwayElement, "title", (title == null ? "" : title));
		KGMLhelper.addNewAttribute(pathwayElement, "image", (image == null ? "" : image.toString()));
		KGMLhelper.addNewAttribute(pathwayElement, "link", (link == null ? "" : link.toString()));
		
		// HashSet<String> sourcePathways = new HashSet<String>();
		if (entries != null) {
			/*
			 * for (Entry e : entries) {
			 * if (e.getMapRef()==null || e.getMapRef().getValue()==null || e.getMapRef().getValue().length()<=0)
			 * continue;
			 * String id = e.getSourcePathwayKeggId();
			 * if (id!=null && id.length()>0)
			 * sourcePathways.add(id);
			 * }
			 */
			// boolean writeSourcePathwayInfo = sourcePathways.size()>1;
			for (Entry e : entries) {
				Element entryElement = e.getKgmlEntryElement(true); // writeSourcePathwayInfo
				if (entryElement != null)
					pathwayElement.addContent(entryElement);
			}
		}
		if (relations != null)
			for (Relation r : relations) {
				Element relationElement = r.getKgmlRelationElement();
				if (relationElement != null)
					pathwayElement.addContent(relationElement);
			}
		if (reactions != null)
			for (Reaction r : reactions) {
				Element reactionElement = r.getKgmlReactionElement();
				if (reactionElement != null)
					pathwayElement.addContent(reactionElement);
			}
		return pathwayElement;
	}
	
	public Graph getGraph() {
		Graph graph = new AdjListGraph(new ListenerManager());
		getGraph(graph);
		return graph;
	}
	
	public void getGraph(Graph graph) {
		graph.getListenerManager().transactionStarted(this);
		try {
			getGraphImpl(graph);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private void getGraphImpl(Graph graph) {
		KeggGmlHelper.setKeggId(graph, name.getId());
		KeggGmlHelper.setKeggOrg(graph, org.toString());
		KeggGmlHelper.setKeggMapNumber(graph, number.toString());
		if (title != null)
			KeggGmlHelper.setKeggTitle(graph, title);
		if (image != null)
			KeggGmlHelper.setKeggImageUrl(graph, image.toString());
		if (link != null)
			KeggGmlHelper.setKeggLinkUrl(graph, link.toString());
		graph.setName(prettify(getGroup() + title + " - " + org + number));
		HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
		
		// some nodes with no graphics information are nodes which appear in maps,
		// which are linked from this pathway
		// for these nodes no graph node is created, instead the graph node for
		// a linked map contains additional attributes, which store the information about
		// nodes in that pathway
		ArrayList<Entry> referencedEntries = new ArrayList<Entry>();
		for (Entry e : entries) {
			if (e.getMapRef() == null) {
				Node n = e.addGraphNode(graph);
				if (n != null)
					entry2graphNode.put(e, n);
			} else {
				// this is a node which appears in a linked pathway
				// instead of adding a graph node,
				// the information about this entry needs to be assigned to and stored in a graph node,
				// which represents the referenced map
				referencedEntries.add(e);
			}
		}
		
		// add attributes to graph nodes representing a map link
		// these attributes represent and store information about "hidden" entry nodes (with no graphics information)
		ArrayList<Entry> mapEntries = new ArrayList<Entry>();
		for (Entry re : referencedEntries) {
			IdRef mapRef = re.getMapRef();
			Entry map = mapRef.getRef();
			if (map != null)
				mapEntries.add(map);
		}
		for (Entry map : mapEntries) {
			Node mapNode = entry2graphNode.get(map);
			if (mapNode == null) {
				ErrorMsg.addErrorMessage("No graph node for map with map id " + map.getId() + ": " + map.getVisibleName());
			} else {
				int idx = 0;
				for (Entry re : referencedEntries) {
					if (re.getMapRef().getRef() == map) {
						if (re.getType() != null) {
							String idValue = re.getName().getId();
							KeggGmlHelper.setKeggId(mapNode, idValue, idx);
							KeggGmlHelper.setKeggType(mapNode, idx, re.getType().toString());
							if (re.getLink() != null)
								KeggGmlHelper.setKeggLinkUrl(mapNode, idx, re.getLink().toString());
							idx++;
						}
					}
				}
			}
			
		}
		
		graph.numberGraphElements();
		TreeMap<NodeCombination, ArrayList<Relation>> desiredNodeCombination2RelationList = new TreeMap<NodeCombination, ArrayList<Relation>>();
		TreeMap<NodeCombination, ArrayList<ReactionAndInfo>> desiredNodeCombination2ReactionList = new TreeMap<NodeCombination, ArrayList<ReactionAndInfo>>();
		for (Relation r : relations) {
			Collection<NodeCombination> ncs = r.getDesiredNodeCombinations(entry2graphNode, entries);
			for (NodeCombination nc : ncs) {
				if (!desiredNodeCombination2RelationList.containsKey(nc))
					desiredNodeCombination2RelationList.put(nc, new ArrayList<Relation>());
				desiredNodeCombination2RelationList.get(nc).add(r);
			}
		}
		for (Reaction r : reactions) {
			Collection<NodeCombination> ncs = r.getDesiredNodeCombinations(entry2graphNode, entries, relations, desiredNodeCombination2RelationList.keySet());
			for (NodeCombination nc : ncs) {
				if (!desiredNodeCombination2ReactionList.containsKey(nc))
					desiredNodeCombination2ReactionList.put(nc, new ArrayList<ReactionAndInfo>());
				boolean isReactionProductRequest = nc.isReactionProductReq();
				boolean isReactionSubstrateRequest = nc.isReactionSubstrateReq();
				String substId = KeggGmlHelper.getKeggId(nc.getNodeA());
				String prodId = KeggGmlHelper.getKeggId(nc.getNodeB());
				ReactionAndInfo ri =
						new ReactionAndInfo(
								r,
								isReactionProductRequest,
								isReactionSubstrateRequest,
								substId,
								prodId);
				desiredNodeCombination2ReactionList.get(nc).add(ri);
			}
		}
		graph.numberGraphElements();
		TreeSet<NodeCombination> allNCs = new TreeSet<NodeCombination>();
		allNCs.addAll(desiredNodeCombination2RelationList.keySet());
		allNCs.addAll(desiredNodeCombination2ReactionList.keySet());
		Stack<NodeCombination> workStack = new Stack<NodeCombination>();
		workStack.addAll(allNCs);
		while (!workStack.empty()) {
			NodeCombination nc = workStack.pop();
			// System.out.println(nc.toString());
			Node a = nc.getNodeA();
			Node b = nc.getNodeB();
			NodeCombination del = null;
			// search remaining node Combinations for inverse combination and delete it subsequently
			for (Enumeration<NodeCombination> e = workStack.elements(); e.hasMoreElements();) {
				NodeCombination n = e.nextElement();
				if (n.getNodeA() == b && n.getNodeB() == a) {
					del = n;
					break;
				}
			}
			ArrayList<ReactionAndInfo> reactionsRequestingThisInverseNodeCombination = null;
			ArrayList<Relation> relationsRequestingThisInverseNodeCombination = null;
			if (del != null) {
				relationsRequestingThisInverseNodeCombination = desiredNodeCombination2RelationList.get(del);
				reactionsRequestingThisInverseNodeCombination = desiredNodeCombination2ReactionList.get(del);
				workStack.removeElement(del);
			}
			
			ArrayList<ReactionAndInfo> reactionsRequestingThisNodeCombination = desiredNodeCombination2ReactionList.get(nc);
			ArrayList<Relation> relationsRequestingThisNodeCombination = desiredNodeCombination2RelationList.get(nc);
			
			boolean dirA = false;
			boolean dirB = false;
			if (relationsRequestingThisNodeCombination != null)
				for (Relation rel : relationsRequestingThisNodeCombination) {
					if (rel.isDirectedRelationDefinedBySubtypes())
						dirA = true;
				}
			if (relationsRequestingThisInverseNodeCombination != null)
				for (Relation rel : relationsRequestingThisInverseNodeCombination) {
					if (rel.isDirectedRelationDefinedBySubtypes())
						dirB = true;
				}
			
			if (reactionsRequestingThisNodeCombination != null)
				for (ReactionAndInfo ri : reactionsRequestingThisNodeCombination) {
					if (ri.getReaction() != null && ri.getReaction().getType() == ReactionType.irreversible)
						if (ri.isProductReq())
							dirA = true;
					if (ri.getReaction() != null && ri.getReaction().getType() == ReactionType.reversible) {
						if (ri.isProductReq())
							dirA = true;
						if (ri.isSubstrateReq())
							dirB = true;
					}
				}
			if (reactionsRequestingThisInverseNodeCombination != null)
				for (ReactionAndInfo ri : reactionsRequestingThisInverseNodeCombination) {
					if (ri.getReaction() != null && ri.getReaction().getType() == ReactionType.irreversible)
						if (ri.isProductReq())
							dirB = true;
					if (ri.getReaction() != null && ri.getReaction().getType() == ReactionType.reversible) {
						if (ri.isProductReq())
							dirB = true;
						if (ri.isSubstrateReq())
							dirA = true;
					}
				}
			boolean directed = ((dirA || dirB) && !(dirA && dirB));
			if (dirB && !dirA) {
				Node t = a;
				a = b;
				b = t;
			}
			Edge e = graph.addEdge(a, b, directed, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, directed));
			
			Reaction.processEdgeReactionInformation(e, reactionsRequestingThisNodeCombination, reactionsRequestingThisInverseNodeCombination);
			
			ArrayList<Relation> relReq = new ArrayList<Relation>();
			if (relationsRequestingThisNodeCombination != null)
				relReq.addAll(relationsRequestingThisNodeCombination);
			if (relationsRequestingThisInverseNodeCombination != null)
				relReq.addAll(relationsRequestingThisInverseNodeCombination);
			Relation.processEdgeRelationInformation(e, relReq);
		}
		graph.numberGraphElements();
		for (Node node : graph.getNodes()) {
			String type = KeggGmlHelper.getKeggType(node);
			if (type.equals("hidden compound"))
				processDefaultNodePosition(node);
		}
		graph.setModified(false);
	}
	
	private String prettify(String s) {
		if (s != null) {
			s = StringManipulationTools.stringReplace(s, "/", "_");
		}
		return s;
	}
	
	private String getGroup() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "";
		StringBuilder sb = new StringBuilder();
		String[] group = KeggHelper.getGroupFromMapNumber(number.toString(), title);
		if (group != null && group.length > 0) {
			int idx = 0;
			int max = group.length - 1;
			for (String g : group) {
				if (idx == max)
					break;
				sb.append(g + ".");
				idx++;
			}
		}
		return sb.toString();
	}
	
	private void processDefaultNodePosition(Node node) {
		if (node.getDegree() >= 2) {
			Vector2d ctr = NodeTools.getCenter(node.getNeighbors());
			KeggGmlHelper.setKeggGraphicsX(node, (int) ctr.x);
			KeggGmlHelper.setKeggGraphicsY(node, (int) ctr.y);
		} else
			if (node.getDegree() == 1) {
				Node n = node.getNeighbors().iterator().next();
				KeggGmlHelper.setKeggGraphicsX(node, KeggGmlHelper.getKeggGraphicsX(n) - 50);
				KeggGmlHelper.setKeggGraphicsY(node, KeggGmlHelper.getKeggGraphicsY(n));
			}
	}
	
	public static Pathway getPathwayFromGraph(
			Graph graph,
			Collection<Gml2PathwayWarningInformation> warnings,
			Collection<Gml2PathwayErrorInformation> errors,
			HashMap<Entry, Node> entry2graphNode) {
		KeggId name = null;
		MapOrg org = null;
		MapNumber number = null;
		
		String nameValue = KeggGmlHelper.getKeggId(graph);
		if (nameValue == null || nameValue.length() <= 0)
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.PATHWAY_ID_MISSING, graph));
		else
			name = new KeggId(nameValue);
		
		String orgValue = KeggGmlHelper.getKeggOrg(graph);
		if (orgValue == null || orgValue.length() <= 0)
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.PATHWAY_ORG_MISSING, graph));
		else
			org = new MapOrg(orgValue);
		
		String numberValue = KeggGmlHelper.getKeggMapNumber(graph);
		if (numberValue == null || numberValue.length() <= 0)
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.MAP_NUMBER_MISSING, graph));
		else
			number = new MapNumber(numberValue);
		
		String title = null;
		Url imageUrl = null;
		Url linkUrl = null;
		
		String titleValue = KeggGmlHelper.getKeggTitle(graph);
		if (titleValue == null || titleValue.length() <= 0)
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.PATHWAY_TITLE_MISSING, graph));
		else
			title = titleValue;
		
		String imageUrlValue = KeggGmlHelper.getKeggImageUrl(graph);
		if (imageUrlValue == null || imageUrlValue.length() <= 0)
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.PATHWAY_IMAGEURL_MISSING, graph));
		else
			imageUrl = new Url(imageUrlValue);
		
		String linkUrlValue = KeggGmlHelper.getKeggLinkUrl(graph);
		if (linkUrlValue == null || linkUrlValue.length() <= 0)
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.PATHWAY_LINKURL_MISSING, graph));
		else
			linkUrl = new Url(linkUrlValue);
		
		Collection<Entry> entries = null;
		Collection<Reaction> reactionsWithEnzymes, reactionsWithoutEnzymes = null;
		Collection<Relation> relations = null;
		
		KgmlIdGenerator idGenerator = new KgmlIdGenerator();
		
		List<Node> graphNodes = graph.getNodes();
		entries = Entry.getEntryElementsFromGraphNodes(idGenerator, graphNodes, warnings, errors, entry2graphNode);
		if (entries == null || entries.size() <= 0)
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.NO_ENTRY_ELEMENTS_DEFINED, graph));
		
		reactionsWithEnzymes = Reaction.getReactionElementsFromGraphNodes(entries, graphNodes, warnings, errors);
		Collection<Edge> graphEdges = graph.getEdges();
		reactionsWithoutEnzymes = Reaction.getReactionElementsFromGraphEdges(entries, graphEdges, warnings, errors);
		if (reactionsWithEnzymes == null || reactionsWithoutEnzymes == null ||
				(reactionsWithEnzymes.size() + reactionsWithoutEnzymes.size()) <= 0)
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.NO_REACTION_ELEMENTS_DEFINED, graph));
		Collection<Reaction> reactions = new ArrayList<Reaction>();
		reactions.addAll(reactionsWithEnzymes);
		reactions.addAll(reactionsWithoutEnzymes);
		reactionsWithEnzymes = null;
		reactionsWithoutEnzymes = null;
		
		relations = Relation.getRelationElementsFromGraph(entries, graph, warnings, errors);
		if (relations == null || relations.size() <= 0)
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.NO_RELATION_ELEMENTS_DEFINED, graph));
		
		Pathway pathway = new Pathway(name, org, number, title, imageUrl, linkUrl, entries, reactions, relations);
		return pathway;
	}
	
	public Collection<Reaction> findReaction(String reactionId) {
		ArrayList<Reaction> result = new ArrayList<Reaction>();
		if (reactions != null)
			for (Reaction r : reactions) {
				if (r.getId().equals(reactionId))
					result.add(r);
			}
		return result;
	}
	
	public Collection<Reaction> findReaction(Collection<String> reactionIds) {
		ArrayList<Reaction> result = new ArrayList<Reaction>();
		if (reactions != null)
			for (Reaction r : reactions) {
				if (reactionIds.contains(r.getId()))
					result.add(r);
			}
		return result;
	}
	
	public Collection<Relation> findRelations(String relationSrcTgtIds) {
		Collection<Relation> result = new ArrayList<Relation>();
		if (relationSrcTgtIds == null || relationSrcTgtIds.indexOf("/") <= 0)
			return result;
		String srcId = relationSrcTgtIds.substring(0, relationSrcTgtIds.indexOf("/"));
		String tgtId = relationSrcTgtIds.substring(relationSrcTgtIds.indexOf("/") + 1);
		if (relations != null)
			for (Relation r : relations) {
				if (r.getSourceEntry().getName().getId().equals(srcId) &&
						r.getTargetEntry().getName().getId().equals(tgtId))
					result.add(r);
			}
		return result;
	}
	
	public static void showKgmlErrors(Collection<Gml2PathwayErrorInformation> errors, Collection<Gml2PathwayWarningInformation> warnings) {
		JDialog errorWindow = new KGMLerrorWindow(warnings, errors);
		errorWindow.setLocationRelativeTo(MainFrame.getInstance());
		errorWindow.setVisible(true);
	}
	
	private static KgmlIdGenerator idGen = new KgmlIdGenerator();
	
	public static int getNextID() {
		return idGen.getNextID();
	}
	
	public static void resetIdGen() {
		idGen.reset();
	}
	
	public Collection<Entry> findMultipleEntries(ArrayList<Entry> validEntries) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		HashMap<String, ArrayList<Entry>> id2entryList = new HashMap<String, ArrayList<Entry>>();
		for (Entry e : entries) {
			if (validEntries != null && validEntries.size() > 0 && !validEntries.contains(e))
				continue;
			String id = e.getName().getId();
			if (!id2entryList.containsKey(id))
				id2entryList.put(id, new ArrayList<Entry>());
			id2entryList.get(id).add(e);
		}
		for (ArrayList<Entry> listOfEntriesWithSameId : id2entryList.values()) {
			if (listOfEntriesWithSameId.size() > 1) {
				result.addAll(listOfEntriesWithSameId);
			}
		}
		return result;
	}
	
	public void mergeMultipleEntriesOfType(EntryType et, ArrayList<Entry> validEntries) {
		// 1. enumerate entries and ids
		HashMap<String, ArrayList<Entry>> id2entryList = new HashMap<String, ArrayList<Entry>>();
		for (Entry e : entries) {
			if (validEntries != null && validEntries.size() > 0 && !validEntries.contains(e))
				continue;
			if (et == null || (et == e.getType())) {
				String id = e.getName().getId();
				if (!id2entryList.containsKey(id))
					id2entryList.put(id, new ArrayList<Entry>());
				id2entryList.get(id).add(e);
			}
		}
		// 2. for each id with more than one possible entry,
		// multiple entries will be removed
		HashMap<Entry, Entry> invalidEntry2newEntry = new HashMap<Entry, Entry>();
		for (ArrayList<Entry> listOfEntriesWithSameId : id2entryList.values()) {
			if (listOfEntriesWithSameId.size() > 1) {
				Entry firstEntry = listOfEntriesWithSameId.get(0);
				for (int i = 1; i < listOfEntriesWithSameId.size(); i++) {
					Entry notNeededEntry = listOfEntriesWithSameId.get(i);
					if (notNeededEntry.getReactions() != null)
						for (KeggId rid : notNeededEntry.getReactions())
							firstEntry.addReaction(rid);
					if (notNeededEntry.getComponents() != null)
						for (IdRef cid : notNeededEntry.getComponents())
							firstEntry.addComponent(cid);
					entries.remove(notNeededEntry);
					invalidEntry2newEntry.put(notNeededEntry, firstEntry);
				}
			}
		}
		for (Entry e : entries)
			if (e.getMapRef() != null && invalidEntry2newEntry.keySet().contains(e.getMapRef().getRef())) {
				Entry newEntry = invalidEntry2newEntry.get(e.getMapRef().getRef());
				e.setMapRef(new IdRef(newEntry, newEntry.getId().getValue()));
			}
		// multiple entries are removed, update all references to these entries, and
		// replace them with the valid (first) one
		for (Relation r : relations) {
			if (r.getSourceEntry() != null && invalidEntry2newEntry.containsKey(r.getSourceEntry()))
				r.setSourceEntry(invalidEntry2newEntry.get(r.getSourceEntry()));
			if (r.getTargetEntry() != null && invalidEntry2newEntry.containsKey(r.getTargetEntry()))
				r.setTargetEntry(invalidEntry2newEntry.get(r.getTargetEntry()));
			for (IdRef ir : r.getSubtypeRefs()) {
				if (ir.getRef() != null && invalidEntry2newEntry.containsKey(ir.getRef())) {
					ir.setRef(invalidEntry2newEntry.get(ir.getRef()));
				}
			}
		}
		for (Reaction r : reactions) {
			for (Entry invalidEntry : invalidEntry2newEntry.keySet())
				r.removePossibleEntry(invalidEntry);
		}
	}
	
	public void removeMergeTheseEntriesIfPossible(Collection<Entry> removeMergeTheseEntries) {
		// 1. enumerate entries and ids
		HashMap<String, HashSet<Entry>> id2entryList = new HashMap<String, HashSet<Entry>>();
		for (Entry e : entries) {
			String id = e.getName().getId();
			if (!id2entryList.containsKey(id))
				id2entryList.put(id, new HashSet<Entry>());
			id2entryList.get(id).add(e);
		}
		// 2. for each id with more than one possible entry,
		// multiple entries will be removed
		HashMap<Entry, Entry> invalidEntry2newEntry = new HashMap<Entry, Entry>();
		HashSet<Entry> validEntries = new HashSet<Entry>();
		HashSet<String> done = new HashSet<String>();
		for (Entry removeThisEntryIfPossible : removeMergeTheseEntries) {
			if (validEntries.contains(removeMergeTheseEntries))
				continue;
			for (String checkId : id2entryList.keySet()) {
				if (done.contains(checkId))
					continue;
				HashSet<Entry> listOfEntriesWithSameId = id2entryList.get(checkId);
				if (listOfEntriesWithSameId.size() > 1) {
					if (!listOfEntriesWithSameId.contains(removeThisEntryIfPossible))
						continue;
					done.add(checkId);
					System.out.println("Multiple entry (" + listOfEntriesWithSameId.size() + "): " + checkId);
					Entry validEntry = null;
					for (Entry eee : listOfEntriesWithSameId) {
						System.out.println("--- entry: " + eee.getVisibleName());
						if (eee.getVisibleName() != null && eee.getVisibleName().contains("TITLE:")) {
							validEntry = eee;
							validEntries.add(validEntry);
						}
					}
					if (validEntry == null)
						for (Entry eee : listOfEntriesWithSameId) {
							if (eee.getSourcePathwayKeggId().equalsIgnoreCase(eee.getId().getValue())) {
								validEntry = eee;
								validEntries.add(validEntry);
							}
						}
					
					if (validEntry == null)
						for (Entry eee : listOfEntriesWithSameId) {
							if (eee == removeThisEntryIfPossible)
								continue;
							validEntry = eee;
							validEntries.add(validEntry);
							break;
						}
					if (validEntry != null) {
						for (Entry removeThisEntryIfPossible2 : listOfEntriesWithSameId) {
							if (removeThisEntryIfPossible2 == validEntry)
								continue;
							if (removeThisEntryIfPossible2.getReactions() != null)
								for (KeggId rid : removeThisEntryIfPossible2.getReactions())
									validEntry.addReaction(rid);
							if (removeThisEntryIfPossible2.getComponents() != null)
								for (IdRef cid : removeThisEntryIfPossible2.getComponents())
									validEntry.addComponent(cid);
							entries.remove(removeThisEntryIfPossible2);
							// id2entryList.get(checkId).remove(removeThisEntryIfPossible2);
							System.out.println("(3) Delete entry: " + removeThisEntryIfPossible2.getVisibleName());
							invalidEntry2newEntry.put(removeThisEntryIfPossible2, validEntry);
						}
					}
				}
			}
		}
		for (Entry e : entries)
			if (e.getMapRef() != null && invalidEntry2newEntry.keySet().contains(e.getMapRef().getRef())) {
				Entry newEntry = invalidEntry2newEntry.get(e.getMapRef().getRef());
				e.setMapRef(new IdRef(newEntry, newEntry.getId().getValue()));
			}
		// multiple entries are removed, update all references to these entries, and
		// replace them with the valid (first) one
		for (Relation r : relations) {
			if (r.getSourceEntry() != null && invalidEntry2newEntry.containsKey(r.getSourceEntry()))
				r.setSourceEntry(invalidEntry2newEntry.get(r.getSourceEntry()));
			if (r.getTargetEntry() != null && invalidEntry2newEntry.containsKey(r.getTargetEntry()))
				r.setTargetEntry(invalidEntry2newEntry.get(r.getTargetEntry()));
			for (IdRef ir : r.getSubtypeRefs()) {
				if (ir.getRef() != null && invalidEntry2newEntry.containsKey(ir.getRef())) {
					ir.setRef(invalidEntry2newEntry.get(ir.getRef()));
				}
			}
		}
		for (Reaction r : reactions) {
			for (Entry invalidEntry : invalidEntry2newEntry.keySet())
				r.removePossibleEntry(invalidEntry);
		}
	}
}
