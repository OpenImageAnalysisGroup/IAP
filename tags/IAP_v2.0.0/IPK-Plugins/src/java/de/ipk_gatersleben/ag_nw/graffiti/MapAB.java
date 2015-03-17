package de.ipk_gatersleben.ag_nw.graffiti;

import java.util.HashMap;

import org.graffiti.graph.Node;

/**
 * @author Christian Klukas
 */
public class MapAB {
	
	private final HashMap<Node, Node> hashMapA;
	private final HashMap<Node, Node> hashMapB;
	
	public MapAB(HashMap<Node, Node> hashMapA, HashMap<Node, Node> hashMapB) {
		this.hashMapA = hashMapA;
		this.hashMapB = hashMapB;
		
	}
	
	public HashMap<Node, Node> getA() {
		return hashMapA;
	}
	
	public HashMap<Node, Node> getB() {
		return hashMapB;
	}
	
}
