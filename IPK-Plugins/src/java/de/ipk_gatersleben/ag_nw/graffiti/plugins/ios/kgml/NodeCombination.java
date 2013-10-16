/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import org.AttributeHelper;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;

public class NodeCombination implements Comparable<Object> {
	private Node a, b;
	
	private boolean isProductRequest, isSubstrateRequest;
	
	private ReactionType reactionType;
	
	public String sourceInformationSub;
	public String sourceInformationReac;
	public String sourceInformationProd;
	
	public NodeCombination(Node a, Node b,
						boolean isProductRequest, boolean isSubstrateRequest,
						ReactionType reactionType) {
		// this(a,b);
		assert a != null;
		assert b != null;
		this.a = a;
		this.b = b;
		this.isProductRequest = isProductRequest;
		this.isSubstrateRequest = isSubstrateRequest;
		this.reactionType = reactionType;
	}
	
	public Node getNodeA() {
		return a;
	}
	
	public Node getNodeB() {
		return b;
	}
	
	public ReactionType getReactionType() {
		return reactionType;
	}
	
	@Override
	public boolean equals(Object obj) {
		NodeCombination nc = (NodeCombination) obj;
		if (nc.a == a && nc.b == b)
			return true;
		return false;
	}
	
	public int compareTo(Object o) {
		NodeCombination nc = (NodeCombination) o;
		if (nc.a == a && nc.b == b)
			return 0;
		if (nc.a.getID() < a.getID())
			return -1;
		if (nc.a.getID() > a.getID())
			return 1;
		if (nc.b.getID() < b.getID())
			return -1;
		if (nc.b.getID() > b.getID())
			return 1;
		return 0;
	}
	
	@Override
	public String toString() {
		String n1 = AttributeHelper.getLabel(a, "?") + " (" + a.getID() + ")";
		String n2 = AttributeHelper.getLabel(b, "?") + " (" + b.getID() + ")";
		return n1 + " --> " + n2;
	}
	
	public boolean isReactionProductReq() {
		return isProductRequest;
	}
	
	public boolean isReactionSubstrateReq() {
		return isSubstrateRequest;
	}
	
	public void setSourceInformation(String substrate, String reaction, String product) {
		this.sourceInformationSub = substrate;
		this.sourceInformationReac = reaction;
		this.sourceInformationProd = product;
	}
	
}
