/*
 * Created on 10.09.2004 by Christian Klukas
 */
package org.graffiti.managers;

import org.graffiti.core.GenericFileFilter;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class GravistoFileFilter extends GenericFileFilter {
	
	String description;
	
	/**
	 * @param extension
	 */
	public GravistoFileFilter(String extension, String description) {
		super(extension);
		this.description = description;
	}
	
	@Override
	public String getDescription() {
		return description + " (*" + getExtension() + ")";
	}
}
