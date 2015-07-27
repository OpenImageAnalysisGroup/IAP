// ==============================================================================
//
// DesktopMenuManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DesktopMenuManager.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.ErrorMsg;
import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.MainFrame;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * Manages menu entries for the internal frames contained in a desktop pane. A
 * MenuManager is associated with a {@link javax.swing.JDesktopPane} and a {@link javax.swing.JMenu}. The associated menu is always updated to
 * contain entries for all internal frames in the desktop pane. Selecting such
 * a frame entry selects the corresponding internal frame. In addition,
 * actions for arranging the frames are added to the menu.
 * 
 * @author Michael Forster
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:26 $
 */
public class DesktopMenuManager
					implements MenuListener, SessionListener {
	// ~ Instance fields ========================================================
	
	/** The associated desktop */
	private JDesktopPane desktop;
	
	/** The associated menu */
	private JMenu menu;
	
	/** Menu items created by this manager */
	private List<JComponent> windowItems = new LinkedList<JComponent>();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Create a MenuManager object and associate it with a desktop and a menu.
	 * 
	 * @param desktop
	 *           The associated desktop
	 * @param menu
	 *           The associated menu
	 * @throws NullPointerException
	 *            if a passed parameter is null
	 */
	public DesktopMenuManager(JDesktopPane desktop, JMenu menu) {
		if (desktop == null)
			throw new NullPointerException("desktop must not be null");
		
		if (menu == null)
			throw new NullPointerException("menu must not be null");
		
		this.desktop = desktop;
		this.menu = menu;
		
		menu.addMenuListener(this);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Dispose this manager. Reset the menu, remove all listeners and make this
	 * class eligible for garbage collection.
	 */
	public void dispose() {
		clearMenu();
		menu.removeMenuListener(this);
	}
	
	/**
	 * Ignored.
	 * 
	 * @see javax.swing.event.MenuListener#menuCanceled(javax.swing.event.MenuEvent)
	 */
	public void menuCanceled(MenuEvent e) {
	}
	
	/**
	 * Ignored.
	 * 
	 * @see javax.swing.event.MenuListener#menuDeselected(javax.swing.event.MenuEvent)
	 */
	public void menuDeselected(MenuEvent e) {
	}
	
	/**
	 * Updates the associated menu.
	 * 
	 * @see javax.swing.event.MenuListener#menuSelected(javax.swing.event.MenuEvent)
	 */
	public void menuSelected(MenuEvent e) {
		clearMenu();
		fillMenu();
	}
	
	/**
	 * Add a separator to the associated menu.
	 */
	private void addSeparator() {
		JSeparator sep = new JPopupMenu.Separator();
		menu.add(sep);
		windowItems.add(sep);
	}
	
	/**
	 * Arrange all internal frames in cascading order.
	 */
	public void cascade() {
		final int DX = 26; // horizontal displacement
		final int DY = 26; // vertical displacement
		
		restoreFrames();
		
		JInternalFrame[] frames = desktop.getAllFrames();
		
		Dimension deskSize = desktop.getSize();
		Dimension minSize = minimumFrameSize();
		
		// number of frames to be placed in one turn
		int horizontal = 1 + ((deskSize.width - minSize.width) / DX);
		int vertical = 1 + ((deskSize.height - minSize.height) / DY);
		int framesCount = Math.min(frames.length, Math.min(horizontal, vertical));
		
		// calculate frame positions
		for (int i = 0; i < frames.length; i++) {
			JInternalFrame frame = frames[i];
			
			int x = ((frames.length - i - 1) % framesCount * DX);
			int y = ((frames.length - i - 1) % framesCount * DY);
			int width = deskSize.width - (DX * (framesCount - 1));
			int height = deskSize.height - (DY * (framesCount - 1));
			
			frame.setBounds(x, y, width, height);
		}
	}
	
	/**
	 * Remove all created menu entries.
	 */
	@SuppressWarnings("unchecked")
	private void clearMenu() {
		for (Iterator it = windowItems.iterator(); it.hasNext();) {
			Component item = (Component) it.next();
			menu.remove(item);
		}
		
		windowItems.clear();
	}
	
	/**
	 * Fill entries into the menu.
	 */
	private void fillMenu() {
		if (menu.getMenuComponentCount() > 0)
			addSeparator();
		
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Tile", false);
		item.setMnemonic('T');
		menu.add(item);
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				tile();
				item.setState(!item.getState());
			}
		});
		windowItems.add(item);
		
		final JCheckBoxMenuItem item2 = new JCheckBoxMenuItem("Cascade");
		item2.setMnemonic('C');
		menu.add(item2);
		item2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				cascade();
				item2.setSelected(!item2.isSelected());
			}
		});
		windowItems.add(item2);
		
		addSeparator();
		
		JInternalFrame currentFrame = desktop.getSelectedFrame();
		JInternalFrame[] frames = desktop.getAllFrames();
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		
		for (int i = 0; i < frames.length; i++) {
			final JInternalFrame frame = frames[i];
			JMenuItem item3 = new FrameMenuItem(frame);
			if (frame instanceof GraffitiInternalFrame) {
				GraffitiInternalFrame gif = (GraffitiInternalFrame) frame;
				if (es != null && (gif.getView() == es.getActiveView()))
					item3.setSelected(true);
				else
					item3.setSelected(false);
			} else
				item3.setSelected(frame == currentFrame);
			
			menu.add(item3);
			windowItems.add(item3);
		}
	}
	
	/**
	 * Calculate the smallest minimum size of all frames.
	 * 
	 * @return the smallest minimum size of all frames
	 */
	private Dimension minimumFrameSize() {
		JInternalFrame[] frames = desktop.getAllFrames();
		
		Dimension result = desktop.getSize();
		
		for (int i = 0; i < frames.length; i++) {
			Dimension minSize = frames[i].getMinimumSize();
			
			result.width = Math.min(result.width, minSize.width);
			result.height = Math.min(result.height, minSize.height);
		}
		
		return result;
	}
	
	/**
	 * De-iconify and de-maximize all frames.
	 */
	private void restoreFrames() {
		JInternalFrame[] frames = desktop.getAllFrames();
		
		for (int i = 0; i < frames.length; i++) {
			JInternalFrame frame = frames[i];
			
			try {
				frame.setMaximum(false);
				frame.setIcon(false);
			} catch (PropertyVetoException e) {
				// should not happen
				e.printStackTrace();
				assert false;
			}
		}
	}
	
	/**
	 * Arrange all internal frames in grid fashion.
	 */
	void tile() {
		restoreFrames();
		
		JInternalFrame[] frames = desktop.getAllFrames();
		
		Dimension deskSize = desktop.getSize();
		Dimension minSize = minimumFrameSize();
		
		// number of rows & columns
		double maxColumns = (double) deskSize.width / minSize.width;
		double maxRows = (double) deskSize.height / minSize.height;
		
		int cols = Math.max((int) Math.rint(Math.sqrt(
							(frames.length * maxColumns) / maxRows)), 1);
		int rows = (frames.length / cols);
		
		while ((cols * rows) < frames.length)
			rows++;
		
		// calculate frame positions
		for (int i = 0; i < frames.length; i++) {
			JInternalFrame frame = frames[i];
			
			int width = deskSize.width / cols;
			int height = deskSize.height / rows;
			
			int x = (i % cols) * width;
			int y = (i / cols) * height;
			
			// fill up last row and column
			if ((i % cols) == (cols - 1))
				width = deskSize.width - x;
			
			if ((i / cols) == (rows - 1))
				height = deskSize.height - y;
			
			frame.setBounds(x, y, width, height);
		}
	}
	
	// ~ Inner Classes ==========================================================
	
	/**
	 * A menu item associated to a frame. Selecting the item selects the
	 * associated frame
	 * 
	 * @author Michael Forster
	 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:26 $
	 */
	class FrameMenuItem
						extends JRadioButtonMenuItem
						implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/** The associated frame */
		private JInternalFrame frame;
		
		/**
		 * Create a WindowMenuItem object and associated it to a frame.
		 * 
		 * @param frame
		 *           The associated frame.
		 */
		public FrameMenuItem(JInternalFrame frame) {
			super(frame.getTitle());
			this.frame = frame;
			addActionListener(this);
		}
		
		/**
		 * Selects the associated frame
		 * 
		 * @param event
		 *           ignored
		 */
		public void actionPerformed(ActionEvent event) {
			if (frame.isIcon()) {
				try {
					frame.setIcon(false);
				} catch (PropertyVetoException e) {
					ErrorMsg.addErrorMessage(e);
				}
			} else {
				JDesktopPane parent = (JDesktopPane) frame.getParent();
				if (parent == null) {
					MainFrame.showMessageDialog("Window can't be activated. Internal error: frame-parent (JDesktopPane) is NULL.", "Error");
				} else {
					parent.getDesktopManager().activateFrame(frame);
				}
				try {
					frame.setSelected(true);
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			}
		}
	}
	
	public void sessionChanged(Session s) {
		clearMenu();
	}
	
	public void sessionDataChanged(Session s) {
		// empty
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
