/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 4, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.util;

import java.util.ArrayList;

// import netscape.javascript.JSObject;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class MyUtility {
	
	public static void navigate(ArrayList<NavigationButton> path, String target) {
		try {
			String ft = clean(target);
			StringBuilder sb = new StringBuilder();
			for (NavigationButton ne : path) {
				if (ne.getTitle().equalsIgnoreCase(target))
					break;
				if (sb.length() > 0)
					sb.append(".");
				sb.append(ne.getTitle());
			}
			if (sb.length() > 0)
				sb.append(".");
			sb.append(target);
			target = sb.toString();
			target = clean(target);
			
			if (target.startsWith("Initialize"))
				target = target.substring("Initialize".length());
			if (target.startsWith("Overview"))
				target = target.substring("Overview".length());
			if (target.startsWith("."))
				target = target.substring(".".length());
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: NAVIGATE TO: " + target);
			// JSObject win = null;// JDK8, unclear how to do this: JSObject.getWindow(ReleaseInfo.getApplet());
			// win.eval("window.location.hash='" + target + "';");
			// win.eval("document.title='IAP: " + ft + "';");
		} catch (Exception e1) {
			// empty
		}
	}
	
	private static String clean(String target) {
		target = StringManipulationTools.stringReplace(target, "<html><b><code>", "");
		target = StringManipulationTools.stringReplace(target, "\\\\ ", "");
		target = StringManipulationTools.stringReplace(target, " \\\\", "");
		target = StringManipulationTools.stringReplace(target, "// ", "");
		target = StringManipulationTools.stringReplace(target, " //", "");
		target = StringManipulationTools.stringReplace(target, "-- ", "");
		target = StringManipulationTools.stringReplace(target, " --", "");
		target = StringManipulationTools.stringReplace(target, "|| ", "");
		target = StringManipulationTools.stringReplace(target, " ||", "");
		return target;
	}
	
}
