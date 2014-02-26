/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 28, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * @author klukas
 */
public class FlowLayoutImproved extends FlowLayout {
	
	private static final long serialVersionUID = 1L;
	private int lines;
	
	public FlowLayoutImproved(int left, int hgap, int vgap) {
		super(left, hgap, vgap);
		// setAlignOnBaseline(true);
	}
	
	@Override
	public Dimension preferredLayoutSize(Container target) {
		int gapH = getHgap();
		int gapV = getVgap();
		int maxW = target.getWidth();
		int curX = 0;
		int curY = gapV;
		int maxHeightInLine = 0;
		int comps = 0;
		lines = 0;
		for (Component c : target.getComponents()) {
			if (!c.isVisible())
				continue;
			comps++;
			if (c.getPreferredSize().height > maxHeightInLine)
				maxHeightInLine = c.getPreferredSize().height;
			if (curX > 0)
				curX += gapH;
			if (curX + c.getPreferredSize().width + gapH > maxW) {
				lines++;
				curX = 0;
				if (curY > 0)
					curY += gapV;
				curY += maxHeightInLine;
				maxHeightInLine = c.getPreferredSize().height;
			}
			curX += c.getPreferredSize().width;
		}
		if (curX > 0 && comps > lines)
			curY += maxHeightInLine + gapV;
		Insets insets = target.getInsets();
		curY += insets.top + insets.bottom;
		return new Dimension(maxW, curY);
	}
	
	public int getLines(Container target) {
		preferredLayoutSize(target);
		return lines;
	}
}
