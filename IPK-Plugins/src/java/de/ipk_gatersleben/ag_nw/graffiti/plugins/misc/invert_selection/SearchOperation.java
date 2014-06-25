/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.07.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

public enum SearchOperation {
	include,
	greater,
	smaller,
	equals,
	endswith,
	startswith,
	regexpsearch,
	topN,
	bottomN
}
