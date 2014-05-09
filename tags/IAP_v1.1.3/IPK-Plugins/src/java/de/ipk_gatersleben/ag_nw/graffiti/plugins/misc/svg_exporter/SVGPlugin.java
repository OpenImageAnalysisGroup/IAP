/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * This plugin provides a print feature.
 * 
 * @author Christian Klukas
 */
public class SVGPlugin extends IPK_PluginAdapter {
	
	/**
	 * Empty constructor.
	 * <p>
	 * Creates the <code>SVGAlgorithm</code> instance.
	 * </p>
	 * 
	 * @see SVGAlgorithm
	 */
	public SVGPlugin() {
		super();
		
		this.algorithms = new Algorithm[] {
							new GraphicExport(),
							new WebsiteGeneration()
				/*
				 * new JPGAlgorithm(), new PNGAlgorithm(),
				 * new PDFAlgorithm(), new SVGAlgorithm()
				 */
				};
	}
}
