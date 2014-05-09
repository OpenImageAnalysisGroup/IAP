/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import java.util.TreeSet;

import org.StringManipulationTools;

/**
 * @author klukas
 */
public class ImageAnalysisCommandManager {
	
	public static String getList(String heading, TreeSet<String> cs) {
		StringBuilder res = new StringBuilder();
		res.append(heading + " (" + cs.size() + ")" + "<ul>");
		if (cs.size() == 0)
			res.append("<li>[NOT SPECIFIED]");
		else {
			int n = 0;
			for (String c : cs) {
				res.append("<li>" + StringManipulationTools.stringReplace(c, ";", ";<br>"));
				n++;
				if (cs.size() > 15 && n >= 15) {
					res.append("<li>...");
					break;
				}
			}
		}
		res.append("</ul>");
		return res.toString();
	}
}
