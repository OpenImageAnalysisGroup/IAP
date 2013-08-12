// ==============================================================================
//
// MessageListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MessageListener.java,v 1.1 2011-01-31 09:04:24 klukas Exp $

package org.graffiti.plugin.view;

import org.graffiti.editor.MessageType;

/**
 * Represents listener which gets messsages for theirs displaying on the GUI
 * components (e.g. status bar).
 * 
 * @version $Revision: 1.1 $
 */
public interface MessageListener {
	// ~ Static fields/initializers =============================================
	
	// ~ Methods ================================================================
	
	// /**
	// * The constants specify GUI components where the messages recieved by this
	// * listener have to be displayed.
	// */
	// public static final String STATUSBAR = "statusBar";
	//
	// /**
	// * Method <code>showMesssage</code> displays message on GUI components
	// * according to the specified type.
	// *
	// * @param message a message string to be displayed
	// * @param type a type of the message (e.g. ERROR)
	// * @param whereto a location for displaying this message
	// */
	// public void showMesssage(String message, int type, String whereto);
	
	/**
	 * Method <code>showMesssage</code> displays a message on GUI components
	 * according to the specified type.
	 * 
	 * @param message
	 *           a message string to be displayed
	 * @param type
	 *           a type of the message (e.g. ERROR)
	 */
	public void showMesssage(String message, MessageType type);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
