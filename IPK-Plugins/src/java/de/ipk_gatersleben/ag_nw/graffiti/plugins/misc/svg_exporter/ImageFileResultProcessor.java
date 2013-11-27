/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 16, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.io.File;

/**
 * @author klukas
 */
public interface ImageFileResultProcessor {
	
	public void processCreatedImageFile(File imageFile);
	
	public void processCreatedWebsiteFile(File htmlImageFile);
	
	public void processCreatedTabWebsiteFile(File htmlTabbedFile);
	
}
