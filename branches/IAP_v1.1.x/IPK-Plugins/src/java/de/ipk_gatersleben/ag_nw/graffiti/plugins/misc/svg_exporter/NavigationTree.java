/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Jul 18, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.StringManipulationTools;

/**
 * @author klukas
 */
public class NavigationTree {
	
	private final TreeSet<String> titles;
	private final HashMap<String, String> fileName2title;
	
	/**
	 * @param titles
	 * @param fileName2title
	 */
	public NavigationTree(TreeSet<String> titles,
						HashMap<String, String> fileName2title) {
		this.titles = titles;
		this.fileName2title = fileName2title;
	}
	
	/**
	 * @param pathwayLinks
	 */
	public void outputLinks(StringBuilder pathwayLinks) {
		Stack<String> currentLevel = new Stack<String>();
		int currLevel = 0;
		for (String title : titles) {
			String[] levelNames = title.split("\\.");
			currLevel = currentLevel.size();
			
			// last item is file extension, before is the file name
			int thisLevelSize = levelNames.length - 1;
			while (currLevel > 0
								&& !currentLevel.peek().equals(levelNames[currLevel - 1])) {
				currentLevel.pop();
				currLevel--;
				pathwayLinks.append("		</ul>§");
			}
			while (currLevel < thisLevelSize) {
				String thisLevelDescription = levelNames[currLevel];
				pathwayLinks.append("		" + (currLevel >= 1 ? "<li>" : "")
									+ StringManipulationTools.UnicodeToHtml(thisLevelDescription)
									+ "<ul>" + "§");
				currentLevel.push(thisLevelDescription);
				currLevel++;
			}
			
			String fileName = "error";
			for (Entry<String, String> e : fileName2title.entrySet()) {
				if (e.getValue().equals(title)) {
					fileName = e.getKey();
				}
			}
			String ddd = levelNames[currLevel];
			pathwayLinks.append("		<li><a href=\"" + fileName
								+ "\" target=\"main\">"
								+ StringManipulationTools.UnicodeToHtml(ddd) + "</a><!-- "
								+ StringManipulationTools.UnicodeToHtml(title) + " -->§");
		}
		while (currLevel > 0) {
			currentLevel.pop();
			currLevel--;
			pathwayLinks.append("		</ul>§");
		}
	}
}
