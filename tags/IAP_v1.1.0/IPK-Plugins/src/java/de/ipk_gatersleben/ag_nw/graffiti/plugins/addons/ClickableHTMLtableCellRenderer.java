/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 15, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.AttributeHelper;

import de.ipk_gatersleben.ag_nw.graffiti.services.PatchedHTMLEditorKit;

/**
 * @author klukas
 */
public class ClickableHTMLtableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	
	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
						boolean isSelected, boolean hasFocus, int row, int column) {
		
		final JEditorPane jep;
		jep = new JEditorPane();
		jep.setEditorKitForContentType("text/html", new PatchedHTMLEditorKit());
		if (isSelected)
			jep.setBorder(BorderFactory.createLineBorder(UIManager.getColor("ToggleButton.select"), 5));
		else
			jep.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
		jep.setContentType("text/html");
		// jep.setFont(new Font("Serif", Font.PLAIN, 40));
		jep.setText((String) value);
		jep.setEditable(false);
		jep.setBackground(new JPanel().getBackground());
		jep.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
					jep.setCursor(new Cursor(Cursor.HAND_CURSOR));
				if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
					jep.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String url = e.getURL().toString();
					if (url.equals("http://save"))
						; // missing
					else
						AttributeHelper.showInBrowser(url);
				}
				// System.out.println(e.getDescription()+": "+e.getURL().toString());
			}
		});
		
		return jep;
	}
	
}
