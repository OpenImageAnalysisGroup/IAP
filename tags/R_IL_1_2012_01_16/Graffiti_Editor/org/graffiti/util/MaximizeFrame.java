// ==============================================================================
//
// MaximizeFrame.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MaximizeFrame.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.util;

import javax.swing.JInternalFrame;

/**
 * Internal frame that hides the title bar when maximized. Intented to be used
 * together with {@link org.graffiti.util.MaximizeManager}. This extends {@link javax.swing.JInternalFrame} in a single point: Its layout is wrapped
 * with a {@link org.graffiti.util.MaximizeLayout}. Instead of extending this
 * class, a client can alternatively extend JInternalFrame (or a subclass of
 * it) and set its layout manually, e.&nbsp;g., by using the following code:
 * 
 * <pre>
 * setRootPaneCheckingEnabled(false);
 * setLayout(new MaximizeLayout(getLayout()));
 * setRootPaneCheckingEnabled(true);
 * </pre>
 * 
 * @author Michael Forster
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:26 $
 * @see org.graffiti.util.MaximizeManager
 * @see org.graffiti.util.MaximizeLayout
 */
public class MaximizeFrame
					extends JInternalFrame {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Create a MaximizeFrame object.
	 * 
	 * @see JInternalFrame#JInternalFrame()
	 */
	public MaximizeFrame() {
		super();
		init();
	}
	
	/**
	 * Create a MaximizeFrame object.
	 * 
	 * @see JInternalFrame#JInternalFrame(java.lang.String)
	 */
	public MaximizeFrame(String title) {
		super(title);
		init();
	}
	
	/**
	 * Create a MaximizeFrame object.
	 * 
	 * @see JInternalFrame#JInternalFrame(java.lang.String, boolean)
	 */
	public MaximizeFrame(String title, boolean resizable) {
		super(title, resizable);
		init();
	}
	
	/**
	 * Create a MaximizeFrame object.
	 * 
	 * @see JInternalFrame#JInternalFrame(java.lang.String, boolean, boolean)
	 */
	public MaximizeFrame(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
		init();
	}
	
	/**
	 * Create a MaximizeFrame object.
	 * 
	 * @see JInternalFrame#JInternalFrame(java.lang.String, boolean, boolean, boolean)
	 */
	public MaximizeFrame(String title, boolean resizable, boolean closable,
						boolean maximizable) {
		super(title, resizable, closable, maximizable);
		init();
	}
	
	/**
	 * Create a MaximizeFrame object.
	 * 
	 * @see JInternalFrame#JInternalFrame(java.lang.String, boolean, boolean, boolean, boolean)
	 */
	public MaximizeFrame(String title, boolean resizable, boolean closable,
						boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
		init();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Wrap the frame&quot;s layout into a {@link MaximizeLayout}.
	 */
	private void init() {
		setRootPaneCheckingEnabled(false);
		setLayout(new MaximizeLayout(getLayout()));
		setRootPaneCheckingEnabled(true);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
