/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (13.12.2001 15:57:09)
 * 
 * @author:
 */
@SuppressWarnings("unchecked")
public class ClassesAll {
	protected java.util.Vector allCs = null;
	protected java.util.Vector attributsNames = null;
	protected java.util.Vector attributsSelected = null;
	protected static java.util.Vector inputVectors = new java.util.Vector();
	
	/**
	 * AllClasses constructor comment.
	 */
	public ClassesAll() {
		
		super();
		allCs = new java.util.Vector();
		attributsSelected = new java.util.Vector();
		attributsNames = new java.util.Vector();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 11:36:15)
	 */
	public void addAttributsNames(java.util.Vector attributsNamesTemp) {
		
		attributsNames = attributsNamesTemp;
		
	}
	
	/**
	 * Zuweisung der ausgew�hlte Elemente
	 * Speicherung der Indexe der ausgew�hlten Attribute
	 * der Parameter a ist ein Vector mit den Namen dieser Attribute
	 * Creation date: (17.12.2001 00:29:37)
	 */
	public void addAttributsSelected(java.util.Vector a) {
		
		for (int i = 0; i < a.size(); i++) {
			
			attributsSelected.addElement(new Integer(getAttributNameIndex((String) a.elementAt(i))));
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:00:27)
	 * 
	 * @param oneClass
	 *           com.sun.tools.doclets.oneone.OneOne
	 */
	public void addClass(ClassesClass c) {
		allCs.add(c);
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 22:09:45)
	 * 
	 * @param param
	 *           java.util.Vector[]
	 */
	public void addClasses(java.util.Vector[] classes) {
		
		// System.out.println("AllClasses addClasses - Hinzuf�gen Klassen  "+classes.length);
		
		// Anzahl der Eigenschaften
		int attributCount = 0;
		
		for (int i = 0; i < classes.length; i++) {
			
			if (classes[i].size() != 0) {
				attributCount = ((qmwi.kseg.som.CSV_SOM_dataEntry) classes[i].elementAt(0)).getColumnData().length;
				break;
			}
			
		}
		
		// Aus.a("attributCount",attributCount);
		
		// Alle Klassen hinf�gen
		for (int i = 0; i < classes.length; i++) {
			
			// System.out.println("AllClasses addClasses - Hinzuf�gen "+i+".Klasse");
			
			// System.out.println("AllClasses addClasses - TempKlasse erstellen");
			
			// neue tempor�re Klasse
			ClassesClass c = new ClassesClass();
			
			// System.out.println("AllClasses addClasses - Hinzuf�gen der Objekte");
			
			// Zuweisen der Objekte
			addObjects(c, classes[i], attributCount);
			
			// Hinzuf�gen der Klasse
			addClass(c);
			
			// System.out.println("AllClasses addClasses - Hinzuf�gen "+i+".Klasse fertig");
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 22:09:45)
	 * 
	 * @param param
	 *           java.util.Vector[]
	 */
	public void addNeurons(double[][] neurons) {
		
		for (int ic = 0; ic < getCountClasses(); ic++) {
			
			// Aus.a("ic",ic);
			
			getClass(ic).getNeuron().setWeightSize(neurons.length);
			
			for (int iv = 0; iv < neurons.length; iv++) {
				
				// Aus.a("iv",iv);
				
				// Aus.a("neurons[iv][ic]",neurons[iv][ic]);
				
				// Aus.a("getClass(ic).getNeuron().getCountWeights()",getClass(ic).getNeuron().getCountWeights());
				
				getClass(ic).getNeuron().setWeight(iv, neurons[iv][ic]);
				
				// Aus.a("getClass(ic).getNeuron().getWeight(ias)",getClass(ic).getNeuron().getWeight(ias));
				
			}
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 22:15:35)
	 * 
	 * @param oneClass
	 *           java.util.Vector
	 */
	public void addObjects(ClassesClass c, java.util.Vector classData, int attributsCount) {
		
		for (int i = 0; i < classData.size(); i++) {
			
			ClassesObject o = new ClassesObject(attributsCount);
			
			qmwi.kseg.som.CSV_SOM_dataEntry dataEntry = (qmwi.kseg.som.CSV_SOM_dataEntry) classData.elementAt(i);
			
			o.setAttributs(dataEntry.getColumnData());
			
			o.setSkalar(this);
			
			c.addObject(o);
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 23:02:46)
	 * 
	 * @param oneClassIndex
	 *           int
	 * @param oneObjectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public String getAttribut(int classIndex, int objectIndex, int attributIndex) {
		
		return ((ClassesClass) allCs.elementAt(classIndex)).getAttribut(objectIndex, attributIndex);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 12:01:41)
	 * 
	 * @return java.lang.String
	 * @param i
	 *           int
	 */
	public String getAttributName(int i) {
		return (String) attributsNames.elementAt(i);
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 12:02:30)
	 * 
	 * @return int
	 * @param columnTemp
	 *           java.lang.String
	 */
	public int getAttributNameIndex(String attributName) {
		
		int attributIndex = -1;
		
		for (int i = 0; i < attributsNames.size(); i++) {
			
			if (attributName.equalsIgnoreCase(getAttributName(i)))
				attributIndex = i;
			
		}
		
		return attributIndex;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 00:57:39)
	 * 
	 * @return int
	 * @param i
	 *           int
	 */
	public int getAttributSelectedIndex(int index) {
		return ((Integer) attributsSelected.elementAt(index)).intValue();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 00:57:39)
	 * 
	 * @return int
	 * @param i
	 *           int
	 */
	public String getAttributSelectedName(int index) {
		
		return getAttributName(((Integer) attributsSelected.elementAt(index)).intValue());
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 11:47:25)
	 * 
	 * @return java.util.Vector
	 */
	public java.util.Vector getAttributsNames() {
		return attributsNames;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 11:47:25)
	 * 
	 * @return java.util.Vector
	 */
	public java.util.Vector getAttributsSelected() {
		return attributsSelected;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.ClassesOneClass
	 */
	public ClassesClass getClass(int i) {
		return (ClassesClass) allCs.get(i);
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 22:54:16)
	 * 
	 * @return int
	 */
	public int getCountAttributs() {
		
		for (int i = 0; i < getCountClasses(); i++) {
			
			if (getCountObjects(i) != 0)
				return getClass(i).getObject(0).getCountAttributs();
			
		}
		
		return 0;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:05:28)
	 * 
	 * @return int
	 */
	public int getCountAttributsNames() {
		
		return attributsNames.size();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (17.12.2001 01:05:28)
	 * 
	 * @return int
	 */
	public int getCountAttributsSelected() {
		
		return attributsSelected.size();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 22:49:41)
	 * 
	 * @return int
	 */
	public int getCountClasses() {
		
		return allCs.size();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 22:55:53)
	 * 
	 * @return int
	 */
	public int getCountObjects(int classIndex) {
		
		return getClass(classIndex).getCountObjects();
		
	}
	
	/**
	 * Gesamt-Homogenit�t ist die Summe der einzelnen Klassenhomogenit�ten
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public double getHeterogenitaet() {
		
		double heterogenitaet = 0;
		
		for (int i = 0; i < getCountClasses(); i++) {
			
			// Aus.a("Klasse "+i, "");
			
			heterogenitaet += getClass(i).getHeterogenitaet(this, i);
			
		}
		
		// Aus.a("Gesamtheterogenitaet", heterogenitaet);
		
		return heterogenitaet / getCountClasses();
		
	}
	
	/**
	 * Gesamt-Homogenit�t ist die Summe der einzelnen Klassenhomogenit�ten
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public double getHomogenitaet() {
		
		double homogenitaet = 0;
		
		for (int i = 0; i < getCountClasses(); i++) {
			
			// Aus.a("Klasse "+i, "");
			
			homogenitaet += getClass(i).getHomogenitaet();
			
		}
		
		// Aus.a("Gesamthomogenit�t", homogenit�t);
		
		return homogenitaet / getCountClasses();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.ClassesOneClass
	 */
	public static ClassesObjectInputVector getInputVector(int o) {
		
		return (ClassesObjectInputVector) inputVectors.elementAt(o);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.ClassesOneClass
	 */
	public static java.util.Vector getInputVectors() {
		
		return inputVectors;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.ClassesOneClass
	 */
	public static int getInputVectorsSize() {
		
		return inputVectors.size();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 23:02:46)
	 * 
	 * @param oneClassIndex
	 *           int
	 * @param oneObjectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public ClassesNeuron getNeuron(int classIndex) {
		
		return ((ClassesClass) allCs.elementAt(classIndex)).getNeuron();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 23:02:46)
	 * 
	 * @param oneClassIndex
	 *           int
	 * @param oneObjectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public ClassesObject getObject(int classIndex, int objectIndex) {
		
		return ((ClassesClass) allCs.elementAt(classIndex)).getObject(objectIndex);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 */
	public void setClass(int i, ClassesClass c) {
		
		allCs.add(i, c);
		
	}
}
