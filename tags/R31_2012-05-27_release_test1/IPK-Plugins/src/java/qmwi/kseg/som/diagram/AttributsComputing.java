/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (14.12.2001 01:20:07)
 * 
 * @author:
 */
public class AttributsComputing {
	/**
	 * Insert the method's description here.
	 * Creation date: (16.12.2001 23:19:10)
	 */
	public static void average(ClassesAll ca, AttributsAll aa) {
		
		// Berechnung des Durchschnitts f�r alle Klassen
		
		for (int ic = 0; ic < ca.getCountClasses(); ic++) {
			
			ClassesClass cc = ca.getClass(ic);
			
			AttributsClass ac = new AttributsClass();
			
			// Berechnung des Durchschnitts f�r alle gew�hlten Attribute
			
			for (int ia = 0; ia < ca.getCountAttributsSelected(); ia++) {
				
				int attributIndex = ca.getAttributSelectedIndex(ia);
				
				// Berechnung des Durchschnitts
				
				float sum = 0;
				
				float count = 0;
				
				for (int io = 0; io < cc.getCountObjects(); io++) {
					
					int tmp = 0;
					
					try {
						
						// eine Zelle kann einen falschen Wert aufweisen, und mu�t daher besondert behandelt werden
						
						tmp = Integer.parseInt(cc.getObject(io).getAttribut(attributIndex));
						
					} catch (NumberFormatException e) {
						
						// z.B. wenn keine Zahl vorkommt
						
						Aus.a("tmp2", tmp);
						
					}
					
					sum += tmp;
					
					++count;
				}
				
				// neue Attributklasse
				
				AttributsAttribut attribut = new AttributsAttribut();
				
				// hinzuf�gen des Durchscnitts
				
				attribut.average = sum / count;
				
				if (cc.getCountObjects() == 0)
					attribut.average = 0;
				
				// Attributsklasse der Klassegruppe zuordnen
				
				ac.addAttribut(attribut);
				
			}
			
			aa.addClass(ac);
			
		}
		
	}
}
