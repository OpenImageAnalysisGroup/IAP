/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.06.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import org.AttributeHelper;

public class JLabelHTMLlink extends JLabel {
	String labelText;
	boolean defaultTooltip = false;
	
	public JLabelHTMLlink(String label, final String url) {
		super("<html>" + label);
		labelText = label;
		defaultTooltip = true;
		setUrl(url);
		setForeground(Color.BLUE);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		setCursor(c);
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				AttributeHelper.showInBrowser(urlLink);
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
				setText("<html><u>" + labelText);
			}
			
			public void mouseExited(MouseEvent e) {
				setText("<html>" + labelText);
			}
		});
	}
	
	public JLabelHTMLlink(String label, String tooltip, final Runnable runOnClick) {
		super("<html>" + label);
		labelText = label;
		defaultTooltip = false;
		setToolTipText(tooltip);
		setForeground(Color.BLUE);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		setCursor(c);
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				runOnClick.run();
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
				setText("<html><u>" + labelText);
			}
			
			public void mouseExited(MouseEvent e) {
				setText("<html>" + labelText);
			}
		});
	}
	
	private String urlLink;
	
	public void setUrl(String url) {
		urlLink = url;
		if (defaultTooltip)
			setToolTipText("Open " + url);
	}
	
	public JLabelHTMLlink(String htmlText, final String url, String tooltip) {
		this(htmlText, url, tooltip, true);
	}
	
	public JLabelHTMLlink(String htmlText, final String url, String tooltip, final boolean highlight) {
		super(htmlText);
		defaultTooltip = tooltip == null;
		if (tooltip != null)
			setToolTipText(tooltip);
		setUrl(url);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		setCursor(c);
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				AttributeHelper.showInBrowser(urlLink);
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
				
			}
			
			Color oldColor;
			boolean oldOpaque;
			
			public void mouseEntered(MouseEvent e) {
				if (!highlight || url == null || url.length() <= 0)
					return;
				oldOpaque = isOpaque();
				setOpaque(true);
				oldColor = getBackground();
				setBackground(new Color(240, 240, 255));
			}
			
			public void mouseExited(MouseEvent e) {
				if (!highlight)
					return;
				setOpaque(oldOpaque);
				setBackground(oldColor);
			}
		});
	}
	
	private static final long serialVersionUID = 1L;
	
	public void setLabelText(String text) {
		labelText = text;
		setText(text);
	}
	
}
