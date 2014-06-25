package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.swig_cola;

import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.HelperClass;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

import colajava.ColaEdge;
import colajava.Rectangle;

public class Graph2ColaHelper implements HelperClass {
	
	public static HashMap<Node, Integer> getNode2IdxMap(Collection<Node> nodeList) {
		HashMap<Node, Integer> node2idx = new HashMap<Node, Integer>();
		int idx = 0;
		for (Node n : nodeList) {
			node2idx.put(n, idx++);
		}
		return node2idx;
	}
	
	public static HashMap<Integer, Node> getIdx2NodeMap(Collection<Node> nodeList) {
		HashMap<Integer, Node> idx2node = new HashMap<Integer, Node>();
		int idx = 0;
		for (Node n : nodeList) {
			idx2node.put(idx++, n);
		}
		return idx2node;
	}
	
	public static Rectangle getRectangle(Node n) {
		Vector2d pos = AttributeHelper.getPositionVec2d(n);
		Vector2d size = AttributeHelper.getSize(n);
		Rectangle r = new Rectangle(pos.x - size.x / 2, pos.x + size.x / 2, pos.y - size.y / 2, pos.y + size.y / 2);
		return r;
	}
	
	public static ColaEdge getEdge(HashMap<Node, Integer> node2idx, Edge e) {
		Integer a = node2idx.get(e.getSource());
		Integer b = node2idx.get(e.getTarget());
		if (a != null && b != null) {
			ColaEdge ce = new ColaEdge(a, b);
			return ce;
		} else
			return null;
	}
	
}
