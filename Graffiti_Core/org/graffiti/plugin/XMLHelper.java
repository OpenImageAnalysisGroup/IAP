// ==============================================================================
//
// XMLHelper.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: XMLHelper.java,v 1.3 2012-11-07 14:41:59 klukas Exp $

package org.graffiti.plugin;

// Java imports
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Contains some (static) auxiliary methods for writing XML.
 */
public class XMLHelper implements HelperClass {
	// ~ Static fields/initializers =============================================
	
	/** Indicates whether or not indent XML elements. */
	public static boolean useIndentation = false;
	
	// ~ Methods ================================================================
	
	/**
	 * Returns a string used to separate XML elements for better readability.
	 * 
	 * @return XML element delimiter string
	 */
	public static String getDelimiter() {
		// return GeneralUtils.getNewLineDelimiter();
		return "";
	}
	
	/**
	 * Returns a String containing <code>n</code> spaces (or the empty String
	 * if <code>useIndentation</code> is set to <code>false</code>).
	 * 
	 * @param n
	 *           number of spaces
	 * @return DOCUMENT ME!
	 */
	public static String spc(int n) {
		if (useIndentation) {
			StringBuffer sb = new StringBuffer();
			
			for (int i = 0; i < n; i++) {
				sb.append(" ");
			}
			
			return sb.toString();
		} else {
			return "";
		}
	}
	
	public static String getOuterXml(Node node) throws TransformerException {
		DOMSource nodeSource = new DOMSource(node);
		// System.out.println("ClassTypeNodeDOMsource:"+nodeSource.getClass().getCanonicalName());
		StringWriter resultStringWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(resultStringWriter);
		
		Transformer outerXmlTransformer = TransformerFactory.newInstance()
				.newTransformer();
		// System.out.println("OutTransformer:"+outerXmlTransformer.getClass().getCanonicalName());
		outerXmlTransformer.setOutputProperty("omit-xml-declaration", "yes");
		outerXmlTransformer.transform(nodeSource, streamResult);
		
		String result = resultStringWriter.toString();
		result = StringManipulationTools.stringReplace(result, "'", "&apos;");
		return result;
	}
	
	// public static String getOuterXmlPretty(Node n) throws IOException, TransformerException, JDOMException {
	// ByteArrayInputStream is = new ByteArrayInputStream(getOuterXml(n).getBytes("UTF-8"));
	// Document doc = getDocument(is);
	// StringWriter resultStringWriter = new StringWriter();
	// XMLOutputter serializer = new XMLOutputter();
	// serializer.setFormat(Format.getPrettyFormat());
	// serializer.output(getJDOMfromDOM(doc), resultStringWriter);
	// String result = resultStringWriter.toString();
	// result = StringManipulationTools.stringReplace(result, "'", "&apos;");
	// return result;
	// }
	//
	// public static org.jdom.Document getJDOMfromDOM(org.w3c.dom.Document doc) {
	// DOMBuilder db = new DOMBuilder();
	// return db.build(doc);
	// }
	
	/**
	 * @param xmlString
	 * @return
	 */
	public static Node getXMLnodeFromString(String xmlString) {
		Document d = getDocumentFromXMLstring(xmlString);
		if (d != null)
			return d.getFirstChild();
		else
			return d;
	}
	
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	/**
	 * @param res
	 * @return
	 */
	public static Document getDocumentFromXMLstring(String res) {
		// System.out.println("Try to parse:\n"+res);
		// Set namespaceAware to true to get a DOM Level 2 tree with nodes
		// containing namespace information. This is necessary because the
		// default value from JAXP 1.0 was defined to be false.
		dbf.setNamespaceAware(false);
		DocumentBuilder db;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			// Step 3: parse the input file
			InputSource is = new InputSource(new StringReader(res));
			doc = db.parse(is);
			// System.err.println("Type of XML Document Builder: " + db.getClass().getCanonicalName());
			return doc;
		} catch (NullPointerException e) {
			ErrorMsg.addErrorMessage("Null Pointer Exception, data could not be retrieved.<br>"
					+ e.getLocalizedMessage());
		} catch (SAXException e) {
			ErrorMsg.addErrorMessage("SAX Exception while processing experimental data.<br>"
					+ e.getLocalizedMessage());
		} catch (IOException e) {
			ErrorMsg.addErrorMessage("IO Exception while processing experimental data.<br>"
					+ e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			ErrorMsg.addErrorMessage("Format Parser Configuration Exception while processing experimental data.<br>"
					+ e.getLocalizedMessage());
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("Exception, data could not be processed.<br>"
					+ e.getLocalizedMessage());
		}
		return null;
	}
	
	public static Document getDocument(InputStream inpS) {
		// System.out.println("Try to parse:\n"+res);
		// Set namespaceAware to true to get a DOM Level 2 tree with nodes
		// containing namesapce information. This is necessary because the
		// default value from JAXP 1.0 was defined to be false.
		dbf.setNamespaceAware(false);
		DocumentBuilder db;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			// Step 3: parse the input file
			InputSource is = new InputSource(inpS);
			doc = db.parse(is);
			// System.err.println("Type of XML Document Builder: " +
			// db.getClass().getCanonicalName());
			return doc;
		} catch (NullPointerException e) {
			ErrorMsg
					.addErrorMessage("Null Pointer Exception, data could not be retrieved.<br>"
							+ e.getLocalizedMessage());
		} catch (SAXException e) {
			ErrorMsg
					.addErrorMessage("Format Parser (SAX) Exception while processing experimental data.<br>"
							+ e.getLocalizedMessage());
		} catch (IOException e) {
			ErrorMsg
					.addErrorMessage("IO Exception while processing experimental data.<br>"
							+ e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			ErrorMsg
					.addErrorMessage("Format Parser Configuration Exception while processing experimental data.<br>"
							+ e.getLocalizedMessage());
		}
		return null;
	}
	
	public static void validate(Document doc, URL xsdLocation) throws Exception {
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(xsdLocation);
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(doc));
	}
	
	public static void writeXMLDataToFile(Document doc, String path_and_filename) {
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			Result result = new StreamResult(new File(path_and_filename));
			Source source = new DOMSource(doc);
			t.transform(source, result);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static String getOuterXmlPretty(Node n) throws IOException, TransformerException, JDOMException {
		ByteArrayInputStream is = new ByteArrayInputStream(getOuterXml(n).getBytes("UTF-8"));
		Document doc = getDocument(is);
		StringWriter resultStringWriter = new StringWriter();
		XMLOutputter serializer = new XMLOutputter();
		serializer.setFormat(Format.getPrettyFormat());
		serializer.output(getJDOMfromDOM(doc), resultStringWriter);
		String result = resultStringWriter.toString();
		result = StringManipulationTools.stringReplace(result, "'", "&apos;");
		return result;
	}
	
	public static org.jdom.Document getJDOMfromDOM(org.w3c.dom.Document doc) {
		DOMBuilder db = new DOMBuilder();
		return db.build(doc);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
