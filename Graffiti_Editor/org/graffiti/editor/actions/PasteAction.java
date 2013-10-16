// ==============================================================================
//
// PasteAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PasteAction.java,v 1.1 2011-01-31 09:04:22 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.selection.Selection;

/**
 * Represents a graph element paste action.
 * 
 * @version $Revision: 1.1 $
 */
public class PasteAction extends SelectionAction {
	// ~ Constructors ===========================================================
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int pasteOffset = 50;
	
	/**
	 * Constructs a new popup action.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public PasteAction(MainFrame mainFrame) {
		super("edit.paste", mainFrame);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the help context for the action.
	 * 
	 * @return the help context for the action
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	HashMap<String, Integer> pasteHash2Offset = new HashMap<String, Integer>();
	
	public static String pastedNodeID = "pastedNode";
	
	/**
	 * Executes this action.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		String gml = ClipboardService.readFromClipboardAsText();
		boolean isGMLformat = true;
		if (!(gml != null && gml.startsWith("graph ["))) {
			isGMLformat = false;
		}
		if (gml == null) {
			MainFrame.showMessageDialog("Clipboard data could not be read. Can not proceed.", "Information");
			return;
		}
		
		String ext = "gml";
		IOManager ioManager = MainFrame.getInstance().getIoManager();
		try {
			InputSerializer is = ioManager.createInputSerializer(null, "." + ext);
			Graph newGraph = new AdjListGraph(new ListenerManager());
			if (isGMLformat)
				is.read(new StringReader(gml), newGraph);
			else {
				gml = StringManipulationTools.stringReplace(gml, "\r", "");
				int x = 100;
				int y = 100;
				for (String line : gml.split("\n")) {
					if (line.trim().length() <= 0)
						continue;
					Node newNode = newGraph.addNode();
					AttributeHelper.setLabel(newNode, line.trim());
					AttributeHelper.setPosition(newNode, x, y);
					y += 100;
				}
			}
			for (Node node : newGraph.getNodes())
				AttributeHelper.setAttribute(node, "", pastedNodeID, Boolean.TRUE);
			Graph workGraph = getGraph();
			boolean showGraphInNewView = false;
			if (workGraph == null) {
				workGraph = new AdjListGraph();
				showGraphInNewView = true;
			}
			
			String hashCode = gml.hashCode() + "§" + workGraph.hashCode();
			
			if (!pasteHash2Offset.containsKey(hashCode))
				pasteHash2Offset.put(hashCode, 0);
			pasteHash2Offset.put(hashCode, pasteHash2Offset.get(hashCode) + pasteOffset);
			int off = pasteHash2Offset.get(hashCode);
			AttributeHelper.moveGraph(newGraph, off, off);
			
			newGraph.setModified(false);
			
			Collection<GraphElement> newElements = workGraph.addGraph(newGraph);
			Selection sel = getSelection();
			if (sel == null)
				sel = new Selection();
			sel.clear();
			sel.addAll(newElements);
			
			if (!showGraphInNewView) {
				// MainFrame.getInstance().getSessionManager().getActiveSession().getActiveView().repaint(null);
				MainFrame.getInstance().getSessionManager().getActiveSession().getActiveView().completeRedraw();
			} else {
				MainFrame.getInstance().showGraph(workGraph, e);
			}
			mainFrame.getActiveEditorSession().getSelectionModel().selectionChanged();
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given
	 * list of selected items.
	 * 
	 * @param items
	 *           the items, which determine the internal state of the <code>enable</code> flag.
	 */
	@Override
	protected void enable(List<?> items) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.actions.SelectionAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true; /*
						 * String gml = ClipboardService.readFromClipboardAsText();
						 * return (gml!=null && gml.startsWith("graph ["));
						 */
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
