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
	
	protected JCheckBox jFontStyleBold;
	protected JCheckBox jFontStyleItalic;
	protected JCheckBox jFontStyleShadow;
	protected JComboBox jFontStyleFrame;
	
	public LabelStyleAttributeEditor(final Displayable disp) {
		super(disp);
		jFontStyleBold = new JCheckBox("<html><b>bold");
		jFontStyleItalic = new JCheckBox("<html><i>italic");
		jFontStyleShadow = new JCheckBox("<html>shadow");
		jFontStyleFrame = new JComboBox(LabelFrameSetting.values());
		
		jFontStyleBold.setOpaque(false);
		jFontStyleItalic.setOpaque(false);
		jFontStyleShadow.setOpaque(false);
		jFontStyleFrame.setOpaque(false);
		
		jFontStyleBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText("<html><b>bold");
				jFontStyleItalic.setText("<html><i>italic");
				jFontStyleShadow.setText("<html>shadow");
			}
		});
		jFontStyleItalic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText("<html><b>bold");
				jFontStyleItalic.setText("<html><i>italic");
				jFontStyleShadow.setText("<html>shadow");
			}
		});
		jFontStyleShadow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText("<html><b>bold");
				jFontStyleItalic.setText("<html><i>italic");
				jFontStyleShadow.setText("<html>shadow");
			}
		});
		jFontStyleFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jFontStyleBold.setText("<html><b>bold");
				jFontStyleItalic.setText("<html><i>italic");
				jFontStyleShadow.setText("<html>shadow");
			}
		});
	}
	
	public JComponent getComponent() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			ArrayList<JComponent> ll = new ArrayList<JComponent>();
			ll.add(jFontStyleBold);
			ll.add(jFontStyleItalic);
			// ll.add(jFontStyleShadow);
			ll.add(jFontStyleFrame);
			return TableLayout.getMultiSplitVertical(ll);
		} else
			return TableLayout.getSplitVertical(jFontStyleBold, jFontStyleItalic, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			jFontStyleFrame.setSelectedItem(null);
			jFontStyleBold.setText("<html><font color=\"gray\"><b>~ bold");
			jFontStyleItalic.setText("<html><font color=\"gray\"><i>~ italic");
			jFontStyleShadow.setText("<html><font color=\"gray\">~ shadow");
		} else {
			jFontStyleBold.setText("<html><b>bold");
			jFontStyleItalic.setText("<html><i>italic");
			jFontStyleShadow.setText("<html>shadow");
			String sel = ((LabelStyleAttribute) displayable).getString();
			jFontStyleBold.setSelected(sel.toUpperCase().indexOf("BOLD") >= 0);
			jFontStyleItalic.setSelected(sel.toUpperCase().indexOf("ITALIC") >= 0);
			jFontStyleShadow.setSelected(sel.toUpperCase().indexOf("SHADOW") >= 0);
			jFontStyleFrame.setSelectedItem(LabelFrameSetting.getSettingFromString(sel));
		}
	}
	
	public void setValue() {
		boolean isBold = jFontStyleBold.isSelected();
		boolean isItalic = jFontStyleItalic.isSelected();
		boolean isShadow = jFontStyleShadow.isSelected();
		LabelFrameSetting frame = (LabelFrameSetting) jFontStyleFrame.getSelectedItem();
		String xyz = jFontStyleFrame.getSelectedItem() == null ? "~" : "";
		String ab = jFontStyleBold.getText() + jFontStyleItalic.getText() + jFontStyleShadow.getText() + xyz;
		if (ab.indexOf("~") < 0) {
			String style = "";
			if (isBold || isItalic || isShadow || frame != LabelFrameSetting.NO_FRAME) {
				if (isBold)
					style = "bold";
				if (isItalic) {
					if (style.length() > 0)
						style = style + "," + "italic";
					else
						style = "italic";
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
