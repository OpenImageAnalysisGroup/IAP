package de.ipk.ag_ba.gui.picture_gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.image.operation.ImageOperation;

/**
 * @author Christian Klukas
 */
public class MyImageIcon extends ImageIcon {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	// private int width, height;
	
	IOurl fileURLmain, fileURLlabel;
	
	int imageAvailable;
	
	private BinaryFileInfo bfi;
	
	// private File file;
	
	public MyImageIcon(Component observer, int width, int height, IOurl fileMain, IOurl fileLabel, BinaryFileInfo bfi)
			throws MalformedURLException {
		initImageData(observer, width, height, fileMain, fileLabel, bfi);
	}
	
	public synchronized void initImageData(final Component observer, int width, int height, IOurl fileMain, IOurl fileLabel, BinaryFileInfo bfi)
			throws MalformedURLException {
		this.bfi = bfi;
		String description = fileMain.getFileName();
		fileURLmain = fileMain;
		fileURLlabel = fileLabel;
		// this.file = file;
		setDescription(description);
		
		try {
			BufferedImage i = null;
			ResourceIOHandler ioh = ResourceIOManager.getHandlerFromPrefix(fileURLmain.getPrefix());
			InputStream ins = ioh.getPreviewInputStream(fileURLmain, width);
			i = ins != null ? ImageIO.read(ins) : null;
			if (i != null && width != 128) {
				i = new de.ipk.ag_ba.image.structures.Image(i)
						.io()
						.grayscale()
						.border_4sides(4, java.awt.Color.RED.getRGB())
						.canvas()
						// .drawImage(loadingIcon, i.getWidth() - (int) (64 * SystemAnalysis.getHiDPIScaleFactor()) - 4,
						// (int) (i.getHeight() * SystemAnalysis.getHiDPIScaleFactor() - 64 * SystemAnalysis.getHiDPIScaleFactor()) - 6)
						.getImage()
						.resize((int) (DataSetFileButton.ICON_HEIGHT * SystemAnalysis.getHiDPIScaleFactor()),
								(int) (DataSetFileButton.ICON_HEIGHT * SystemAnalysis.getHiDPIScaleFactor()), true)
						.io()
						.replaceColor(Color.BLACK.getRGB(), ImageOperation.BACKGROUND_COLORint)
						.replaceColor(Color.WHITE.getRGB(), ImageOperation.BACKGROUND_COLORint)
						.getImage()
						.getAsBufferedImage(true);
			}
			if (i == null || width != 128) {
				BackgroundThreadDispatcher.addTask(new Runnable() {
					
					@Override
					public void run() {
						BufferedImage i;
						try {
							i = new de.ipk.ag_ba.image.structures.Image(fileURLmain).resize(
									(int) (DataSetFileButton.ICON_HEIGHT * SystemAnalysis.getHiDPIScaleFactor()),
									(int) (DataSetFileButton.ICON_HEIGHT * SystemAnalysis.getHiDPIScaleFactor()), true)
									.getAsBufferedImage(true);
							setImage(i);
							observer.repaint();
						} catch (Exception e) {
							imageAvailable = 0;
							
							i = null;
							
							ImageIcon ic;
							try {
								ic = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(
										FileSystemHandler.getFile(fileURLmain));
								if (ic == null) {
									i = toBufferedImage(new ImageIcon(MainFrame.getInstance().getIconImage()).getImage());
								} else
									i = toBufferedImage(ic.getImage());
							} catch (Exception err2) {
								i = null;
							}
							
							if (i != null) {
								int maxS = i.getHeight() > i.getWidth() ? i.getHeight() : i.getWidth();
								double factor = 128 * SystemAnalysis.getHiDPIScaleFactor() / maxS;
								i = resize(i, (int) (i.getWidth() * factor), (int) (i.getHeight() * factor));
								setImage(i);
							}
						}
					}
				}, "Load full res image", true, Integer.MAX_VALUE);
				imageAvailable = 1;
				setImage(i);
			}
		} catch (Exception e1) {
			imageAvailable = 0;
			
			BufferedImage i = null;
			
			ImageIcon ic;
			try {
				ic = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(
						FileSystemHandler.getFile(fileURLmain));
				if (ic == null) {
					i = null;
				} else
					i = toBufferedImage(ic.getImage());
			} catch (Exception e2) {
				i = null;
			}
			
			if (i == null)
				i = IAPimages.getImageIAP("img/ext/gpl2/Gnome-Image-Loading-64b.png", (int) (64 * SystemAnalysis.getHiDPIScaleFactor())).getAsBufferedImage(false);
			if (i != null) {
				int maxS = i.getHeight() > i.getWidth() ? i.getHeight() : i.getWidth();
				double factor = width * SystemAnalysis.getHiDPIScaleFactor() / maxS;
				int bordersizeSides = (int) (i.getWidth() * factor - i.getWidth()) / 2;
				int borderSizeTopBottom = (int) (i.getHeight() * factor - i.getHeight()) / 2;
				int translatex = -i.getWidth() / 2;
				int translatey = -i.getHeight() / 2;
				i = new ImageOperation(i)
						.replaceColor(-16777216, ImageOperation.BACKGROUND_COLORint)
						.addBorder(bordersizeSides, borderSizeTopBottom, translatex, translatey, ImageOperation.BACKGROUND_COLORint)
						.getImage().getAsBufferedImage(true);
				setImage(i);
			}
		}
	}
	
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		
		// This code ensures that all the pixels in the image are loaded
		// image = new ImageIcon(image).getImage();
		
		// Determine if the image has transparent pixels; for this method's
		// implementation, see Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);
		
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
			ErrorMsg.addErrorMessage(e);
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
	
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}
		
		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		
		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}
	
	public static BufferedImage resize(BufferedImage image, int width, int height) {
		if (true)
			return new de.ipk.ag_ba.image.structures.Image(image).io().resize(width, height)
					.replaceColor(0, ImageOperation.BACKGROUND_COLORint)
					.getImage().getAsBufferedImage(true);
		else {
			int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
			BufferedImage resizedImage = new BufferedImage(width, height, type);
			Graphics2D g = resizedImage.createGraphics();
			g.setComposite(AlphaComposite.Src);
			
			// g.setRenderingHint(RenderingHints.KEY_RENDERING,
			// RenderingHints.VALUE_RENDER_QUALITY);
			//
			// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			
			g.drawImage(image, 0, 0, width, height, null);
			g.dispose();
			return resizedImage;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		ImageObserver observer = getImageObserver();
		
		if (observer == null) {
			observer = c;
		}
		
		Image image = getImage();
		int width = image.getWidth(observer);
		int height = image.getHeight(observer);
		float hf = SystemAnalysis.getHiDPIScaleFactor();
		final Graphics2D g2d = (Graphics2D) g.create(x, y, (int) (width * hf),
				(int) (height * hf));
		
		g2d.scale(1f / hf, 1f / hf);
		if (hf > 1)
			g2d.drawImage(image,
					(int) (-x / hf - x * hf),
					(int) (-y * hf),
					observer);
		else
			g2d.drawImage(image,
					0,
					0,
					observer);
		g2d.scale(1, 1);
		g2d.dispose();
	}
	
	public BinaryFileInfo getBinaryFileInfo() {
		return bfi;
	}
}
