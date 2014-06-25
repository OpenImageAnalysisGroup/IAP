package org.graffiti.graph;

import java.util.List;

public interface NodeHierarchy {
	public List<Node> getDirectChildNodes(Node n);
	
	public List<Node> getAllChildNodes(Node n);
	
	public List<Node> getDirectChildNodes(List<Node> n);
	
	public List<Node> getAllChildNodes(List<Node> n);
	
	public List<Node> getDirectParentNodes(Node n);
	
	public List<Node> getAllParentNodes(Node n);
	
	public List<Node> getDirectParentNodes(List<Node> n);
	
	public List<Node> getAllParentNodes(List<Node> n);
}
