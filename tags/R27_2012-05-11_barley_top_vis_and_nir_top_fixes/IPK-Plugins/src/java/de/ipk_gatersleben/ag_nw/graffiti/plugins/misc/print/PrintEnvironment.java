/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $ID:$
 * Created on 16.08.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print;

/**
 * Encapsulates the properties of the printing
 * Environment, like Zoom, colored printing, ...
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */
public final class PrintEnvironment {
	
	private static boolean printHeader = true;
	private static boolean printFooter = true;
	private static boolean zoomToOnePage = false;
	
	public static void setZoomToOnePage(boolean zoomToOnePage) {
		PrintEnvironment.zoomToOnePage = zoomToOnePage;
	}
	
	public static boolean isZoomToOnePage() {
		return zoomToOnePage;
	}
	
	public static void setPrintHeader(boolean printHeader) {
		PrintEnvironment.printHeader = printHeader;
	}
	
	public static boolean isPrintHeader() {
		return printHeader;
	}
	
	public static void setPrintFooter(boolean printFooter) {
		PrintEnvironment.printFooter = printFooter;
	}
	
	public static boolean isPrintFooter() {
		return printFooter;
	}
	
}
