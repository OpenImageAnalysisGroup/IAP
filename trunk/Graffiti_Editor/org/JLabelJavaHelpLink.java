/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 21.06.2005 by Christian Klukas
 */
package org;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashSet;

import javax.help.BadIDException;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JLabel;

import org.graffiti.editor.MainFrame;

public class JLabelJavaHelpLink extends JLabel {
	private static final long serialVersionUID = 1L;
	String labelText;
	
	public JLabelJavaHelpLink(String label, final String topic) {
		super(label);
		
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp)) {
			setText("");
			return;
		}
		
		labelText = label;
		setToolTipText("Show Help-Topic \"" + topic + "\"");
		setForeground(Color.BLUE);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		setCursor(c);
		ActionListener al = getHelpActionListener(topic);
		if (al == null) {
			labelText = "[error]";
			setText(labelText);
			return;
		}
		
		final ActionListener fal = al;
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (fal == null)
					MainFrame.showMessageDialog("A internal error occured, the help topic can not be shown.", "Error");
				else
					fal.actionPerformed(new ActionEvent(e.getSource(), e.getID(), ""));
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
	
	// private static HelpBroker hb;
	// private static HelpSet hs;
	
	private static HashSet<String> invalidTopics = new HashSet<String>();
	
	public static ActionListener getHelpActionListener(final String topic) {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp)) {
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MainFrame.showMessageDialog("Help-Function is currently not available for this release", "Not yet implemented");
				}
			};
			return al;
		}
		
		if (topic == null)
			return null;
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (invalidTopics.contains(topic))
					return;
				try {
					ActionListener res;
					HelpBroker hb = null;
					HelpSet hs = null;
					if (hs == null) {
						String helpHS = "main.hs";
						URL hsURL = HelpSet.findHelpSet(JLabelJavaHelpLink.class.getClassLoader(), helpHS);
						hs = new HelpSet(JLabelJavaHelpLink.class.getClassLoader(), hsURL);
					}
					if (hb == null) {
						hb = hs.createHelpBroker();
					}
					try {
						hb.setCurrentID(topic);
					} catch (BadIDException err) {
						ErrorMsg.addErrorMessage("Internal Error: Help Topic " + topic + " is unknown!");
						res = null;
					}
					res = new CSH.DisplayHelpFromSource(hb);
					if (res != null)
						res.actionPerformed(e);
					else {
						if (topic != null)
							invalidTopics.add(topic);
					}
				} catch (Exception ee) {
					ErrorMsg.addErrorMessage(ee);
				}
			}
		};
		return al;
	}
	
}
