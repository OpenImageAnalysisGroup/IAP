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
public class ClassesNeuron {
	private double[] weights = null;
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:54:25)
	 * 
	 * @param count
	 *           int
	 */
	public ClassesNeuron() {
		
		super();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:54:25)
	 * 
	 * @param count
	 *           int
	 */
	public ClassesNeuron(int count) {
		
		super();
		weights = new double[count];
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:22:53)
	 * 
	 * @param size
	 *           int
	 */
	public int getCountWeights() {
		return weights.length;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public double getWeight(int i) {
		return weights[i];
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:50:28)
	 * 
	 * @return java.lang.String[]
	 */
	public double[] getWeights() {
		return weights;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:21:37)
	 * 
	 * @param i
	 *           int
	 * @param stringTemp
	 *           java.lang.String
	 */
	public void setWeight(int i, double w) {
		weights[i] = w;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public void setWeights(double[] w) {
		weights = w;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:21:37)
	 * 
	 * @param i
	 *           int
	 * @param stringTemp
	 *           java.lang.String
	 */
	public void setWeightSize(int size) {
		
		weights = new double[size];
		
	}
}
