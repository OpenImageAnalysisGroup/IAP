/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

class CenterFrame extends Frame {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param applicationName
	 */
	public CenterFrame(String applicationName) {
		super(applicationName);
	}
	
	public void centerFrame() {
		/* Center the frame */
		GraphicsEnvironment ge = GraphicsEnvironment
							.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle virtualBounds = new Rectangle();
		int j = 0;
		// for (int j = 0; j < gs.length; j++) {
		GraphicsDevice gd = gs[j];
		GraphicsConfiguration[] gc = gd.getConfigurations();
		// for (int i = 0; i < gc.length; i++) {
		virtualBounds = virtualBounds.union(gc[0].getBounds());
		// }
		// }
		
		// Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frameDim = getBounds();
		setLocation((virtualBounds.width - frameDim.width) / 2,
							(virtualBounds.height - frameDim.height) / 2);
	}
}