/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.awt.Color;
import java.awt.Graphics;

import org.StringManipulationTools;

public class MyGraphicsTools {
	/**
	 * Replace occurrences of a substring.
	 * http://ostermiller.org/utils/StringHelper.html
	 * StringHelper.replace("1-2-3", "-", "|");<br>
	 * result: "1|2|3"<br>
	 * StringHelper.replace("-1--2-", "-", "|");<br>
	 * result: "|1||2|"<br>
	 * StringHelper.replace("123", "", "|");<br>
	 * result: "123"<br>
	 * StringHelper.replace("1-2---3----4", "--", "|");<br>
	 * result: "1-2|-3||4"<br>
	 * StringHelper.replace("1-2---3----4", "--", "---");<br>
	 * result: "1-2----3------4"<br>
	 * 
	 * @param s
	 *           String to be modified.
	 * @param find
	 *           String to find.
	 * @param replace
	 *           String to replace.
	 * @return a string with all the occurrences of the string to find replaced.
	 * @throws NullPointerException
	 *            if s is null.
	 */
	public static String stringReplace(String s, String find, String replace) {
		return StringManipulationTools.stringReplace(s, find, replace);
	}
	
	/**
	 * @param g
	 * @param color
	 * @param color2
	 * @param x1
	 * @param y1
	 * @param w
	 * @param h
	 */
	public static void drawFrame(Graphics g, Color color, Color color2, int x1, int y1, int w, int h) {
		g.setColor(color2);
		g.drawLine(x1, y1 + h, x1 + w, y1 + h);
		g.drawLine(x1 + w, y1, x1 + w, y1 + h);
		g.setColor(color);
		g.drawLine(x1, y1, x1 + w, y1);
		g.drawLine(x1, y1, x1, y1 + h);
	}
	
	public static void drawBarChartVarianceLine(
						Graphics g,
						int height,
						int borderNode,
						int barWidth,
						double maxValue,
						Double curVariance,
						int x1,
						int y1,
						int w) {
		int topX = x1 + w / 2;
		int topY = y1;
		int varianceY =
							(int) ((height - borderNode * 2)
												/ maxValue
							* curVariance.doubleValue());
		g.setColor(Color.BLACK);
		int sideX1 = -barWidth / 5;
		int sideX2 = +barWidth / 5;
		g.setColor(Color.BLACK);
		g.drawLine(topX, topY - varianceY, topX, topY + varianceY);
		g.setColor(Color.BLACK);
		g.drawLine(
							topX + sideX1,
							topY - varianceY,
							topX + sideX2,
							topY - varianceY);
		g.drawLine(
							topX + sideX1,
							topY + varianceY,
							topX + sideX2,
							topY + varianceY);
	}
}