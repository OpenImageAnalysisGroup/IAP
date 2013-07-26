package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.Map.Entry;

import org.graffiti.graph.Node;

public class MyEntry implements Entry<Node, Integer> {
	
	private Node n;
	private int v;
	
	public MyEntry(Node graphNode, int res) {
		this.n = graphNode;
		this.v = res;
	}
	
	public Node getKey() {
		return n;
	}
	
	public Integer getValue() {
		return v;
	}
	
	public Integer setValue(Integer value) {
		v = value;
		return value;
	}
	
}
