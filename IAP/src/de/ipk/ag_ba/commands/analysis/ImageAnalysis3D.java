/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.analysis;

import info.clearthought.layout.TableLayout;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ImageAnalysis3D {
	
	private static boolean saveInDatabase = true; // result view not complete
	
	// in case this is set to
	// false
	
	public static BufferedImage convertToGrayscale(BufferedImage source) {
		BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		return op.filter(source, null);
	}
	
	//
	// private static Histogram getHistogram(Image img, int band) {
	// ParameterBlock pb1 = new ParameterBlock();
	// BufferedImage bi = toBufferedImage(img);
	// // GravistoService.showImage(bi, "rrr");
	// pb1.addSource(bi);
	// pb1.add(null); // The ROI
	// pb1.add(1);
	// pb1.add(1); // Sampling
	// pb1.add(new int[] { 127 }); // Bins
	// pb1.add(new double[] { 2 });
	// pb1.add(new double[] { 255 }); // Range for
	// // inclusion
	// PlanarImage dummyImage1 = JAI.create("histogram", pb1);
	// javax.media.jai.Histogram histo1 = (javax.media.jai.Histogram)
	// dummyImage1.getProperty("histogram");
	// return histo1;
	// }
	//
	
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		
		// Determine if the image has transparent pixels; for this method's
		// implementation, see Determining If an Image Has Transparent Pixels
		boolean hasAlpha = false;
		
		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}
			
			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}
		
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		return bimage;
	}
	
	public static Image convertToGreen(BufferedImage source) {
		ImageFilter filter = new RGBImageFilter() {
			@Override
			public final int filterRGB(int x, int y, int rgb) {
				return rgb & 0xFF00FF00;
			}
		};
		
		ImageProducer ip = new FilteredImageSource(source.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	public static Image convertToRed(BufferedImage source) {
		ImageFilter filter = new RGBImageFilter() {
			@Override
			public final int filterRGB(int x, int y, int rgb) {
				return rgb & 0xFFFF0000;
			}
		};
		
		ImageProducer ip = new FilteredImageSource(source.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	public static Image convertToBlue(BufferedImage source) {
		ImageFilter filter = new RGBImageFilter() {
			@Override
			public final int filterRGB(int x, int y, int rgb) {
				return rgb & 0xFF0000FF;
			}
		};
		
		ImageProducer ip = new FilteredImageSource(source.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	public static JComponent getImageZoomSlider(final ArrayList<ZoomedImage> zoomedImages) {
		
		int FPS_MIN = 0;
		int FPS_MAX = 400;
		int FPS_INIT = 100;
		
		final JSlider sliderZoom = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);
		
		final JLabel lbl = new JLabel("Zoom (100%)");
		
		// Turn on labels at major tick marks.
		sliderZoom.setMajorTickSpacing(100);
		sliderZoom.setMinorTickSpacing(10);
		sliderZoom.setPaintTicks(true);
		sliderZoom.setPaintLabels(true);
		sliderZoom.setOpaque(false);
		sliderZoom.setVisible(false);
		BackgroundTaskHelper.executeLaterOnSwingTask(200, new Runnable() {
			public void run() {
				sliderZoom.setVisible(true);
			}
		});
		
		for (ZoomedImage zoomedImage : zoomedImages) {
			if (zoomedImage.getInt() == 0)
				zoomedImage.setInt(100);
			else {
				updateZoom(zoomedImage, lbl, sliderZoom, zoomedImage.getInt());
				sliderZoom.setValue(zoomedImage.getInt());
			}
		}
		
		sliderZoom.addChangeListener(new ChangeListener() {
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
		
		return TableLayout.getSplitVertical(lbl, sliderZoom, TableLayout.PREFERRED, TableLayout.PREFERRED);
	}
	
	private static void updateZoom(final ZoomedImage zoomedImage, final JLabel lbl, JSlider s, int val) {
		lbl.setText("Zoom (" + val + "%)");
		zoomedImage.setInt(val);
	}
	
	public static void setSaveInDatabase(boolean saveInDatabase) {
		ImageAnalysis3D.saveInDatabase = saveInDatabase;
	}
	
	public static boolean isSaveInDatabase() {
		return saveInDatabase;
	}
}
