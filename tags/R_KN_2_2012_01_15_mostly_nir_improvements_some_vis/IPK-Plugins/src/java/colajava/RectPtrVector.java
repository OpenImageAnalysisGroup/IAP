/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class RectPtrVector {
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected RectPtrVector(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(RectPtrVector obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
	
	@Override
	protected void finalize() {
		delete();
	}
	
	public synchronized void delete() {
		if (swigCPtr != 0 && swigCMemOwn) {
			swigCMemOwn = false;
			colaJNI.delete_RectPtrVector(swigCPtr);
		}
		swigCPtr = 0;
	}
	
	public RectPtrVector() {
		this(colaJNI.new_RectPtrVector__SWIG_0(), true);
	}
	
	public RectPtrVector(long n) {
		this(colaJNI.new_RectPtrVector__SWIG_1(n), true);
	}
	
	public long size() {
		return colaJNI.RectPtrVector_size(swigCPtr, this);
	}
	
	public long capacity() {
		return colaJNI.RectPtrVector_capacity(swigCPtr, this);
	}
	
	public void reserve(long n) {
		colaJNI.RectPtrVector_reserve(swigCPtr, this, n);
	}
	
	public boolean isEmpty() {
		return colaJNI.RectPtrVector_isEmpty(swigCPtr, this);
	}
	
	public void clear() {
		colaJNI.RectPtrVector_clear(swigCPtr, this);
	}
	
	public void add(Rectangle x) {
		colaJNI.RectPtrVector_add(swigCPtr, this, Rectangle.getCPtr(x));
	}
	
	public Rectangle get(int i) {
		long cPtr = colaJNI.RectPtrVector_get(swigCPtr, this, i);
		return (cPtr == 0) ? null : new Rectangle(cPtr, false);
	}
	
	public void set(int i, Rectangle x) {
		colaJNI.RectPtrVector_set(swigCPtr, this, i, Rectangle.getCPtr(x));
	}
	
}