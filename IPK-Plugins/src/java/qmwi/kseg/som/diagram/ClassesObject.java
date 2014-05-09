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
public class ClassesObject {
	java.lang.String[] attributs = null;
	double skalar = 0;
	double[] inputVector = null;
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:54:25)
	 * 
	 * @param count
	 *           int
	 */
	public ClassesObject(int count) {
		super();
		attributs = new String[count];
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:20:46)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public String getAttribut(int i) {
		return attributs[i];
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:50:28)
	 * 
	 * @return java.lang.String[]
	 */
	public String[] getAttributs() {
		return attributs;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:22:53)
	 * 
	 * @param size
	 *           int
	 */
	public int getCountAttributs() {
		return attributs.length;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public double[] getInputVector() {
		
		return inputVector;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public double getInputVectorValue(int inputVectorIndex) {
		
		return inputVector[inputVectorIndex];
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public double getSkalar() {
		
		return skalar;
		
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
	public void setAttribut(int i, String a) {
		attributs[i] = a;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public void setAttributs(String[] a) {
		attributs = a;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public void setInputVector(double[] iv) {
		
		inputVector = iv;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (10.12.2001 18:52:10)
	 * 
	 * @param attribs
	 *           java.lang.String[]
	 */
	public void setSkalar(ClassesAll ca) {
		
		double sum = 0;
		
		for (int i = 0; i < ca.getCountAttributsSelected(); i++) {
			
			try {
				
				sum += Math.pow(Double.parseDouble(getAttribut(ca.getAttributSelectedIndex(i))), 2.0);
				
			}

			catch (NumberFormatException e) {
				
				// Aus.ac("keine Zahl");
				
			}
			
		}
		
		if (sum > 0)
			skalar = 1 / Math.sqrt(sum);
		// else Aus.ac("keine Zahlen");
		
	}
}
