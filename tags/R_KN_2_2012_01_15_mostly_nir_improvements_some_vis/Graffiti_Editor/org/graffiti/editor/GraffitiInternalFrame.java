// ==============================================================================
//
// GraffitiInternalFrame.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiInternalFrame.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.editor;

import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;

import javax.swing.JInternalFrame;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.SystemInfo;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

/**
 * A specialized internal frame for the graffiti editor. A <code>GraffitiInternalFrame</code> is always resizable, closeable,
 * maximizable and iconifyable.
 * 
 * @see javax.swing.JInternalFrame
 * @see MainFrame
 */
public class GraffitiInternalFrame
					extends JInternalFrame // MaximizeFrame
{
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The session this frame is in. */
	private EditorSession session;
	
	/** The view this frame contains. */
	private View view;
	
	/** The number of this internal frame. */
	private int frameNumber;
	
	private String initTitle;
	
	Border b = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraffitiInternalFrame</code>.
	 */
	public GraffitiInternalFrame() {
		super();
		this.setMaximizable(true);
		this.setClosable(true);
		this.setResizable(true);
		this.setIconifiable(true);
		
		// this.setFocusable(false);
		setFocusTraversalPolicyProvider(true);
		setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());
		
		setPreferredSize(new Dimension(320, 200));
		
		b = getBorder();
		// setBorder(null);
		
		GravistoService.getInstance().addFrame(this);
		
	}
	
	@Override
	public void doLayout() {
		super.doLayout();
		/*
		 * if (isMaximum() && getBorder()!=null) {
		 * setBorder(null);
		 * doLayout();
		 * }
		 * if (!isMaximum && getBorder()==null) {
		 * setBorder(b);
		 * doLayout();
		 * }
		 */
	}
	
	public String toString() {
		return getTitle();
	}
	
	@Override
	protected void processContainerEvent(ContainerEvent e) {
		super.processContainerEvent(e);
	}
	
	/**
	 * Constructor that sets the session, as well as the title.
	 * 
	 * @param session
	 *           the session this frame is in.
	 * @param view
	 *           DOCUMENT ME!
	 * @param title
	 *           the title of this internal frame.
	 */
	public GraffitiInternalFrame(final EditorSession session, final View view, String title, boolean otherViewWillBeClosed) {
		this();
		this.session = session;
		this.view = view;
		if (otherViewWillBeClosed)
			frameNumber = session.getViews().size();
		else
			frameNumber = session.getViews().size() + 1;
		setTitle(title);
		// setTitle(ErrorMsg.removeHTMLtags(title));
		setListener();
		
		// setOpaque(true);
		// setBackground(Color.WHITE);
		
		// if (ReleaseInfo.isRunningAsApplet())
		
	}
	
	private String getViewType(final View view) {
		String vt = "";
		if (view != null)
			vt = " (" + view.getViewName() + ")";
		if (vt.indexOf("default") >= 0)
			vt = "";
		return vt;
	}
	
	// ~ Methods ================================================================
	
	private void setListener() {
		addInternalFrameListener(new InternalFrameListener() {
			public void internalFrameOpened(InternalFrameEvent e) {
			}
			
			public void internalFrameClosing(InternalFrameEvent e) {
				
			}
			
			public void internalFrameClosed(InternalFrameEvent e) {
				GraffitiInternalFrame.this.session = null;
				// ListenerManager lm = session.getGraph().getListenerManager();
				// try {
				// lm.removeAttributeListener(view);
				// lm.removeEdgeListener(view);
				// lm.removeNodeListener(view);
				// lm.removeGraphListener(view);
				// } catch (ListenerNotFoundException err) {
				// ErrorMsg.addErrorMessage(err);
				// }
			}
			
			public void internalFrameIconified(InternalFrameEvent e) {
			}
			
			public void internalFrameDeiconified(InternalFrameEvent e) {
			}
			
			public void internalFrameActivated(InternalFrameEvent e) {
				// System.out.println("frame activated "+e.toString());
				session.setActiveView(view);
				MainFrame.getInstance().setActiveSession(session, view);
			}
			
			public void internalFrameDeactivated(InternalFrameEvent e) {
				// if (MainFrame.getInstance().getActiveSession()==session)
				// MainFrame.getInstance().setActiveSession(null, null);
			}
		});
	}
	
	// public GraffitiInternalFrame(GraffitiFrame frame) {
	// this();
	// this.session = frame.getSession();
	// this.view = frame.getView();
	// this.frameNumber = frame.getFrameNumber();
	// setTitle(frame.getInitTitle());
	// setListener();
	// }
	
	/**
	 * Returns the session this frame is opened in.
	 * 
	 * @return the session this frame is opened in.
	 */
	public EditorSession getSession() {
		return session;
	}
	
	public static String startTitle = null;
	
	/**
	 * Sets the title of this frame and its associated button and menu button.
	 * 
	 * @param title
	 *           the new title of the frame.
	 */
	@Override
	public void setTitle(String title) {
		// this.initTitle = ErrorMsg.removeHTMLtags(title);
		initTitle = title;
		String frameTitle = title + " - view " + frameNumber;
		super.setTitle(frameTitle);
		if (SystemInfo.isMac()) {
			if (startTitle == null)
				startTitle = MainFrame.getInstance().getTitle();
			// on mac when using the mac os x native style the frame border
			// for internal frames may be disabled (which looks better as the
			// internal frames in maximized state otherwise have a large border
			// and shadow), in this case (getBorder() == null) instead of setting
			// the internal frame title, the main application title is modified
			// for maximized internal frames, this a custom, sometimes also used
			// in other operating systems
			if (isSelected())
				if (getBorder() == null) {
					MainFrame.getInstance().setTitle(startTitle + " - " + frameTitle);
				} else
					MainFrame.getInstance().setTitle(startTitle);
		}
	}
	
	@Override
	public String getTitle() {
		return initTitle + " - view " + frameNumber + getViewType(view);
	}
	
	/**
	 * Returns the view of this frame.
	 * 
	 * @return the view of this frame.
	 */
	public View getView() {
		return view;
	}
	
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public String getInitTitle() {
		return initTitle;
	}
	
	/**
	 * @param editorSessionOfTargetGraph
	 */
	public void setSession(EditorSession s) {
		this.session = s;
		frameNumber = session.getViews().size();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
