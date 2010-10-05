package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.StringManipulationTools;

/**
 * @author Christian Klukas
 */
public class JMyFilePanel extends JPanel {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	public JScrollPane scrollpane;
	private final FilePanelHeader header;

	public JMyFilePanel(FilePanelHeader filePanelHeader) {
		this.header = filePanelHeader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		Component[] comps = getComponents();
		int maxY = 0;
		for (int i = 0; i < comps.length; i++) {
			Component c = comps[i];
			maxY = (c.getY() + c.getHeight() > maxY) ? c.getY() + c.getHeight() : maxY;
		}
		return new Dimension(scrollpane.getWidth() - 15, maxY);
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

	private void setDefaultMessage(String msg) {
		this.defaultMessage = msg;
	}

	public boolean getIsButtonEnabled() {
		return header.isButtonEnabled();
	}

	public boolean getIsWarningDisplayed() {
		return warning;
	}

}
