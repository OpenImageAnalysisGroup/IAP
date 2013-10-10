/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class AlignmentConstraint extends CompoundConstraint {
	private long swigCPtr;
	
	protected AlignmentConstraint(long cPtr, boolean cMemoryOwn) {
		super(colaJNI.SWIGAlignmentConstraintUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(AlignmentConstraint obj) {
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
			colaJNI.delete_AlignmentConstraint(swigCPtr);
		}
		swigCPtr = 0;
		super.delete();
	}
	
	public AlignmentConstraint(double pos) {
		this(colaJNI.new_AlignmentConstraint(pos), true);
	}
	
	@Override
	public void updatePosition() {
		colaJNI.AlignmentConstraint_updatePosition(swigCPtr, this);
	}
	
	public void fixPos(double pos) {
		colaJNI.AlignmentConstraint_fixPos(swigCPtr, this, pos);
	}
	
	public void unfixPos() {
		colaJNI.AlignmentConstraint_unfixPos(swigCPtr, this);
	}
	
	public void setOffsets(OffsetList value) {
		colaJNI.AlignmentConstraint_offsets_set(swigCPtr, this, OffsetList.getCPtr(value), value);
	}
	
	public OffsetList getOffsets() {
		long cPtr = colaJNI.AlignmentConstraint_offsets_get(swigCPtr, this);
		return (cPtr == 0) ? null : new OffsetList(cPtr, false);
	}
	
	public void setGuide(SWIGTYPE_p_void value) {
		colaJNI.AlignmentConstraint_guide_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
	}
	
	public SWIGTYPE_p_void getGuide() {
		long cPtr = colaJNI.AlignmentConstraint_guide_get(swigCPtr, this);
		return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
	}
	
	public void setPosition(double value) {
		colaJNI.AlignmentConstraint_position_set(swigCPtr, this, value);
	}
	
	public double getPosition() {
		return colaJNI.AlignmentConstraint_position_get(swigCPtr, this);
	}
	
	public void setIsFixed(boolean value) {
		colaJNI.AlignmentConstraint_isFixed_set(swigCPtr, this, value);
	}
	
	public boolean getIsFixed() {
		return colaJNI.AlignmentConstraint_isFixed_get(swigCPtr, this);
	}
	
	@Override
	public void generateVariables(SWIGTYPE_p_std__vectorTvpsc__Variable_p_t vars) {
		colaJNI.AlignmentConstraint_generateVariables(swigCPtr, this, SWIGTYPE_p_std__vectorTvpsc__Variable_p_t.getCPtr(vars));
	}
	
	@Override
	public void generateSeparationConstraints(SWIGTYPE_p_std__vectorTvpsc__Variable_p_t vars, SWIGTYPE_p_std__vectorTvpsc__Constraint_p_t cs) {
		colaJNI.AlignmentConstraint_generateSeparationConstraints(swigCPtr, this, SWIGTYPE_p_std__vectorTvpsc__Variable_p_t.getCPtr(vars),
							SWIGTYPE_p_std__vectorTvpsc__Constraint_p_t.getCPtr(cs));
	}
	
	public void setVariable(SWIGTYPE_p_vpsc__Variable value) {
		colaJNI.AlignmentConstraint_variable_set(swigCPtr, this, SWIGTYPE_p_vpsc__Variable.getCPtr(value));
	}
	
	public SWIGTYPE_p_vpsc__Variable getVariable() {
		long cPtr = colaJNI.AlignmentConstraint_variable_get(swigCPtr, this);
		return (cPtr == 0) ? null : new SWIGTYPE_p_vpsc__Variable(cPtr, false);
	}
	
}