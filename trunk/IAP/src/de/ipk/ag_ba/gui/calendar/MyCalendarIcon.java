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
	
	private static SimpleDateFormat sdfD = new SimpleDateFormat("d");
	private static SimpleDateFormat sdfM = new SimpleDateFormat("MMM");
	private static SimpleDateFormat sdfMMM = new SimpleDateFormat("MMM");
	private static SimpleDateFormat sdfY = new SimpleDateFormat("yy");
	private static SimpleDateFormat sdfYY = new SimpleDateFormat("yyyy");
	private static Font flarge = new Font("SansSerif", Font.BOLD, 17);
	private static Font flargeM = new Font("SansSerif", Font.BOLD, 14);
	private static Font fsmall = new Font("SansSerif", Font.BOLD, 8);
	
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		super.paintIcon(c, g, x, y);
		paintCalendarInfo(g, x, y);
	}
	
	@Override
	public Image getCurrentImage() {
		Image i = null;
		try {
			i = GravistoService.loadImage(IAPmain.class, calendarEntity.getActionImage(), 48, 48);
			paintCalendarInfo(i.getGraphics(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	
	private void paintCalendarInfo(Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		if (imgS < 48) {
			g2d.scale(imgS / 48d, imgS / 48d);
			g2d.translate((48 - imgS) / 2, (48 - imgS) / 3);
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
			g2d.drawString(dateD, x + add + 15, y + 39);
			g2d.setFont(fsmall);
			g2d.setColor(Color.WHITE);
			g2d.drawString(dateM, x + 2, y + 21);
			w = g2d.getFontMetrics().stringWidth(dateY);
			add = -w;
			g2d.drawString(dateY, x + 31 + add, y + 22);
		} else {
			String dateYY = sdfYY.format(cal.getTime());
			String dateM = sdfMMM.format(cal.getTime());
			g2d.rotate(-0.2);
			g2d.setFont(flargeM);
			int w = g2d.getFontMetrics().stringWidth(dateM);
			int add = -w / 2;
			g2d.setColor(Color.DARK_GRAY);
			g2d.drawString(dateM, x + add + 15, y + 38);
			g2d.setFont(fsmall);
			g2d.setColor(Color.WHITE);
			w = g2d.getFontMetrics().stringWidth(dateYY);
			add = -w / 2;
			g2d.drawString(dateYY, x + 15 + add, y + 22);
		}
	}
}
