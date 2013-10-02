/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class ClusterVector {
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected ClusterVector(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(ClusterVector obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
	
	@Override
	protected void finalize() {
		delete();
	}
	
	public synchronized void delete() {
		if (swigCPtr != 0 && swigCMemOwn) {
			swigCMemOwn = false;
			colaJNI.delete_ClusterVector(swigCPtr);
		}
		swigCPtr = 0;
	}
	
	public ClusterVector() {
		this(colaJNI.new_ClusterVector__SWIG_0(), true);
	}
	
	public ClusterVector(long n) {
		this(colaJNI.new_ClusterVector__SWIG_1(n), true);
	}
	
	public long size() {
		return colaJNI.ClusterVector_size(swigCPtr, this);
	}
	
	public long capacity() {
		return colaJNI.ClusterVector_capacity(swigCPtr, this);
	}
	
	public void reserve(long n) {
		colaJNI.ClusterVector_reserve(swigCPtr, this, n);
	}
	
	public boolean isEmpty() {
		return colaJNI.ClusterVector_isEmpty(swigCPtr, this);
	}
	
	public void clear() {
		colaJNI.ClusterVector_clear(swigCPtr, this);
	}
	
	public void add(Cluster x) {
		colaJNI.ClusterVector_add(swigCPtr, this, Cluster.getCPtr(x));
	}
	
	public Cluster get(int i) {
		long cPtr = colaJNI.ClusterVector_get(swigCPtr, this, i);
		return (cPtr == 0) ? null : new Cluster(cPtr, false);
	}
	
	public void set(int i, Cluster x) {
		colaJNI.ClusterVector_set(swigCPtr, this, i, Cluster.getCPtr(x));
	}
	
}