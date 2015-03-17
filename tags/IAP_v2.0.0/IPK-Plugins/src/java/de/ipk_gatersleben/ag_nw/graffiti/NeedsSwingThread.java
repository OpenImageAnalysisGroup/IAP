/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 13, 2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti;

/**
 * @author Christian Klukas
 *         All plugins that create Dialog and therefore need to run
 *         in the Swing User Interface Thread should implement this
 *         interface. All plugins "marked" with this interface
 *         are not run by the new plugin manager in a new thread but
 *         directly in the Swing Thread.
 */
public interface NeedsSwingThread {

}
