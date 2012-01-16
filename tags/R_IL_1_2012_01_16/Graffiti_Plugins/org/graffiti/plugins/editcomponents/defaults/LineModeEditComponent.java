// ==============================================================================
//
// LineModeEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LineModeEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.graffiti.attributes.FloatAttribute;
import org.graffiti.graphics.LineModeAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * Used to edit the values of a <code>Dash</code>.
 */
public class LineModeEditComponent
					extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** Panel used to group the different entries for "phase" and dash array. */
	private JPanel panel;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Standard constructor.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public LineModeEditComponent(Displayable disp) {
		super(disp);
		panel = new JPanel();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>ValueEditComponent</code>'s <code>JComponent</code>.
	 * 
	 * @return the <code>ValueEditComponent</code>'s <code>JComponent</code>.
	 */
	public JComponent getComponent() {
		LineModeAttribute lmAttr = (LineModeAttribute) displayable;
		
		JComponent phaseComp = (new FloatEditComponent(new FloatAttribute(
							"phase", lmAttr.getDashPhase()))).getComponent();
		this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		this.panel.add(phaseComp);
		
		float[] dashs = lmAttr.getDashArray();
		
		if (dashs != null) {
			JComponent dashComp = null;
			
			for (int i = 0; i < dashs.length; i++) {
				dashComp = new FloatEditComponent(new FloatAttribute("dash" +
									i, dashs[i])).getComponent();
				this.panel.add(dashComp);
			}
		}
		
		this.panel.revalidate();
		
		return this.panel;
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
	public void setEditFieldValue() {
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>.
	 */
	public void setValue() {
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
