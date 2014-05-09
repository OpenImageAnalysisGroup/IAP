/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_style;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.LabelFrameSetting;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class LabelStyleAttributeEditor
					extends AbstractValueEditComponent {
	
	private static final String SHADOWSTRING = "<html>shadow";
	private static final String MOUSEOVERSTRING = "<html>Mouse over";
	private static final String ITALICSTRING = "<html><i>italic";
	private static final String BOLDSTRING = "<html><b>bold";
	protected JCheckBox jFontStyleBold;
	protected JCheckBox jFontStyleItalic;
	protected JCheckBox jFontStyleMouseOver;
	protected JCheckBox jFontStyleShadow;
	protected JComboBox jFontStyleFrame;
	
	public LabelStyleAttributeEditor(final Displayable disp) {
		super(disp);
		jFontStyleBold = new JCheckBox(BOLDSTRING);
		jFontStyleItalic = new JCheckBox(ITALICSTRING);
		jFontStyleMouseOver = new JCheckBox(MOUSEOVERSTRING);
		jFontStyleShadow = new JCheckBox(SHADOWSTRING);
		jFontStyleFrame = new JComboBox(LabelFrameSetting.values());
		
		jFontStyleBold.setOpaque(false);
		jFontStyleItalic.setOpaque(false);
		jFontStyleMouseOver.setOpaque(false);
		jFontStyleShadow.setOpaque(false);
		jFontStyleFrame.setOpaque(false);
		
		jFontStyleBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText(BOLDSTRING);
				jFontStyleItalic.setText(ITALICSTRING);
				jFontStyleMouseOver.setText(MOUSEOVERSTRING);
				jFontStyleShadow.setText(SHADOWSTRING);
			}
		});
		jFontStyleItalic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText(BOLDSTRING);
				jFontStyleItalic.setText(ITALICSTRING);
				jFontStyleMouseOver.setText(MOUSEOVERSTRING);
				jFontStyleShadow.setText(SHADOWSTRING);
			}
		});
		jFontStyleMouseOver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText(BOLDSTRING);
				jFontStyleItalic.setText(ITALICSTRING);
				jFontStyleMouseOver.setText(MOUSEOVERSTRING);
				jFontStyleShadow.setText(SHADOWSTRING);
			}
		});
		jFontStyleShadow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText(BOLDSTRING);
				jFontStyleItalic.setText(ITALICSTRING);
				jFontStyleMouseOver.setText(MOUSEOVERSTRING);
				jFontStyleShadow.setText(SHADOWSTRING);
			}
		});
		jFontStyleFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText(BOLDSTRING);
				jFontStyleItalic.setText(ITALICSTRING);
				jFontStyleMouseOver.setText(MOUSEOVERSTRING);
				jFontStyleShadow.setText(SHADOWSTRING);
			}
		});
	}
	
	public JComponent getComponent() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			ArrayList<JComponent> ll = new ArrayList<JComponent>();
			ll.add(jFontStyleBold);
			ll.add(jFontStyleItalic);
			ll.add(jFontStyleMouseOver);
			// ll.add(jFontStyleShadow);
			ll.add(jFontStyleFrame);
			return TableLayout.getMultiSplitVertical(ll);
		} else
			return TableLayout.get3SplitVertical(jFontStyleBold, jFontStyleItalic, jFontStyleMouseOver, TableLayoutConstants.PREFERRED,
					TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			jFontStyleFrame.setSelectedItem(null);
			jFontStyleBold.setText("<html><font color=\"gray\"><b>~ bold");
			jFontStyleItalic.setText("<html><font color=\"gray\"><i>~ italic");
			jFontStyleMouseOver.setText("<html><font color=\"gray\"><i>~ Mouse over");
			jFontStyleShadow.setText("<html><font color=\"gray\">~ shadow");
		} else {
			jFontStyleBold.setText(BOLDSTRING);
			jFontStyleItalic.setText(ITALICSTRING);
			jFontStyleMouseOver.setText(MOUSEOVERSTRING);
			jFontStyleShadow.setText(SHADOWSTRING);
			String sel = ((LabelStyleAttribute) displayable).getString();
			jFontStyleBold.setSelected(sel.toUpperCase().indexOf("BOLD") >= 0);
			jFontStyleItalic.setSelected(sel.toUpperCase().indexOf("ITALIC") >= 0);
			jFontStyleMouseOver.setSelected(sel.toUpperCase().indexOf("MOUSEOVER") >= 0);
			jFontStyleShadow.setSelected(sel.toUpperCase().indexOf("SHADOW") >= 0);
			jFontStyleFrame.setSelectedItem(LabelFrameSetting.getSettingFromString(sel));
		}
	}
	
	public void setValue() {
		boolean isBold = jFontStyleBold.isSelected();
		boolean isItalic = jFontStyleItalic.isSelected();
		boolean isMouseOver = jFontStyleMouseOver.isSelected();
		boolean isShadow = jFontStyleShadow.isSelected();
		LabelFrameSetting frame = (LabelFrameSetting) jFontStyleFrame.getSelectedItem();
		String xyz = jFontStyleFrame.getSelectedItem() == null ? "~" : "";
		String ab = jFontStyleBold.getText() + jFontStyleItalic.getText() + jFontStyleShadow.getText() + jFontStyleMouseOver.getText() + xyz;
		if (ab.indexOf("~") < 0) {
			String style = "";
			if (isBold || isItalic || isShadow || isMouseOver || frame != LabelFrameSetting.NO_FRAME) {
				if (isBold)
					style = "bold";
				if (isItalic) {
					if (style.length() > 0)
						style = style + "," + "italic";
					else
						style = "italic";
				}
				if (isMouseOver) {
					if (style.length() > 0)
						style = style + "," + "mouseover";
					else
						style = "mouseover";
				}
				if (isShadow) {
					if (style.length() > 0)
						style = style + "," + "shadow";
					else
						style = "shadow";
				}
				if (frame != LabelFrameSetting.NO_FRAME) {
					if (style.length() > 0)
						style = style + "," + frame.toGMLstring();
					else
						style = frame.toGMLstring();
				}
			} else
				style = "plain";
			((LabelStyleAttribute) displayable).setString(style);
		}
	}
}
