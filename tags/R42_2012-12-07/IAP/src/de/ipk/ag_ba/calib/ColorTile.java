/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 20, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.calib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.color.Color_CIE_Lab;

import de.ipk.ag_ba.image.operation.ImageConverter;

/**
 * @author klukas
 */
public class ColorTile extends JComponent {
	private static final long serialVersionUID = 1L;
	private final double a1, a2;
	private final double b1, b2;
	
	private BufferedImage img;
	
	public ColorTile(double a1, double a2, double b1, double b2, int xg, int yg) {
		this.a1 = a1;
		this.a2 = a2;
		this.b1 = b1;
		this.b2 = b2;
		
		setToolTipText(xg + ":" + yg + " A= " + a1 + " ... " + a2 + " / B= " + b1 + " ... " + b2);
		
		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
		
		initColors();
		
		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				initColors();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}
	
	protected void initColors() {
		int w = getWidth();
		int h = getHeight();
		if (w < 1 || h < 1)
			return;
		double l = 0;
		
		Color_CIE_Lab col = new Color_CIE_Lab(0, 0, 0);
		int c = Color.white.getRGB();
		int[][] image = new int[w][h];
		for (int x = 0; x < w; x++) {
			double a = x / (double) w * (a2 - a1) + a1;
			for (int y = 0; y < h; y++) {
				l = Math.max(x / (double) w * 100, y / (double) h * 100);
				
				double b = y / (double) h * (b1 - b2) + b1;
				col.setL(l);
				col.setA(a);
				col.setB(b);
				try {
					c = col.getColorXYZ().getColor().getRGB();
				} catch (IllegalArgumentException e) {
					c = Color.BLACK.getRGB();
				}
				image[x][y] = c;
			}
		}
		img = ImageConverter.convert2AtoBI(image);
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (img != null)
			g.drawImage(img, 0, 0, null);
	}
}
