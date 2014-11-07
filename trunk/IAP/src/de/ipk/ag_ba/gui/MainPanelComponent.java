/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.AttributeHelper;
import org.Colors;

import de.ipk.ag_ba.gui.util.FlowLayoutImproved;
import de.ipk_gatersleben.ag_nw.graffiti.services.PatchedHTMLEditorKit;

/**
 * @author klukas
 */
public class MainPanelComponent {
	
	HashMap<String, Object> properties = new HashMap<String, Object>();
	private JComponent component;
	private Collection<String> htmlTextPanels;
	
	public MainPanelComponent(JComponent gui) {
		this.component = gui;
	}
	
	public Collection<String> getHTML() {
		return htmlTextPanels;
	}
	
	public MainPanelComponent(Collection<String> htmlTextPanels) {
		this(htmlTextPanels, null);
	}
	
	public MainPanelComponent(Collection<String> htmlTextPanels, Color optCustomBackgroundColor) {
		this.htmlTextPanels = htmlTextPanels;
		
		ArrayList<JComponent> infos = new ArrayList<JComponent>();
		for (String txt : htmlTextPanels) {
			final JEditorPane jep = getTextComponent(optCustomBackgroundColor, txt);
			
			infos.add(jep);
		}
		if (infos.size() > 1) {
			JComponent jp = new JPanel(new FlowLayoutImproved(FlowLayout.LEFT, 20, 20));
			jp.setOpaque(false);
			
			for (JComponent jc : infos)
				jp.add(jc);
			
			jp.validate();
			
			this.component = jp;
		} else
			if (infos.size() == 1) {
				JComponent jp = new JPanel(new TableLayout(new double[][] { { TableLayout.FILL }, { TableLayout.PREFERRED } }));
				jp.setOpaque(false);
				jp.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				
				jp.add(infos.get(0), "0,0");
				
				jp.validate();
				
				this.component = jp;
				
			}
		component.putClientProperty("isHTML", true);
	}
	
	public static JEditorPane getTextComponent(Color optCustomBackgroundColor, String txt) {
		final JEditorPane jep;
		jep = new JEditorPane();
		jep.setEditorKitForContentType("text/html", new PatchedHTMLEditorKit());
		jep.setContentType("text/html");
		jep.setText(txt);
		jep.setEditable(false);
		jep.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		if (!(optCustomBackgroundColor != null && optCustomBackgroundColor.getRed() == 1
				&& optCustomBackgroundColor.getGreen() == 1 && optCustomBackgroundColor.getBlue() == 1)) {
			jep.setOpaque(true);
			if (optCustomBackgroundColor != null)
				jep.setBackground(optCustomBackgroundColor);
			else
				jep.setBackground(Colors.brighten(IAPnavigationPanel.getTabColor(), 0.8, 1.2));
		} else
			jep.setOpaque(false);
		jep.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
					jep.setCursor(new Cursor(Cursor.HAND_CURSOR));
				if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
					jep.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String url = e.getURL().toString();
					AttributeHelper.showInBrowser(url);
				}
			}
		});
		return jep;
	}
	
	public MainPanelComponent(String htmlTextPanel) {
		this(getList(htmlTextPanel));
	}
	
	public MainPanelComponent(JComponent ip, boolean borderAroundTheComponent) {
		if (borderAroundTheComponent) {
			JComponent jp = TableLayout.getSplit(ip, null, TableLayout.PREFERRED, TableLayout.FILL);
			jp.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
			jp = TableLayout.getSplitVertical(jp, null, TableLayout.PREFERRED, TableLayout.FILL);
			jp = TableLayout.getSplitVertical(jp, null, TableLayout.PREFERRED, TableLayout.FILL);
			this.component = jp;
		} else {
			this.component = ip;
		}
	}
	
	public MainPanelComponent(Collection<JComponent> res, int border) {
		JComponent jp = new JPanel(new FlowLayoutImproved(FlowLayout.LEFT, border, border));
		jp.setOpaque(false);
		
		for (JComponent jc : res)
			jp.add(jc);
		
		jp.validate();
		
		this.component = jp;
	}
	
	private static ArrayList<String> getList(String htmlTextPanel) {
		ArrayList<String> txt = new ArrayList<String>();
		txt.add(htmlTextPanel);
		return txt;
	}
	
	public JComponent getGUI() {
		return component;
	}
	
	public void setProperty(String p, Object o) {
		properties.put(p, o);
	}
	
	public Object getProperty(String p) {
		return properties.get(p);
	}
	
	public void setGUI(JComponent gui) {
		this.component = gui;
	}
}
