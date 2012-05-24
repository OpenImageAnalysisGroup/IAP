/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (17.12.2001 01:23:56)
 * 
 * @author:
 */
public class AttributsAttribut {
	@SuppressWarnings("unchecked")
	public java.util.Vector valuesPercent;
	public float average = 0;
	public java.util.Vector<Integer> valuesCount;
	
	/**
	 * AttributsOneAttribut constructor comment.
	 */
	@SuppressWarnings("unchecked")
	public AttributsAttribut() {
		super();
		valuesPercent = new java.util.Vector();
		valuesCount = new java.util.Vector<Integer>();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:45:47)
	 * 
	 * @return java.lang.String
	 * @param valueIndex
	 *           int
	 */
	public double getValueCount(int valueIndex) {
		
		if (valueIndex < valuesCount.size()) {
			
			return (((Integer) valuesCount.elementAt(valueIndex)).doubleValue());
			
			// Aus.a("valueCount",((Integer) valuesCount.elementAt(valueIndex)).doubleValue());
			
		}

		else {
			
			// Aus.a("keine Anzahl an Werten vorhanden");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 00:34:21)
	 * 
	 * @param valuesIndex
	 *           int
	 */
	public void initializeValueCount(int count) {
		
		// Aus.a("count",count);
		
		for (int i = 0; i < count; i++) {
			
			valuesCount.add(new Integer(0));
			
		}
		
		// Aus.a("valuesCount.size()",valuesCount.size());
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 00:34:21)
	 * 
	 * @param valuesIndex
	 *           int
	 */
	public void inkValueCount(int valuesIndex) {
		
		// Aus.a("valuesCount.size()",valuesCount.size());
		
		if (valuesIndex < valuesCount.size())
			;
		else {
			
			valuesCount.add(new Integer(0));
			
			// Aus.a("neuer Wert");
			
		}
		
		// Aus.a("valuesIndex",valuesIndex);
		
		// Aus.a("valuesCount.size()",valuesCount.size());
		
		int count = ((Integer) valuesCount.elementAt(valuesIndex)).intValue() + 1;
		
		valuesCount.setElementAt(new Integer(count), valuesIndex);
		
		/*
		 * if (valuesCount.size()==0 || valuesCount.size()-1<valuesIndex) valuesCount.add(new Integer(1));
		 * else {
		 * int count = ((Integer) valuesCount.elementAt(valuesIndex)).intValue()+1;
		 * valuesCount.setElementAt(new Integer(count),valuesIndex);
		 * }
		 */

	}
}
