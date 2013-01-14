// ==============================================================================
//
// LabelAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LabelAttribute.java,v 1.2 2012-11-07 14:41:59 klukas Exp $

package org.graffiti.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.BreakIterator;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.LabelFrameSetting;
import org.StringManipulationTools;
import org.color.ColorUtil;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.ColorSetAndGetSupport;
import org.graffiti.attributes.FieldAlreadySetException;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Node;

/**
 * Contains the graphic attribute label
 * 
 * @version $Revision: 1.2 $
 */
public abstract class LabelAttribute extends HashMapAttribute implements
		GraphicAttributeConstants {
	// ~ Instance fields
	// ========================================================
	
	private int lastComponentHeight = -1;
	
	public void setLastComponentHeight(int height) {
		lastComponentHeight = height;
	}
	
	public int getLastComponentHeight() {
		if (lastComponentHeight >= 0)
			return lastComponentHeight;
		else
			return (new JLabel(getLabel())).getPreferredSize().height;
	}
	
	private int lastComponentWidth = -1;
	
	public void setLastComponentWidth(int width) {
		lastComponentWidth = width;
	}
	
	public int getLastComponentWidth() {
		if (lastComponentWidth >= 0)
			return lastComponentWidth;
		else
			return (new JLabel(getLabel())).getPreferredSize().width;
	}
	
	// private JLabel lastLabel = null;
	
	private static final int defaultSize = 12; // new
	// JLabel().getFont().getSize();
	private static final String defaultFont = getDefaultFont();
	
	public void setLastLabel(JLabel lastLabel) {
		// this.lastLabel = lastLabel;
	}
	
	private static final String defaultFontName = "Arial";
	
	private static String getDefaultFont() {
		return defaultFontName;
		// String name = "Arial"; // new JLabel().getFont().getFamily();
		// return name;
	}
	
	// ~ Constructors
	// ===========================================================
	public LabelAttribute() {
		this(LABELGRAPHICS);
		ErrorMsg
				.addErrorMessage("INTERNAL ERROR, Label Attribute Created, with no ID!");
	}
	
	/**
	 * Constructor for Label.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public LabelAttribute(String id) {
		this(id, "");
	}
	
	/**
	 * Constructor for Label.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param l
	 *           the label-value of the attribute.
	 */
	public LabelAttribute(String id, String l) {
		super(id);
		StringAttribute alignment = (StringAttribute) StringAttribute
				.getTypedStringAttribute(ANCHOR, "c");
		alignment.setDescription("A string constant describing "
				+ "predefined positions of the label.");
		add(alignment, false);
		add(StringAttribute.getTypedStringAttribute(LABEL, l), false);
		add(StringAttribute.getTypedStringAttribute(FONTNAME, defaultFont),
				false);
		add(StringAttribute.getTypedStringAttribute(ALIGNMENT, "center"/*
																							 * "left"
																							 * right,center
																							 */), false);
		add(new IntegerAttribute(FONTSIZE, defaultSize), false);
		add(StringAttribute.getTypedStringAttribute(FONTSTYLE, "plain"/*
																							 * italic,bold
																							 */), false);
		add(StringAttribute.getTypedStringAttribute(TEXTCOLOR, ColorUtil
				.getHexFromColor(java.awt.Color.BLACK)), false);
		add(new StringAttribute("type", "text"), false);
	}
	
	@Override
	public void add(Attribute a, boolean inform)
			throws AttributeExistsException, FieldAlreadySetException {
		if (attributes.containsKey(a.getId())) {
			cacheSet(attributes.get(a.getId()), a.getValue());
		} else
			super.add(a, inform);
	}
	
	private void cacheSet(Attribute attribute, Object value) {
		attribute.setValue(value);
	}
	
	private void cacheSetS(StringAttribute stringAttribute, String sv) {
		stringAttribute.setValue(sv);
	}
	
	@Override
	public void add(Attribute a) throws AttributeExistsException,
			FieldAlreadySetException {
		if (attributes.containsKey(a.getId())) {
			cacheSet(attributes.get(a.getId()), a.getValue());
		} else
			super.add(a);
	}
	
	// ~ Methods
	// ================================================================
	
	public void setAlignment(String a) {
		cacheSetS((StringAttribute) attributes.get(ANCHOR), a);
	}
	
	public String getAlignment() {
		if (attributes.get(ANCHOR) != null)
			return ((StringAttribute) attributes.get(ANCHOR)).getString();
		else
			return GraphicAttributeConstants.CENTERED;
	}
	
	public String getAlignmentText() {
		return ((StringAttribute) attributes.get(ALIGNMENT)).getString();
	}
	
	public void setFontName(String f) {
		cacheSetS((StringAttribute) attributes.get(FONTNAME), f);
	}
	
	public String getFontName() {
		try {
			return ((StringAttribute) attributes.get(FONTNAME)).getString();
		} catch (NullPointerException npe) {
			return defaultFont;
		}
	}
	
	public void setFontStyle(String fs) {
		cacheSetS((StringAttribute) attributes.get(FONTSTYLE), fs);
	}
	
	public String getFontStyle() {
		try {
			return ((StringAttribute) attributes.get(FONTSTYLE)).getString();
		} catch (NullPointerException npe) {
			return "plain";
		}
	}
	
	public int getFontStyleJava() {
		int fontStyleInt = 0;
		String fontStyle = getFontStyle();
		if (fontStyle.indexOf("bold") >= 0)
			fontStyleInt += Font.BOLD;
		if (fontStyle.indexOf("italic") >= 0)
			fontStyleInt += Font.ITALIC;
		return fontStyleInt;
	}
	
	public void setFontSize(int fs) {
		((IntegerAttribute) attributes.get(FONTSIZE)).setInteger(fs);
	}
	
	public int getFontSize() {
		try {
			return ((IntegerAttribute) attributes.get(FONTSIZE)).getInteger();
		} catch (NullPointerException npe) {
			return defaultSize;
		}
	}
	
	/**
	 * Sets the 'label'-value.
	 * 
	 * @param l
	 *           the 'label'-value to be set.
	 */
	public void setLabel(String l) {
		if (attributes.get(LABEL) == null)
			add(StringAttribute.getTypedStringAttribute(LABEL, l), false);
		else
			((StringAttribute) attributes.get(LABEL)).setString(l);
	}
	
	/**
	 * Returns the 'label'-value of the encapsulated label.
	 * 
	 * @return the 'label'-value of the encapsulated label.
	 */
	public String getLabel() {
		try {
			return ((StringAttribute) attributes.get(LABEL)).getString();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Set the 'textcolor'-value.
	 * 
	 * @param tc
	 *           the 'textcolor'-value to be set.
	 */
	public void setTextcolor(String tc) {
		cacheSetS((StringAttribute) attributes.get(TEXTCOLOR), tc);
	}
	
	public void setTextcolor(Color c) {
		cacheSetS((StringAttribute) attributes.get(TEXTCOLOR), ColorUtil.getHexFromColor(c));
	}
	
	/**
	 * Returns the 'textcolor'-value of the encapsulated label.
	 * 
	 * @return the 'textcolor'-value of the encapsulated label.
	 */
	public Color getTextcolor() {
		try {
			return ColorUtil.getColorFromHex((((StringAttribute) attributes
					.get(TEXTCOLOR)).getString()));
		} catch (Exception err) {
			return Color.black;
		}
	}
	
	// public void wordWrap() {
	// if (!(getAttributable() instanceof Node))
	// return;
	// if (lastLabel == null)
	// lastLabel = new JLabel(getLabel());
	// String fontName = getFontName();
	// int fontStyleInt = getFontStyleJava();
	// ;
	// int fontSize = getFontSize();
	// lastLabel.setFont(new Font(fontName, fontStyleInt, fontSize));
	//
	// FontMetrics fm = lastLabel.getFontMetrics(lastLabel.getFont());
	// int containerWidth = (int) AttributeHelper
	// .getSize((Node) getAttributable()).x;
	//
	// BreakIterator boundary = BreakIterator.getWordInstance();
	//
	// boundary.setText(getLabel(true));
	//
	// StringBuffer trial = new StringBuffer();
	// StringBuffer real = new StringBuffer("<html>");
	//
	// int start = boundary.first();
	// for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
	// .next()) {
	// String word = getLabel(true).substring(start, end);
	// trial.append(word);
	// int trialWidth = SwingUtilities.computeStringWidth(fm, trial
	// .toString());
	// if (trialWidth > containerWidth) {
	// trial = new StringBuffer(word);
	// if (word.length() > 2 && !real.toString().endsWith("-")
	// && !real.toString().endsWith("("))
	// real.append("<br>");
	// }
	// real.append(word);
	// }
	//
	// String result = real.toString();
	// result = ErrorMsg.stringReplace(result, "<html><br>", "<html>");
	// result = ErrorMsg.stringReplace(result, "<br> <br>", " <br>");
	// result = ErrorMsg.stringReplace(result, "<br> <br>", " <br>");
	// if (result.indexOf("<br>") <= 0) {
	// result = ErrorMsg.stringReplace(result, "<html>", "");
	// result = ErrorMsg.stringReplace(result, "</html>", "");
	// }
	// setLabel(result);
	// }
	
	private String getLabel(boolean stripHTML) {
		String result = getLabel();
		if (stripHTML) {
			result = StringManipulationTools.removeHTMLtags(result);
		}
		return result;
	}
	
	public boolean getUseDropShadow() {
		String s = getFontStyle().toUpperCase();
		return s.contains("SHADOW");
	}
	
	public LabelFrameSetting getLabelFrameSetting() {
		String s = getFontStyle();
		for (LabelFrameSetting lfs : LabelFrameSetting.values())
			if (lfs != LabelFrameSetting.NO_FRAME && s.contains(lfs.toGMLstring()))
				return lfs;
		return LabelFrameSetting.NO_FRAME;
	}
	
	public int getShadowOffX() {
		if (!attributes.containsKey(GraphicAttributeConstants.SHADOWOFFSET))
			add(new IntegerAttribute(GraphicAttributeConstants.SHADOWOFFSET, 1));
		return ((IntegerAttribute) attributes
				.get(GraphicAttributeConstants.SHADOWOFFSET)).getInteger();
	}
	
	public int getShadowOffY() {
		if (!attributes.containsKey(GraphicAttributeConstants.SHADOWOFFSET))
			add(new IntegerAttribute(GraphicAttributeConstants.SHADOWOFFSET, 1));
		return ((IntegerAttribute) attributes
				.get(GraphicAttributeConstants.SHADOWOFFSET)).getInteger();
	}
	
	public Color getShadowTextColor() {
		if (!attributes.containsKey(GraphicAttributeConstants.SHADOWCOLOR)) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(
					GraphicAttributeConstants.SHADOWCOLOR, ColorUtil
							.getHexFromColor(Color.LIGHT_GRAY));
			newAtt.setParent(this);
			attributes.put(GraphicAttributeConstants.SHADOWCOLOR, newAtt);
		}
		ColorSetAndGetSupport colorAtt = (ColorSetAndGetSupport) attributes
				.get(GraphicAttributeConstants.SHADOWCOLOR);
		Color resultCol = colorAtt.getColor();
		return resultCol;
	}
	
	public void wordWrap() {
		if (!(getAttributable() instanceof Node))
			return;
		JLabel lastLabel = new JLabel(getLabel());
		String fontName = getFontName();
		int fontStyleInt = getFontStyleJava();;
		int fontSize = getFontSize();
		lastLabel.setFont(new Font(fontName, fontStyleInt, fontSize));
		
		FontMetrics fm = lastLabel.getFontMetrics(lastLabel.getFont());
		int containerWidth = (int) AttributeHelper
				.getSize((Node) getAttributable()).x;
		
		BreakIterator boundary = BreakIterator.getWordInstance();
		
		boundary.setText(getLabel(true));
		
		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html>");
		
		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
				.next()) {
			String word = getLabel(true).substring(start, end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial
					.toString());
			if (trialWidth > containerWidth) {
				trial = new StringBuffer(word);
				if (word.length() > 2 && !real.toString().endsWith("-")
						&& !real.toString().endsWith("("))
					real.append("<br>");
			}
			real.append(word);
		}
		
		String result = real.toString();
		result = StringManipulationTools.stringReplace(result, "<html><br>", "<html>");
		result = StringManipulationTools.stringReplace(result, "<br> <br>", " <br>");
		result = StringManipulationTools.stringReplace(result, "<br> <br>", " <br>");
		if (result.indexOf("<br>") <= 0) {
			result = StringManipulationTools.stringReplace(result, "<html>", "");
			result = StringManipulationTools.stringReplace(result, "</html>", "");
		}
		setLabel(result);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
