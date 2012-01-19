/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: InvertSelectionAlgorithm.java,v 1.1 2011-01-31 08:59:36 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;

import javax.swing.KeyStroke;

import org.SystemInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class InvertSelectionAlgorithm
					extends AbstractAlgorithm {
	
	Selection selection;
	
	/**
	 * Constructs a new instance.
	 */
	public InvertSelectionAlgorithm() {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Selection sel = new Selection("id");
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		selection = MainFrame.getInstance().getActiveEditorSession().
							getSelectionModel().getActiveSelection();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		graph.getListenerManager().transactionStarted(this);
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		for (GraphElement ge : graph.getGraphElements()) {
			if (!currentSelElements.contains(ge))
				sel.add(ge);
		}
		MainFrame.getInstance().getActiveEditorSession().
							getSelectionModel().setActiveSelection(sel);
		graph.getListenerManager().transactionFinished(this);
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Invert Selection";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke('I', SystemInfo.getAccelModifier());
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	/**
	 * Sets the selection on which the algorithm works.
	 * 
	 * @param selection
	 *           the selection
	 */
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
}
