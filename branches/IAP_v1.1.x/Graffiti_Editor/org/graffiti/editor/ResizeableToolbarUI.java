package org.graffiti.editor;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicToolBarUI;

/**
 * Realizes a JToolbar which is resizeable when detached
 * and can just be docked again by clicking on close.
 * 
 * @author Hendrik Rohn, Christian Klukas
 */

class ResizeableToolbarUI extends BasicToolBarUI {
	
	@Override
	protected RootPaneContainer createFloatingWindow(JToolBar toolbar) {
		JDialog detachedToolbar = (JDialog) super.createFloatingWindow(toolbar);
		detachedToolbar.setResizable(true);
		
		final JToolBar fToolbar = toolbar;
		
		detachedToolbar.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		ArrayList<WindowListener> windowListeners = new ArrayList<WindowListener>();
		for (WindowListener wl : detachedToolbar.getWindowListeners()) {
			windowListeners.add(wl);
		}
		for (WindowListener wl : windowListeners)
			detachedToolbar.removeWindowListener(wl);
		
		detachedToolbar.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {
				fToolbar.setFloatable(false);
				fToolbar.validate();
				fToolbar.repaint();
			}
			
			public void windowClosed(WindowEvent e) {
				MainFrame.getInstance().setSidePanel(fToolbar, fToolbar.getWidth());
				fToolbar.setFloatable(true);
				fToolbar.validate();
				fToolbar.repaint();
			}
			
			public void windowClosing(WindowEvent e) {
			}
			
			public void windowDeactivated(WindowEvent e) {
			}
			
			public void windowDeiconified(WindowEvent e) {
			}
			
			public void windowIconified(WindowEvent e) {
			}
			
			public void windowOpened(WindowEvent e) {
			}
		});
		detachedToolbar.validate();
		detachedToolbar.pack();
		return detachedToolbar;
	}
	
	@Override
	public boolean canDock(Component c, Point p) {
		return false;// super.canDock(c, p);
	}
	
}