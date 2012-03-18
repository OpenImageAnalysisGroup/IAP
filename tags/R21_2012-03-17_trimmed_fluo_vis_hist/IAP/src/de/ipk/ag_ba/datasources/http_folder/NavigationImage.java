/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 11, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * @author klukas
 */
public class NavigationImage {
	BufferedImage imageDefault, imageNavigation;
	private final String staticID;
	
	public NavigationImage(BufferedImage image, String optStaticId) {
		imageDefault = image;
		imageNavigation = image;
		staticID = optStaticId;
	}
	
	public ImageIcon getImageIcon() {
		return new ImageIcon(imageDefault);
	}
	
	public String optGetStaticId() {
		return staticID;
	}
}
