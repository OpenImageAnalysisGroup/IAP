/*
 * $Id: SAX2Parser.java,v 1.1 2012-11-07 14:43:37 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/util/SAX2Parser.java $
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

package org.sbml.jsbml.util;

import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * An interface for XML SAX2 parsers.
 * 
 * @author Marco Donizelli
 * @since 0.8
 * @version $Rev: 1116 $
 */
public interface SAX2Parser {

	/**
	 * 
	 * Parses the content of a byte stream as XML, using a <i>nonvalidating</i>
	 * parser and the specified SAX2 default event handler.
	 * 
	 * @param byteStream
	 *            The byte stream which content has to be parsed as XML.
	 * @param handler
	 *            The SAX2 default event handler to use for parsing
	 *            <code>byteStream</code>.
	 * @param namespaceAware
	 *            A flag to indicate whether the parser should know about
	 *            namespaces or not.
	 * 
	 */
	public void parse(InputStream byteStream, DefaultHandler handler,
			boolean namespaceAware);

	/**
	 * 
	 * Parses the content of a byte stream as XML, using a <i>validating</i>
	 * parser and the specified SAX2 default event handler.
	 * 
	 * @param byteStream
	 *            The byte stream which content has to be parsed as XML.
	 * @param handler
	 *            The SAX2 default event handler to use for parsing
	 *            <code>byteStream</code>.
	 * @param schemas
	 *            An optional array of either <code>java.io.File</code>
	 *            instances containing the abstract pathnames, or of
	 *            <code>java.io.String</code> instances containing the URIs,
	 *            pointing to the schemas to use in the validation process. If
	 *            set to <code>null</code>, the schemas defined in the data set
	 *            will be used. If set to <code>null</code> and no schemas are
	 *            found in the data set, an exception is most likely to be
	 *            thrown by the underlying implementation.
	 * 
	 */
	public void parse(InputStream byteStream, DefaultHandler handler,
			Object[] schemas);

	/**
	 * 
	 * Parses the content of a character stream as XML, using a
	 * <i>nonvalidating</i> parser and the specified SAX2 default event handler.
	 * 
	 * @param characterStream
	 *            The character stream which content has to be parsed as XML.
	 * @param handler
	 *            The SAX2 default event handler to use for parsing
	 *            <code>characterStream</code>.
	 * @param namespaceAware
	 *            A flag to indicate whether the parser should know about
	 *            namespaces or not.
	 * 
	 */
	public void parse(Reader characterStream, DefaultHandler handler,
			boolean namespaceAware);

	/**
	 * 
	 * Parses the content of a character stream as XML, using a
	 * <i>validating</i> parser and the specified SAX2 default event handler.
	 * 
	 * @param characterStream
	 *            The character stream which content has to be parsed as XML.
	 * @param handler
	 *            The SAX2 default event handler to use for parsing
	 *            <code>characterStream</code>.
	 * @param schemas
	 *            An optional array of either <code>java.io.File</code>
	 *            instances containing the abstract pathnames, or of
	 *            <code>java.io.String</code> instances containing the URIs,
	 *            pointing to the schemas to use in the validation process. If
	 *            set to <code>null</code>, the schemas defined in the data set
	 *            will be used. If set to <code>null</code> and no schemas are
	 *            found in the data set, an exception is most likely to be
	 *            thrown by the underlying implementation.
	 * 
	 */
	public void parse(Reader characterStream, DefaultHandler handler,
			Object[] schemas);
}
