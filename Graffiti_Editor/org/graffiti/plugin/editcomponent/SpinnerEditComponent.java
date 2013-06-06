// ==============================================================================
//
// SpinnerEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SpinnerEditComponent.java,v 1.2 2013-06-06 10:27:00 klukas Exp $

package org.graffiti.plugin.editcomponent;

import java.awt.Dimension;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.LongAttribute;
import org.graffiti.attributes.ShortAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.parameter.IntegerParameter;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.2 $
 */
public class SpinnerEditComponent
		extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** The default step width for floating point numbers. */
	private final Double DEFAULT_STEP = new Double(0.5d);
	
	/** The spinner component used. */
	private JSpinner jSpinner;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for SpinnerEditComponent.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public SpinnerEditComponent(final Displayable disp) {
		super(disp);
		
		SpinnerNumberModel model;
		
		if (disp instanceof IntegerAttribute || disp instanceof ByteAttribute ||
				disp instanceof LongAttribute || disp instanceof ShortAttribute ||
				disp instanceof IntegerParameter) {
			model = new SpinnerNumberModel(new Integer(0), null, null, new Integer(1));
		} else {
			model = new SpinnerNumberModel(new Double(0d), null, null,
					DEFAULT_STEP);
		}
		
		this.jSpinner = new JSpinner(model) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected JComponent createEditor(SpinnerModel model)
			{
				if (disp instanceof IntegerAttribute || disp instanceof ByteAttribute ||
						disp instanceof LongAttribute || disp instanceof ShortAttribute ||
						disp instanceof IntegerParameter) {
					return super.createEditor(model);
				} else {
					return new NumberEditor(this, "0.00000");// needed decimal format
				}
			}
		};
		
		// this.spinner = new JSpinner();
		// this.spinner.setBorder(BorderFactory.createEmptyBorder());
		
		jSpinner.setOpaque(false);
		
		// this.spinner.setSize(100, 40);
		// this.spinner.setMinimumSize(new Dimension(40, 10));
		// this.spinner.setPreferredSize(new Dimension(100, 40));
		displayable = null; // ensure setDisplayable really does sth
		this.setDisplayable(disp);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>ValueEditComponent</code>'s <code>JComponent</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public JComponent getComponent() {
		jSpinner.setMinimumSize(new Dimension(0, 30));
		jSpinner.setPreferredSize(new Dimension(50, 30));
		jSpinner.setMaximumSize(new Dimension(2000, 30));
		return jSpinner;
	}
	
	/**
	 * Sets the displayable.
	 * 
	 * @param disp
	 */
	@Override
	public void setDisplayable(Displayable disp) {
		this.displayable = disp;
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
	@Override
	public void setEditFieldValue() {
		if (showEmpty) {
			((JSpinner.DefaultEditor) this.jSpinner.getEditor()).getTextField().setText(EMPTY_STRING);
		} else {
			jSpinner.setValue(this.displayable.getValue());
			
			ChangeEvent ce = new ChangeEvent(jSpinner);
			
			for (int i = 0; i < jSpinner.getChangeListeners().length; i++) {
				jSpinner.getChangeListeners()[i].stateChanged(ce);
			}
		}
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.AbstractValueEditComponent#setShowEmpty(boolean)
	 */
	@Override
	public void setShowEmpty(boolean showEmpty) {
		if (this.showEmpty != showEmpty) {
			super.setShowEmpty(showEmpty);
		}
		
		this.setEditFieldValue();
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>. But only if it is different.
	 */
	@Override
	public void setValue() {
		if (jSpinner.getEditor() != null && jSpinner.getEditor() instanceof NumberEditor) {
			NumberEditor ne = (NumberEditor) jSpinner.getEditor();
			String txt = ne.getTextField().getText();
			try {
				if (txt.equals(EMPTY_STRING)) {
					return;
				}
				if (txt.startsWith("*")) {
					Double p = Double.parseDouble(txt.substring("*".length()));
					if (this.displayable.getValue() instanceof Double) {
						this.displayable.setValue((Double) this.displayable.getValue() * p);
						return;
					} else
						if (this.displayable.getValue() instanceof Integer) {
							this.displayable.setValue((int) ((Integer) this.displayable.getValue() * p));
							return;
						}
				} else
					if (txt.startsWith("/")) {
						Double p = Double.parseDouble(txt.substring("/".length()));
						if (this.displayable.getValue() instanceof Double) {
							this.displayable.setValue((Double) this.displayable.getValue() / p);
							return;
						} else
							if (this.displayable.getValue() instanceof Integer) {
								this.displayable.setValue((int) ((Integer) this.displayable.getValue() / p));
								return;
							}
					} else
						if (txt.startsWith("+")) {
							Double p = Double.parseDouble(txt.substring("+".length()));
							if (this.displayable.getValue() instanceof Double) {
								this.displayable.setValue((Double) this.displayable.getValue() + p);
								return;
							} else
								if (this.displayable.getValue() instanceof Integer) {
									this.displayable.setValue((int) ((Integer) this.displayable.getValue() + p));
									return;
								}
						}
				
			} catch (NumberFormatException nfe) {
				//
			}
		}
		try {
			jSpinner.getEditor();
			// String txt1 = ne.getTextField().getText();
			// System.out.println("A: "+txt1);
			jSpinner.commitEdit();
			// String txt2 = ne.getTextField().getText();
			// System.out.println("B: "+txt2);
			// System.out.println(this.displayable.getValue()+" <-?-> "+this.jSpinner.getValue());
			if (!this.displayable.getValue().equals(this.jSpinner.getValue()))
				this.displayable.setValue(this.jSpinner.getValue());
			// String txt3 = ne.getTextField().getText();
			// System.out.println("C: "+txt3);
		} catch (ParseException e) {
			// input not parsable
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
