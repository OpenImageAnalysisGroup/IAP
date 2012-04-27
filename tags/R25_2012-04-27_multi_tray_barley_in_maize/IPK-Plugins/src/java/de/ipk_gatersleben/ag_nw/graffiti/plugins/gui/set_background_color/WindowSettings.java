/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.set_background_color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugins.views.defaults.GraffitiView;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * DOCTODO: Include class header
 */
public class WindowSettings
					extends AbstractAlgorithm
					implements ProvidesGeneralContextMenu, ActionListener {
	
	JMenuItem myMenuItem, showAllElements, showElements, hideElements;
	
	public String getName() {
		return "Show/Hide Graph Edges";
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		// normally SystemInfo.getAccelModifier() is used
		// mac os x used Cmd+H as a system shortcut,
		// therefore, here the Ctrl key is hard coded
		return KeyStroke.getKeyStroke('H', ActionEvent.CTRL_MASK);
	}
	
	/**
	 * DOCTODO: Include method header
	 * 
	 * @throws PreconditionException
	 *            DOCUMENT ME!
	 */
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	public void execute() {
		JComponent view = null;
		try {
			MainFrame mf = GravistoService.getInstance().getMainFrame();
			view = (JComponent) mf.getActiveSession().getActiveView();
		} catch (Exception e) {
			//
		}
		if (view != null && view instanceof GraffitiView) {
			GraffitiView v = (GraffitiView) view;
			v.setBlockEdges(!v.getBlockEdges());
			GraphHelper.issueCompleteRedrawForView(v, v.getGraph());
			if (v.getBlockEdges())
				MainFrame.showMessage("Display of edges has been disabled for current view", MessageType.INFO);
			else
				MainFrame.showMessage("Display of edges has been enabled for current view", MessageType.INFO);
		} else {
			MainFrame.showMessageDialog("Command can not be executed on this kind of view!", "Error");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.ContextMenuPlugin#getCurrentContextMenuItem()
	 */
	public JMenuItem[] getCurrentContextMenuItem() {
		myMenuItem = new JMenuItem("Show/hide graph edges (view setting)");
		myMenuItem.addActionListener(this);
		
		showAllElements = new JMenuItem("Show all elements (undo hide)");
		showAllElements.addActionListener(this);
		showElements = new JMenuItem("Show selected elements (undo hide)");
		showElements.addActionListener(this);
		hideElements = new JMenuItem("Hide selected elements");
		hideElements.addActionListener(this);
		try {
			GravistoService.getInstance().algorithmAttachData(this);
			if (selection.getElements().size() > 0)
				return new JMenuItem[] { myMenuItem, showAllElements, showElements, hideElements };
			else
				return new JMenuItem[] { myMenuItem, showAllElements };
		} catch (Exception e) {
			return new JMenuItem[] { myMenuItem };
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == myMenuItem) {
			GravistoService.getInstance().algorithmAttachData(this);
			try {
				this.check();
				this.execute();
			} catch (PreconditionException e1) {
				ErrorMsg.addErrorMessage(e1);
			}
			this.reset();
		}
		if (e.getSource() == showElements) {
			GravistoService.getInstance().algorithmAttachData(this);
			graph.getListenerManager().transactionStarted(this);
			AttributeHelper.setHidden(selection.getElements(), false);
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
		if (e.getSource() == hideElements) {
			GravistoService.getInstance().algorithmAttachData(this);
			graph.getListenerManager().transactionStarted(this);
			AttributeHelper.setHidden(selection.getElements(), true);
			for (Node n : selection.getNodes()) {
				for (Edge edge : n.getEdges()) {
					AttributeHelper.setHidden(true, edge);
				}
			}
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
		if (e.getSource() == showAllElements) {
			GravistoService.getInstance().algorithmAttachData(this);
			graph.getListenerManager().transactionStarted(this);
			AttributeHelper.setHidden(graph.getGraphElements(), false);
			graph.getListenerManager().transactionFinished(this);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
		
	}
}
