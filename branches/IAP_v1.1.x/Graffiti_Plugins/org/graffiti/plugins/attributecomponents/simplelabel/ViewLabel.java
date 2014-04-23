/*
 * Created on 28.07.2005 by Christian Klukas
 */
package org.graffiti.plugins.attributecomponents.simplelabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.LabelFrameSetting;

public class ViewLabel extends JLabel {
	
	private static final long serialVersionUID = 1L;
	
	boolean empty;
	
	boolean isDrop;
	boolean usesDrop;
	
	LabelFrameSetting frame;
	
	double strokeWidth = 1d;
	
	private Color shadowColor;
	
	private final Color borderColor;
	
	private int offShadowX;
	private int offShadowY;
	
	private boolean mouseoverActivated = true;
	
	private boolean highlight;
	
	public ViewLabel(String labelText, LabelFrameSetting frame, final double strokeWidth, boolean isDrop, boolean usesDrop, Color borderColor) {
		super(labelText);
		this.frame = frame;
		this.strokeWidth = strokeWidth;
		this.borderColor = borderColor;
		if (labelText == null) {
			this.empty = true;
		} else {
			this.empty = labelText.length() <= 0;
		}
		this.isDrop = isDrop;
		this.usesDrop = usesDrop;
	}
	
	public void setDefaultTextPositioning() {
		setVerticalTextPosition(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	@Override
	public void paint(Graphics g) {
		if (mouseoverActivated && !highlight)
			return;
		boolean emptyText = getText() == null || getText().trim().length() == 0;
		if (frame != LabelFrameSetting.NO_FRAME)
			LabelComponent.paintRectangleOrOval(frame, g, getX(), getY(), getWidth(), getHeight(), true, strokeWidth, borderColor, emptyText);
		int offX = offShadowX / 2;
		int offY = offShadowY / 2;
		if (offShadowX > 0)
			if (offX * 2 < offShadowX)
				offX++;
		if (offShadowY > 0)
			if (offY * 2 < offShadowY)
				offY++;
		if (offShadowX < 0)
			if (offX * 2 > offShadowX)
				offX--;
		if (offShadowY < 0)
			if (offY * 2 > offShadowY)
				offY--;
		if (frame != LabelFrameSetting.NO_FRAME)
			LabelComponent.paintRectangleOrOval(frame, g, getX(), getY(), getWidth(), getHeight(), false, strokeWidth, borderColor, emptyText);
		if (shadowColor != null) {
			g.translate(offX, offY);
			Color c = getForeground();
			setForeground(shadowColor);
			super.paint(g);
			setForeground(c);
			g.translate(-offX, -offY);
			
			offX = offShadowX / 2;
			offY = offShadowY / 2;
			g.translate(-offX, -offY);
			super.paint(g);
			g.translate(offX, offY);
		} else {
			try {
				super.paint(g);
			} catch (Exception e) {
				//
			}
		}
	}
	
	@Override
	protected void paintBorder(Graphics g) {
		super.paintBorder(g);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
	
	@Override
	public String getText() {
		if (super.getText() != null)
			return super.getText();
		else
			return "";
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.setSize(strokeWidth / 1 + d.width + 8 + (offShadowX > 0 ? offShadowX : -offShadowX), strokeWidth / 1 + d.height
							+ (offShadowY > 0 ? offShadowY : -offShadowY));
		return d;
	}
	
	public void setShadowColor(Color shadowColor) {
		this.shadowColor = shadowColor;
	}
	
	public void setShadowOffset(int offx, int offy) {
		this.offShadowX = offx;
		this.offShadowY = offy;
	}
	
	public void setShow(boolean mouseoverActivated) {
		this.mouseoverActivated = mouseoverActivated;
	}
	
	public void highlight(boolean highlight) {
		this.highlight = highlight;
	}
	
	// public double offX() {
	// return -5; // -super.getPreferredSize().getWidth()*0.1d;
	// }
}
