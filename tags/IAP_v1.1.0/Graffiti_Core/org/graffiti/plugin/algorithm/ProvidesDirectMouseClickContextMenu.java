/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package org.graffiti.plugin.algorithm;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

import org.graffiti.graph.Graph;

/**
 * @author Christian Klukas
 */
public interface ProvidesDirectMouseClickContextMenu {
	
	JMenuItem[] getContextCommand(MouseEvent lastMouseE, Component lastMouseSrc, Graph graph);
	
}
