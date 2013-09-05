// ==============================================================================
//
// GraphMLWriter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphMLWriter.java,v 1.1 2011-01-31 09:03:25 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphml;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.AttributeHelper;
import org.ReleaseInfo;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graphics.AWTImageAttribute;
import org.graffiti.graphics.LineModeAttribute;
import org.graffiti.plugin.io.AbstractOutputSerializer;
import org.graffiti.plugins.ios.exporters.gml.GMLWriter;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Class <code>GraphMLWriter</code> is the main class for writing a graph to a
 * graphML file. This class uses the JAXP API to build a DOM tree which is
 * then written to a specified <code>OutputStream</code>.
 * 
 * @author ruediger
 */
public class GraphMLWriter
					extends AbstractOutputSerializer {
	// ~ Static fields/initializers =============================================
	
	/** The default namespace that will be used in the graphML file. */
	private static final String GRAPHML_ROOT = "http://graphml.graphdrawing" +
						".org/xmlns/graphml";
	
	/** The W3C schema instance namespace. */
	private static final String SCHEMA_INSTANCE = "http://www.w3.org/2001/" +
						"XMLSchema-instance";
	
	/** The location of the schema the written graphML file shall conform to. */
	private static final String SCHEMA_LOCATION = GRAPHML_ROOT + " " +
						GRAPHML_ROOT + "/graphml-structure-1.0rc.xsd";
	
	/** The logger for this class. */
	private static final Logger logger = Logger.getLogger(GraphMLWriter.class.getName());
	
	// ~ Instance fields ========================================================
	
	/** The mapping from nodes to ids. */
	private NodeMap nodeMap;
	
	/** The mapping from Gravisto attribute types to graphML attribute types. */
	private TypeMap typeMap;
	
	/** Indicates whether edges are directed by default or not. */
	private boolean edgedefault;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraphMLWriter</code>.
	 */
	public GraphMLWriter() {
		super();
		this.nodeMap = new NodeMap();
		this.typeMap = new TypeMap();
	}
	
	// ~ Methods ================================================================
	
	/*
	 *
	 */
	public String[] getExtensions() {
		String[] exts = { ".graphml"/* , ".xml" */};
		
		return exts;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "GraphML" /* , "GraphXML" */};
	}
	
	/*
	 *
	 */
	public void write(OutputStream stream, Graph g)
						throws IOException {
		// create a new document
		Document doc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException pce) {
			throw new IOException(pce.getMessage());
		}
		
		// a list to collect all the attribute declarations
		KeyMap graphAttr = new KeyMap();
		KeyMap nodeAttr = new KeyMap();
		KeyMap edgeAttr = new KeyMap();
		
		// create the graphml element as the root node of the file
		Element root = doc.createElement("graphml");
		root.setAttribute("xmlns", GRAPHML_ROOT);
		root.setAttribute("xmlns:xsi", SCHEMA_INSTANCE);
		root.setAttribute("xsi:schemaLocation", SCHEMA_LOCATION);
		
		// create a comment
		Date date = new Date();
		Comment comment = doc.createComment(" Created by the Gravisto/" + ReleaseInfo.getRunningReleaseStatus().toString() + " " +
							"graphML writer plugin on " + date.toString() + ". ");
		doc.appendChild(comment);
		
		Node rootNode = doc.appendChild(root);
		
		// graph declaration
		Element graph = doc.createElement("graph");
		graph.setAttribute("id", "G");
		
		// set the edgedefault attribute of the graph element
		if (g.isUndirected()) {
			graph.setAttribute("edgedefault", "undirected");
			this.edgedefault = false;
		} else {
			graph.setAttribute("edgedefault", "directed");
			this.edgedefault = true;
		}
		
		// graph attributes
		Map<?, String> gAttributes = new Hashtable<Object, String>();
		createDataDeclarationsMap(g.getAttributes(), graphAttr, "ga",
							gAttributes, "");
		
		// create the key elements for graph attributes
		appendKeyDeclarations(graphAttr, "graph", doc, root);
		
		// create the data elements for the graph
		appendDataDeclarations(gAttributes, doc, graph);
		
		// create elements for the nodes - addition to the document is deferred
		// until all nodes and edges are processed.
		Element[] nodes = new Element[g.getNumberOfNodes()];
		int nonodes = -1;
		
		for (Iterator<?> itr = g.getNodesIterator(); itr.hasNext();) {
			Map<?, String> nAttributes = new Hashtable<Object, String>();
			org.graffiti.graph.Node n = (org.graffiti.graph.Node) itr.next();
			int id = nodeMap.add(n);
			Element node = doc.createElement("node");
			node.setAttribute("id", "n" + id);
			if (n.getViewID() != 0)
				AttributeHelper.setAttribute(n, "", "zlevel", n.getViewID());
			
			// add node attributes
			createDataDeclarationsMap(n.getAttributes(), nodeAttr, "na",
								nAttributes, "");
			appendDataDeclarations(nAttributes, doc, node);
			
			if (n.getViewID() != 0)
				AttributeHelper.deleteAttribute(n, "", "zlevel");
			
			nodes[++nonodes] = node;
		}
		
		// append the keys for nodes
		appendKeyDeclarations(nodeAttr, "node", doc, root);
		
		// add edges
		Element[] edges = new Element[g.getNumberOfEdges()];
		int noedges = 0;
		
		for (Iterator<?> itr = g.getEdgesIterator(); itr.hasNext();) {
			Map<?, String> eAttributes = new Hashtable<Object, String>();
			Edge e = (Edge) itr.next();
			int sourceId = nodeMap.getId(e.getSource());
			int targetId = nodeMap.getId(e.getTarget());
			Element edge = doc.createElement("edge");
			edge.setAttribute("id", "e" + noedges);
			if (e.getViewID() != 0)
				AttributeHelper.setAttribute(e, "", "zlevel", e.getViewID());
			edge.setAttribute("source", "n" + sourceId);
			edge.setAttribute("target", "n" + targetId);
			
			// if necessary set the directed attribute
			if (e.isDirected() != edgedefault) {
				edge.setAttribute("directed", "" + e.isDirected());
			}
			
			// add edge attributes
			createDataDeclarationsMap(e.getAttributes(), edgeAttr, "ea",
								eAttributes, "");
			appendDataDeclarations(eAttributes, doc, edge);
			
			if (e.getViewID() != 0)
				AttributeHelper.deleteAttribute(e, "", "zlevel");
			
			edges[noedges] = edge;
			++noedges;
		}
		
		// append the keys for edges
		appendKeyDeclarations(edgeAttr, "edge", doc, root);
		
		// build the rest of the document - add nodes and edges to the document
		rootNode.appendChild(graph);
		
		for (int i = 0; i < nodes.length; ++i) {
			graph.appendChild(nodes[i]);
		}
		
		for (int i = 0; i < edges.length; ++i) {
			graph.appendChild(edges[i]);
		}
		
		// create a transformer for writing to the output stream
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException tce) {
			throw new IOException(tce.getMessage());
		}
		
		// set output properties
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		// System.out.println(transformer.getOutputProperty(OutputKeys.METHOD));
		Source src = new DOMSource(doc);
		Result result = new StreamResult(stream);
		
		try {
			transformer.transform(src, result);
		} catch (TransformerException te) {
			throw new IOException(te.getMessage());
		}
		
		stream.close();
	}
	
	/**
	 * Adds an <code>Element</code> to the DOM tree for each data declaration
	 * in the given <code>Map</code>.
	 * 
	 * @param gAttributes
	 *           the mapping from ids to values for each data
	 *           declaration to be created.
	 * @param doc
	 *           the document in which to create the data element.
	 * @param parent
	 *           the parent element to which to add the data elements the
	 *           method creates.
	 */
	private void appendDataDeclarations(Map<?, String> gAttributes, Document doc,
						Element parent) {
		for (Iterator<?> itr = gAttributes.keySet().iterator(); itr.hasNext();) {
			String id = (String) itr.next();
			String data = (String) gAttributes.get(id);
			Element dataElem = doc.createElement("data");
			Text dataElemText = doc.createTextNode(data);
			dataElem.setAttribute("key", id);
			dataElem.appendChild(dataElemText);
			parent.appendChild(dataElem);
		}
	}
	
	/**
	 * Adds a graphml key element for each entry in the specified <code>KeyMap</code> to the specified <code>Document</code> at the
	 * specified <code>Element</code>.
	 * 
	 * @param km
	 *           the <code>KeyMap</code> mapping paths to identifiers and
	 *           types.
	 * @param forAttr
	 *           indicates for which graphml element type the key
	 *           declaration is.
	 * @param doc
	 *           the document to which to add the key element node.
	 * @param root
	 *           the root element to which to append the created key element
	 *           as child node.
	 */
	private void appendKeyDeclarations(KeyMap km, String forAttr, Document doc,
						Element root) {
		String[] paths = km.getPaths();
		
		// iterate over all possible attribute paths
		for (int i = 0; i < paths.length; ++i) {
			KeyData[] kd = km.getTypes(paths[i]);
			
			// iterate over the types that can appear at that path and create
			// a key element for each
			for (int k = 0; k < kd.length; ++k) {
				Element key = doc.createElement("key");
				key.setAttribute("id",
									forAttr.substring(0, 1) + "a" + kd[k].getId());
				assert !forAttr.equals("");
				key.setAttribute("for", forAttr);
				
				// determine the graphML type - if there is none, use string
				String graphMLType = typeMap.getGraphMLType(kd[k].getType());
				
				if (graphMLType == null) {
					System.err.println("No corresponding graphML type for " +
							kd[k].getType() + " - using string.");
					key.setAttribute("attr.type", "string");
				} else {
					key.setAttribute("attr.type", graphMLType);
				}
				
				// add the path as the name attribute of the key element and
				// append the element to the specified root
				key.setAttribute("attr.name", paths[i]);
				root.appendChild(key);
			}
		}
	}
	
	/**
	 * Creates the <code>KeyMap</code> by recursively processing the specified <code>CollectionAttribute</code>.
	 * 
	 * @param ca
	 *           the <code>CollectionAttribute</code> to be processed.
	 * @param km
	 *           the <code>KeyMap</code> to be extended.
	 * @param suffix
	 *           the suffix that will be used for the ids in the graphML
	 *           file.
	 * @param data
	 *           mapping from identifier to the corresponding attribute value
	 *           represented as a <code>string</code>.
	 */
	@SuppressWarnings("unchecked")
	private void createDataDeclarationsMap(CollectionAttribute ca, KeyMap km,
						String suffix, Map data, String parent) {
		for (Iterator itr = ca.getCollection().keySet().iterator(); itr.hasNext();) {
			String id = (String) itr.next();
			Attribute attr = ca.getAttribute(id);
			
			// if the attribute is a CollectionAttribute the method calls itself
			// recursively, ...
			try {
				CollectionAttribute c = (CollectionAttribute) attr;
				createDataDeclarationsMap(c, km, suffix, data, parent + "." + c.getId());
			}
			// ... otherwise the attribute's path and type are added to the
			// key map
			catch (ClassCastException cce) {
				String path = attr.getPath();
				if (!path.startsWith(".")) {
					path = parent + "." + path.substring(path.lastIndexOf(".") + 1);
				}
				String text = "";
				String type = attr.getClass().getName();
				int attid = km.add(path, type);
				
				// special case for writing line mode attributes
				// TODO this might be modified once the attributes work as
				// expected.
				if (attr instanceof LineModeAttribute &&
									attr.getPath().equals(".graphics.linemode")) {
					text = handleLineMode(attr);
				}
				// special case when writing an image attribute
				else
					if (attr instanceof AWTImageAttribute) {
						text = handleImageAttribute(attr, text);
					}
					// the general case for writing attributes
					else {
						text = attr.getValue().toString();
					}
				data.put(suffix + attid, text);
			}
		}
	}
	
	private String handleImageAttribute(Attribute attr, String text) {
		logger.fine("writing AWTImageAttribute at path " +
							attr.getPath());
		
		AWTImageAttribute iAttr = (AWTImageAttribute) attr;
		Image image = iAttr.getImage();
		
		BufferedImage bi = (BufferedImage) image;
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try {
			boolean writerFound = ImageIO.write(bi, "png", output);
			assert writerFound : "no image writer was found.";
			
			text = Base64.encodeBytes(output.toByteArray(),
								Base64.DONT_BREAK_LINES);
		} catch (IOException e) {
			logger.warning("could not writer AWTImageAttribute " +
								"at path " + iAttr.getPath() + ".");
			text = "";
		}
		return text;
	}
	
	private String handleLineMode(Attribute attr) {
		String text;
		LineModeAttribute lma = (LineModeAttribute) attr;
		float[] dashArray = lma.getDashArray();
		float dashPhase = lma.getDashPhase();
		
		text = GMLWriter.getValues(dashArray, " ") + " " + dashPhase;
		
		logger.fine("writing attribute .graphics.linemode with " +
							"value " + text + ".");
		return text;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
