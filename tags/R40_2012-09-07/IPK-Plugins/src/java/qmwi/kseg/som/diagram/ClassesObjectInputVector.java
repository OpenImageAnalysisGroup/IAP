/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (10.12.2001 18:46:17)
 * 
 * @author:
 */
public class ClassesObjectInputVector {
	private int classIndex = 0;
	private double[] inputVector = null;
	private int objectIndex = 0;
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:54:25)
	 * 
	 * @param count
	 *           int
	 */
	public ClassesObjectInputVector() {
		
		super();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:54:25)
	 * 
	 * @param count
	 *           int
	 */
	public ClassesObjectInputVector(int c, int o, double[] iv) {
		
		super();
		classIndex = c;
		objectIndex = o;
		inputVector = iv;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public int getClassIndex() {
		
		return classIndex;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public double[] getInputVector() {
		
		return inputVector;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public int getInputVectorSize() {
		
		return inputVector.length;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public int getObjectIndex() {
		
		return objectIndex;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public void setClassIndex(int c) {
		
		classIndex = c;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public void setInputVector(double[] iv) {
		
		inputVector = iv;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public void setObjectIndex(int o) {
		
		objectIndex = o;
		
	}
}
