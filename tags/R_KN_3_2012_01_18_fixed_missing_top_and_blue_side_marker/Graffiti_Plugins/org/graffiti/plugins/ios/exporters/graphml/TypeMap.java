// ==============================================================================
//
// TypeMap.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: TypeMap.java,v 1.1 2011-01-31 09:03:25 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphml;

import java.util.HashMap;

import org.ErrorMsg;

/**
 * This class provides a mapping from Gravisto attributes types to graphML
 * attribute types.
 * 
 * @author ruediger
 */
class TypeMap {
	// ~ Instance fields ========================================================
	
	/** Maps Gravisto attribute types to graphML attribute types. */
	private final HashMap<String, String> map;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>TypeMap</code>.
	 */
	TypeMap() {
		this.map = new HashMap<String, String>();
		
		// add the straight forward mapping for the base attributes
		this.map.put("org.graffiti.attributes.BooleanAttribute", "boolean");
		this.map.put("org.graffiti.attributes.IntegerAttribute", "int");
		this.map.put("org.graffiti.attributes.LongAttribute", "long");
		this.map.put("org.graffiti.attributes.FloatAttribute", "float");
		this.map.put("org.graffiti.attributes.DoubleAttribute", "double");
		this.map.put("org.graffiti.attributes.StringAttribute", "string");
		
		// advanced mappings
		this.map.put("org.graffiti.attributes.ShortAttribute", "int");
		this.map.put("org.graffiti.attributes.ByteAttribute", "int");
		this.map.put("org.graffiti.attributes.NodeShapeAttribute", "string");
		this.map.put("org.graffiti.attributes.EdgeShapeAttribute", "string");
		this.map.put("org.graffiti.graphics.AWTImageAttribute", "string");
		this.map.put("org.graffiti.graphics.LineModeAttribute", "string");
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the graphML attribute type for a given Gravisto attribute type, <code>null</code> if there is no such type in the map.
	 * 
	 * @param gravistoType
	 *           the Gravisto attribute type.
	 * @return the graphML attribute type.
	 */
	@SuppressWarnings("unchecked")
	String getGraphMLType(String gravistoType) {
		String graphMLType = this.map.get(gravistoType);
		if (graphMLType == null) {
			// check possible super types of class...
			try {
				Class c = Class.forName(gravistoType);
				for (String key : map.keySet()) {
					Class known = Class.forName(key);
					if (known.isAssignableFrom(c))
						return map.get(key);
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		return graphMLType;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
