/*
 * Created on 05.11.2004
 */
package org.graffiti.plugins.modes.defaults;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * @author klukas
 */
public class SelectionRectangle extends JComponent {
	private static final long serialVersionUID = 1L;
	private static Color drawColForeGround = new Color(255, 0, 0, 40);
	// private static Color drawColShadow = new Color(50, 50, 50, 40);
	private static Color drawColBorder = new Color(80, 10, 10, 100);
	
	public SelectionRectangle() {
		setOpaque(false);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(drawColForeGround);
		g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
		g.setColor(drawColBorder);
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
	}
}
