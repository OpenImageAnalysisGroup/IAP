/*
 * Created on May 22, 2005
 * @author Rafael Santos (rafael.santos@lac.inpe.br)
 * 
 * Part of the Java Advanced Imaging Stuff site
 * (http://www.lac.inpe.br/~rafael.santos/Java/JAI)
 * 
 * STATUS: Complete.
 * 
 * Redistribution and usage conditions must be done under the
 * Creative Commons license:
 * English: http://creativecommons.org/licenses/by-nc-sa/2.0/br/deed.en
 * Portuguese: http://creativecommons.org/licenses/by-nc-sa/2.0/br/deed.pt
 * More information on design and applications are on the projects' page
 * (http://www.lac.inpe.br/~rafael.santos/Java/JAI).
 */
package display;

import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.RenderedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This class represents a JPanel which contains two scrollable instances of
 * DisplayJAIWithPixelInfo. The scrolling bars of both images are synchronized
 * so scrolling one image will automatically scroll the other.
 */
@SuppressWarnings("restriction")
public class DisplayTwoSynchronizedImages extends JPanel implements AdjustmentListener {
	private static final long serialVersionUID = 1L;
	/** The DisplayJAIWithPixelInfo for the first image. */
	protected DisplayJAIWithPixelInfo dj1;
	/** The DisplayJAIWithPixelInfo for the second image. */
	protected DisplayJAIWithPixelInfo dj2;
	/** The JScrollPane which will contain the first of the images */
	protected JScrollPane jsp1;
	/** The JScrollPane which will contain the second of the images */
	protected JScrollPane jsp2;

	/**
	 * Creates an instance of this class, setting the components' layout,
	 * creating two instances of DisplayJAIWithPixelInfo for the two images and
	 * creating/registering event handlers for the scroll bars.
	 * 
	 * @param im1
	 *           the first image (left side)
	 * @param im2
	 *           the second image (right side)
	 */
	public DisplayTwoSynchronizedImages(RenderedImage im1, RenderedImage im2) {
		super();
		setLayout(new GridLayout(1, 2));
		dj1 = new DisplayJAIWithPixelInfo(im1); // Instances of DisplayJAI for the
		dj2 = new DisplayJAIWithPixelInfo(im2); // two images
		jsp1 = new JScrollPane(dj1); // JScrollPanes for the both
		jsp2 = new JScrollPane(dj2); // instances of DisplayJAI
		add(jsp1);
		add(jsp2);
		// Retrieve the scroll bars of the images and registers adjustment
		// listeners to them.
		// Horizontal scroll bar of the first image.
		jsp1.getHorizontalScrollBar().addAdjustmentListener(this);
		// Vertical scroll bar of the first image.
		jsp1.getVerticalScrollBar().addAdjustmentListener(this);
		// Horizontal scroll bar of the second image.
		jsp2.getHorizontalScrollBar().addAdjustmentListener(this);
		// Vertical scroll bar of the second image.
		jsp2.getVerticalScrollBar().addAdjustmentListener(this);
	}

	/**
	 * This method changes the first image to be displayed.
	 * 
	 * @param newImage
	 *           the new first image.
	 */
	public void setImage1(RenderedImage newimage) {
		dj1.set(newimage);
		repaint();
	}

	/**
	 * This method changes the second image to be displayed.
	 * 
	 * @param newImage
	 *           the new second image.
	 */
	public void setImage2(RenderedImage newimage) {
		dj2.set(newimage);
		repaint();
	}

	/**
	 * This method returns the first image.
	 * 
	 * @return the first image.
	 */
	public RenderedImage getImage1() {
		return dj1.getSource();
	}

	/**
	 * This method returns the second image.
	 * 
	 * @return the second image.
	 */
	public RenderedImage getImage2() {
		return dj2.getSource();
	}

	/**
	 * This method returns the first DisplayJAIWithPixelInfo component.
	 * 
	 * @return the first DisplayJAIWithPixelInfo component.
	 */
	public DisplayJAIWithPixelInfo getDisplayJAIComponent1() {
		return dj1;
	}

	/**
	 * This method returns the second DisplayJAIWithPixelInfo component.
	 * 
	 * @return the second DisplayJAIWithPixelInfo component.
	 */
	public DisplayJAIWithPixelInfo getDisplayJAIComponent2() {
		return dj2;
	}

	/**
	 * This method will be called when any of the scroll bars of the instances of
	 * DisplayJAIWithPixelInfo are changed. The method will adjust the scroll bar
	 * of the other DisplayJAIWithPixelInfo as needed.
	 * 
	 * @param e
	 *           the AdjustmentEvent that ocurred (meaning that one of the scroll
	 *           bars position has changed.
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		// If the horizontal bar of the first image was changed...
		if (e.getSource() == jsp1.getHorizontalScrollBar()) {
			// We change the position of the horizontal bar of the second image.
			jsp2.getHorizontalScrollBar().setValue(e.getValue());
		}
		// If the vertical bar of the first image was changed...
		if (e.getSource() == jsp1.getVerticalScrollBar()) {
			// We change the position of the vertical bar of the second image.
			jsp2.getVerticalScrollBar().setValue(e.getValue());
		}
		// If the horizontal bar of the second image was changed...
		if (e.getSource() == jsp2.getHorizontalScrollBar()) {
			// We change the position of the horizontal bar of the first image.
			jsp1.getHorizontalScrollBar().setValue(e.getValue());
		}
		// If the vertical bar of the second image was changed...
		if (e.getSource() == jsp2.getVerticalScrollBar()) {
			// We change the position of the vertical bar of the first image.
			jsp1.getVerticalScrollBar().setValue(e.getValue());
		}
	} // end adjustmentValueChanged

} // end class