// ==============================================================================
//
// ImageBundle.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ImageBundle.java,v 1.1 2011-01-31 09:04:46 klukas Exp $

package org.graffiti.core;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * The resource bundle for the images used in the system. This class is
 * implemented using the singlton pattern such that there will always be just
 * on instance of a <code>ImangeBundle</code> created.
 * 
 * @see GenericBundle
 */
public class ImageBundle
					extends GenericBundle {
	// ~ Static fields/initializers =============================================
	
	/** The only instance which will be created and returned. */
	private static ImageBundle instance = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>ImageBundle</code>.
	 */
	protected ImageBundle() {
		super();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of a <code>Bundle</code> this class generates.
	 */
	public static ImageBundle getInstance() {
		if (instance == null) {
			instance = new ImageBundle();
		}
		
		return instance;
	}
	
	/**
	 * Returns the specified image icon or a blank icon, if the specified image
	 * icon could not be found.
	 * 
	 * @param name
	 *           the property name of the icon.
	 * @return the specified image icon.
	 */
	public ImageIcon getIcon(String name) {
		if (name == null) {
			return null;
		}
		
		URL location = getRes(name);
		
		if (location != null) {
			return new ImageIcon(location);
		} else {
			return new ImageIcon(getRes("icon.blank"));
		}
	}
	
	/**
	 * Returns the specified image or a blank image, if the specified image
	 * could not be found.
	 * 
	 * @param name
	 *           the property name of the icon.
	 * @return the specified icon.
	 */
	public Image getImage(String name) {
		if (name == null) {
			return null;
		}
		
		URL location = getRes(name);
		
		if (location != null) {
			return new ImageIcon(location).getImage();
		} else {
			return new ImageIcon(getRes("icon.blank")).getImage();
		}
	}
	
	/**
	 * Returns the specified image icon or a blank icon, if the specified image
	 * icon could not be found.
	 * 
	 * @param name
	 *           the property name of the icon.
	 * @return the specified image icon.
	 */
	public ImageIcon getImageIcon(String name) {
		if (name == null) {
			return null;
		}
		
		URL location = getRes(name);
		
		if (location != null) {
			return new ImageIcon(location);
		} else {
			return new ImageIcon(getRes("icon.blank"));
		}
	}
	
	/**
	 * @see org.graffiti.core.GenericBundle#getBundleLocation()
	 */
	@Override
	protected String getBundleLocation() {
		return "org/graffiti/core/ImageBundle";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
