/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class CompoundImagePositionAttributeEditor extends AbstractValueEditComponent {
	protected JComboBox positionSelection;
	
	public CompoundImagePositionAttributeEditor(Displayable disp) {
		super(disp);
		positionSelection = new JComboBox(getPosiblePositions(true));
		String val = disp.getValue().toString();
		positionSelection.setSelectedItem(val);
	}
	
	public static String[] getPosiblePositions(boolean includeEmpty) {
		if (includeEmpty)
			return new String[] {
								EMPTY_STRING,
								GraphicAttributeConstants.CENTERED,
								GraphicAttributeConstants.CENTERED_FIT,
								GraphicAttributeConstants.LEFT,
								GraphicAttributeConstants.RIGHT,
								GraphicAttributeConstants.ABOVE,
								GraphicAttributeConstants.BELOW,
								GraphicAttributeConstants.AUTO_OUTSIDE,
								GraphicAttributeConstants.HIDDEN };
		else
			return new String[] {
								GraphicAttributeConstants.CENTERED,
								GraphicAttributeConstants.LEFT,
								GraphicAttributeConstants.RIGHT,
								GraphicAttributeConstants.ABOVE,
								GraphicAttributeConstants.BELOW,
								GraphicAttributeConstants.AUTO_OUTSIDE,
								GraphicAttributeConstants.HIDDEN };
		
	}
	
	public JComponent getComponent() {
		return positionSelection;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			positionSelection.setSelectedItem(EMPTY_STRING);
		} else {
			positionSelection.setSelectedItem(displayable.getValue().toString());
		}
	}
	
	public void setValue() {
		String text = (String) positionSelection.getSelectedItem();
		if (!text.equals(EMPTY_STRING)
							&& !this.displayable.getValue().toString().equals(text)) {
			this.displayable.setValue(text);
		}
	}
	
	public static boolean isCenteredPosition(Node n) {
		String position = (String) AttributeHelper.getAttributeValue(n, "image", "image_position",
							GraphicAttributeConstants.AUTO_OUTSIDE, "", true);
		return position.equals(GraphicAttributeConstants.CENTERED);
	}
	
	public static boolean isCenteredFitPosition(Node n) {
		String position = (String) AttributeHelper.getAttributeValue(n, "image", "image_position",
							GraphicAttributeConstants.AUTO_OUTSIDE, "", true);
		return position.equals(GraphicAttributeConstants.CENTERED_FIT);
	}
}
