/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (13.12.2001 15:51:00)
 * 
 * @author:
 */
public class ClassesClass {
	protected ClassesNeuron neuron = null;
	protected java.util.Vector<ClassesObject> objects = null;
	
	/**
	 * SomClass constructor comment.
	 */
	@SuppressWarnings("unchecked")
	public ClassesClass() {
		super();
		neuron = new ClassesNeuron();
		objects = new java.util.Vector();
	}
	
	/**
	 * SomClass constructor comment.
	 */
	@SuppressWarnings("unchecked")
	public ClassesClass(int i) {
		super();
		neuron = new ClassesNeuron(i);
		objects = new java.util.Vector();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:00:27)
	 * 
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public void addObject(ClassesObject o) {
		objects.add(o);
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:18:04)
	 * 
	 * @param objectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public ClassesObject getAttribut(int objectIndex) {
		
		if (objects.size() != 0)
			return (ClassesObject) objects.elementAt(objectIndex);
		
		else {
			
			Aus.a("keine Objekte vorhanden", "");
			
			return null;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:18:04)
	 * 
	 * @param objectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public String getAttribut(int objectIndex, int attributIndex) {
		
		if (objects.size() != 0)
			return ((ClassesObject) objects.elementAt(objectIndex)).getAttribut(attributIndex);
		
		else
			return "keine Objekte vorhanden";
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 22:58:13)
	 * 
	 * @return int
	 */
	public int getCountObjects() {
		return objects.size();
	}
	
	/**
	 * Euklidische Distanz:
	 * Wurzel aus der Summe der Quadrate der Abst�nde der Eigenschaften zweier Objekte
	 * Bsp: a | a1=2, a2=4
	 * b | b1=4, b2=6
	 * ED = Wurzel( (a1-b1)*(a1-b1) + (a2-b2)*(a2-b2) )
	 * ED = Wurzel( (2-4)*(2-4) + (4-6)*(4-6) )
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.OneObject
	 */
	public double getEuklidDistance(ClassesObject object, ClassesNeuron neuron) {
		
		// Summe der Quadrate
		double sum = 0;
		
		// für alle Eigenschaften den quadratisches Abstand bestimmen
		for (int i = 0; i < object.getInputVector().length; i++) {
			
			// Abstand der Eigenschaften
			double d = object.getInputVectorValue(i) - neuron.getWeight(i);
			
			// Aus.a("iv", getInputVectorValue(objectIndex, i));
			
			// Aus.a("nw", getNeuronWeight(i));
			
			// Aus.a("d",d);
			
			// Quadrierung des Abstands und Aufsummieren
			sum += Math.pow(d, 2.0);
			
		}
		
		// Aus.ac("Math.sqrt(sum)", Math.sqrt(sum));
		
		// Aus.a("Math.sqrt(sum)",Math.sqrt(sum));
		
		// Aus.a("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
		
		// Ergebnis Wurzel aus der Summe
		return Math.sqrt(sum);
		
	}
	
	/**
	 * Homogenit�t ist die Summe der Euklidische Abst�nde zwischen den Objekten und dem Neuron
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public double getHeterogenitaet(ClassesAll ca, int actualClassIndex) {
		
		if (getCountObjects() == 0)
			return 0.0;
		
		if (ca.getCountClasses() == 1)
			return 0.0;
		
		double heterogenitaet = 0;
		
		for (int io = 0; io < getCountObjects(); io++) {
			
			ClassesObject o = getObject(io);
			
			double heterogenitaetObject = 0;
			
			for (int ic = 0; ic < ca.getCountClasses(); ic++) {
				
				if (ic != actualClassIndex)
					heterogenitaetObject += getEuklidDistance(o, ca.getClass(ic).getNeuron());
				
			}
			
			heterogenitaet += heterogenitaetObject / (ca.getCountClasses() - 1);
			
		}
		
		// Aus.a("heterogenit�t", heterogenitaet);
		
		// Aus.a("HHHHHHHHHHHHHHHHHHHHHHHHHHH", "");
		
		return heterogenitaet / getCountObjects();
		
	}
	
	/**
	 * Homogenit�t ist die Summe der Euklidische Abst�nde zwischen den Objekten und dem Neuron
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public double getHomogenitaet() {
		
		if (getCountObjects() == 0)
			return 2.0;
		
		double homogenitaet = 0;
		
		for (int io = 0; io < getCountObjects(); io++) {
			
			homogenitaet += getEuklidDistance(getObject(io), getNeuron());
			
		}
		
		// Aus.a("homogenit�t", homogenitaet);
		
		// Aus.a("HHHHHHHHHHHHHHHHHHHHHHHHHHH", "");
		
		return homogenitaet / getCountObjects();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:18:04)
	 * 
	 * @param objectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public double getInputVectorValue(int objectIndex, int valueIndex) {
		
		return getObject(objectIndex).getInputVectorValue(valueIndex);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.OneObject
	 */
	public ClassesNeuron getNeuron() {
		return neuron;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:18:04)
	 * 
	 * @param objectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public double getNeuronWeight(int weightIndex) {
		
		if (neuron != null)
			return neuron.getWeight(weightIndex);
		
		else {
			
			Aus.ac("kein Neuron vorhanden");
			
			return 0;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 20:18:04)
	 * 
	 * @param objectIndex
	 *           int
	 * @param attributIndex
	 *           int
	 */
	public double[] getNeuronWeights() {
		
		if (neuron != null)
			return neuron.getWeights();
		
		else {
			
			Aus.ac("kein Neuron vorhanden");
			
			return null;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 16:02:07)
	 * 
	 * @return qmwi.kseq.som.processing.OneObject
	 */
	public ClassesObject getObject(int i) {
		
		if (objects.get(i) != null)
			return (ClassesObject) objects.get(i);
		
		else {
			
			Aus.a("Objekt nicht vorhanden");
			
			return null;
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public void setNeuron(double[] w) {
		
		neuron.setWeights(w);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (13.12.2001 15:59:21)
	 * 
	 * @param i
	 *           int
	 * @param oneObject
	 *           qmwi.kseq.som.processing.OneObject
	 */
	public void setObject(int i, ClassesObject o) {
		
		objects.add(i, o);
		
	}
}
