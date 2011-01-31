/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2005 by Christian Klukas
 */
package org;

public class ObjectRef implements HelperClass {
	
	private Object data;
	private String toStringVal = null;
	
	public ObjectRef() {
		// empty
	}
	
	public ObjectRef(String toStringDef, Object initData) {
		this.toStringVal = toStringDef;
		this.data = initData;
	}
	
	@Override
	public String toString() {
		if (toStringVal == null)
			return super.toString();
		else
			return toStringVal;
	}
	
	public synchronized void setObject(Object data) {
		this.data = data;
	}
	
	public synchronized Object getObject() {
		return data;
	}
	
	public synchronized void setIfGreater(int v) {
		if ((Integer) data < v)
			data = v;
	}
	
	public synchronized void setIfLess(int v) {
		if ((Integer) data > v)
			data = v;
	}
	
	public synchronized void addLong(long v) {
		if (data == null)
			data = v;
		else
			data = (Long) data + v;
	}
	
	public synchronized Long getLong() {
		return (Long) data;
	}
}
