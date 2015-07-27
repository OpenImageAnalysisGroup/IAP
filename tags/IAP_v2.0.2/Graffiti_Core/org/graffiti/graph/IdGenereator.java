/*
 * Created on 16.11.2004 by Christian Klukas
 */
package org.graffiti.graph;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class IdGenereator {
	private static int currentID = 0;
	
	public static synchronized int getNextID() {
		return currentID++;
	}
}
