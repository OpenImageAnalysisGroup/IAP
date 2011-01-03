package de.ipk.ag_ba.gui.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class PopupListener extends MouseAdapter {
	
	private final JPopupMenu popup;
	
	public PopupListener(JPopupMenu popup) {
		this.popup = popup;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	
	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}