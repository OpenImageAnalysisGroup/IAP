/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 15, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.awt.image.BufferedImage;

import org.graffiti.plugin.view.View;

/**
 * @author klukas
 */
public class BufferedImageResult {
	BufferedImage bi;
	double scale;
	View view;
	String filename;
	
	public BufferedImageResult(BufferedImage b, double s, View v) {
		this.bi = b;
		this.scale = s;
		this.view = v;
	}
	
	public BufferedImage getBufferedImage() {
		return bi;
	}
	
	public double getScale() {
		return scale;
	}
	
	public View getView() {
		return view;
	}
	
	public void setFileName(String filename) {
		this.filename = filename;
	}
	
	public String getFileName() {
		return filename;
	}
}
