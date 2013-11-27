/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (16.12.2001 18:09:24)
 * 
 * @author:
 */
@SuppressWarnings("unchecked")
public class AttributsAll {
	java.util.Vector allAs = null;
	
	public java.util.Vector attributsValues;
	
	public java.util.Vector valuesSortIndex;
	
	/**
	 * AllAttributs constructor comment.
	 */
	public AttributsAll() {
		super();
		allAs = new java.util.Vector();
	}
	
	/**
	 * AllAttributs constructor comment.
	 */
	public AttributsAll(ClassesAll cAll) {
		super();
		allAs = new java.util.Vector();
		attributsValues = new java.util.Vector();
		valuesSortIndex = new java.util.Vector();
		addValues(cAll);
		sortValues();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 23:01:53)
	 * 
	 * @return java.lang.String
	 * @param classIndex
	 *           int
	 * @param attributIndex
	 *           int
	 * @param valueIndex
	 *           int
	 */
	public int addAttributsValue(int attributIndex, String value) {
		
		if (value == null)
			value = "";
		
		if (attributIndex < attributsValues.size()) {
			
			java.util.Vector values = (java.util.Vector) attributsValues.elementAt(attributIndex);
			
			int index = -1;
			
			for (int i = 0; i < values.size(); i++) {
				
				if (value.equalsIgnoreCase((String) values.elementAt(i))) {
					
					index = i;
					
					// Aus.a("Attributauspr�gung schon vorhanden");
					
				}
				
			}
			
			if (index == -1) {
				
				values.add(value);
				
				index = values.size() - 1;
				
				// Aus.a("Attributauspr�gung hinzugef�gt");
				
			}
			
			// Aus.a("index",index);
			
			return index;
			
		}

		else {
			
			// Aus.a("nicht soviele Attribute vorhanden");
			
			java.util.Vector v = new java.util.Vector();
			
			v.add(value);
			
			attributsValues.add(v);
			
			// Aus.a("neues Attribut hinzugef�gt");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 18:12:31)
	 * 
	 * @param attribut
	 *           qmwi.kseq.som.processing.OneAttribut
	 */
	public void addClass(AttributsClass c) {
		allAs.add(c);
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 00:21:01)
	 * 
	 * @param cAll
	 *           qmwi.kseg.som.processing.ClassesAll
	 */
	public void addValues(ClassesAll cAll) {
		
		for (int ic = 0; ic < cAll.getCountClasses(); ic++) {
			
			// Aus.a("ic",ic);
			
			// beinhaltet f�r alle Attribute die Werte
			
			AttributsClass ac = new AttributsClass();
			
			// speichert die Objekte einer Segmentklasse
			
			ClassesClass cc = cAll.getClass(ic);
			
			// Aus.a("cc",cc);
			
			// Aus.a("cc.getCountObjects()",cc.getCountObjects());
			
			for (int ia = 0; ia < cAll.getCountAttributs(); ia++) {
				
				// Sind keine Objekte in der Klasse sind auch keine Objekte vorhanden
				
				if (cc.getCountObjects() == 0)
					break;
				
				// Aus.a("   ia",ia);
				
				// speichert Attributwerte eines bestimmten Attribut f�r eine Klasse
				
				AttributsAttribut aa = new AttributsAttribut();
				
				// initialisieren der Attributsz�hlung
				
				aa.initializeValueCount(getCountAttributValues(ia));
				
				for (int io = 0; io < cc.getCountObjects(); io++) {
					
					// Hinzuf�gen einer Attributsauspr�gung
					
					int valueIndex = addAttributsValue(ia, cc.getObject(io).getAttribut(ia));
					
					// Aus.a("cc.getObject(0).getAttribut(ia)",cc.getObject(0).getAttribut(ia));
					
					// Aus.a("getAttributsValue(ia, valueIndex)",getAttributsValue(ia, valueIndex));
					
					// Aktualisierung der Anzahl der einer bestimmten Attributsauspr�gung
					
					aa.inkValueCount(valueIndex);
					
					// Aus.a("inkValueCount(valueIndex)",aa.getValueCount(valueIndex));
					
				}
				
				// Attributs hinzuf�gen
				
				ac.addAttribut(aa);
				
				// Aus.a();
				
			}
			
			// Alle Attribute der Klasse hinzuf�gen
			
			allAs.add(ac);
			
		}
		
		// System.out.println("fertig");
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 18:11:07)
	 * 
	 * @return qmwi.kseq.som.processing.OneAttribut
	 */
	public AttributsAttribut getAttribut(int classIndex, int attributeIndex) {
		
		if (classIndex < allAs.size())
			return ((AttributsClass) allAs.elementAt(classIndex)).getAttribut(attributeIndex);
		
		else {
			
			Aus.a("keine Attributsklassen vorhanden");
			
			return null;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 23:01:53)
	 * 
	 * @return java.lang.String
	 * @param classIndex
	 *           int
	 * @param attributIndex
	 *           int
	 * @param valueIndex
	 *           int
	 */
	public String getAttributsValue(int attributIndex, int valueIndex) {
		
		if (attributIndex < attributsValues.size()) {
			
			java.util.Vector values = (java.util.Vector) attributsValues.elementAt(attributIndex);
			
			if (valueIndex < values.size())
				return (String) values.elementAt(valueIndex);
			
			else {
				
				Aus.a("nicht soviele Werte vorhanden");
				
				return "";
				
			}
			
		}

		else {
			
			Aus.a("nicht soviele Attribute vorhanden");
			
			return "";
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 23:01:53)
	 * 
	 * @return java.lang.String
	 * @param classIndex
	 *           int
	 * @param attributIndex
	 *           int
	 * @param valueIndex
	 *           int
	 */
	public java.util.Vector getAttributsValues(int attributIndex) {
		
		if (attributIndex < attributsValues.size()) {
			
			java.util.Vector values = (java.util.Vector) attributsValues.elementAt(attributIndex);
			
			return values;
			
		}

		else {
			
			Aus.a("nicht soviele Attribute vorhanden");
			
			return null;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 23:01:53)
	 * 
	 * @return java.lang.String
	 * @param classIndex
	 *           int
	 * @param attributIndex
	 *           int
	 * @param valueIndex
	 *           int
	 */
	public double getAttributValueCount(int classIndex, int attributIndex, int valueIndex) {
		
		// Aus.a("(AttributsClass) allAs.elementAt(classIndex)",(AttributsClass) allAs.elementAt(classIndex));
		
		if (classIndex < allAs.size())
			return ((AttributsClass) allAs.elementAt(classIndex)).getAttributValueCount(attributIndex, valueIndex);
		
		else {
			
			Aus.a("keine Attributsklassen vorhanden");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:31:48)
	 * 
	 * @return qmwi.kseq.som.processing.AttributsOneClass
	 * @param attributesOneClassIndex
	 *           int
	 */
	public AttributsClass getClass(int classIndex) {
		
		if (classIndex < allAs.size())
			return (AttributsClass) allAs.elementAt(classIndex);
		
		else {
			
			Aus.a("keine Attributsklassen vorhanden");
			
			return null;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 02:54:16)
	 * 
	 * @return int
	 */
	public int getCountAttributs() {
		
		for (int i = 0; i < getCountClasses(); i++) {
			
			int temp = getCountAttributs(i);
			
			if (temp != 0)
				return temp;
			
		}
		
		Aus.a("keine Attributsklassen vorhanden");
		
		return 0;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 21:19:44)
	 * 
	 * @param classIndex
	 *           int
	 */
	public int getCountAttributs(int classIndex) {
		
		if (classIndex < allAs.size())
			return (getClass(classIndex)).getCountAttributs();
		
		else {
			
			Aus.a("keine Attributsklassen vorhanden");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 21:19:44)
	 * 
	 * @param classIndex
	 *           int
	 */
	public int getCountAttributValues(int attributeIndex) {
		
		if (attributeIndex < attributsValues.size()) {
			
			java.util.Vector v = (java.util.Vector) attributsValues.elementAt(attributeIndex);
			
			return v.size();
			
		}

		else {
			
			// Aus.a("keine Attributsklassen vorhanden");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 02:54:16)
	 * 
	 * @return int
	 */
	public int getCountClasses() {
		return allAs.size();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 00:51:47)
	 * 
	 * @return int
	 * @param str
	 *           java.lang.String
	 */
	public int getSortValuesIndex(int attributIndex, int valueIndex) {
		
		if (attributIndex < attributsValues.size()) {
			
			int[] iPos = (int[]) valuesSortIndex.elementAt(attributIndex);
			
			if (valueIndex < iPos.length)
				return iPos[valueIndex];
			
			else {
				
				Aus.a("nicht soviele Werte vorhanden");
				
				return 0;
				
			}
			
		}

		else {
			
			Aus.a("keine Attribute");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 00:51:47)
	 * 
	 * @return int
	 * @param str
	 *           java.lang.String
	 */
	public int getValuesIndex(int attributsIndex, String str) {
		
		if (attributsIndex < attributsValues.size()) {
			
			java.util.Vector values = (java.util.Vector) attributsValues.elementAt(attributsIndex);
			
			int index = -1;
			
			for (int i = 0; i < values.size(); i++) {
				
				if (str.equalsIgnoreCase((String) values.elementAt(i)))
					index = i;
				
			}
			
			if (index == -1) {
				
				values.addElement(str);
				
				index = values.size() - 1;
				
			}
			
			Aus.a("index", index);
			
			return index;
			
		}

		else {
			
			Aus.a("nicht soviele Attribute vorhanden");
			
			java.util.Vector v = new java.util.Vector();
			
			v.add(str);
			
			attributsValues.add(v);
			
			Aus.a("neues Attribut hinzugef�gt");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 00:51:47)
	 * 
	 * @return int
	 * @param str
	 *           java.lang.String
	 */
	public void sortValues() {
		
		for (int a = 0; a < getCountAttributs(); a++) {
			
			int[] ipos = null;
			
			// Aus.ac("Attribut  ",a);
			
			java.util.Vector org = getAttributsValues(a);
			
			for (int i = 0; i < org.size(); i++) {
				
				// Aus.ac(System.out.print((String) org.elementAt(i)+"  ");
				
			}
			
			ipos = new int[org.size()];
			
			for (int i = 0; i < ipos.length; i++) {
				
				ipos[i] = i;
				
			}
			
			// Aus.ac("ipos.length ",ipos.length);
			
			for (int i = 0; i < ipos.length; i++) {
				
				// System.out.print(ipos[i]+"  ");
				
			}
			
			// System.out.println();
			
			int ci = 0;
			
			for (int si = 0; si < org.size(); si++) {
				
				String temp = (String) org.elementAt(org.size() - 1);
				
				temp.trim();
				
				org.setElementAt(temp, org.size() - 1);
				
				java.util.Vector out = org;
				
				for (int i = si; i < out.size(); i++) {
					
					// System.out.print((String) out.elementAt(i)+"  ");
					
				}
				
				// System.out.println();
				
				ci = si;
				
				for (int i = si; i < org.size(); i++) {
					
					if (((String) org.elementAt(i)).compareTo((String) org.elementAt(ci)) < 0)
						ci = i;
					
				}
				
				temp = (String) org.elementAt(si);
				
				org.setElementAt(org.elementAt(ci), si);
				
				org.setElementAt(temp, ci);
				
				int iTemp = ipos[si];
				
				ipos[si] = ipos[ci];
				
				ipos[ci] = iTemp;
				
			}
			
			// System.out.print("/");
			
			for (int i = 0; i < org.size(); i++) {
				
				// System.out.print((String) org.elementAt(i)+"/");
				
			}
			
			// System.out.println();
			
			// Aus.ac("ipos.length ",ipos.length);
			
			for (int i = 0; i < ipos.length; i++) {
				
				// System.out.print(ipos[i]+"/  ");
				
			}
			
			valuesSortIndex.add(ipos);
			
			// Aus.ac("Attribut "+a+" sortiert","");
			
		}
		
	}
}
