/*
 * $Id: LayoutConstant.java,v 1.1 2012-11-07 14:43:36 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/extensions/layout/src/org/sbml/jsbml/ext/layout/LayoutConstant.java $
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2012 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.ext.layout;

public class LayoutConstant {

	/**
	 * The namespace URI of this parser.
	 */
	public static final String namespaceURI = "http://www.sbml.org/sbml/level3/version1/layout/version1";
	public static final String namespaceURI_L2 = "http://projects.eml.org/bcb/sbml/level2";
	
	public static final String shortLabel = "layout";

	public static final String listOfLayouts = "listOfLayouts";
	public static final String listOfCompartmentGlyphs = "listOfCompartmentGlyphs";
	public static final String listOfSpeciesGlyphs = "listOfSpeciesGlyphs";
	public static final String listOfReactionGlyphs = "listOfReactionGlyphs";
	public static final String listOfTextGlyphs = "listOfTextGlyphs";
	public static final String listOfAdditionalGraphicalObjects = "listOfAdditionalGraphicalObjects";
	public static final String listOfCurveSegments = "listOfCurveSegments";
	public static final String listOfSpeciesReferenceGlyphs = "listOfSpeciesReferenceGlyphs";

	public static final String layout = "layout";
	public static final String compartmentGlyph = "compartmentGlyph";
	public static final String speciesGlyph = "speciesGlyph";
	public static final String reactionGlyph = "reactionGlyph";
	public static final String textGlyph = "textGlyph";
	public static final String speciesReferenceGlyph = "speciesReferenceGlyph";
	public static final String boundingBox = "boundingBox";
	public static final String position = "position";
	public static final String dimensions = "dimensions";
	public static final String start = "start";
	public static final String end = "end";
	public static final String curve = "curve";
	public static final String curveSegment = "curveSegment";

	// Attribute names of the Point XML type
	public static final String x = "x";
	public static final String y = "y";
	public static final String z = "z";
	
	// Attribute names of the Dimensions XML type	
	public static final String depth = "depth";
	public static final String width = "width";
	public static final String height = "height";
	
	// Attribute names of the CompartmentGlyph XML type
	public static final String compartment = "compartment";

	// Attribute names of the speciesGlyph XML type
	public static final String species = "species";
	
	// Attribute names of the textGlyph XML type
	public static final String graphicalObject = "graphicalObject";
	public static final String text = "text";
	public static final String originOfText = "originOfText";

	// Attribute names of the speciesReferenceGlyph XML type
	public static final String speciesReference = "speciesReference";
	public static final String role = "role";


	public static final String basePoint1 = "basePoint1";
	public static final String basePoint2 = "basePoint2";
	public static final String reaction = "reaction";
	

	
}
