// ==============================================================================
//
// AbstractValueEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractValueEditComponent.java,v 1.1 2011-01-31 09:04:30 klukas Exp $

package org.graffiti.plugin.editcomponent;

import javax.swing.JComponent;

import org.graffiti.event.AttributeEvent;
import org.graffiti.plugin.Displayable;

/**
 * The class <code>AbstractValueEditComponent</code> provides some generic
 * implementation for <code>ValueEditComponent</code>s.
 * 
 * @see ValueEditComponent
 */
public abstract class AbstractValueEditComponent
					extends ValueEditComponentAdapter {
	// ~ Instance fields ========================================================
	
	/** The field to edit the value of the displayable. */
	protected JComponent editField;
	
	/**
	 * Set to true if this component should display nothing instead of the
	 * value of the attribute it represents.
	 */
	public boolean showEmpty = false;
	
	// ~ Constructors ===========================================================
	
	protected AbstractValueEditComponent() {
		this(null);
	}
	
	/**
	 * Constructs a new <code>AbstractValueEditComponent</code>.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	protected AbstractValueEditComponent(Displayable disp) {
		super(disp);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the displayable.
	 * 
	 * @param disp
	 */
	public void setDisplayable(Displayable disp) {
		this.displayable = disp;
	}
	
	/**
	 * Returns the <code>Attribute</code> instance the current <code>ValueEditComponent</code> contains.
	 * 
	 * @return the <code>Attribute</code> instance the current <code>ValueEditComponent</code> contains.
	 */
	public Displayable getDisplayable() {
		return this.displayable;
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		getComponent().setEnabled(enabled);
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#isEnabled()
	 */
	public boolean isEnabled() {
		return getComponent().isEnabled();
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#setShowEmpty(boolean)
	 */
	public void setShowEmpty(boolean showEmpty) {
		this.showEmpty = showEmpty;
		setEditFieldValue();
	}
	
	/**
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#getShowEmpty()
	 */
	public boolean getShowEmpty() {
		return this.showEmpty;
	}
	
	/**
	 * Called after a change of an displayable took place.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	@Override
	public void postAttributeChanged(AttributeEvent e) {
		if (e.getAttribute().equals(this.displayable)) {
			setEditFieldValue();
		}
	}
	
	/**
	 * Called before a change of an displayable takes place.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	@Override
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	// /**
	// * Called just before an displayable is added.
	// *
	// * @param e the AttributeEvent detailing the changes.
	// */
	// public void preAttributeAdded(AttributeEvent e) {}
	//
	// /**
	// * Called just before an displayable is removed.
	// *
	// * @param e the AttributeEvent detailing the changes.
	// */
	// public void preAttributeRemoved(AttributeEvent e) {}
	//
	// /**
	// * Called just before an displayable is removed.
	// *
	// * @param e the AttributeEvent detailing the changes.
	// */
	// public void postAttributeRemoved(AttributeEvent e) {}
	//
	// /**
	// * Called if a transaction got started.
	// *
	// * @param t the transaction event.
	// */
	// public void transactionStarted(TransactionEvent t) {}
	//
	// /**
	// * Called if a transaction got finished.
	// *
	// * @param t the transaction event.
	// */
	// public void transactionFinished(TransactionEvent t) {}
	//
	// /**
	// * Called after an displayable as been added.
	// *
	// * @param e the AttributeEvent detailing changes.
	// */
	// public void postAttributeAdded(AttributeEvent e) {}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
