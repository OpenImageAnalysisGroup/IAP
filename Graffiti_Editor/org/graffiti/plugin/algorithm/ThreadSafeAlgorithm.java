// ==============================================================================
//
// ThreadSafeAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================

package org.graffiti.plugin.algorithm;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public abstract class ThreadSafeAlgorithm implements Algorithm {
	/**
	 * DOCUMENT ME!
	 * 
	 * @param jc
	 * @return true, if an GUI was set, false if no interface is needed
	 */
	abstract public boolean setControlInterface(final ThreadSafeOptions options, JComponent jc);
	
	abstract public void executeThreadSafe(ThreadSafeOptions options);
	
	abstract public void resetDataCache(ThreadSafeOptions options);
	
	public KeyStroke getAcceleratorKeyStroke() {
		return null;
	}
	
	public boolean showMenuIcon() {
		return false;
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
	
}
