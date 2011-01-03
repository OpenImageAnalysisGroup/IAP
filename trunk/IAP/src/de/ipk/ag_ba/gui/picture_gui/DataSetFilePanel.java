package de.ipk.ag_ba.gui.picture_gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.StringManipulationTools;

import de.ipk.ag_ba.gui.util.PopupListener;

/**
 * @author Christian Klukas
 */
public class DataSetFilePanel extends JPanel {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane scrollpane;
	private final FilePanelHeader header;
	private PopupListener popupListener;
	
	public DataSetFilePanel(FilePanelHeader filePanelHeader) {
		this.header = filePanelHeader;
		
		JPopupMenu popup = getPopupMenu();
		
		popupListener = new PopupListener(popup);
		addMouseListener(popupListener);
	}
	
	private JPopupMenu getPopupMenu() {
		int sz = DataSetFileButton.ICON_WIDTH;
		JPopupMenu popup = new JPopupMenu("Button Size");
		
		JMenuItem menuItemCompact = new JCheckBoxMenuItem("Default Size", sz == 128);
		menuItemCompact.addActionListener(getModifyButtonSize(128));
		popup.add(menuItemCompact);
		
		JMenuItem menuItemLarge = new JCheckBoxMenuItem("Large Buttons", sz == 256);
		menuItemLarge.addActionListener(getModifyButtonSize(256));
		popup.add(menuItemLarge);
		
		JMenuItem menuItemExtraLarge = new JCheckBoxMenuItem("Extra Large Buttons", sz == 512);
		menuItemExtraLarge.addActionListener(getModifyButtonSize(512));
		popup.add(menuItemExtraLarge);
		return popup;
	}
	
	protected ActionListener getModifyButtonSize(final int size) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				DataSetFileButton.ICON_HEIGHT = size;
				DataSetFileButton.ICON_WIDTH = size;
				fill();
				
				JPopupMenu popup = getPopupMenu();
				
				removeMouseListener(DataSetFilePanel.this.popupListener);
				DataSetFilePanel.this.popupListener = new PopupListener(popup);
				addMouseListener(DataSetFilePanel.this.popupListener);
			}
		};
	}
	
	@Override
	public Dimension getPreferredSize() {
		Component[] comps = getComponents();
		int maxY = 0;
		for (int i = 0; i < comps.length; i++) {
			Component c = comps[i];
			maxY = (c.getY() + c.getHeight() > maxY) ? c.getY() + c.getHeight() : maxY;
		}
		return new Dimension(getScrollpane().getWidth() - 15, maxY);
	}
	
	boolean warning = false;
	
	public void setHeader(boolean enableButton, String msg, boolean warning, boolean isDefault) {
		
		if (isDefault)
			setDefaultMessage(msg);
		
		if (msg == null || StringManipulationTools.removeHTMLtags(msg).length() <= 0)
			msg = defaultMessage;
		
		header.setText("<html>" + msg);
		header.enableButton(enableButton);
		this.warning = warning;
		if (warning) {
			header.setBackground(new Color(255, 220, 220));
		} else {
			header.setBackground(new Color(240, 245, 240));
		}
	}
	
	private String defaultMessage;
	private Runnable filler;
	
	private void setDefaultMessage(String msg) {
		this.defaultMessage = msg;
	}
	
	public boolean getIsButtonEnabled() {
		return header.isButtonEnabled();
	}
	
	public boolean getIsWarningDisplayed() {
		return warning;
	}
	
	public void setFiller(Runnable runnable) {
		this.filler = runnable;
	}
	
	public void fill() {
		if (filler != null)
			filler.run();
	}
	
	public void setScrollpane(JScrollPane scrollpane) {
		this.scrollpane = scrollpane;
	}
	
	public JScrollPane getScrollpane() {
		return scrollpane;
	}
	
}
