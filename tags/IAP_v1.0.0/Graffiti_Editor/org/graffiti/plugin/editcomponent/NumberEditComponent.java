// ==============================================================================
//
// NumberEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NumberEditComponent.java,v 1.2 2013-06-06 10:27:00 klukas Exp $

package org.graffiti.plugin.editcomponent;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.graffiti.plugin.Displayable;

/**
 * <code>NumberEditComponent</code> provides an abstract implementation for
 * editing numerical attributes.
 * 
 * @see AbstractValueEditComponent
 * @see Number
 * @see javax.swing.JTextField
 */
public abstract class NumberEditComponent
		extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** The gui element of this component. */
	protected SpinnerEditComponent spinnerEditComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new integer edit component.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	protected NumberEditComponent(Displayable disp) {
		super(disp);
		spinnerEditComponent = new SpinnerEditComponent(disp);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>JComponent</code> associated with this value edit
	 * component. In this case a JSpinner.
	 * 
	 * @return the <code>JComponent</code> associated with this value edit
	 *         component.
	 */
	@Override
	public JComponent getComponent() {
		// System.out.println(displayable.getName());
		JComponent defaultResult = spinnerEditComponent.getComponent();
		if (displayable.getIcon() != null) {
			JComponent jc = displayable.getIcon();
			if (defaultResult != null)
				defaultResult.setPreferredSize(new Dimension(defaultResult.getMinimumSize().width, defaultResult.getPreferredSize().height));
			if (defaultResult != null)
				defaultResult.setMinimumSize(new Dimension(0, defaultResult.getMinimumSize().height));
			JPanel jp = (JPanel) TableLayout.getSplit(jc, defaultResult, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
			jp.setOpaque(false);
			return jp;
		} else
			return defaultResult;
		
	}
	
	/**
	 * Sets the displayable.
	 * 
	 * @param attr
	 *           DOCUMENT ME!
	 */
	@Override
	public void setDisplayable(Displayable attr) {
		this.displayable = attr;
		spinnerEditComponent.setDisplayable(attr);
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
	@Override
	public void setEditFieldValue() {
		spinnerEditComponent.setEditFieldValue();
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.AbstractValueEditComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		spinnerEditComponent.setEnabled(enabled);
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.AbstractValueEditComponent#setShowEmpty(boolean)
	 */
	@Override
	public void setShowEmpty(boolean showEmpty) {
		super.setShowEmpty(showEmpty);
		this.showEmpty = showEmpty;
		spinnerEditComponent.setShowEmpty(showEmpty);
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>. Calls setAttribute in the associated spinner,
	 * i.e. it only changes the value if it is different.
	 */
	@Override
	public void setValue() {
		// System.out.println(spinnerEditComponent+"");
		spinnerEditComponent.setValue();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
