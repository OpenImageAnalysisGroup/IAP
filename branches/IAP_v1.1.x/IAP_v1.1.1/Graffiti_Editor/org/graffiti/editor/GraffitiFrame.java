// ==============================================================================
//
// GraffitiInternalFrame.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiFrame.java,v 1.1 2011-01-31 09:04:27 klukas Exp $

package org.graffiti.editor;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.ErrorMsg;
import org.graffiti.plugin.view.CustomFullscreenView;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

/**
 * A specialized internal frame for the graffiti editor. A <code>GraffitiInternalFrame</code> is always resizable, closeable,
 * maximizable and iconifyable.
 * 
 * @see javax.swing.JInternalFrame
 * @see MainFrame
 */
public class GraffitiFrame
					extends JFrame // MaximizeFrame
{
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	/** The session this frame is in. */
	private EditorSession session;
	
	/** The view this frame contains. */
	private View view;
	
	private int frameNumber;
	
	private String initTitle;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraffitiInternalFrame</code>.
	 */
	public GraffitiFrame(final org.graffiti.editor.GraffitiInternalFrame internalFrame, boolean fullscreen) {
		super();
		// Ensure that however the window is closed, it actually causes this
		// detach() method to be fired instead.
		
		final GraffitiFrame thisFrame = this;
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public final void windowClosing(final WindowEvent event) {
				MainFrame.getInstance().removeDetachedFrame(thisFrame);
				setVisible(false);
				MainFrame.getInstance().frameClosing(internalFrame.getSession(), internalFrame.getView());
				dispose();
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				MainFrame.getInstance().setActiveSession(session, view);
				session.setActiveView(view);
				
				GravistoService.getInstance().framesDeselect();
				super.windowActivated(e);
				for (InternalFrameListener ifl : internalFrame.getInternalFrameListeners()) {
					ifl.internalFrameActivated(new InternalFrameEvent(internalFrame, e.getID()));
				}
			}
		});
		
		if (fullscreen) {
			this.setUndecorated(true);
			// setAlwaysOnTop(true);
			this.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
				}
				
				public void keyReleased(KeyEvent e) {
				}
				
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						// fullscreenenabled1.setSelected(false);
						detachOrAttachActiveFrame(false);
					}
				}
			});
			setVisible(true);
			setExtendedState(Frame.MAXIMIZED_BOTH);
		}
		
		this.session = internalFrame.getSession();
		this.view = internalFrame.getView();
		super.setTitle(internalFrame.getTitle());
		this.frameNumber = internalFrame.getFrameNumber();
		this.initTitle = internalFrame.getInitTitle();
		
		if (view.putInScrollPane()) {
			JScrollPane jsp = new JScrollPane(view.getViewComponent());
			jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			
			jsp.setWheelScrollingEnabled(true);
			
			view.getViewComponent().getParent().setBackground(Color.WHITE);
			
			if (MainFrame.isViewProvidingToolbar(view)) {
				Container j = this;
				MainFrame.placeViewInContainer(view, jsp, j);
				
			} else {
				setLayout(TableLayout.getLayout(TableLayoutConstants.FILL, TableLayoutConstants.FILL));
				add(jsp, "0,0");
			}
		} else {
			setLayout(TableLayout.getLayout(TableLayoutConstants.FILL, TableLayoutConstants.FILL));
			add(view.getViewComponent(), "0,0");
		}
		
		setIconImage(MainFrame.getInstance().getIconImage());
		
		validate();
		pack();
		
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the session this frame is opened in.
	 * 
	 * @return the session this frame is opened in.
	 */
	public EditorSession getSession() {
		return session;
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
	
	@Override
	public void setTitle(String title) {
		this.initTitle = title;
		String frameTitle = title + " - view " + frameNumber;
		super.setTitle(frameTitle);
	}
	
	public static void detachOrAttachActiveFrame(boolean fullscreen) {
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		View view = es.getActiveView();
		if (view instanceof CustomFullscreenView) {
			CustomFullscreenView cv = (CustomFullscreenView) view;
			cv.switchFullscreenViewMode(!cv.isInFullscreen());
		} else {
			try {
				GraffitiInternalFrame gif = (GraffitiInternalFrame)
									ErrorMsg.findParentComponent(
														view.getViewComponent(),
														GraffitiInternalFrame.class);
				if (gif != null) {
					MainFrame.getInstance().createExternalFrame(
										view.getClass().getCanonicalName(), es, true, fullscreen);
					gif.doDefaultCloseAction();
				} else {
					GraffitiFrame gf = (GraffitiFrame)
										ErrorMsg.findParentComponent(
															view.getViewComponent(),
															GraffitiFrame.class);
					gf.setVisible(false);
					gf.dispose();
					MainFrame.getInstance().createInternalFrame(
										view.getClass().getCanonicalName(), es, true);
				}
			} catch (Exception err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
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
