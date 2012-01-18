/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (16.12.2001 22:19:34)
 * 
 * @author:
 */
public class AttributsClass {
	public java.util.Vector<AttributsAttribut> attributs;
	
	/**
	 * ClassAttributs constructor comment.
	 */
	public AttributsClass() {
		super();
		attributs = new java.util.Vector<AttributsAttribut>();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:35:04)
	 * 
	 * @param attributsOneAttribut
	 *           qmwi.kseq.som.processing.AttributsOneAttribut
	 */
	public void addAttribut(AttributsAttribut a) {
		
		attributs.add(a);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:33:16)
	 * 
	 * @return qmwi.kseq.som.processing.AttributsOneAttribut
	 * @param attributsOneAttributIndex
	 *           int
	 */
	public AttributsAttribut getAttribut(int attributIndex) {
		
		if (attributs.size() != 0)
			return (AttributsAttribut) attributs.elementAt(attributIndex);
		
		else
			return null;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:31:32)
	 * 
	 * @param attributIndex
	 *           int
	 * @param valueIndex
	 *           int
	 */
	public double getAttributValueCount(int attributIndex, int valueIndex) {
		
		// Aus.a("attributs.size()",attributs.size());
		
		if (attributIndex < attributs.size())
			return ((AttributsAttribut) attributs.elementAt(attributIndex)).getValueCount(valueIndex);
		
		else
			return 0;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:58:06)
	 * 
	 * @return int
	 */
	public int getCountAttributs() {
		return attributs.size();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:35:04)
	 * 
	 * @param attributsOneAttribut
	 *           qmwi.kseq.som.processing.AttributsOneAttribut
	 */
	public void setAttribut(int attributIndex, AttributsAttribut a) {
		
		attributs.add(attributIndex, a);
		
	}
}
