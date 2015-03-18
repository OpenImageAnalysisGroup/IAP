/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti;

import org.graffiti.attributes.Attribute;
import org.graffiti.graphics.GraphicAttributeConstants;

/**
 * Useful Attribute Path-Names. (list of global String constants) See also
 * org.graffiti.attributes.GraphicAttributeConstants, which contains other
 * Path names. This is the newer file that should by used by our group
 * (ag_nw). The other class should be used only for graffiti internal path
 * names, not for pathnames that are used by the plugins.
 * 
 * @author klukas
 * @see org.graffiti.attributes.GraphicAttributeConstants
 */
public class AttributeConstants {
	
	/**
	 * Unique identifier for the group in Gatersleben.
	 */
	public static final String AGNW_PATH = "AGNW";
	
	/**
	 * Dynamical defined edge bend attribute.
	 */
	public static final String BENDS =
						GraphicAttributeConstants.GRAPHICS
											+ Attribute.SEPARATOR
											+ GraphicAttributeConstants.BENDS;
	
}
