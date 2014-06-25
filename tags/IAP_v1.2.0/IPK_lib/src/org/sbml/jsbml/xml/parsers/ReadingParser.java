/*
 * $Id: ReadingParser.java,v 1.1 2012-11-07 14:43:35 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/xml/parsers/ReadingParser.java $
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

package org.sbml.jsbml.xml.parsers;

import org.sbml.jsbml.SBMLDocument;

/**
 * The interface to implement for the SBML parsers reading SBML files.
 * 
 * @author Marine Dumousseau
 * @since 0.8
 * @version $Rev: 1116 $
 */
public interface ReadingParser {

	/**
	 * Process the XML attribute and modify 'contextObject' in consequence.
	 * 
	 * For example, if the contextObject is an instance of Reaction and the attributeName is 'fast', 
	 * this method will set the 'fast' variable of the 'contextObject' to 'value'.
	 * Then it will return the modified Reaction instance.
	 * 
	 * @param elementName : the localName of the XML element.
	 * @param attributeName : the attribute localName of the XML element.
	 * @param value : the value of the XML attribute.
	 * @param prefix : the attribute prefix 
	 * @param isLastAttribute : boolean value to know if this attribute is the last attribute of the XML element.
	 * @param contextObject : the object to set or modify depending on the identity of the current attribute. This object 
	 *           represents the context of the XML attribute in the SBMLDocument.
	 * 
	 */
	public void processAttribute(String elementName, String attributeName, String value, String prefix, boolean isLastAttribute, Object contextObject);
	
	/**
	 * Process the text of a XML element and modify 'contextObject' in consequence.
	 * 
	 * For example, if the contextObject is an instance of ModelCreator and the elementName is 'Family',
	 * this method will set the familyName of the 'contextObject' to the text value. Then it will return the 
	 * changed ModelCreator instance.
	 * 
	 * @param elementName : the localName of the XML element.
	 * @param characters : the text of this XML element.
	 * @param contextObject : the object to set or modify depending on the identity of the current element. This object 
	 *            represents the context of the XML element in the SBMLDocument.
	 * 
	 */
	public void processCharactersOf(String elementName, String characters, Object contextObject);

	/**
	 * Process the end of the document. Do the necessary changes in the SBMLDocument.
	 * 
	 * For example, check if all the annotations are valid, etc.
	 * 
	 * @param sbmlDocument : the final initialised SBMLDocument instance.
	 */
	public void processEndDocument(SBMLDocument sbmlDocument);
	
	/**
	 * Process the end of the element 'elementName'. Modify or not the contextObject.
	 * 
	 * @param elementName : the localName of the XML element.
	 * @param prefix : the prefix of the XML element.
	 * @param isNested : boolean value to know if the XML element is a nested element.
	 * @param contextObject : the object to set or modify depending on the identity of the current element. This object 
	 *             represents the context of the XML element in the SBMLDocument.
	 * 
	 * @return true to remove the contextObject from the stack, if false is returned the contextObject will stay on top 
	 *             of the stack 
	 */
	public boolean processEndElement(String elementName, String prefix, boolean isNested, Object contextObject);
	
	/**
	 * Process the namespace and modify the contextObject in consequence.
	 * 
	 * For example, if the contextObject is an instance of SBMLDocument, the namespaces will be stored in the SBMLNamespaces HashMap 
	 * of this SBMLDocument.
	 *
	 * @param elementName : the localName of the XML element.
	 * @param URI : the URI of the namespace
	 * @param prefix : the prefix of the namespace.
	 * @param localName : the localName of the namespace.
	 * @param hasAttributes : boolean value to know if there are attributes after the namespace declarations.
	 * @param isLastNamespace : boolean value to know if this namespace is the last namespace of this element.
	 * @param contextObject : the object to set or modify depending on the identity of the current element. This object 
	 *              represents the context of the XML element in the SBMLDocument.
	 * 
	 */
	public void processNamespace(String elementName, String URI, String prefix, String localName, boolean hasAttributes, boolean isLastNamespace, Object contextObject);
	
	/**
	 * Process the XML element and modify 'contextObject' in consequence.
	 * 
	 * For example, if the contextObject is an instance of Event and the elementName is 'trigger', this method 
	 * will create a new Trigger instance and will set the trigger instance of the 'contextObject' to the new Trigger.
	 * Then the method will return the new Trigger instance which is the new environment.
	 *
	 * @param elementName : the localName of the XML element to process
	 * @param prefix : the prefix of the XML element to process
	 * @param hasAttributes : boolean value to know if this XML element has attributes.
	 * @param hasNamespaces : boolean value to know if this XML element contains namespace declarations.
	 * @param contextObject : the object to set or modify depending on the identity of the current XML element. This object 
	 *             represents the context of the XML element in the SBMLDocument.
	 * @return a new contextObject which represents the environment of the next node/subnode in the SBMLDocument. If null is returned,
	 *             the contextObject will not change.
	 * 
	 */
	public Object processStartElement(String elementName, String prefix, boolean hasAttributes, boolean hasNamespaces, Object contextObject);
	
}
