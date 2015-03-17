/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ZoomedImage extends JPanel implements Scrollable, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	BufferedImage image;
	double scale = 1.0;
	
	private final int maxUnitIncrement = 20;
	
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
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		if (image != null) {
			double x = (getWidth() - scale * image.getWidth()) / 2;
			double y = (getHeight() - scale * image.getHeight()) / 2;
			AffineTransform at = AffineTransform.getTranslateInstance(x, y);
			at.scale(scale, scale);
			g2.drawRenderedImage(image, at);
		} else
			g2.drawString("Click 'Update View' to (re)calculate image", 10, 20);
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (image == null)
			return new Dimension(200, 320);
		int w = (int) (scale * image.getWidth());
		int h = (int) (scale * image.getHeight());
		return new Dimension(w, h);
	}
	
	public int getInt() {
		return (int) (scale * 100d);
	}
	
	public void setInt(int i) {
		this.scale = i / 100d;
		revalidate();
		// repaint();
		// if (getParent()!=null) {
		// JViewport jsp = (JViewport) getParent();
		// jsp.setView(this);
		// }
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
		scrollRectToVisible(r);
	}
	
	/*
	 * (non-Javadoc)
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
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		//
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		//
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// Get the current position.
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL) {
			currentPosition = visibleRect.x;
		} else {
			currentPosition = visibleRect.y;
		}
		
		// Return the number of pixels between currentPosition
		// and the nearest tick mark in the indicated direction.
		if (direction < 0) {
			int newPosition = currentPosition -
					(currentPosition / maxUnitIncrement)
					* maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			return ((currentPosition / maxUnitIncrement) + 1)
					* maxUnitIncrement
					- currentPosition;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		//
		
	}
	
	public JComponent getZoomSlider(Integer oldZoom) {
		ArrayList<ZoomedImage> r = new ArrayList<ZoomedImage>();
		r.add(this);
		return getImageZoomSlider(r, oldZoom);
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
		repaint();
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public static JComponent getImageZoomSlider(final ArrayList<ZoomedImage> zoomedImages, Integer oldZoom) {
		
		int FPS_MIN = 0;
		int FPS_MAX = oldZoom > 0 ? 200 : 400;
		int FPS_INIT = 100;
		
		final JSlider sliderZoom = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, oldZoom > 0 ? oldZoom : FPS_INIT);
		
		final JLabel lbl = new JLabel("Zoom (" + (oldZoom > 0 ? oldZoom : FPS_INIT) + "%)");
		
		// Turn on labels at major tick marks.
		sliderZoom.setMajorTickSpacing(100);
		sliderZoom.setMinorTickSpacing(10);
		sliderZoom.setPaintTicks(true);
		sliderZoom.setPaintLabels(true);
		sliderZoom.setOpaque(false);
		sliderZoom.setVisible(false);
		BackgroundTaskHelper.executeLaterOnSwingTask(200, new Runnable() {
			@Override
			public void run() {
				sliderZoom.setVisible(true);
			}
		});
		
		for (ZoomedImage zoomedImage : zoomedImages) {
			if (zoomedImage.getInt() == 0 || oldZoom <= 0)
				zoomedImage.setInt(100);
			else {
				updateZoom(zoomedImage, lbl, sliderZoom, oldZoom > 0 ? oldZoom : zoomedImage.getInt());
				sliderZoom.setValue(oldZoom > 0 ? oldZoom : zoomedImage.getInt());
			}
		}
		
		sliderZoom.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider s = (JSlider) e.getSource();
				int val = s.getValue() - s.getValue() % 5;
				if (val < 5)
					val = 5;
				for (ZoomedImage zoomedImage : zoomedImages) {
					if (val != zoomedImage.getInt()) {
						zoomedImage.setInt(val);
						updateZoom(zoomedImage, lbl, s, val);
					}
				}
			}
		});
		
		JPanel res = TableLayout.getSplitVertical(lbl, sliderZoom, TableLayout.PREFERRED, TableLayout.PREFERRED);
		
		res.putClientProperty("slider", sliderZoom);
		
		return res;
	}
	
	private static void updateZoom(final ZoomedImage zoomedImage, final JLabel lbl, JSlider s, int val) {
		lbl.setText("Zoom (" + val + "%)");
		zoomedImage.setInt(val);
		s.putClientProperty("zoomValue", val);
	}
	
}