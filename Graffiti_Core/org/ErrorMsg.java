/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org;

import java.awt.Component;
import java.awt.Container;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * @author klukas
 */
public class ErrorMsg implements HelperClass {
	private static LinkedList<String> errorMessages = new LinkedList<String>();
	private static LinkedList<String> errorMessagesShort = new LinkedList<String>();
	private static String statusMsg = null;
	
	private static boolean rethrowErrorMessages = true;
	
	public static void setRethrowErrorMessages(boolean rethrowErrorMessages) {
		ErrorMsg.rethrowErrorMessages = rethrowErrorMessages;
	}
	
	public static DecimalFormat getDecimalFormat(String pattern) {
		pattern = StringManipulationTools.stringReplace(pattern, ",", "");
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern(pattern);
		return df;
	}
	
	/**
	 * Adds a errorMessage to a global list. The error messages can be retrieved
	 * with <code>getErrorMessages</code> and cleared with <code>clearErrorMessages</code>.
	 * 
	 * @param errorMsg
	 */
	public synchronized static void addErrorMessage(String errorMsg) {
		if (rethrowErrorMessages)
			throw new Error(errorMsg);
		synchronized (errorMessages) {
			StackTraceElement[] stack = Java_1_5_compatibility.getStackFrame();
			String res;
			String firstMethod = "";
			if (stack == null) {
				res = "<br><font color=\"gray\"><code>No Stack Information (Running on Java 1.4 or lower)</code></font><hr>";
			} else {
				res = "<br><font color=\"gray\">Stack:<br><small><small><code>";
				boolean thisMethodFound = false;
				for (int i = 0; i < stack.length; i++) {
					if (stack[i].getLineNumber() < 0)
						continue;
					if (thisMethodFound) {
						String methodName = stack[i].getMethodName();
						if (methodName == null || methodName.length() <= 0)
							methodName = stack[i].getClass().getName(); // if
						// methodname
						// is empty,
						// the
						// constructor
						// caused the
						// problem
						res = res + "     Line: " + stack[i].getLineNumber() + " Method: " + stack[i].getClassName() + "/"
											+ methodName + "<br>";
						if (firstMethod.length() <= 0 && methodName != null && !methodName.endsWith("addErrorMessage")) {
							firstMethod = ", Line " + stack[i].getLineNumber() + " Method " + stack[i].getClassName() + "/"
												+ methodName;
						}
					}
					if (stack[i].getMethodName().equalsIgnoreCase("addErrorMessage"))
						thisMethodFound = true;
				}
				res = res + "</code></small></small></font><hr>";
			}
			String err = "<b>Error: " + errorMsg + "</b>" + res;
			if (!errorMessages.contains(err))
				errorMessages.add(err);
			synchronized (errorMessagesShort) {
				if (firstMethod.length() > 0)
					firstMethod = ", " + firstMethod;
				if (!errorMessagesShort.contains(errorMsg + firstMethod))
					;
				errorMessagesShort.add(errorMsg + firstMethod);
			}
		}
	}
	
	public synchronized static void setStatusMessage(String statusMsg) {
		synchronized (errorMessages) {
			ErrorMsg.statusMsg = statusMsg;
		}
	}
	
	/**
	 * Removes the current error messages. E.g. after showing them to the user.
	 */
	public synchronized static void clearErrorMessages() {
		synchronized (errorMessages) {
			errorMessages.clear();
			statusMsg = null;
		}
		synchronized (errorMessagesShort) {
			errorMessagesShort.clear();
		}
	}
	
	/**
	 * Returns pending error messages that were not shown to the user immediatly.
	 * 
	 * @return Pending Error Messages
	 */
	public synchronized static String[] getErrorMessages() {
		synchronized (errorMessages) {
			int statusAvail = 0;
			if (statusMsg != null && errorMessages.size() > 0)
				statusAvail = 1;
			
			String[] result = new String[errorMessages.size() + statusAvail];
			if (statusMsg != null && errorMessages.size() > 0)
				result[0] = "Last Status: " + statusMsg;
			int i = 0;
			for (Iterator<String> it = errorMessages.iterator(); it.hasNext();) {
				result[statusAvail + (i++)] = it.next();
			}
			return result;
		}
	}
	
	public synchronized static String[] getErrorMessagesShort() {
		synchronized (errorMessagesShort) {
			String[] result = new String[errorMessagesShort.size()];
			int i = 0;
			for (Iterator<String> it = errorMessagesShort.iterator(); it.hasNext();) {
				String s = it.next();
				if (s != null && s.length() > 0) {
					s = StringManipulationTools.stringReplace(s, "\"", "");
					s = StringManipulationTools.stringReplace(s, ">", "");
					s = StringManipulationTools.stringReplace(s, "<", "");
					s = StringManipulationTools.stringReplace(s, "#", "");
					s = StringManipulationTools.stringReplace(s, "?", "");
					s = StringManipulationTools.stringReplace(s, "&", "");
				} else
					s = "";
				result[(i++)] = s;
			}
			return result;
		}
	}
	
	/**
	 * @return
	 */
	public synchronized static String getErrorMessagesAsXML() {
		String errorTag = "error";
		String[] errmsg = getErrorMessages();
		String res = "";
		if (errmsg != null)
			for (int i = 0; i < errmsg.length; i++)
				res += "<" + errorTag + ">" + StringManipulationTools.UnicodeToHtml(errmsg[i]) + "</" + errorTag + ">";
		return "<errormessages>" + res + "</errormessages>";
	}
	
	public static int getErrorMsgCount() {
		return errorMessages.size();
	}
	
	public static String getLastStatusMessage() {
		return statusMsg;
	}
	
	private static ApplicationStatus apploadingCompleted = ApplicationStatus.INITIALIZATION;
	
	private static Collection<Runnable> finishedListeners = new ArrayList<Runnable>();
	static Collection<Runnable> finishedAddonLoadingListeners = new ArrayList<Runnable>();
	
	public static ApplicationStatus getAppLoadingStatus() {
		return apploadingCompleted;
	}
	
	public static void setAppLoadingCompleted(ApplicationStatus status) {
		apploadingCompleted = status;
		if (apploadingCompleted == ApplicationStatus.PROGRAM_LOADING_FINISHED) {
			Collection<Runnable> fl;
			synchronized (finishedListeners) {
				fl = new ArrayList<Runnable>(finishedListeners);
			}
			for (Runnable r : fl) {
				SwingUtilities.invokeLater(r);
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					finishedListeners.clear();
				}
			});
		}
		if (apploadingCompleted == ApplicationStatus.ADDONS_LOADED
							|| (apploadingCompleted == ApplicationStatus.PROGRAM_LOADING_FINISHED && ReleaseInfo
												.getIsAllowedFeature(FeatureSet.ADDON_LOADING))) {
			Collection<Runnable> fl;
			synchronized (finishedAddonLoadingListeners) {
				fl = new ArrayList<Runnable>(finishedAddonLoadingListeners);
			}
			for (Runnable r : fl) {
				SwingUtilities.invokeLater(r);
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					finishedAddonLoadingListeners.clear();
				}
			});
		}
	}
	
	public static boolean areApploadingAndFinishActionsCompleted() {
		if (getAppLoadingStatus() == ApplicationStatus.INITIALIZATION)
			return false;
		boolean result;
		synchronized (finishedListeners) {
			result = finishedListeners.size() == 0;
		}
		return result;
	}
	
	public static void addOnAppLoadingFinishedAction(Runnable actionListener) {
		if (getAppLoadingStatus() != ApplicationStatus.INITIALIZATION) {
			SwingUtilities.invokeLater(actionListener);
		} else {
			synchronized (finishedListeners) {
				finishedListeners.add(actionListener);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Object findChildComponent(Component c, Class searchClass) {
		if (c == null)
			return null;
		// System.out.println(c.getClass().getCanonicalName());
		if (c.getClass() == searchClass)
			return c;
		try {
			Object o = c.getClass().asSubclass(searchClass);
			if (o != null)
				return c;
		} catch (Exception err) {
			// Component c is not of desired type
		}
		if (c instanceof JComponent)
			for (Component jj : ((JComponent) c).getComponents()) {
				Object res = findChildComponent(jj, searchClass);
				if (res != null)
					return res;
			}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void findChildComponents(Component c, Class searchClass, ArrayList<Object> result) {
		if (c == null)
			return;
		// System.out.println(c.getClass().getCanonicalName());
		if (c.getClass() == searchClass)
			result.add(c);
		else {
			try {
				Object o = c.getClass().asSubclass(searchClass);
				if (o != null)
					result.add(o);
			} catch (Exception err) {
				// Component c is not of desired type
			}
		}
		if (c instanceof Container)
			for (Component jj : ((Container) c).getComponents()) {
				findChildComponents(jj, searchClass, result);
			}
	}
	
	@SuppressWarnings("unchecked")
	public static Object findParentComponent(Component c, Class searchClass) {
		if (c == null)
			return null;
		// System.out.println(c.getClass().getCanonicalName());
		if (c.getClass() == searchClass)
			return c;
		try {
			Object o = c.getClass().asSubclass(searchClass);
			if (o != null)
				return c;
		} catch (Exception err) {
			// Component c is not of desired type
		}
		return findParentComponent(c.getParent(), searchClass);
	}
	
	public static void addErrorMessage(Exception e) {
		if (rethrowErrorMessages)
			throw new Error(e);
		
		addErrorMessage(e.getLocalizedMessage());
		e.printStackTrace();
	}
	
	public static void addOnAddonLoadingFinishedAction(Runnable runnable) {
		if (getAppLoadingStatus() == ApplicationStatus.ADDONS_LOADED)
			runnable.run();
		else
			finishedAddonLoadingListeners.add(runnable);
	}
	
}