/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class ReferencingPolygon extends PolygonInterface {
	private long swigCPtr;
	
	protected ReferencingPolygon(long cPtr, boolean cMemoryOwn) {
		super(colaJNI.SWIGReferencingPolygonUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(ReferencingPolygon obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
	
	@Override
	protected void finalize() {
		delete();
	}
	
	@Override
	public synchronized void delete() {
		if (swigCPtr != 0 && swigCMemOwn) {
			swigCMemOwn = false;
			colaJNI.delete_ReferencingPolygon(swigCPtr);
		}
		swigCPtr = 0;
		super.delete();
	}
	
	public ReferencingPolygon() {
		this(colaJNI.new_ReferencingPolygon__SWIG_0(), true);
	}
	
	public ReferencingPolygon(Polygon poly, Router router) {
		this(colaJNI.new_ReferencingPolygon__SWIG_1(Polygon.getCPtr(poly), poly, Router.getCPtr(router), router), true);
	}
	
	@Override
	public void clear() {
		colaJNI.ReferencingPolygon_clear(swigCPtr, this);
	}
	
	@Override
	public boolean empty() {
		return colaJNI.ReferencingPolygon_empty(swigCPtr, this);
	}
	
	@Override
	public int size() {
		return colaJNI.ReferencingPolygon_size(swigCPtr, this);
	}
	
	@Override
	public int id() {
		return colaJNI.ReferencingPolygon_id(swigCPtr, this);
	}
	
	@Override
	public Point at(int index) {
		return new Point(colaJNI.ReferencingPolygon_at(swigCPtr, this, index), false);
	}
	
	public void set_id(int value) {
		colaJNI.ReferencingPolygon__id_set(swigCPtr, this, value);
	}
	
	public int get_id() {
		return colaJNI.ReferencingPolygon__id_get(swigCPtr, this);
	}
	
	public void setPs(SWIGTYPE_p_std__vectorTstd__pairTAvoid__Polygon_const_p_unsigned_short_t_t value) {
		colaJNI.ReferencingPolygon_ps_set(swigCPtr, this, SWIGTYPE_p_std__vectorTstd__pairTAvoid__Polygon_const_p_unsigned_short_t_t.getCPtr(value));
	}
	
	public SWIGTYPE_p_std__vectorTstd__pairTAvoid__Polygon_const_p_unsigned_short_t_t getPs() {
		long cPtr = colaJNI.ReferencingPolygon_ps_get(swigCPtr, this);
		return (cPtr == 0) ? null : new SWIGTYPE_p_std__vectorTstd__pairTAvoid__Polygon_const_p_unsigned_short_t_t(cPtr, false);
	}
	
}
