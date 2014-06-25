/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

public class ReactionAndInfo {
	
	private Reaction reaction;
	private boolean isReactionProductRequest, isReactionSubstrateRequest;
	private String substId;
	private String prodId;
	
	// public ReactionAndInfo(Reaction reaction, boolean isProductRequest, boolean isSubstrateRequest, boolean directed) {
	// this.reaction = reaction;
	// this.isReactionProductRequest = isProductRequest;
	// this.isReactionSubstrateRequest = isSubstrateRequest;
	// this.directed = directed;
	// }
	
	public ReactionAndInfo(Reaction reaction, boolean isProductRequest, boolean isSubstrateRequest,
						String substId, String prodId) {
		this.reaction = reaction;
		this.isReactionProductRequest = isProductRequest;
		this.isReactionSubstrateRequest = isSubstrateRequest;
		this.substId = substId;
		this.prodId = prodId;
	}
	
	public boolean isProductReq() {
		return isReactionProductRequest;
	}
	
	public boolean isSubstrateReq() {
		return isReactionSubstrateRequest;
	}
	
	public Reaction getReaction() {
		return reaction;
	}
	
	/**
	 * May return multiple IDs (e.g. "rn:R02030 rn:R07390")
	 * 
	 * @return
	 */
	public String getProdId() {
		return prodId;
	}
	
	/**
	 * May return multiple IDs (e.g. "rn:R02030 rn:R07390")
	 * 
	 * @return
	 */
	public String getSubstId() {
		return substId;
	}
	
}
