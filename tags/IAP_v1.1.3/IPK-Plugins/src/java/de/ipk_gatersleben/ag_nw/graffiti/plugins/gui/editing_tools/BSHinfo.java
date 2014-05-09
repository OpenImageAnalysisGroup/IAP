/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 29, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import org.graffiti.plugin.io.resources.IOurl;

/**
 * @author klukas
 */
public class BSHinfo {
	
	public String targetMenu;
	public boolean nodeCommand;
	String firstLine;
	protected String cmdsrc;
	
	public BSHinfo(IOurl url) {
		cmdsrc = DefaultContextMenuManager.getContent(url);
		firstLine = DefaultContextMenuManager.getFirstOrSecondLine(url, "@");
		targetMenu = "";
		if (firstLine.startsWith("@")) {
			firstLine = firstLine.replaceFirst("@", "");
			if (firstLine.indexOf(":") > 0) {
				targetMenu = firstLine.substring(0, firstLine.indexOf(":"));
				firstLine = firstLine.substring(firstLine.indexOf(":") + 1);
			}
		} else
			firstLine = url + " (no @desc in first line)";
		nodeCommand = false;
		String nn = BSHscriptMenuEntry.isNodeCommand(firstLine);
		if (nn != null) {
			firstLine = nn;
			nodeCommand = true;
		}
	}
	
}
