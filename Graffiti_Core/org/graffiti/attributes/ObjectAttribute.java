/*
 * Created on 15.01.2004
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.graffiti.attributes;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ObjectAttribute extends StringAttribute {
	Object myValue;
	
	@Override
	public void setString(String value) {
		// assert value!=null;
		myValue = value;
		super.setString(value);
	}
	
	@Override
	public String getString() {
		if (myValue == null)
			return null;
		return myValue.toString();
	}
	
	/**
	 * @param id
	 * @throws IllegalIdException
	 */
	public ObjectAttribute(String id) throws IllegalIdException {
		super(id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.attributes.AbstractAttribute#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		// assert v!=null;
		myValue = v;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.attributes.Attribute#setDefaultValue()
	 */
	@Override
	public void setDefaultValue() {
		myValue = new Object();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.attributes.Attribute#getValue()
	 */
	@Override
	public Object getValue() {
		return myValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.core.DeepCopy#copy()
	 */
	@Override
	public Object copy() {
		ObjectAttribute oa = new ObjectAttribute(getId());
		oa.setString(getString());
		return oa;
	}
	
}
