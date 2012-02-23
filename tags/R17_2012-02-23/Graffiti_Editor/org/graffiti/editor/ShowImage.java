/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 15, 2010 by Christian Klukas
 */
package org.graffiti.editor;

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;

/**
 * @author klukas
 */
public class ShowImage extends Panel {
	private static final long serialVersionUID = 2163700797926226041L;
	BufferedImage image;
	
	public ShowImage(BufferedImage img) {
		image = img;
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}
}