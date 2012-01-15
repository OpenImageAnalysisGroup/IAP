// ==============================================================================
//
// LabelTool.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LabelTool.java,v 1.1 2011-01-31 09:03:34 klukas Exp $

package org.graffiti.plugins.modes.defaults;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.CompositeAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.undo.ChangeAttributesEdit;

/**
 * A tool for creating and editing labels of graphelements.
 * 
 * @author Holleis
 * @version $Revision: 1.1 $
 */
public class LabelTool
					extends MegaTools {
	// ~ Instance fields ========================================================
	
	// maybe put this somewhere else?
	
	/** DOCUMENT ME! */
	protected final String labelConst = "label";
	
	// ~ Methods ================================================================
	
	/**
	 * Invoked if user presses mouse button.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		
		Component clickedComp = findComponentAt(e, e.getX(), e.getY());
		
		if (clickedComp instanceof GraphElementComponent) {
			GraphElement ge =
								((GraphElementComponent) clickedComp).getGraphElement();
			LabelAttribute labelAttr =
								(LabelAttribute) searchForAttribute(ge.getAttribute(""),
													LabelAttribute.class);
			
			ChangeAttributesEdit edit;
			
			if (labelAttr != null) {
				String oldLabel = labelAttr.getLabel();
				String newLabel = showEditDialog(clickedComp, oldLabel);
				
				if (!oldLabel.equals(newLabel)) {
					edit = new ChangeAttributesEdit(session.getGraph(), labelAttr, geMap);
					labelAttr.setLabel(newLabel);
					undoSupport.postEdit(edit);
				}
				
				clickedComp.getParent().repaint();
			} else { // no label found
			
				String newLabel = showEditDialog(clickedComp, "");
				
				if (ge instanceof Node) {
					labelAttr = new NodeLabelAttribute(labelConst);
				} else {
					labelAttr = new EdgeLabelAttribute(labelConst);
				}
				
				ge.addAttribute(labelAttr, "");
				edit = new ChangeAttributesEdit(session.getGraph(), labelAttr, geMap);
				labelAttr.setLabel(newLabel);
				
				undoSupport.postEdit(edit);
				clickedComp.getParent().repaint();
			}
			
		}
	}
	
	@Override
	public void activate() {
		super.activate();
		MainFrame.showMessage("Click onto a node or an edge to edit/create a label", MessageType.INFO);
	}
	
	/**
	 * DOCUMENT ME!
	 */
	public void reset() {
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param attr
	 *           DOCUMENT ME!
	 * @param attributeType
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected Attribute searchForAttribute(Attribute attr, Class<LabelAttribute> attributeType) {
		if (attributeType.isInstance(attr)) {
			return attr;
		} else {
			if (attr instanceof CollectionAttribute) {
				Iterator<?> it =
									((CollectionAttribute) attr).getCollection().values()
														.iterator();
				
				while (it.hasNext()) {
					Attribute newAttr =
										searchForAttribute((Attribute) it.next(), attributeType);
					
					if (newAttr != null) {
						return newAttr;
					}
				}
			} else
				if (attr instanceof CompositeAttribute) {
					// TODO: treat those correctly; some of those have not yet
					// been correctly implemented
					return null;
				}
		}
		
		return null;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param parent
	 *           DOCUMENT ME!
	 * @param initialText
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected String showEditDialog(Component parent, String initialText) {
		String returnValue =
							JOptionPane.showInputDialog(parent, "Enter new label:", initialText);
		
		if (returnValue == null) {
			return initialText;
		} else {
			return returnValue;
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		mouseMoved(e);
	}
	
	public String getToolName() {
		return "LabelTool";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
