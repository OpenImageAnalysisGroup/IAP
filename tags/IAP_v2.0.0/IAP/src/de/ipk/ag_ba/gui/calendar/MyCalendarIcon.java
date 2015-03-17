/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;

import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class MyCalendarIcon extends ImageIcon implements DynamicPaintingIcon {
	private static final long serialVersionUID = 1L;
	private final NavigationButtonCalendar2 calendarEntity;
	private final int imgS;
	
	public MyCalendarIcon(ImageIcon icon, NavigationButtonCalendar2 n, int imgS) {
		super(icon.getImage());
		this.calendarEntity = n;
		this.imgS = imgS;
	}
	
	@Override
	public int getIconWidth() {
		double f = SystemAnalysis.getHiDPIScaleFactor();
		int w = super.getIconWidth();
		return (int) (w / f);
	}
	
	@Override
	public int getIconHeight() {
		double f = SystemAnalysis.getHiDPIScaleFactor();
		int h = super.getIconHeight();
		return (int) (h / f);
	}
	
	private static SimpleDateFormat sdfD = new SimpleDateFormat("d");
	private static SimpleDateFormat sdfM = new SimpleDateFormat("MMM");
	private static SimpleDateFormat sdfMMM = new SimpleDateFormat("MMM");
	private static SimpleDateFormat sdfY = new SimpleDateFormat("yy");
	private static SimpleDateFormat sdfYY = new SimpleDateFormat("yyyy");
	private static Font flarge = new Font("SansSerif", Font.BOLD, (int) (17 * SystemAnalysis.getHiDPIScaleFactor()));
	private static Font flargeM = new Font("SansSerif", Font.BOLD, (int) (14 * SystemAnalysis.getHiDPIScaleFactor()));
	private static Font fsmall = new Font("SansSerif", Font.BOLD, (int) (8 * SystemAnalysis.getHiDPIScaleFactor()));
	
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		((Graphics2D) g).scale(1 / SystemAnalysis.getHiDPIScaleFactor(), 1 / SystemAnalysis.getHiDPIScaleFactor());
		super.paintIcon(c, g, x, y);
		paintCalendarInfo(g, x, y);
		((Graphics2D) g).scale(SystemAnalysis.getHiDPIScaleFactor(), SystemAnalysis.getHiDPIScaleFactor());
	}
	
	@Override
	public Image getCurrentImage() {
		Image i = null;
		try {
			i = GravistoService.loadImage(IAPmain.class, calendarEntity.getActionImage(),
					(int) (48 * SystemAnalysis.getHiDPIScaleFactor()), (int) (48 * SystemAnalysis.getHiDPIScaleFactor()));
			paintCalendarInfo(i.getGraphics(), 0, 0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return i;
	}
	
	private void paintCalendarInfo(Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		if (imgS < 48) {
			g2d.scale(imgS / SystemAnalysis.getHiDPIScaleFactor() / 48d, imgS / SystemAnalysis.getHiDPIScaleFactor() / 48d);
			g2d.translate((48 * SystemAnalysis.getHiDPIScaleFactor() - imgS) / 2, (48 * SystemAnalysis.getHiDPIScaleFactor() - imgS) / 3);
		}
		GregorianCalendar cal = calendarEntity.getCalendar();
		String dateD = sdfD.format(cal.getTime());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (calendarEntity.isShowSpecificDay()) {
			String dateM = sdfM.format(cal.getTime()).toUpperCase();
			String dateY = sdfY.format(cal.getTime());
			g2d.rotate(-0.2);
			g2d.setFont(flarge);
			int w = g2d.getFontMetrics().stringWidth(dateD);
			int add = -w / 2;
			g2d.setColor(Color.DARK_GRAY);
			g2d.drawString(dateD, x + add + 15 * SystemAnalysis.getHiDPIScaleFactor(), y + 39 * SystemAnalysis.getHiDPIScaleFactor());
			g2d.setFont(fsmall);
			g2d.setColor(Color.WHITE);
			g2d.drawString(dateM, x + 2 * SystemAnalysis.getHiDPIScaleFactor(), y + 21 * SystemAnalysis.getHiDPIScaleFactor());
			w = g2d.getFontMetrics().stringWidth(dateY);
			add = -w;
			g2d.drawString(dateY, x + 31 * SystemAnalysis.getHiDPIScaleFactor() + add, y + 21 * SystemAnalysis.getHiDPIScaleFactor());
		} else {
			String dateYY = sdfYY.format(cal.getTime());
			String dateM = sdfMMM.format(cal.getTime());
			g2d.rotate(-0.19);
			g2d.setFont(flargeM);
			int w = g2d.getFontMetrics().stringWidth(dateM);
			int add = (int) (-w / 1.5);
			g2d.setColor(Color.DARK_GRAY);
			g2d.drawString(dateM, x + add + 18 * SystemAnalysis.getHiDPIScaleFactor(), y + 38 * SystemAnalysis.getHiDPIScaleFactor());
			g2d.setFont(fsmall);
			g2d.setColor(Color.WHITE);
			w = g2d.getFontMetrics().stringWidth(dateYY);
			add = -w / 2;
			g2d.drawString(dateYY, x + 17 * SystemAnalysis.getHiDPIScaleFactor() + add, y + 22 * SystemAnalysis.getHiDPIScaleFactor());
		}
	}
}
