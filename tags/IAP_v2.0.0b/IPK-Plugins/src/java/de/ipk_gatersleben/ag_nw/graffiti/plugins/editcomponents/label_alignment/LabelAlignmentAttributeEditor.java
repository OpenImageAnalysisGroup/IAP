/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.label_alignment;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AlignmentSetting;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class LabelAlignmentAttributeEditor extends AbstractValueEditComponent {
	
	JComponent alignmentSelection;
	PositionButton left, right, above, below, insideTop, insideTopLeft, insideTopRight, insideCenter, insideBottom, insideBottomLeft, insideBottomRight,
			insideLeft, insideRight;
	
	PositionButton
						leftBorderT, leftBorderC, leftBorderB,
						topBorderL, topBorderC, topBorderR,
						rightBorderT, rightBorderC, rightBorderB,
						bottomBorderL, bottomBorderC, bottomBorderR;
	
	PositionButton
						aboveLeft, aboveRight, belowLeft, belowRight;
	
	private static final String NOT_SET = "[not set]";
	
	private boolean isNodeEditor = true;
	
	String currentSelection = NOT_SET;
	
	int www = 11;
	int hhh = 11;
	
	public LabelAlignmentAttributeEditor(final Displayable disp) {
		super(disp);
		isNodeEditor = ((Attribute) disp).getAttributable() instanceof Node;
		left = new PositionButton(this, AlignmentSetting.LEFT, "Left", false);
		right = new PositionButton(this, AlignmentSetting.RIGHT, "Right", false);
		above = new PositionButton(this, AlignmentSetting.ABOVE, "Above", false);
		below = new PositionButton(this, AlignmentSetting.BELOW, "Below", false);
		insideTop = new PositionButton(this, AlignmentSetting.INSIDETOP, "Inside, top", false);
		insideTopLeft = new PositionButton(this, AlignmentSetting.INSIDETOPLEFT, "Inside, top-left", false);
		insideTopRight = new PositionButton(this, AlignmentSetting.INSIDETOPRIGHT, "Inside, top-right", false);
		insideCenter = new PositionButton(this, AlignmentSetting.CENTERED, "Center", false);
		insideBottom = new PositionButton(this, AlignmentSetting.INSIDEBOTTOM, "Inside, bottom", false);
		insideBottomLeft = new PositionButton(this, AlignmentSetting.INSIDEBOTTOMLEFT, "Inside, bottom-left", false);
		insideBottomRight = new PositionButton(this, AlignmentSetting.INSIDEBOTTOMRIGHT, "Inside, bottom-right", false);
		insideLeft = new PositionButton(this, AlignmentSetting.INSIDELEFT, "Inside, left", false);
		insideRight = new PositionButton(this, AlignmentSetting.INSIDERIGHT, "Inside, right", false);
		
		leftBorderT = new PositionButton(this, AlignmentSetting.BORDER_LEFT_TOP, "Left border, top", true);
		leftBorderC = new PositionButton(this, AlignmentSetting.BORDER_LEFT_CENTER, "Left border, center", true);
		leftBorderB = new PositionButton(this, AlignmentSetting.BORDER_LEFT_BOTTOM, "Left border, bottom", true);
		topBorderL = new PositionButton(this, AlignmentSetting.BORDER_TOP_LEFT, "Top border, left", true);
		topBorderC = new PositionButton(this, AlignmentSetting.BORDER_TOP_CENTER, "Top border, center", true);
		topBorderR = new PositionButton(this, AlignmentSetting.BORDER_TOP_RIGHT, "Top border, right", true);
		rightBorderT = new PositionButton(this, AlignmentSetting.BORDER_RIGHT_TOP, "Right border, top", true);
		rightBorderC = new PositionButton(this, AlignmentSetting.BORDER_RIGHT_CENTER, "Right border, center", true);
		rightBorderB = new PositionButton(this, AlignmentSetting.BORDER_RIGHT_BOTTOM, "Right border, bottom", true);
		bottomBorderL = new PositionButton(this, AlignmentSetting.BORDER_BOTTOM_LEFT, "Bottom border, left", true);
		bottomBorderC = new PositionButton(this, AlignmentSetting.BORDER_BOTTOM_CENTER, "Bottom border, center", true);
		bottomBorderR = new PositionButton(this, AlignmentSetting.BORDER_BOTTOM_RIGHT, "Bottom border, right", true);
		
		aboveLeft = new PositionButton(this, AlignmentSetting.ABOVELEFT, "Above, left", false);
		aboveRight = new PositionButton(this, AlignmentSetting.ABOVERIGHT, "Above, right", false);
		belowLeft = new PositionButton(this, AlignmentSetting.BELOWLEFT, "Below, left", false);
		belowRight = new PositionButton(this, AlignmentSetting.BELOWRIGHT, "Below, right", false);
		
		int h = left.getPreferredSize().height;
		
		ArrayList<JComponent> buttonsLeft = new ArrayList<JComponent>();
		buttonsLeft.add(check(aboveLeft, h));
		buttonsLeft.add(getFillerN(h));
		buttonsLeft.add(getFillerN(h));
		buttonsLeft.add(check(left, h));
		buttonsLeft.add(getFillerN(h));
		buttonsLeft.add(getFillerN(h));
		buttonsLeft.add(check(belowLeft, h));
		JComponent colLeft = TableLayout.getMultiSplitVertical(buttonsLeft);
		
		ArrayList<JComponent> buttonsBorderLeft = new ArrayList<JComponent>();
		if (isNodeEditor)
			buttonsBorderLeft.add(getFillerN(h));
		buttonsBorderLeft.add(getFillerB(h));
		buttonsBorderLeft.add(leftBorderT);
		buttonsBorderLeft.add(leftBorderC);
		buttonsBorderLeft.add(leftBorderB);
		buttonsBorderLeft.add(getFillerB(h));
		if (isNodeEditor)
			buttonsBorderLeft.add(getFillerN(h));
		JComponent colBorderLeft = TableLayout.getMultiSplitVertical(buttonsBorderLeft);
		
		ArrayList<JComponent> buttonsCenterCol = new ArrayList<JComponent>();
		if (isNodeEditor)
			buttonsCenterCol.add(TableLayout.get3Split(getFillerN(h), check(above, h), getFillerN(h), TableLayoutConstants.PREFERRED,
								TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		buttonsCenterCol.add(TableLayout.get3Split(topBorderL, topBorderC, topBorderR, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
							TableLayoutConstants.PREFERRED));
		buttonsCenterCol.add(TableLayout.get3Split(check(insideTopLeft, h), check(insideTop, h), check(insideTopRight, h), TableLayoutConstants.PREFERRED,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		buttonsCenterCol.add(TableLayout.get3Split(check(insideLeft, h), insideCenter, check(insideRight, h), TableLayoutConstants.PREFERRED,
				TableLayoutConstants.PREFERRED,
							TableLayoutConstants.PREFERRED));
		buttonsCenterCol.add(TableLayout.get3Split(check(insideBottomLeft, h), check(insideBottom, h), check(insideBottomRight, h),
				TableLayoutConstants.PREFERRED,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		buttonsCenterCol.add(TableLayout.get3Split(bottomBorderL, bottomBorderC, bottomBorderR, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
							TableLayoutConstants.PREFERRED));
		if (isNodeEditor)
			buttonsCenterCol.add(TableLayout.get3Split(getFillerN(h), check(below, h), getFillerN(h), TableLayoutConstants.PREFERRED,
								TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		JComponent colMiddle = TableLayout.getMultiSplitVertical(buttonsCenterCol);
		
		ArrayList<JComponent> buttonsBorderRight = new ArrayList<JComponent>();
		if (isNodeEditor)
			buttonsBorderRight.add(getFillerN(h));
		buttonsBorderRight.add(getFillerB(h));
		buttonsBorderRight.add(rightBorderT);
		buttonsBorderRight.add(rightBorderC);
		buttonsBorderRight.add(rightBorderB);
		buttonsBorderRight.add(getFillerB(h));
		if (isNodeEditor)
			buttonsBorderRight.add(getFillerN(h));
		JComponent colBorderRight = TableLayout.getMultiSplitVertical(buttonsBorderRight);
		
		ArrayList<JComponent> buttonsRight = new ArrayList<JComponent>();
		if (isNodeEditor)
			buttonsRight.add(check(aboveRight, h));
		buttonsRight.add(getFillerN(h));
		buttonsRight.add(getFillerN(h));
		buttonsRight.add(check(right, h));
		buttonsRight.add(getFillerN(h));
		buttonsRight.add(getFillerN(h));
		if (isNodeEditor)
			buttonsRight.add(check(belowRight, h));
		JComponent colRight = TableLayout.getMultiSplitVertical(buttonsRight);
		
		ArrayList<JComponent> columns = new ArrayList<JComponent>();
		if (isNodeEditor)
			columns.add(colLeft);
		columns.add(colBorderLeft);
		columns.add(colMiddle);
		columns.add(colBorderRight);
		if (isNodeEditor)
			columns.add(colRight);
		
		alignmentSelection = TableLayout.get3Split(new JLabel(), TableLayout.getMultiSplit(columns, TableLayoutConstants.PREFERRED, 0, 0, 0, 0),
							new JLabel(), 2, TableLayoutConstants.PREFERRED, 2);
		alignmentSelection.setBackground(Color.WHITE);
		alignmentSelection.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		currentSelection = ((LabelAlignmentAttribute) getDisplayable()).getString();
		
		updateButtonState();
	}
	
	private JComponent check(PositionButton nodeEditor, int h) {
		if (isNodeEditor)
			return nodeEditor;
		else
			return getFillerB(h);
	}
	
	private JComponent getFillerN(int height) {
		return new PositionButton(this, AlignmentSetting.HIDDEN, "Hidden", false);
		// JComponent l = new JLabel();
		// l.setPreferredSize(new Dimension(www, hhh));
		// l.setMinimumSize(new Dimension(www, hhh));
		// l.setSize(new Dimension(www, hhh));
		// l.setToolTipText("");
		// return l;
	}
	
	private JComponent getFillerB(int height) {
		return new PositionButton(this, AlignmentSetting.HIDDEN, "Hidden", false);
		// JComponent l = new JLabel();
		// l.setPreferredSize(new Dimension(www, hhh));
		// l.setMinimumSize(new Dimension(www, hhh));
		// l.setSize(new Dimension(www, hhh));
		// l.setOpaque(true);
		// l.setBackground(Color.WHITE);
		// l.setToolTipText("");
		// return l;
	}
	
	/*
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#setShowEmpty(boolean)
	 */
	@Override
	public void setShowEmpty(boolean showEmpty) {
		super.setShowEmpty(showEmpty);
	}
	
	void updateButtonState() {
		for (PositionButton button : new PositionButton[] {
							left, right, above, below, insideTop, insideCenter, insideBottom,
							insideTopLeft, insideTopRight, insideLeft, insideRight, insideBottomLeft, insideBottomRight,
							aboveLeft, aboveRight, belowLeft, belowRight,
							topBorderL, topBorderC, topBorderR,
							leftBorderT, leftBorderC, leftBorderB,
							rightBorderT, rightBorderC, rightBorderB,
							bottomBorderL, bottomBorderC, bottomBorderR }) {
			if (!showEmpty) {
				if (currentSelection.equals(button.getAlignmentSetting())) {
					button.setSelected(true);
				} else {
					button.setSelected(false);
					
				}
			} else
				button.setSelected(false);
		}
		if (!showEmpty) {
			insideCenter.setText("");
			if (currentSelection.equals(GraphicAttributeConstants.AUTO_OUTSIDE)) {
				left.setSelected(true);
				right.setSelected(true);
				above.setSelected(true);
				below.setSelected(true);
			}
		} else
			insideCenter.setText("<html><small>~");
	}
	
	public JComponent getComponent() {
		return alignmentSelection;
	}
	
	public void setEditFieldValue() {
		currentSelection = ((LabelAlignmentAttribute) getDisplayable()).getString();
		updateButtonState();
	}
	
	public void setValue() {
		String bs = currentSelection;
		if (!showEmpty)
			((LabelAlignmentAttribute) displayable).setString(bs);
	}
}
