/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_reaction;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class ReactionEntry {
	private String entryID = null;
	private String name = null;
	private String equatation = null;
	
	private ArrayList<String> substrates = new ArrayList<String>();
	private ArrayList<String> products = new ArrayList<String>();
	private ArrayList<String> substratesCnt = new ArrayList<String>();
	private ArrayList<String> productsCnt = new ArrayList<String>();
	
	private ArrayList<String> enzymes = new ArrayList<String>();
	
	// _exists tags are by definition always existant for an entry
	private static final String entryTag_exists = "ENTRY";
	private static final String nameTag_exists = "NAME";
	private static final String enzymeTag_exists = "ENZYME";
	public static final String equationTag_exists = "EQUATION";
	public static final String endTag_exists = "///";
	
	public static boolean isValidReactionStart(String line) {
		boolean res = (line != null && line.startsWith(entryTag_exists));
		return res;
	}
	
	@Override
	public String toString() {
		return entryID + " (" + name + ")";
	}
	
	public void processInputLine(String id, ArrayList<String> lines) {
		if (id == null || id.length() < 3)
			return;
		for (String l : lines)
			id = id + " " + l;
		if (id.startsWith(entryTag_exists)) {
			id = id.replace("Reaction", "");
			entryID = id.substring(entryTag_exists.length()).trim();
		} else
			if (id.startsWith(enzymeTag_exists)) {
				String enzymeList = id.substring(enzymeTag_exists.length()).trim();
				String[] enzArr = enzymeList.split(" ");
				for (String e : enzArr)
					enzymes.add(e.trim());
			} else
				if (id.startsWith(equationTag_exists)) {
					equatation = id.substring(equationTag_exists.length()).trim();
					processSubstratesAndProducts(equatation);
					if (substrates.size() <= 0)
						System.out.println("Problematic: " + equatation);
				} else
					if (id.startsWith(nameTag_exists)) {
						name = id.substring(nameTag_exists.length()).trim();
					}
		// all other types are ignored
	}
	
	private void processSubstratesAndProducts(String equ) {
		// substrates <=> products
		String div = " <=>";
		if (equ.indexOf(div) >= 0) {
			String subs = equ.substring(0, equ.indexOf(div));
			String prod = equ.substring(equ.indexOf(div) + div.length());
			processSubs(subs);
			processProds(prod);
		}
	}
	
	private void processSubs(String subs) {
		substrates.addAll(getValues(subs, substratesCnt));
	}
	
	private void processProds(String prods) {
		products.addAll(getValues(prods, productsCnt));
	}
	
	private Collection<String> getValues(String vl, Collection<String> cnts) {
		ArrayList<String> res = new ArrayList<String>();
		String[] va = vl.split(" \\+");
		for (String v : va) {
			v = v.trim();
			if (v.indexOf(" ") > 0) {
				String cnt = v.substring(0, v.indexOf(" "));
				cnts.add(cnt);
				v = v.substring(v.indexOf(" ") + 1);
				v = v.trim();
			} else
				cnts.add("");
			if (v.startsWith("C")) {
				res.add("cpd:" + v);
			} else
				if (v.startsWith("G")) {
					res.add("gl:" + v);
				} else {
					System.out.println("CHECK: " + va.toString());
					res.add(v);
				}
		}
		return res;
	}
	
	public boolean isValid() {
		return entryID != null;
	}
	
	/**
	 * @return Identification (Reaction ID)
	 */
	public String getID() {
		return entryID;
	}
	
	public ArrayList<String> getSubstrateNames() {
		return substrates;
	}
	
	public ArrayList<String> getProductNames() {
		return products;
	}
	
	public ArrayList<String> getEnzymeNames() {
		return enzymes;
	}
}
