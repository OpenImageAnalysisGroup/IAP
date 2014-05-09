// ==============================================================================
//
// LineModeAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LineModeAttribute.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.CompositeAttribute;
import org.graffiti.attributes.FloatAttribute;
import org.graffiti.attributes.HashMapAttribute;

/**
 * Attribute class for a dash-object, which encapsulates the information needed
 * to specify stroke properties.
 * 
 * @author schoeffl
 * @version $Revision: 1.1 $
 */
public class LineModeAttribute
					extends CompositeAttribute {
	// ~ Instance fields ========================================================
	
	/** The encapsulated object. */
	private Dash dash;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for LineModeAttribute.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public LineModeAttribute(String id) {
		this(id, new Dash());
	}
	
	/**
	 * Constructor for LineModeAttribute.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param d
	 *           the dash-value of the attribute.
	 */
	public LineModeAttribute(String id, Dash d) {
		super(id);
		this.dash = d;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.attributes.CompositeAttribute#setAttribute(String, Attribute)
	 */
	@Override
	public void setAttribute(String id, Attribute att)
						throws AttributeNotFoundException, IllegalArgumentException {
		throw new UnsupportedOperationException("TODO!");
	}
	
	/**
	 * @see org.graffiti.attributes.CompositeAttribute#getAttributes()
	 */
	@Override
	public CollectionAttribute getAttributes() {
		HashMapAttribute ret = new HashMapAttribute(getId());
		HashMapAttribute da = new HashMapAttribute("dasharray");
		float[] dashArray = this.dash.getDashArray();
		
		for (int i = dashArray.length - 1; i >= 0; i--) {
			da.add(new FloatAttribute("dash" + i, dashArray[i]));
		}
		
		FloatAttribute dp =
							new FloatAttribute("dashphase", this.dash.getDashPhase());
		
		ret.add(da);
		ret.add(dp);
		
		return ret;
	}
	
	/**
	 * Sets the dashArray of the encapsulated Dash to the given array.
	 * 
	 * @param da
	 *           ths new array to set the dashArray to.
	 */
	public void setDashArray(float[] da) {
		boolean allZero = true;
		if (da == null)
			dash.setDashArray(da);
		else {
			for (float t : da) {
				if (Math.abs(t) > 0.000001) {
					allZero = false;
					break;
				}
			}
			if (!allZero)
				this.dash.setDashArray(da);
			else
				setDefaultValue();
		}
	}
	
	/**
	 * Returns the dashArray of the encapsulated Dash.
	 * 
	 * @return the dashArray of the encapsulated Dash.
	 */
	public float[] getDashArray() {
		return dash.getDashArray();
	}
	
	/**
	 * Sets the dashPhase of the encapsulated Dash to the given value.
	 * 
	 * @param dp
	 *           the new value for the dashPhase.
	 */
	public void setDashPhase(float dp) {
		this.dash.setDashPhase(dp);
	}
	
	/**
	 * Returns the dashPhase of the encapsulated Dash.
	 * 
	 * @return the dashPhase of the encapsulated Dash.
	 */
	public float getDashPhase() {
		return dash.getDashPhase();
	}
	
	/**
	 * @see org.graffiti.attributes.Attribute#setDefaultValue()
	 */
	public void setDefaultValue() {
		if (dash == null) {
			dash = new Dash();
		}
	}
	
	/**
	 * Returns a dash object contained in this attribute.
	 * 
	 * @return a dash object contained in this attribute.
	 */
	public Object getValue() {
		return dash;
	}
	
	/**
	 * Returns a deep copy of this <code>Attribute</code>.
	 * 
	 * @return a deep copy of this <code>Attribute</code>.
	 * @see org.graffiti.core.DeepCopy#copy()
	 */
	public Object copy() {
		float[] oldDA = dash.getDashArray();
		
		if (oldDA == null) {
			return new LineModeAttribute(idd, new Dash(null, dash.getDashPhase()));
		} else {
			float[] newDA = new float[oldDA.length];
			
			for (int i = oldDA.length - 1; i >= 0; i--) {
				newDA[i] = oldDA[i];
			}
			
			return new LineModeAttribute(idd,
								new Dash(newDA, dash.getDashPhase()));
		}
	}
	
	/**
	 * @see org.graffiti.attributes.AbstractAttribute#doSetValue(Object)
	 */
	@Override
	protected void doSetValue(Object v)
						throws IllegalArgumentException {
		try {
			if (v instanceof String) {
				String val = (String) v;
				String[] values = val.split(" ");
				if (values.length >= 2) {
					setDashPhase(Float.parseFloat(values[values.length - 1])); // last value is dash phase, see GML writer
					float[] dashArr = new float[values.length - 1];
					for (int i = 0; i < values.length - 1; i++)
						dashArr[i] = Float.parseFloat(values[i]);
					setDashArray(dashArr);
				}
			} else
				this.dash = (Dash) v;
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException(cce.getMessage());
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
