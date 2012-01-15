/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing;

import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;

public class DBGETresult {
	String id, names, url;
	
	public DBGETresult(String memId, String memNames, String memUrl) {
		this.id = memId;
		if (memNames != null)
			memNames = StringManipulationTools.stringReplace(memNames, "<br>", "");
		this.names = memNames;
		this.url = memUrl;
	}
	
	@Override
	public String toString() {
		if (getSynonyms(50) != null && getSynonyms(50).length() > 0)
			return id + ": " + getFirstName() + " (" + getSynonyms(50) + ")";
		else
			return id + ": " + getFirstName();
	}
	
	private String getSynonyms(int maxLen) {
		if (names == null || names.length() <= 0)
			return "";
		String full = StringManipulationTools.stringReplace(names, getFirstName(), "");
		if (full.startsWith(";"))
			full = full.substring(1);
		if (full.startsWith(","))
			full = full.substring(1);
		if (full.length() > maxLen)
			return new String(full.substring(0, maxLen - 3) + "...").trim();
		else
			return full.trim();
	}
	
	/**
	 * If end == null, get everything from start to the end of line
	 */
	public static String getContentBetween(String start, String end, String content) {
		if (content.indexOf(start) >= 0) {
			String rest = content.substring(content.indexOf(start) + start.length());
			if (end == null) {
				return rest;
			} else
				if (rest.indexOf(end) >= 0) {
					String res = rest.substring(0, rest.indexOf(end));
					return res;
				}
		}
		return null;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getFirstName() {
		if (names != null && names.indexOf(";") > 0)
			return names.substring(0, names.indexOf(";"));
		else
			if (names != null && names.indexOf(",") > 0)
				return names.substring(0, names.indexOf(","));
			else
				return names;
	}
	
	public EntryType getEntryTypeFromId() {
		if (id == null || id.indexOf(":") <= 0)
			return null;
		else {
			if (id.startsWith("cpd:"))
				return EntryType.compound;
			if (id.startsWith("ec:"))
				return EntryType.enzyme;
			if (id.startsWith("path:"))
				return EntryType.map;
			if (id.startsWith("ko:"))
				return EntryType.ortholog;
			return null;
		}
	}
	
	public static String getWebsite(ArrayList<String> lines) {
		StringBuilder sb = new StringBuilder();
		boolean started = false;
		if (lines != null)
			for (String line : lines) {
				if (line.indexOf("<body>") >= 0) {
					line = line.substring(line.indexOf("<body>") + "<body>".length());
					started = true;
				}
				if (started)
					sb.append(line);
			}
		return sb.toString();
	}
	
}
