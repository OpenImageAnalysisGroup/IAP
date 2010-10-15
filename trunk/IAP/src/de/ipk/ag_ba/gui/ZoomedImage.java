/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * @author klukas
 * 
 */
public class ZoomedImage extends JPanel implements Scrollable, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	BufferedImage image;
	double scale = 1.0;

	private int maxUnitIncrement = 20;
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}

	public ZoomedImage(BufferedImage image) {
		this.image = image;
		setAutoscrolls(true);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		double x = (getWidth() - scale * image.getWidth()) / 2;
		double y = (getHeight() - scale * image.getHeight()) / 2;
		AffineTransform at = AffineTransform.getTranslateInstance(x, y);
		at.scale(scale, scale);
		g2.drawRenderedImage(image, at);
	}

	public Dimension getPreferredSize() {
		int w = (int) (scale * image.getWidth());
		int h = (int) (scale * image.getHeight());
		return new Dimension(w, h);
	}

	public int getInt() {
		return (int) (scale*100d);
	}

	public void setInt(int i) {
		this.scale = i/100d;
		revalidate();
//		repaint();
//		if (getParent()!=null) {
//				JViewport jsp = (JViewport) getParent();
//				jsp.setView(this);
//		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	 public void mouseDragged(MouseEvent e) {
       Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
       scrollRectToVisible(r);
   }
	 
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		 if (orientation == SwingConstants.HORIZONTAL)
	        return visibleRect.width - maxUnitIncrement;
	    else
	        return visibleRect.height - maxUnitIncrement;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		 //Get the current position.
	    int currentPosition = 0;
	    if (orientation == SwingConstants.HORIZONTAL) {
	        currentPosition = visibleRect.x;
	    } else {
	        currentPosition = visibleRect.y;
	    }

	    //Return the number of pixels between currentPosition
	    //and the nearest tick mark in the indicated direction.
	    if (direction < 0) {
	        int newPosition = currentPosition -
	                         (currentPosition / maxUnitIncrement)
	                          * maxUnitIncrement ;
	        return (newPosition == 0) ? maxUnitIncrement : newPosition;
	    } else {
	        return ((currentPosition / maxUnitIncrement) + 1)
	                 * maxUnitIncrement
	                 - currentPosition;
	    }
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}