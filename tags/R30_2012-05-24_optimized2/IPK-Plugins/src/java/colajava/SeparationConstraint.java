/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class SeparationConstraint extends CompoundConstraint {
	private long swigCPtr;
	
	protected SeparationConstraint(long cPtr, boolean cMemoryOwn) {
		super(colaJNI.SWIGSeparationConstraintUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(SeparationConstraint obj) {
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
			colaJNI.delete_SeparationConstraint(swigCPtr);
		}
		swigCPtr = 0;
		super.delete();
	}
	
	public SeparationConstraint(long l, long r, double g, boolean equality) {
		this(colaJNI.new_SeparationConstraint__SWIG_0(l, r, g, equality), true);
	}
	
	public SeparationConstraint(long l, long r, double g) {
		this(colaJNI.new_SeparationConstraint__SWIG_1(l, r, g), true);
	}
	
	public SeparationConstraint(AlignmentConstraint l, AlignmentConstraint r, double g, boolean equality) {
		this(colaJNI.new_SeparationConstraint__SWIG_2(AlignmentConstraint.getCPtr(l), l, AlignmentConstraint.getCPtr(r), r, g, equality), true);
	}
	
	public SeparationConstraint(AlignmentConstraint l, AlignmentConstraint r, double g) {
		this(colaJNI.new_SeparationConstraint__SWIG_3(AlignmentConstraint.getCPtr(l), l, AlignmentConstraint.getCPtr(r), r, g), true);
	}
	
	public void setLeft(long value) {
		colaJNI.SeparationConstraint_left_set(swigCPtr, this, value);
	}
	
	public long getLeft() {
		return colaJNI.SeparationConstraint_left_get(swigCPtr, this);
	}
	
	public void setRight(long value) {
		colaJNI.SeparationConstraint_right_set(swigCPtr, this, value);
	}
	
	public long getRight() {
		return colaJNI.SeparationConstraint_right_get(swigCPtr, this);
	}
	
	public void setAl(AlignmentConstraint value) {
		colaJNI.SeparationConstraint_al_set(swigCPtr, this, AlignmentConstraint.getCPtr(value), value);
	}
	
	public AlignmentConstraint getAl() {
		long cPtr = colaJNI.SeparationConstraint_al_get(swigCPtr, this);
		return (cPtr == 0) ? null : new AlignmentConstraint(cPtr, false);
	}
	
	public void setAr(AlignmentConstraint value) {
		colaJNI.SeparationConstraint_ar_set(swigCPtr, this, AlignmentConstraint.getCPtr(value), value);
	}
	
	public AlignmentConstraint getAr() {
		long cPtr = colaJNI.SeparationConstraint_ar_get(swigCPtr, this);
		return (cPtr == 0) ? null : new AlignmentConstraint(cPtr, false);
	}
	
	public void setGap(double value) {
		colaJNI.SeparationConstraint_gap_set(swigCPtr, this, value);
	}
	
	public double getGap() {
		return colaJNI.SeparationConstraint_gap_get(swigCPtr, this);
	}
	
	public void setEquality(boolean value) {
		colaJNI.SeparationConstraint_equality_set(swigCPtr, this, value);
	}
	
	public boolean getEquality() {
		return colaJNI.SeparationConstraint_equality_get(swigCPtr, this);
	}
	
	@Override
	public void generateVariables(SWIGTYPE_p_std__vectorTvpsc__Variable_p_t vars) {
		colaJNI.SeparationConstraint_generateVariables(swigCPtr, this, SWIGTYPE_p_std__vectorTvpsc__Variable_p_t.getCPtr(vars));
	}
	
	@Override
	public void generateSeparationConstraints(SWIGTYPE_p_std__vectorTvpsc__Variable_p_t vs, SWIGTYPE_p_std__vectorTvpsc__Constraint_p_t cs) {
		colaJNI.SeparationConstraint_generateSeparationConstraints(swigCPtr, this, SWIGTYPE_p_std__vectorTvpsc__Variable_p_t.getCPtr(vs),
							SWIGTYPE_p_std__vectorTvpsc__Constraint_p_t.getCPtr(cs));
	}
	
	public void setSeparation(double gap) {
		colaJNI.SeparationConstraint_setSeparation(swigCPtr, this, gap);
	}
	
	public void setVpscConstraint(SWIGTYPE_p_vpsc__Constraint value) {
		colaJNI.SeparationConstraint_vpscConstraint_set(swigCPtr, this, SWIGTYPE_p_vpsc__Constraint.getCPtr(value));
	}
	
	public SWIGTYPE_p_vpsc__Constraint getVpscConstraint() {
		long cPtr = colaJNI.SeparationConstraint_vpscConstraint_get(swigCPtr, this);
		return (cPtr == 0) ? null : new SWIGTYPE_p_vpsc__Constraint(cPtr, false);
	}
	
}