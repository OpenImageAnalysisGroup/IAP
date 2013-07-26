/*
 * $Id: XMLTriple.java,v 1.1 2012-11-07 14:43:38 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/xml/XMLTriple.java $
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

package org.sbml.jsbml.xml;




/** 
 * Representation of a qualified XML name.
 * <p>
 * <em style='color: #555'>
		This class of objects is defined by jsbml only and has no direct
		equivalent in terms of SBML components.  This class is not prescribed by
		the SBML specifications, although it is used to implement features
		defined in SBML.
		</em>

 * <p>
 * A 'triple' in the jsbml XML layer encapsulates the notion of qualified
 * name, meaning an element name or an attribute name with an optional
 * namespace qualifier.  An {@link XMLTriple} instance carries up to three data items:
 * <p>
 * <ul>
 * <p>
 * <li> The name of the attribute or element; that is, the attribute name
 * as it appears in an XML document or data stream;
 * <p>
 * <li> The XML namespace prefix (if any) of the attribute.  For example,
 * in the following fragment of XML, the namespace prefix is the string
 * <code>mysim</code> and it appears on both the element
 * <code>someelement</code> and the attribute <code>attribA</code>.  When
 * both the element and the attribute are stored as {@link XMLTriple} objects,
 * their <i>prefix</i> is <code>mysim</code>.
 * <div class='fragment'><pre>
		&lt;mysim:someelement mysim:attribA='value' /&gt;
		</pre></div>
 * <p>
 * <li> The XML namespace URI with which the prefix is associated.  In
 * XML, every namespace used must be declared and mapped to a URI.
 * <p>
 * </ul>
 * <p>
 * {@link XMLTriple} objects are the lowest-level data item in the XML layer
 * of jsbml.  Other objects such as {@link XMLToken} make use of {@link XMLTriple}
 * objects.
 * @since 0.8
 * @version $Rev: 1116 $
 */
public class XMLTriple {

	private String name = "";
	private String prefix = "";
	private String namespaceURI = "";
	
	/**
	 * Equality comparison method for XMLTriple.
	 * <p>
	 *
	 * @param sb a reference to an object to which the current object
	 * instance will be compared
	 *
	 * @return <code>true</code> if <code>sb</code> refers to the same underlying 
	 * native object as this one, <code>false</code> otherwise
	 */
	public boolean equals(Object sb)
	{
		
		if (sb instanceof XMLTriple) {
			boolean equals = true;
			XMLTriple xmlTriple = (XMLTriple) sb;
			
			// The attributes should never been null
			
			equals &= name.equals(xmlTriple.getName());
			equals &= prefix.equals(xmlTriple.getPrefix());
			equals &= namespaceURI.equals(xmlTriple.getURI());
			
			return equals;
		}
		 
		 return false;
	}

	/**
	 * Returns a hashcode for this XMLTriple object.
	 *
	 * @return a hash code usable by Java methods that need them.
	 */
	public int hashCode()
	{
		return name.hashCode() + prefix.hashCode() + namespaceURI.hashCode();
	}


	/**
	 * Creates a new, empty {@link XMLTriple}.
	 */
	public XMLTriple() {
	
	}


	/**
	 * Creates a new {@link XMLTriple} with the given <code>name</code>, <code>uri</code> and and 
	 * <code>prefix</code>.
	 * <p>
	 * @param name a string, name for the {@link XMLTriple}.
	 * @param uri a string, URI of the {@link XMLTriple}.
	 * @param prefix a string, prefix for the URI of the {@link XMLTriple},
	 */
	public XMLTriple(String name, String uri, String prefix) {
	
		if (name != null) {
			this.name = name;
		}
		if (uri != null) {
			namespaceURI = uri;
		}
		if (prefix != null) {
			this.prefix = prefix;
		}
		 

	}


	/**
	 * Creates a new {@link XMLTriple} by splitting the given <code>triplet</code> on the
	 * separator character <code>sepchar</code>.
	 * <p>
	 * Triplet may be in one of the following formats:
	 * <ul>
	 * <li> name
	 * <li> uri sepchar name
	 * <li> uri sepchar name sepchar prefix
	 * </ul>
	 * @param triplet a string representing the triplet as above
	 * @param sepchar a character, the sepchar used in the triplet
	 * <p>
	 */
	public XMLTriple(String triplet, char sepchar) {
		
		if (triplet == null) {
			throw new IllegalArgumentException("Cannot create an XMLTriple with a null argument.");
		}
		// parse the triplet
		String[] tokens = triplet.split("" + sepchar);
		
		if (tokens.length == 1) {
			name = tokens[0].trim();
		} else if (tokens.length == 2) {
			namespaceURI = tokens[0].trim();
			name = tokens[1].trim();
		} else if (tokens.length == 3) {
			namespaceURI = tokens[0].trim();
			name = tokens[1].trim();
			prefix = tokens[2].trim();
		} else {
			throw new IllegalArgumentException(String.format(
									"Cannot create an XMLTriple with the argument '%s' and the separator '%s'.",
									triplet, sepchar));
		}
	}


	/**
	 * Creates a new {@link XMLTriple} by splitting the given <code>triplet</code> 
	 * separated by space.
	 * <p>
	 * Triplet may be in one of the following formats:
	 * <ul>
	 * <li> name
	 * <li> uri  name
	 * <li> uri  name  prefix
	 * </ul>
	 * @param triplet a string representing the triplet as above
	 * <p>
	 */
	public XMLTriple(String triplet) {
		this(triplet, ' ');
	}


	/**
	 * Creates a copy of this {@link XMLTriple} set.
	 */
	public XMLTriple(XMLTriple orig) {
		
		name = new String(orig.getName());
		namespaceURI = new String(orig.getURI());
		prefix = new String(orig.getPrefix());
		
	}


	/**
	 * Creates and returns a deep copy of this {@link XMLTriple} set.
	 * <p>
	 * @return a (deep) copy of this {@link XMLTriple} set.
	 */
	public XMLTriple clone() {
		return new XMLTriple(this);
	}


	/**
	 * Returns the <em>name</em> portion of this {@link XMLTriple}.
	 * <p>
	 * @return a string, the name from this {@link XMLTriple}.
	 */
	public String getName() {
		return name;
	}


	/**
	 * Returns the <em>prefix</em> portion of this {@link XMLTriple}.
	 * <p>
	 * @return a string, the <em>prefix</em> portion of this {@link XMLTriple}.
	 */
	public String getPrefix() {
		return prefix;
	}


	/**
	 * Returns the <em>URI</em> portion of this {@link XMLTriple}.
	 * <p>
	 * @return URI a string, the <em>prefix</em> portion of this {@link XMLTriple}.
	 */
	public String getURI() {
		return namespaceURI;
	}


	/**
	 * Returns the prefixed name from this {@link XMLTriple}.
	 * <p>
	 * @return a string, the prefixed name from this {@link XMLTriple}.
	 */
	public String getPrefixedName() {
		return prefix.length() == 0 ? name : prefix + ":" + name;
	}


	/**
	 * Predicate returning <code>true</code> or <code>false</code> depending on whether 
	 * this {@link XMLTriple} is empty.
	 * <p>
	 * @return <code>true</code> if this {@link XMLTriple} is empty, <code>false</code> otherwise.
		   <p>
	 * @deprecated libSBML internal
	 */
	@Deprecated
	public boolean isEmpty() {
		return ( getName().length() == 0
		        && getURI().length() == 0
		        && getPrefix().length() == 0);
	}


}
