// ==============================================================================
//
// TransactionListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: TransactionListener.java,v 1.1 2011-01-31 09:05:00 klukas Exp $

package org.graffiti.event;

import java.util.EventListener;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.Attributable;

/**
 * Interface, that contains methods which are called when transactions are
 * started or finished.
 * 
 * @version $Revision: 1.1 $
 */
public interface TransactionListener
					extends EventListener {
	// ~ Methods ================================================================
	
	/**
	 * Called when a transaction has stopped. <br>
	 * The class {@link TransactionHashMap} merges duplicate {@link AttributeEvent}s.
	 * In case the Attribute path for the same {@link Attributable} is not equal,
	 * detailed information about Attribute path is lost. Instead a generic {@link AttributeEvent} containing just the {@link Attributable} is in the list of
	 * changed Objects (see {@link TransactionEvent}).<br>
	 * In case no detailed information about an Attribute change is available, your code
	 * should completely re-process the affected {@link Attributable}.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status);
	
	/**
	 * Called when a transaction has started.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	public void transactionStarted(TransactionEvent e);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
