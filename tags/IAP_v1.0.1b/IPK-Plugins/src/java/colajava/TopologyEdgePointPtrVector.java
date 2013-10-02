/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class TopologyEdgePointPtrVector {
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected TopologyEdgePointPtrVector(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(TopologyEdgePointPtrVector obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
	
	@Override
	protected void finalize() {
		delete();
	}
	
	public synchronized void delete() {
		if (swigCPtr != 0 && swigCMemOwn) {
			swigCMemOwn = false;
			colaJNI.delete_TopologyEdgePointPtrVector(swigCPtr);
		}
		swigCPtr = 0;
	}
	
	public TopologyEdgePointPtrVector() {
		this(colaJNI.new_TopologyEdgePointPtrVector__SWIG_0(), true);
	}
	
	public TopologyEdgePointPtrVector(long n) {
		this(colaJNI.new_TopologyEdgePointPtrVector__SWIG_1(n), true);
	}
	
	public long size() {
		return colaJNI.TopologyEdgePointPtrVector_size(swigCPtr, this);
	}
	
	public long capacity() {
		return colaJNI.TopologyEdgePointPtrVector_capacity(swigCPtr, this);
	}
	
	public void reserve(long n) {
		colaJNI.TopologyEdgePointPtrVector_reserve(swigCPtr, this, n);
	}
	
	public boolean isEmpty() {
		return colaJNI.TopologyEdgePointPtrVector_isEmpty(swigCPtr, this);
	}
	
	public void clear() {
		colaJNI.TopologyEdgePointPtrVector_clear(swigCPtr, this);
	}
	
	public void add(EdgePoint x) {
		colaJNI.TopologyEdgePointPtrVector_add(swigCPtr, this, EdgePoint.getCPtr(x));
	}
	
	public EdgePoint get(int i) {
		long cPtr = colaJNI.TopologyEdgePointPtrVector_get(swigCPtr, this, i);
		return (cPtr == 0) ? null : new EdgePoint(cPtr, false);
	}
	
	public void set(int i, EdgePoint x) {
		colaJNI.TopologyEdgePointPtrVector_set(swigCPtr, this, i, EdgePoint.getCPtr(x));
	}
	
}