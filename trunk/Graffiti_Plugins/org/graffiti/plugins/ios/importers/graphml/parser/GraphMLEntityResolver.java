// ==============================================================================
//
// GraphMLEntityResolver.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphMLEntityResolver.java,v 1.2 2013-05-20 21:13:13 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml.parser;

import java.io.InputStream;
import java.util.logging.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Class <code>GraphMLEntityResolver</code> implements the <code>EntityResolver</code> interface to resolve external entities. In
 * particular, the XML schemas of graphML referred to in the schema location
 * of a graphML file shall be resolved using the locally cached version.
 * 
 * @author ruediger
 */
public class GraphMLEntityResolver
		implements EntityResolver
{
	// ~ Static fields/initializers =============================================
	
	/** The logger for this class. */
	private static final Logger logger = Logger.getLogger(GraphMLEntityResolver.class.getName());
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraphMLEntityResolver</code>.
	 */
	public GraphMLEntityResolver()
	{
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Redirects the external system identifiers pointing to graphML schema
	 * locations to the locally cached schemas.
	 * 
	 * @param publicId
	 *           the public identifier of the external entity being
	 *           referenced, <code>null</code> if none was supplied.
	 * @param systemId
	 *           the system identifier of the external entity being
	 *           referenced.
	 * @return the <code>InputSource</code> object describing the alternative
	 *         input source, <code>null</code> to fall back to the default
	 *         behavior.
	 */
	public InputSource resolveEntity(String publicId, String systemId)
	{
		// the graphML namespace URLs that are supported
		String baseURL = "http://graphml.graphdrawing.org/xmlns/";
		String graphmlURL1 = baseURL + "1.0rc/";
		String graphmlURL2 = baseURL + "graphml/";
		
		logger.finest("publicId: \"" + publicId + "\"");
		logger.finest("systemId: \"" + systemId + "\"");
		
		InputSource is = null;
		
		// the resources for the graphML namespace
		// http://graphml.graphdrawing.org/xmlns/1.0rc
		if (systemId.startsWith(graphmlURL1))
		{
			if (systemId.endsWith(graphmlURL1 + "graphml-attributes.xsd") ||
					systemId.endsWith(graphmlURL1 + "graphml-structure.xsd"))
			{
				is = getSource("graphml-struct.xsd");
			}
			else
				if (systemId.endsWith(graphmlURL1 + "graphml-parseinfo.xsd"))
				{
					is = getSource("graphml-parseinfo.xsd");
				}
				else
				{
					logger.warning("unknown schema location: " + systemId);
				}
		}
		
		// the resources for the graphML namespace
		// http://graphml.graphdrawing.org/xmlns/graphml
		else
			if (systemId.startsWith(graphmlURL2))
			{
				if (systemId.equals(graphmlURL2 + "graphml-attributes-1.0rc.xsd") ||
						systemId.equals(graphmlURL2 + "graphml-structure-1.0rc.xsd"))
				{
					is = getSource("graphml-structure-1.0rc.xsd");
				}
				else
					if (systemId.equals(graphmlURL2 +
							"graphml-parseinfo-1.0rc.xsd"))
					{
						is = getSource("graphml-parseinfo-1.0rc.xsd");
					}
					else
					{
						logger.warning("unknown schema location: " + systemId);
					}
			}
			
			// fallback cases
			else
				if (systemId.endsWith("graphml-attributes-1.0rc.xsd") ||
						systemId.endsWith("graphml-structure-1.0rc.xsd"))
				{
					is = getSource("graphml-structure-1.0rc.xsd");
				}
				else
					if (systemId.endsWith("graphml-attributes.xsd") ||
							systemId.endsWith("graphml-structure.xsd"))
					{
						is = getSource("graphml-struct.xsd");
					}
		
		// for both URLS
		if (systemId.endsWith("xlink.xsd"))
		{
			is = getSource("xlink.xsd");
		}
		
		if (is == null)
		{
			logger.fine("no cached file available for systemID\n\t" + systemId);
		}
		
		return is;
	}
	
	/**
	 * Determines the <code>InputSource</code> for the specified resource and
	 * retuns it.
	 * 
	 * @param resource
	 *           the resource to be determined.
	 * @return the <code>InputSource</code> corresponding to the specified
	 *         resource.
	 */
	private InputSource getSource(String resource)
	{
		logger.fine("searching resource " + resource);
		
		InputStream istr = this.getClass().getResourceAsStream(resource);
		assert istr != null;
		logger.fine("using cached file \"" + resource + "\".");
		
		InputSource is = new InputSource(istr);
		assert is != null : "input source is null";
		
		return is;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
