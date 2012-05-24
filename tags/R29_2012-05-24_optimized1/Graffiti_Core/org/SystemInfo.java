/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jul 16, 2010 by Christian Klukas
 */

package org;

/**
 * @author klukas
 */
public class SystemInfo implements HelperClass {
	
	public static boolean isMac() {
		return AttributeHelper.macOSrunning();
	}
	
	public static boolean isLinux() {
		return AttributeHelper.linuxRunning();
	}
	
	public static int getAccelModifier() {
		return java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		/*
		 * String vers = System.getProperty("os.name").toLowerCase();
		 * if (vers.indexOf("mac") >= 0)
		 * return ActionEvent.META_MASK;
		 * else
		 * return ActionEvent.CTRL_MASK;
		 */
	}
	
}
