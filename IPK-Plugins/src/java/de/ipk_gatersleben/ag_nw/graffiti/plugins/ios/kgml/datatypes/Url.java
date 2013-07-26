/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public class Url {
	
	private String url;
	
	public Url(String url) {
		this.url = url;
	}
	
	public static Url getUrl(String urlValue) {
		if (urlValue == null)
			return null;
		else
			return new Url(urlValue);
	}
	
	@Override
	public String toString() {
		return url;
	}
}
