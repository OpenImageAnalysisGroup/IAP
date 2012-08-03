// ==============================================================================
//
// ColorChooserEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ColorChooserEditComponent.java,v 1.1 2011-01-31 09:03:27 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.AttributeHelper;
import org.MarkComponent;
import org.graffiti.attributes.ColorSetAndGetSupport;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugin.parameter.ColorParameter;

/**
 * This class provides an edit component for editing color attributes by
 * offering a <code>JColorChooser</code>.
 * 
 * @version $Revision: 1.1 $
 * @see javax.swing.JColorChooser
 * @see org.graffiti.graphics.ColorAttribute
 */
public class ColorChooserEditComponent
					extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	public Color emptyColor = Color.LIGHT_GRAY;
	
	/** DOCUMENT ME! */
	public String buttonText = "<html>" + "Choose";
	
	/** DOCUMENT ME! */
	JButton button;
	
	MarkComponent mc;
	
	/** The dialog that is displayed by the ColorChooser. */
	JDialog dialog;
	
	/** DOCUMENT ME! */
	private int opacity = 255;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>ColorChooserEditComponent</code>.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public ColorChooserEditComponent(final Displayable disp) {
		super(disp);
		buttonText = "";
		// buttonText = buttonText+" "+disp.getName();
		button = new JButton(buttonText) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMinimumSize() {
				Dimension res = super.getMinimumSize();
				res.setSize(20, res.getHeight());
				return res;
			}
			
			@Override
			public Dimension getPreferredSize() {
				Dimension res = super.getPreferredSize();
				res.setSize(20, res.getHeight());
				return res;
			}
			
		};
		
		mc = new MarkComponent(button, true, TableLayoutConstants.FILL, false);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JColorChooser colorChooser = new JColorChooser(button.getBackground());
				dialog = JColorChooser.createDialog(ColorChooserEditComponent.this.button,
									"Select the " + disp.getName(), true, colorChooser, new ActionListener() {
										public void actionPerformed(ActionEvent arg0) {
											Color col = colorChooser.getColor();
											buttonText = "<html>" + AttributeHelper.getColorName(col);
											button.setText(buttonText);
											button.setBackground(col);
											mc.setMarkColor(col, col);
											
											updateTooltip();
											// button.setForeground(Colors.getOppositeColor(col));
										}
									}, null);
				dialog.setVisible(true);
			}
		});
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>ValueEditComponent</code>'s <code>JComponent</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public JComponent getComponent() {
		return mc; // button;
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
	public void setEditFieldValue() {
		if (showEmpty) {
			button.setBackground(emptyColor);
			mc.setMarkColor(emptyColor, emptyColor);
			// button.setForeground(Colors.getOppositeColor(emptyColor));
			button.setText(EMPTY_STRING);
			
			updateTooltip();
		} else {
			Color attrColor;
			if (displayable instanceof ColorParameter)
				attrColor = ((ColorParameter) displayable).getColor();
			else
				attrColor = ((ColorSetAndGetSupport) displayable).getColor();
			
			buttonText = "<html>" + AttributeHelper.getColorName(attrColor);
			button.setText(buttonText);
			
			updateTooltip();
			
			// Color attrColor = (Color) this.displayable.getValue();
			
			// save opacity value
			opacity = attrColor.getAlpha();
			
			// use opaque color for button
			Color newColor = new Color(attrColor.getRed(),
								attrColor.getGreen(), attrColor.getBlue());
			
			button.setBackground(newColor);
			mc.setMarkColor(newColor, newColor);
			// button.setForeground(Colors.getOppositeColor(newColor));
		}
	}
	
	private void updateTooltip() {
		button.setToolTipText("<html>Click to change (" + button.getText() + "</b>)");
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>. But only if it is different.
	 */
	public void setValue() {
		if (!button.getText().equals(EMPTY_STRING)) {
			Color buttonColor = this.button.getBackground();
			
			Color displColor;
			if (displayable instanceof ColorParameter)
				displColor = ((ColorParameter) displayable).getColor();
			else
				displColor = ((ColorSetAndGetSupport) this.displayable).getColor();
			if (!displColor.equals(buttonColor)) {
				Color newColor = new Color(buttonColor.getRed(),
									buttonColor.getGreen(), buttonColor.getBlue(),
									this.opacity);
				
				// ((ColorAttribute)this.displayable)
				// .setColor(this.button.getBackground());
				// ((ColorAttribute)this.displayable).setOpacity(this.opacity);
				if (displayable instanceof ColorParameter)
					((ColorParameter) this.displayable).setValue(newColor);
				else
					((ColorSetAndGetSupport) this.displayable).setColor(newColor);
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
