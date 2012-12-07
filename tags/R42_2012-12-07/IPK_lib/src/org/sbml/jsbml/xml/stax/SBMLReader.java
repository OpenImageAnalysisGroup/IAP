/*
 * $Id: SBMLReader.java,v 1.1 2012-11-07 14:43:38 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/xml/stax/SBMLReader.java $
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

package org.sbml.jsbml.xml.stax;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.codehaus.stax2.evt.XMLEvent2;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.util.SimpleTreeNodeChangeListener;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.parsers.AnnotationParser;
import org.sbml.jsbml.xml.parsers.BiologicalQualifierParser;
import org.sbml.jsbml.xml.parsers.MathMLStaxParser;
import org.sbml.jsbml.xml.parsers.ModelQualifierParser;
import org.sbml.jsbml.xml.parsers.ReadingParser;
import org.sbml.jsbml.xml.parsers.SBMLCoreParser;
import org.sbml.jsbml.xml.parsers.StringParser;

import com.ctc.wstx.stax.WstxInputFactory;

/**
 * Provides all the methods to read a SBML file.
 * 
 * @author Marine Dumousseau
 * @author Andreas Dr&auml;ger
 * @author Nicolas Rodriguez
 * @author Clemens Wrzodek
 * @since 0.8
 * @version $Rev: 1187 $
 */
public class SBMLReader {

	/**
	 * Contains all the relationships namespace URI <=> {@link ReadingParser}
	 * implementation classes.
	 */
	private Map<String, Class<? extends ReadingParser>> packageParsers = new HashMap<String, Class<? extends ReadingParser>>();

	
	/**
	 * Contains all the initialized parsers.
	 */
	private Map<String, ReadingParser> initializedParsers = new HashMap<String, ReadingParser>();
	
	/**
	 * Initialize a static instance of the core parser.
	 * This is much more efficient than initializing it again and again every
	 * time we need it (Core bottleneck is the loadFromXML() method, called
	 * from the SBMLCoreParser() constructor each time).
	 */
	private static SBMLCoreParser sbmlCoreParser = new SBMLCoreParser();

	
	/**
	 * Creates the ReadingParser instances and stores them in a
	 * HashMap.
	 * 
	 * @return the map containing the ReadingParser instances.
	 */
	private Map<String, ReadingParser> initializePackageParsers() {
		if (packageParsers.size() == 0) {
			initializePackageParserNamespaces();
		}
		for (String namespace : packageParsers.keySet()) {
		  // Skip already existing Namespaces
		  if (initializedParsers.containsKey(namespace)) continue;
			try {
				initializedParsers.put(namespace, packageParsers.get(namespace).newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return initializedParsers;
	}
	
	/**
	 * Associates any unknown namespaces with the {@link AnnotationParser}.
	 * 
	 */
	private void addAnnotationParsers(StartElement startElement) 
	{
		@SuppressWarnings("unchecked")
		Iterator<Namespace> namespacesIterator = startElement.getNamespaces();
		
		while (namespacesIterator.hasNext()) {
			String namespaceURI = namespacesIterator.next().getNamespaceURI();
			
			if (initializedParsers.get(namespaceURI) == null) {
				initializedParsers.put(namespaceURI, new AnnotationParser());
			}
		}
	}
	

	/**
	 * Gets the ReadingParser class associated with 'namespace'.
	 * 
	 * @param namespace
	 * @return the ReadingParser class associated with 'namespace'. Null if
	 *         there is not matching ReadingParser class.
	 */
	public Class<? extends ReadingParser> getReadingParsers(String namespace) {
		return packageParsers.get(namespace);
	}


	/**
	 * Initializes the packageParser {@link HashMap} of this class.
	 * 
	 */
	public void initializePackageParserNamespaces() {
		JSBML.loadClasses("org/sbml/jsbml/resources/cfg/PackageParserNamespaces.xml", packageParsers);
	}

	/**
	 * Returns <code>true</code> if there is no 'required' attribute for this
	 * namespace URI, false otherwise.
	 * 
	 * @param namespaceURI
	 * @param startElement
	 *            : the StartElement instance representing the SBMLDocument
	 *            element.
	 * @return <code>true</code> if the package represented by the namespace URI
	 *         is required to read the SBML file. If there is no 'required'
	 *         attribute for this namespace URI, return <code>false</code>.
	 */
	private boolean isPackageRequired(String namespaceURI,
			StartElement startElement) {
		@SuppressWarnings("unchecked")
		Iterator<Attribute> att = startElement.getAttributes();

		while (att.hasNext()) {
			Attribute attribute = (Attribute) att.next();

			if (attribute.getName().getNamespaceURI().equals(namespaceURI)) {
				
				// TODO : we have to check that the attribute name is really required !!!! :-)
				
				if (attribute.getValue().toLowerCase().equals("true")) {
					return true;
				}
				return false;
			}
		}
		return false; // By default, a package is not required?
	}

	/**
	 * Reads the file that is passed as argument and write it to the console, 
	 * using the method {@link SBMLWriter.write}.
	 * 
	 * @param args the command line arguments, we are taking the first one as 
	 * the file name to read.
	 * 
	 * @throws IOException if the file name is not valid.
	 * @throws SBMLException if there are any problems reading or writing the SBML model.
	 * @throws XMLStreamException if there are any problems reading or writing the XML file.
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException, XMLStreamException, SBMLException  {

		if (args.length < 1) {
			System.out
					.println("Usage: java org.sbml.jsbml.xml.stax.SBMLReader sbmlFileName");
			System.exit(0);
		}

		String fileName = args[0];

		SBMLDocument testDocument = new org.sbml.jsbml.SBMLReader().readSBMLFromFile(fileName);
		
		System.out.println("Number of namespaces: " + testDocument.getSBMLDocumentNamespaces().size());

		for(String prefix : testDocument.getSBMLDocumentNamespaces().keySet()){
			System.out.println("PREFIX = "+prefix);
			String uri = testDocument.getSBMLDocumentNamespaces().get(prefix);
			System.out.println("URI = "+uri);
		}

		System.out.println("Model NoRDFAnnotation String = \n@" + testDocument.getModel().getAnnotation().getNonRDFannotation() + "@");

		System.out.println("Model Annotation String = \n@" + testDocument.getModel().getAnnotationString() + "@");
		
		int i = 0;
		for (Species species : testDocument.getModel().getListOfSpecies()) {
			// species.getAnnotationString(); // /scratch/rodrigue/src/jsbml/jsbml_trunk/data/yeast_4.02.xml
			System.out.println("SpeciesType Object = " + species.getSpeciesTypeInstance());
			System.out.println("SpeciesType ID = " + species.getSpeciesType());
			if (i++ > 30) {
				break;
			}
		}
		
		// new SBMLWriter().write(testDocument, System.out);
		
		/*
		String mathMLString1 = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n"
			+ "  <apply>\n"
            + "    <times/>\n"
            + "    <ci> uVol </ci>\n"
            + "    <ci> MKP3 </ci>\n"
            + "  </apply>\n"
            + "</math>\n";
		
		String mathMLString2 = "<math:math xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n"
			+ "  <math:apply>\n"
            + "    <math:times/>\n"
            + "    <math:ci> uVol </math:ci>\n"
            + "    <math:ci> MKP3 </math:ci>\n"
            + "  </math:apply>\n"
            + "</math:math>\n";
		
		String notesHTMLString = "<notes>\n" +
			"  <body xmlns=\"" + JSBML.URI_XHTML_DEFINITION + "\">\n " +
			"    <p>The model describes the double phosphorylation of MAP kinase by an ordered mechanism using the Michaelis-Menten formalism. " +
			"Two enzymes successively phosphorylate the MAP kinase, but one phosphatase dephosphorylates both sites.</p>\n" +
			"  </body>\n" +
			"</notes>";
		
		SBMLReader reader = new SBMLReader();
		
		Object astNodeObject1 = reader.readXMLFromString(mathMLString1);
		Object astNodeObject2 = reader.readXMLFromString(mathMLString2);
		Object xmlNodeObject = reader.readXMLFromString(notesHTMLString);
		
		System.out.println("MathML object = " + astNodeObject1);
		System.out.println("MathML object = " + ((AssignmentRule) astNodeObject2).getMath());
		System.out.println("Notes object = " + ((SBase) xmlNodeObject).getNotes().toXMLString());
		*/
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws XMLStreamException 
	 * @throws IOException 
	 */
	public SBMLDocument readSBML(File file) throws IOException, XMLStreamException {
	  return readSBML(file, null);
	}
	
	/**
	 * Reads a SBML String from the given file.
	 * 
	 * @param file
	 *            A file containing SBML content.
	 * @return the matching SBMLDocument instance.
	 * @throws IOException 
	 * @throws XMLStreamException
	 */
  public SBMLDocument readSBML(File file, TreeNodeChangeListener listener) throws IOException, XMLStreamException {
		FileInputStream stream = new FileInputStream(file);
		XMLStreamException exc1 = null;
		Object readObject = null;
		try {
			readObject = readXMLFromStream(stream, listener);
		} catch (XMLStreamException exc) {
			/*
			 * Catching this exception makes sure that we have still the chance
			 * to close the stream. Otherwise it will stay opened although the
			 * execution of this method is over.
			 */
			exc1 = exc;
		} finally {
			try {
				stream.close();
			} catch (IOException exc2) {
				/*
				 * Ok, we lost. No chance to really close this stream. Heavy
				 * error.
				 */
				if (exc1 != null) {
					exc2.initCause(exc1);
				}
				throw exc2;
			} finally {
				if (exc1 != null) {
					throw exc1;
				}
			}
		}
		if (readObject instanceof SBMLDocument) {
			return (SBMLDocument) readObject;
		}
		throw new XMLStreamException(String.format(
				"JSBML could not properly read file %s. Please check if it contains valid SBML. If you think it is valid, please submit a bug report to the bug tracker of JSBML.",
				(file.getPath() == null) ? "null" : file.getAbsolutePath()));
	}

	/**
	 * Reads SBML from a given file.
	 * 
	 * @param file
	 *            The path to an SBML file.
	 * @return the matching SBMLDocument instance.
	 * @throws XMLStreamException
	 * @throws IOException 
	 */
	public SBMLDocument readSBML(String file) throws XMLStreamException,
			IOException {
		return readSBMLFile(file);
	}

	/**
	 * Reads the SBML file 'fileName' and creates/initialises a SBMLDocument
	 * instance.
	 * 
	 * @param fileName
	 *            : name of the SBML file to read.
	 * @return the initialised SBMLDocument.
	 * @throws XMLStreamException
	 * @throws IOException 
	 */
	public SBMLDocument readSBMLFile(String fileName)
			throws XMLStreamException, IOException {
		return readSBML(new File(fileName));
	}

	
	/**
	 * Reads an {@link SBMLDocument} from the given {@link XMLEventReader}
	 * 
	 * @param xmlEventReader
	 * @param listener 
	 * @return
	 * @throws XMLStreamException
	 */
	public SBMLDocument readSBML(XMLEventReader xmlEventReader, TreeNodeChangeListener listener)
		throws XMLStreamException {
		return (SBMLDocument) readXMLFromXMLEventReader(xmlEventReader, listener);		
	}
	
	/**
	 * 
	 * @param xmlEventReader
	 * @return
	 * @throws XMLStreamException
	 */
	public SBMLDocument readSBML(XMLEventReader xmlEventReader) throws XMLStreamException {
	  return readSBML(xmlEventReader, new SimpleTreeNodeChangeListener());
	}
	
	/**
	 * Reads a mathML String into an {@link ASTNode}.
	 * 
	 * @param mathML
	 * @param listener 
	 * @return an {@link ASTNode} representing the given mathML String.
	 * @throws XMLStreamException
	 */
	public ASTNode readMathML(String mathML, TreeNodeChangeListener listener)
		throws XMLStreamException	{
		Object object = readXMLFromString(mathML, listener);		
		if (object != null && object instanceof Constraint) {
			ASTNode math = ((Constraint) object).getMath();			
			if (math != null) {
				return math;
			}
		}		
		return null;
	}
	
	/**
	 * 
	 * @param mathML
	 * @return
	 * @throws XMLStreamException 
	 */
	public ASTNode readMathML(String mathML) throws XMLStreamException {
	  return readMathML(mathML, new SimpleTreeNodeChangeListener());
	}

	/**
	 * Reads a notes XML String into an {@link XMLNode}.
	 * 
	 * @param notesXHTML
	 * @param listener 
	 * @return an {@link XMLNode} representing the given notes String.
	 * @throws XMLStreamException
	 */
	public XMLNode readNotes(String notesXHTML, TreeNodeChangeListener listener)
		throws XMLStreamException {
		Object object = readXMLFromString(notesXHTML, listener);
		if ((object != null) && (object instanceof Constraint)) {
			Constraint constraint = ((Constraint) object);
			
			if (constraint.isSetNotes()) {
				XMLNode notes = constraint.getNotes();
				if (notes != null) {
					return notes;
				}
			} else if (constraint.isSetMessage()) {
				XMLNode message = constraint.getMessage();
				if (message != null) {
					return message;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param notesXHTML
	 * @return
	 * @throws XMLStreamException 
	 */
	public XMLNode readNotes(String notesXHTML) throws XMLStreamException {
	  return readNotes(notesXHTML, new SimpleTreeNodeChangeListener());
	}

	/**
	 * Reads a SBML document from the given <code>stream</code>. 
	 * 
	 * @param stream
	 * @param listener 
	 * @return
	 * @throws XMLStreamException
	 */
	public SBMLDocument readSBMLFromStream(InputStream stream, TreeNodeChangeListener listener)
			throws XMLStreamException {
		WstxInputFactory inputFactory = new WstxInputFactory();
		XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(stream);
		return (SBMLDocument) readXMLFromXMLEventReader(xmlEventReader, listener);		
	}
	
	/**
	 * 
	 * @param stream
	 * @return
	 * @throws XMLStreamException 
	 */
	public SBMLDocument readSBMLFromStream(InputStream stream) throws XMLStreamException {
	  return readSBMLFromStream(stream, new SimpleTreeNodeChangeListener());
	}

	/**
	 * Reads a XML document from the given <code>stream</code>. It need to be a self contain part of
	 * an SBML document. 
	 * 
	 * @param stream
	 * @param listener 
	 * @return
	 * @throws XMLStreamException
	 */
	private Object readXMLFromStream(InputStream stream, TreeNodeChangeListener listener)
			throws XMLStreamException {
		WstxInputFactory inputFactory = new WstxInputFactory();
		XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(stream);
		return readXMLFromXMLEventReader(xmlEventReader, listener);		
	}
	
		
	/**
	 * Reads an XML document from the given {@link XMLEventReader}. It need to represent a self contain part of
	 * an SBML document. It can be either a math element, a notes element or the whole SBML model. If math or notes are given, 
	 * a Rule containing the math or notes will be returned, otherwise an SBMLDocument is returned.
	 * 
	 * 
	 * @param xmlEventReader
	 * @param listener 
	 * @return an <code>Object</code> representing the given XML.
	 * @throws XMLStreamException
	 */
	private Object readXMLFromXMLEventReader(XMLEventReader xmlEventReader, TreeNodeChangeListener listener)  throws XMLStreamException {

		// Making sure that we use the good XML library
		System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
		System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
		System.setProperty("javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory");

		initializePackageParsers();

		XMLEvent event;
		StartElement startElement = null;
		ReadingParser parser = null;
		Stack<Object> sbmlElements = new Stack<Object>();
		QName currentNode = null;
		boolean isNested = false;
		boolean isText = false;
		boolean isHTML = false;
		boolean isRDFSBMLSpecificAnnotation = false;
		boolean isInsideAnnotation = false;
		boolean isInsideNotes = false;
		int rdfDescriptionIndex = -1;
		int annotationDeepness = -1;
		int level = -1, version = -1;
		Object lastElement = null;
		
		Logger logger = Logger.getLogger(SBMLReader.class);
		
		// Read all the elements of the file
		while (xmlEventReader.hasNext()) {
			event = (XMLEvent2) xmlEventReader.nextEvent();

			// StartDocument
			if (event.isStartDocument()) {
				@SuppressWarnings("unused")
				StartDocument startDocument = (StartDocument) event;
				// nothing to do
			}
			// EndDocument
			else if (event.isEndDocument()) {
				@SuppressWarnings("unused")
				EndDocument endDocument = (EndDocument) event;
				// nothing to do?
			}
			// StartElement
			else if (event.isStartElement()) {
				
				startElement = event.asStartElement();
				currentNode = startElement.getName();
				isNested = false;
				isText = false;
				
				addAnnotationParsers(startElement);

				// If the XML element is the sbml element, creates the
				// necessary ReadingParser instances.
				// Creates an empty SBMLDocument instance and pushes it on
				// the SBMLElements stack.
				if (currentNode.getLocalPart().equals("sbml")) {

					SBMLDocument sbmlDocument = new SBMLDocument();

					// the output of the change listener is activated or not via log4j.properties
					sbmlDocument.addTreeNodeChangeListener(listener == null
							? new SimpleTreeNodeChangeListener() : listener);

					for (@SuppressWarnings("unchecked")
							Iterator<Attribute> iterator = startElement.getAttributes(); iterator.hasNext();) 
					{
						Attribute attr = iterator.next();
						if (attr.getName().toString().equals("level")) {
							level = StringTools.parseSBMLInt(attr.getValue());
							sbmlDocument.setLevel(level);
						} else if (attr.getName().toString().equals("version")) {
							version = StringTools.parseSBMLInt(attr.getValue());
							sbmlDocument.setVersion(version);
						}
					}
					sbmlElements.push(sbmlDocument);
				} else if (lastElement == null) {
					// We put a fake Constraint element in the stack that can take either math, notes or message.
					// This a hack to be able to read some mathMl or notes by themselves.

					if (currentNode.getLocalPart().equals("notes") || currentNode.getLocalPart().equals("message")) {
						// Initializing the core parser again and again is a hughe bottleneck
					  // when appending notes!
						initializedParsers.put("", sbmlCoreParser);
						
					} else if (currentNode.getLocalPart().equals("math")) {
						initializedParsers.put("", new MathMLStaxParser());
						initializedParsers.put(ASTNode.URI_MATHML_DEFINITION, new MathMLStaxParser());
						currentNode = new QName(ASTNode.URI_MATHML_DEFINITION, "math");
						
					}
					
					// TODO : will not work with arbitrary SBML part
					// TODO : we need to be able, somehow, to set the Model element in the Constraint
					// to be able to have a fully functional parsing. Without it the functionDefinition, for examples, are
					// not properly recognized.
					Constraint constraint = new Constraint(3,1);
					sbmlElements.push(constraint);
					
				} else if (currentNode.getLocalPart().equals("annotation")) {

					// get the sbml namespace as some element can have similar names in different namespaces
					SBMLDocument sbmlDoc = (SBMLDocument) sbmlElements.firstElement();
					String sbmlNamespace = sbmlDoc.getSBMLDocumentNamespaces().get("xmlns");

					if (currentNode.getNamespaceURI().equals(sbmlNamespace)) {
						if (isInsideAnnotation) {
							logger.warn("Starting to read a new annotation element while the previous annotation element is not finished.");
						}
						isInsideAnnotation = true;
						annotationDeepness++;
					} 
					
				} else if (isInsideAnnotation) {
					// Count the number of open elements to know how deep we are in the annotation
					// We should only parse the RDF is annotationDeepness == 1 && rdfDescriptionIndex == 0 
					annotationDeepness++;
				} 
				else if (currentNode.getLocalPart().equals("notes")) 
				{
					// get the sbml namespace as some element can have similar names in different namespaces
					SBMLDocument sbmlDoc = (SBMLDocument) sbmlElements.firstElement();
					String sbmlNamespace = sbmlDoc.getSBMLDocumentNamespaces().get("xmlns");

					if (currentNode.getNamespaceURI().equals(sbmlNamespace)) {
						isInsideNotes = true;
					}
				}
				
				// setting isRDFSBMLSpecificAnnotation 
				if (currentNode.getLocalPart().equals("RDF") && currentNode.getNamespaceURI().equals(Annotation.URI_RDF_SYNTAX_NS) && annotationDeepness == 1) {
					isRDFSBMLSpecificAnnotation = true;
				} else if (currentNode.getLocalPart().equals("RDF") && currentNode.getNamespaceURI().equals(Annotation.URI_RDF_SYNTAX_NS)) {
					isRDFSBMLSpecificAnnotation = false;
					rdfDescriptionIndex = -1;
				}

				if (currentNode.getLocalPart().equals("Description") && currentNode.getNamespaceURI().equals(Annotation.URI_RDF_SYNTAX_NS) && isRDFSBMLSpecificAnnotation) {
					rdfDescriptionIndex++;
				}

				if (isInsideAnnotation && logger.isDebugEnabled()) {
					logger.debug("startElement : local part = " + currentNode.getLocalPart());
					logger.debug("startElement : annotation deepness = " + annotationDeepness);
					logger.debug("startElement : rdf description index = " + rdfDescriptionIndex);
					logger.debug("startElement : isRDFSBMLSpecificAnnotation = " + isRDFSBMLSpecificAnnotation);
				}

				parser = processStartElement(startElement, currentNode, isHTML,	sbmlElements, annotationDeepness, isRDFSBMLSpecificAnnotation);
				lastElement = sbmlElements.peek();

			}
			// Characters
			else if (event.isCharacters()) {
				Characters characters = event.asCharacters();

				if (!characters.isWhiteSpace()) {
					isText = true; // the characters are not only 'white spaces'
				}
				if(sbmlElements.empty())
					System.out.println(sbmlElements.toString() + " " + sbmlElements.empty());
				if (sbmlElements.peek() instanceof XMLNode || isInsideNotes) {
					isText = true; // We want to keep the whitespace/formatting when reading html block
				}

				// process the text of a XML element.
				if ((parser != null) && !sbmlElements.isEmpty()	&& (isText || isInsideAnnotation)) {
					
					if (isInsideNotes) {
						parser = initializedParsers.get(JSBML.URI_XHTML_DEFINITION);
					} 
//					else if (isInsideAnnotation) {
//						parser = initializedParsers.get("anyAnnotation");
//					}
					
					if (logger.isDebugEnabled()) {
						logger.debug(" Parser = " + parser.getClass().getName());
						logger.debug(" Characters = @" + characters.getData() + "@");
					}
					
					if (currentNode != null) {
						
						// logger.debug("isCharacter : elementName = " + currentNode.getLocalPart());
						
						parser.processCharactersOf(currentNode.getLocalPart(),
								characters.getData(), sbmlElements.peek());
					} else {
						parser.processCharactersOf(null, characters.getData(),
								sbmlElements.peek());
					}
				} else if (isText) {
					logger.warn(String.format("Some characters cannot be read: %s", characters.getData()));
					if (logger.isDebugEnabled()) {
						logger.debug("Parser = " + parser);
						if (sbmlElements.isEmpty()) {
							logger.debug("The Object Stack is empty !!!");
						} else {
							logger.debug("The current Object in the stack is : " + sbmlElements.peek());
						}
					}

					
				}
			}
			// EndElement
			else if (event.isEndElement()) {

				// the method  processEndElement will return null until we arrive at the end of the 'sbml' element.
				lastElement = sbmlElements.peek();

				currentNode = event.asEndElement().getName();
				
				if (currentNode != null) {
					
					boolean isSBMLelement = true;
					
					// get the sbml namespace as some element can have similar names in different namespaces
					if (sbmlElements.firstElement() instanceof SBMLDocument) {
						SBMLDocument sbmlDoc = (SBMLDocument) sbmlElements.firstElement();
						String sbmlNamespace = sbmlDoc.getSBMLDocumentNamespaces().get("xmlns");
						if (!currentNode.getNamespaceURI().equals(sbmlNamespace)) {
							isSBMLelement = false;
						}
					}
					
					if (currentNode.getLocalPart().equals("annotation")) {
						
						if (isSBMLelement) {
							isInsideAnnotation = false;
							annotationDeepness = -1;
							rdfDescriptionIndex = -1;
							isRDFSBMLSpecificAnnotation = false;
						}

					} else if (isInsideAnnotation) {
						annotationDeepness--;
					}
					else if (currentNode.getLocalPart().equals("notes") && isSBMLelement) 
					{
						isInsideNotes = false;
					}
					
					if (currentNode.getLocalPart().equals("Description") 
							&& currentNode.getNamespaceURI().equals(Annotation.URI_RDF_SYNTAX_NS)) 
					{
						rdfDescriptionIndex--;
					}
				}

				SBMLDocument sbmlDocument = processEndElement(currentNode, isNested, isText, isHTML, 
						level, version, parser, sbmlElements, annotationDeepness, isRDFSBMLSpecificAnnotation);
				
				if (sbmlDocument != null) {
					return sbmlDocument;
				}
				

				currentNode = null;
				isNested = false;
				isText = false;
			} 
		}
		
		// We reach the end of the XML fragment and no 'sbml' have been found
		// so we are probably parsing some math or notes String.
		
		if (logger.isDebugEnabled()) {
			logger.debug("no more XMLEvent : stack.size = " + sbmlElements.size());
		
			logger.debug("no more XMLEvent : stack = " + sbmlElements);
		}
		
		initializedParsers.remove("");
		
		if (sbmlElements.size() > 0) {
			return sbmlElements.peek();
		}
		
		return null;
	}

	/**
	 * Reads a SBML model from the given XML String.
	 * 
	 * @param xml
	 * @param listener 
	 * @return
	 */
	public SBMLDocument readSBMLFromString(String xml, TreeNodeChangeListener listener) throws XMLStreamException {
		Object readObject = readXMLFromStream(new ByteArrayInputStream(xml.getBytes()), listener);
		if (readObject instanceof SBMLDocument) {
			return (SBMLDocument) readObject;
		}		
		throw new XMLStreamException("The given file seems not to be a valid SBMl file. Please check it using the SBML online validator.");
	}
	
	/**
	 * 
	 * @param xml
	 * @return
	 * @throws XMLStreamException 
	 */
	public SBMLDocument readSBMLFromString(String xml) throws XMLStreamException {
	  return readSBMLFromString(xml, new SimpleTreeNodeChangeListener());
	}

	/**
	 * Reads an XML String that should the part of a SBML model.
	 * 
	 * @param xml
	 * @param listener 
	 * @return
	 */
	private Object readXMLFromString(String xml, TreeNodeChangeListener listener)
		throws XMLStreamException {
		return readXMLFromStream(new ByteArrayInputStream(xml.getBytes()), listener);
	}

	
	/**
	 * Process a {@link StartElement} event.
	 * 
	 * @param startElement
	 * @param currentNode
	 * @param isHTML
	 * @param initializedParsers
	 * @param sbmlElements
	 * @return
	 */
	private ReadingParser processStartElement(StartElement startElement, QName currentNode, 
			Boolean isHTML, Stack<Object> sbmlElements, int annotationDeepness, boolean isRDFSBMLspecificAnnotation) 
	{		
		Logger logger = Logger.getLogger(SBMLReader.class);		
		ReadingParser parser = null;

		String elementNamespace = currentNode.getNamespaceURI();

		if (logger.isDebugEnabled()) {
			logger.debug("processStartElement : " + currentNode.getLocalPart() + ", " + elementNamespace);
		}
		
		// To be able to parse all the SBML file, the sbml node
		// should have been read first.
		if (!sbmlElements.isEmpty() && (initializedParsers != null)) {

			// All the element should have a namespace.
			if (elementNamespace != null) {
				
				parser = initializedParsers.get(elementNamespace);
				// if the current node is a notes or message element
				// and the matching ReadingParser is a StringParser,
				// we need to set the typeOfNotes variable of the
				// StringParser instance.
				if (currentNode.getLocalPart().equals("notes")
						|| currentNode.getLocalPart().equals("message")) 
				{
					ReadingParser sbmlparser = initializedParsers.get(JSBML.URI_XHTML_DEFINITION);

					if (sbmlparser instanceof StringParser) {
						StringParser notesParser = (StringParser) sbmlparser;
						notesParser.setTypeOfNotes(currentNode.getLocalPart());
					}
				}

				if (parser != null) {

					@SuppressWarnings("unchecked")
					Iterator<Namespace> nam = startElement.getNamespaces();
					@SuppressWarnings("unchecked")
					Iterator<Attribute> att = startElement.getAttributes();
					boolean hasAttributes = att.hasNext();
					boolean hasNamespace = nam.hasNext();

					if ((elementNamespace.equals(Annotation.URI_RDF_SYNTAX_NS) 
							|| elementNamespace.equals(JSBML.URI_PURL_ELEMENTS)
							|| elementNamespace.equals(JSBML.URI_PURL_TERMS)
							|| elementNamespace.equals(Creator.URI_RDF_VCARD_NS)
							|| elementNamespace.equals(ModelQualifierParser.getNamespaceURI())
							|| elementNamespace.equals(BiologicalQualifierParser.getNamespaceURI()))
							&& !isRDFSBMLspecificAnnotation) 
					{
						parser = initializedParsers.get("anyAnnotation");
					}
					else if (annotationDeepness > 0 && elementNamespace.startsWith("http://www.sbml.org/sbml/level")) 
					{
						// This is probably a mistake in the annotation
						// Sending it to the any parser
						parser = initializedParsers.get("anyAnnotation");
					}
					
					if (annotationDeepness > 0 && elementNamespace.equals(JSBML.URI_XHTML_DEFINITION)) {
						parser = initializedParsers.get("anyAnnotation");
					}
					
					// All the subNodes of SBML are processed.
					if (!currentNode.getLocalPart().equals("sbml")) {
						Object processedElement = parser.processStartElement(currentNode.getLocalPart(), 
								currentNode.getPrefix(), hasAttributes,
								hasNamespace, sbmlElements.peek());
						if (processedElement != null) {
							sbmlElements.push(processedElement);
						} else {
							// It is normal to have sometimes null returned as some of the 
							// XML elements are ignored or do not produce a new java object (like 'apply' in mathML).
						}
					}
					
					// process the namespaces
					processNamespaces(nam, currentNode,sbmlElements, parser, hasAttributes);
					
					// Process the attributes
					processAttributes(att, currentNode, sbmlElements, parser, hasAttributes, annotationDeepness, isRDFSBMLspecificAnnotation);

				} else {
					logger.warn(String.format("Cannot find a parser for the %s namespace", elementNamespace));				
				}
			} else {
				logger.warn(String.format("Cannot find a parser for the %s namespace", elementNamespace));			
			}
		}
		
		return parser;
	}

	// TODO : the attributes hasAttributes, hasNamespace, isLastAttribute and  isLastNamespace are probably not needed for XML reading.
	
	/**
	 * Process Namespaces of the current element on the stack.
	 * 
	 * @param nam
	 * @param currentNode
	 * @param initializedParsers
	 * @param sbmlElements
	 * @param hasAttributes
	 */
	private void processNamespaces(Iterator<Namespace> nam, QName currentNode, 
			Stack<Object> sbmlElements,	ReadingParser parser, boolean hasAttributes) 
	{
		Logger logger = Logger.getLogger(SBMLReader.class);
		ReadingParser namespaceParser = null;

		while (nam.hasNext()) {
			Namespace namespace = (Namespace) nam.next();
			boolean isLastNamespace = !nam.hasNext();
			namespaceParser = initializedParsers.get(namespace.getNamespaceURI());
			
			logger.debug("processNamespaces : " + namespace.getNamespaceURI());
			
			// Calling the currentNode parser to store all the declared namespaces
			parser.processNamespace(currentNode.getLocalPart(),
					namespace.getNamespaceURI(),
					namespace.getName().getPrefix(),
					namespace.getName().getLocalPart(),
					hasAttributes, isLastNamespace,
					sbmlElements.peek());
			
			// Calling each corresponding parser, in case they want to initialize things for the currentNode
			if ((namespaceParser != null) && !namespaceParser.getClass().equals(parser.getClass())) {
				
				logger.debug("processNamespaces 2e parser : " + namespaceParser);
				
				namespaceParser.processNamespace(currentNode.getLocalPart(),
						namespace.getNamespaceURI(),
						namespace.getName().getPrefix(),
						namespace.getName().getLocalPart(),
						hasAttributes, isLastNamespace,
						sbmlElements.peek());
			} else if (namespaceParser == null) {
				logger.warn(String.format("Cannot find a parser for the %s namespace", namespace.getNamespaceURI()));
			}
		}

	}
	
	/**
	 * Process Attributes of the current element on the stack.
	 * 
	 * @param att
	 * @param currentNode
	 * @param initializedParsers
	 * @param sbmlElements
	 * @param parser
	 * @param hasAttributes
	 */
	private void processAttributes(Iterator<Attribute> att, QName currentNode, 
			Stack<Object> sbmlElements, ReadingParser parser, boolean hasAttributes, 
			int annotationDeepness, boolean isRDFSBMLSpecificAnnotation) 
	{
		Logger logger = Logger.getLogger(SBMLReader.class);
		ReadingParser attributeParser = null;

		while (att.hasNext()) {

			Attribute attribute = (Attribute) att.next();
			boolean isLastAttribute = !att.hasNext();
			QName attributeName = attribute.getName();

			if (attribute.getName().getNamespaceURI().length() > 0) {
				String attributeNamespaceURI = attribute.getName().getNamespaceURI();

				if ((attributeNamespaceURI.equals(Annotation.URI_RDF_SYNTAX_NS) 
						|| attributeNamespaceURI.equals(JSBML.URI_PURL_ELEMENTS)
						|| attributeNamespaceURI.equals(JSBML.URI_PURL_TERMS)
						|| attributeNamespaceURI.equals(Creator.URI_RDF_VCARD_NS)
						|| attributeNamespaceURI.equals(ModelQualifierParser.getNamespaceURI())
						|| attributeNamespaceURI.equals(BiologicalQualifierParser.getNamespaceURI()))
						&& !isRDFSBMLSpecificAnnotation) 
				{
					attributeParser = initializedParsers.get("anyAnnotation");
				} 
				else if (annotationDeepness > 0 && attributeNamespaceURI.equals(JSBML.URI_XHTML_DEFINITION)) 
				{
					attributeParser = initializedParsers.get("anyAnnotation");
				}
				else if (annotationDeepness > 0 && attributeNamespaceURI.startsWith("http://www.sbml.org/sbml/level")) 
				{
					// This is probably a mistake in the annotation
					// Sending it to the any parser
					parser = initializedParsers.get("anyAnnotation");
				}
				else 
				{
					attributeParser = initializedParsers.get(attributeNamespaceURI);
				}
				
			} else {
				attributeParser = parser;
			}

			if (attributeParser != null) {
				attributeParser.processAttribute(
						currentNode.getLocalPart(),
						attributeName.getLocalPart(),
						attribute.getValue(), attributeName.getPrefix(),
						isLastAttribute, sbmlElements.peek());
			} else {
				logger.warn("Cannot find a parser for the " + attribute.getName().getNamespaceURI() + " namespace");
			}
		}
	}
	
	
	/**
	 * Process the end of an element.
	 * 
	 * @param currentNode
	 * @param isNested
	 * @param isText
	 * @param isHTML
	 * @param level
	 * @param version
	 * @param parser
	 * @param sbmlElements
	 * @param isRDFSBMLSpecificAnnotation
	 * @return
	 */
	private SBMLDocument processEndElement(QName currentNode, Boolean isNested, Boolean isText, 
			Boolean isHTML, int level, int version, ReadingParser parser, 			
			Stack<Object> sbmlElements, int annotationDeepness, boolean isRDFSBMLSpecificAnnotation) {
		Logger logger = Logger.getLogger(SBMLReader.class);
		
		if (logger.isDebugEnabled()) {
			logger.debug("event.isEndElement : stack.size = " + sbmlElements.size());
			logger.debug("event.isEndElement : element name = " + currentNode.getLocalPart());
			
			if (currentNode.getLocalPart().equals("kineticLaw") || currentNode.getLocalPart().startsWith("listOf")
					|| currentNode.getLocalPart().equals("math")) {
				logger.debug("event.isEndElement : stack = " + sbmlElements);
			}
		}		
		// check that the stack did not increase before and after an element ?
		
		if (initializedParsers != null) {
			String elementNamespaceURI = currentNode.getNamespaceURI();
			parser = initializedParsers.get(elementNamespaceURI);

			// if (!isRDFSBMLSpecificAnnotation && isInsideAnnotation) { // This would be safer to use but would prevent any specific parsing of the annotation
			if ((elementNamespaceURI.equals(Annotation.URI_RDF_SYNTAX_NS) 
					|| elementNamespaceURI.equals(JSBML.URI_PURL_ELEMENTS)
					|| elementNamespaceURI.equals(JSBML.URI_PURL_TERMS)
					|| elementNamespaceURI.equals(Creator.URI_RDF_VCARD_NS)
					|| elementNamespaceURI.equals(ModelQualifierParser.getNamespaceURI())
					|| elementNamespaceURI.equals(BiologicalQualifierParser.getNamespaceURI()))
					&& !isRDFSBMLSpecificAnnotation) 
			{
				parser = initializedParsers.get("anyAnnotation");
			}
			else if (annotationDeepness > 0 && elementNamespaceURI.startsWith("http://www.sbml.org/sbml/level")) 
			{
				// This is probably a mistake in the annotation
				// Sending it to the any parser
				parser = initializedParsers.get("anyAnnotation");
			}
			else if (annotationDeepness > 0 && elementNamespaceURI.equals(JSBML.URI_XHTML_DEFINITION)) 
			{
				parser = initializedParsers.get("anyAnnotation");
			}
			// if the current node is a notes or message element and
			// the matching ReadingParser is a StringParser, we need
			// to reset the typeOfNotes variable of the
			// StringParser instance.
			if (currentNode.getLocalPart().equals("notes")
					|| currentNode.getLocalPart().equals("message")) {
				ReadingParser sbmlparser = initializedParsers.get(JSBML.URI_XHTML_DEFINITION);
				if (sbmlparser instanceof StringParser) {
					StringParser notesParser = (StringParser) sbmlparser;
					notesParser.setTypeOfNotes(currentNode.getLocalPart());
				}
			}
			// process the end of the element.
			if (!sbmlElements.isEmpty() && (parser != null)) {

				if (logger.isDebugEnabled()) {
					logger.debug("event.isEndElement : calling parser.processEndElement " + parser.getClass());
				}

				boolean popElementFromTheStack = parser.processEndElement(currentNode.getLocalPart(),
								currentNode.getPrefix(), isNested, sbmlElements.peek());
				// remove the top of the SBMLElements stack at the
				// end of an element if this element is not the sbml
				// element.
				if (!currentNode.getLocalPart().equals("sbml")) {
					if (popElementFromTheStack) {
						sbmlElements.pop();
					}

					// System.out.println("SBMLReader : event.isEndElement : new stack.size = "
					// + SBMLElements.size());

				} else {
					
					logger.debug("event.isEndElement : sbml element found");
					
					// process the end of the document and return
					// the final SBMLDocument
					if (sbmlElements.peek() instanceof SBMLDocument) {
						SBMLDocument sbmlDocument = (SBMLDocument) sbmlElements.peek();
						
						Iterator<Entry<String, ReadingParser>> iterator = initializedParsers.entrySet().iterator();						
						ArrayList<String> readingParserClasses = new ArrayList<String>();

						// Calling endDocument for all parsers						
						while (iterator.hasNext()) {
							Entry<String, ReadingParser> entry = iterator.next();
							ReadingParser sbmlParser = entry.getValue();
							
							if (!readingParserClasses.contains(sbmlParser.getClass().getCanonicalName())) {

								readingParserClasses.add(sbmlParser.getClass().getCanonicalName());

								logger.debug("event.isEndElement : EndDocument found : parser = " + sbmlParser.getClass());

								sbmlParser.processEndDocument(sbmlDocument);

								// call endDocument only on the parser associated with the namespaces
								// declared on the sbml document ??.
							}
						}
						
						logger.debug("event.isEndElement : EndDocument returned.");
						
						return sbmlDocument;
						
					} else {
						// At the end of a sbml node, the
						// SBMLElements stack must contain only a
						// SBMLDocument instance.
						// Otherwise, there is a syntax error in the
						// SBML document
						logger.warn("!!! event.isEndElement : there is a problem in your SBML file !!!!");
						logger.warn("Found an element '" + sbmlElements.peek().getClass().getCanonicalName() + 
								"', expected org.sbml.jsbml.SBMLDocument");
					}
				}
			} else {
				// If SBMLElements.isEmpty => there is a syntax
				// error in the SBMLDocument
				// If parser == null => there is no parser for
				// the namespace of this element
				logger.warn("!!! event.isEndElement : there is a problem in your SBML file !!!!");
				logger.warn("This should never happen, there is probably a problem with the parsers used." +
						"\n Try to check if one needed parser is missing or if you are using a parser in development.");
			}
		} else {
			// The initialized parsers map should be
			// initialized as soon as there is a sbml node.
			// If it is null, there is an syntax error in the SBML
			// file.
			logger.warn("The parsers are not initialized, this should not happen !!!");
		}
		
		// We return null as long as we did not find the SBMLDocument closing tag
		return null;
	}	
	
}
