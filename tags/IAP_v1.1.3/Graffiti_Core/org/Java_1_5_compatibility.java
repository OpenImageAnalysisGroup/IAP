/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.08.2004 by Christian Klukas
 */
package org;

import java.net.URI;
import java.net.URL;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class Java_1_5_compatibility {
	public static void setComponentZorder(JDesktopPane jDesktopPane, JInternalFrame jInternalFrame) {
		try {
			Java_1_5_compatibility_impl.setComponentZorder(jDesktopPane, jInternalFrame);
		} catch (NoSuchMethodError nsme) {
			//
		}
	}
	
	/**
	 * @return
	 */
	public static StackTraceElement[] getStackFrame() {
		try {
			return Java_1_5_compatibility_impl.getStackFrame();
		} catch (NoSuchMethodError nsme) {
			return null;
		}
	}
	
	/**
	 * @param defaultGraph
	 * @return
	 */
	public static URI getURIfromURL(URL url) {
		try {
			return Java_1_5_compatibility_impl.getURIfromURL(url);
		} catch (NoSuchMethodError nsme) {
			return null;
		}
	}
	
	public static String getJavaVersion() {
		return "Java " + System.getProperty("java.version");
		/*
		 * try {
		 * return Java_1_5_compatibility_impl.myStringReplace("x", "x", "Java 1.5 or higher");
		 * } catch(NoSuchMethodError nsme) {
		 * return "Java 1.4 or lower";
		 * }
		 */
	}
}
