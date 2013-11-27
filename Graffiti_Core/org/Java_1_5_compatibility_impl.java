/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 03.08.2004 by Christian Klukas
 */
package org;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class Java_1_5_compatibility_impl {
	public static void setComponentZorder(JDesktopPane jDesktopPane, JInternalFrame jInternalFrame) {
		jDesktopPane.setComponentZOrder(jInternalFrame, 0);
		// throw new NoSuchMethodError("ERR");
	}
	
	/**
	 * @return
	 */
	public static StackTraceElement[] getStackFrame() {
		return Thread.currentThread().getStackTrace();
	}
	
	/**
	 * @param url
	 * @return
	 */
	public static URI getURIfromURL(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			ErrorMsg.addErrorMessage(e);
			throw new NoSuchMethodError("ERR");
		}
	}
	
	public static String myStringReplace(String workString, String search, String replace) {
		return workString.replace(search, replace);
		// return null;
		// throw new NoSuchMethodError("ERR");
	}
}
