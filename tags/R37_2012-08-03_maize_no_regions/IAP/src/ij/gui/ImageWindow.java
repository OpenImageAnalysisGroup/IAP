package ij.gui;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.plugin.frame.Channels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

/** A frame for displaying images. */
public class ImageWindow extends Frame implements FocusListener, WindowListener, WindowStateListener, MouseWheelListener {
	
	public static final int MIN_WIDTH = 128;
	public static final int MIN_HEIGHT = 32;
	
	protected ImagePlus imp;
	protected ImageJ ij;
	protected ImageCanvas ic;
	private double initialMagnification = 1;
	private int newWidth, newHeight;
	protected boolean closed;
	private boolean newCanvas;
	private boolean unzoomWhenMinimizing = true;
	Rectangle maxWindowBounds; // largest possible window on this screen
	Rectangle maxBounds; // Size of this window after it is maximized
	long setMaxBoundsTime;
	
	private static final int XINC = 8;
	private static final int YINC = 12;
	private static final int TEXT_GAP = 10;
	private static int xbase = -1;
	private static int ybase;
	private static int xloc;
	private static int yloc;
	private static int count;
	private static boolean centerOnScreen;
	private static Point nextLocation;
	
	private final int textGap = centerOnScreen ? 0 : TEXT_GAP;
	
	/** This variable is set false if the user presses the escape key or closes the window. */
	public boolean running;
	
	/**
	 * This variable is set false if the user clicks in this
	 * window, presses the escape key, or closes the window.
	 */
	public boolean running2;
	
	public ImageWindow(String title) {
		super(title);
	}
	
	public ImageWindow(ImagePlus imp) {
		this(imp, null);
	}
	
	public ImageWindow(ImagePlus imp, ImageCanvas ic) {
		super(imp.getTitle());
		if (Prefs.blackCanvas && getClass().getName().equals("ij.gui.ImageWindow")) {
			setForeground(Color.white);
			setBackground(Color.black);
		} else {
			setForeground(Color.black);
			if (IJ.isLinux())
				setBackground(ImageJ.backgroundColor);
			else
				setBackground(Color.white);
		}
		ij = IJ.getInstance();
		this.imp = imp;
		if (ic == null) {
			ic = new ImageCanvas(imp);
			newCanvas = true;
		}
		this.ic = ic;
		ImageWindow previousWindow = imp.getWindow();
		setLayout(new ImageLayout(ic));
		add(ic);
		addFocusListener(this);
		addWindowListener(this);
		addWindowStateListener(this);
		
		addKeyListener(new KeyListener() {
			boolean zoomed = false;
			
			@Override
			public void keyTyped(KeyEvent e) {
				if (zoomed)
					ImageWindow.this.ic.zoom100Percent();
				else
					ImageWindow.this.ic.unzoom();
				zoomed = !zoomed;
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		addKeyListener(ij);
		setFocusTraversalKeysEnabled(false);
		if (!(this instanceof StackWindow))
			addMouseWheelListener(this);
		setResizable(true);
		WindowManager.addWindow(this);
		imp.setWindow(this);
		if (previousWindow != null) {
			if (newCanvas)
				setLocationAndSize(false);
			else
				ic.update(previousWindow.getCanvas());
			Point loc = previousWindow.getLocation();
			setLocation(loc.x, loc.y);
			if (!(this instanceof StackWindow)) {
				pack();
				show();
			}
			boolean unlocked = imp.lockSilently();
			boolean changes = imp.changes;
			imp.changes = false;
			previousWindow.close();
			imp.changes = changes;
			if (unlocked)
				imp.unlock();
			WindowManager.setCurrentWindow(this);
		} else {
			setLocationAndSize(false);
			if (ij != null && !IJ.isMacintosh()) {
				Image img = ij.getIconImage();
				if (img != null)
					try {
						setIconImage(img);
					} catch (Exception e) {
					}
			}
			if (centerOnScreen) {
				GUI.center(this);
				centerOnScreen = false;
			} else
				if (nextLocation != null) {
					setLocation(nextLocation);
					nextLocation = null;
				}
			if (Interpreter.isBatchMode() || (IJ.getInstance() == null && this instanceof HistogramWindow)) {
				WindowManager.setTempCurrentImage(imp);
				Interpreter.addBatchModeImage(imp);
			} else
				show();
		}
	}
	
	private void setLocationAndSize(boolean updating) {
		int width = imp.getWidth();
		int height = imp.getHeight();
		Rectangle maxWindow = getMaxWindow(0, 0);
		// if (maxWindow.x==maxWindow.width) // work around for Linux bug
		// maxWindow = new Rectangle(0, maxWindow.y, maxWindow.width, maxWindow.height);
		if (WindowManager.getWindowCount() <= 1)
			xbase = -1;
		if (width > maxWindow.width / 2 && xbase > maxWindow.x + 5 + XINC * 6)
			xbase = -1;
		if (xbase == -1) {
			count = 0;
			xbase = maxWindow.x + 5;
			if (width * 2 <= maxWindow.width)
				xbase = maxWindow.x + maxWindow.width / 2 - width / 2;
			ybase = maxWindow.y;
			xloc = xbase;
			yloc = ybase;
		}
		int x = xloc;
		int y = yloc;
		xloc += XINC;
		yloc += YINC;
		count++;
		if (count % 6 == 0) {
			xloc = xbase;
			yloc = ybase;
		}
		
		int sliderHeight = (this instanceof StackWindow) ? 20 : 0;
		int screenHeight = maxWindow.y + maxWindow.height - sliderHeight;
		double mag = 1;
		while (xbase + XINC * 4 + width * mag > maxWindow.x + maxWindow.width || ybase + height * mag >= screenHeight) {
			// IJ.log(mag+"  "+xbase+"  "+width*mag+"  "+maxWindow.width);
			double mag2 = ImageCanvas.getLowerZoomLevel(mag);
			if (mag2 == mag)
				break;
			mag = mag2;
		}
		
		if (mag < 1.0) {
			initialMagnification = mag;
			ic.setDrawingSize((int) (width * mag), (int) (height * mag));
		}
		ic.setMagnification(mag);
		if (y + height * mag > screenHeight)
			y = ybase;
		if (!updating)
			setLocation(x, y);
		if (Prefs.open100Percent && ic.getMagnification() < 1.0) {
			while (ic.getMagnification() < 1.0)
				ic.zoomIn(0, 0);
			setSize(Math.min(width, maxWindow.width - x), Math.min(height, screenHeight - y));
			validate();
		} else
			pack();
	}
	
	Rectangle getMaxWindow(int xloc, int yloc) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = ge.getMaximumWindowBounds();
		// bounds.x=960; bounds.y=0; bounds.width=960; bounds.height=1200;
		if (IJ.debugMode)
			IJ.log("getMaxWindow: " + bounds + "  " + xloc + "," + yloc);
		if (xloc > bounds.x + bounds.width || yloc > bounds.y + bounds.height) {
			Rectangle bounds2 = getSecondaryMonitorBounds(ge, xloc, yloc);
			if (bounds2 != null)
				return bounds2;
		}
		Dimension ijSize = ij != null ? ij.getSize() : new Dimension(0, 0);
		if (bounds.height > 600) {
			bounds.y += ijSize.height;
			bounds.height -= ijSize.height;
		}
		return bounds;
	}
	
	private Rectangle getSecondaryMonitorBounds(GraphicsEnvironment ge, int xloc, int yloc) {
		// IJ.log("getSecondaryMonitorBounds "+wb);
		GraphicsDevice[] gs = ge.getScreenDevices();
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i = 0; i < gc.length; i++) {
				Rectangle bounds = gc[i].getBounds();
				// IJ.log(j+" "+i+" "+bounds+"  "+bounds.contains(wb.x, wb.y));
				if (bounds != null && bounds.contains(xloc, yloc))
					return bounds;
			}
		}
		return null;
	}
	
	public double getInitialMagnification() {
		return initialMagnification;
	}
	
	/** Override Container getInsets() to make room for some text above the image. */
	@Override
	public Insets getInsets() {
		Insets insets = super.getInsets();
		double mag = ic.getMagnification();
		int extraWidth = (int) ((MIN_WIDTH - imp.getWidth() * mag) / 2.0);
		if (extraWidth < 0)
			extraWidth = 0;
		int extraHeight = (int) ((MIN_HEIGHT - imp.getHeight() * mag) / 2.0);
		if (extraHeight < 0)
			extraHeight = 0;
		insets = new Insets(insets.top + textGap + extraHeight, insets.left + extraWidth, insets.bottom + extraHeight, insets.right + extraWidth);
		return insets;
	}
	
	/** Draws the subtitle. */
	public void drawInfo(Graphics g) {
		if (textGap != 0) {
			Insets insets = super.getInsets();
			if (imp.isComposite()) {
				CompositeImage ci = (CompositeImage) imp;
				if (ci.getMode() == CompositeImage.COMPOSITE)
					g.setColor(ci.getChannelColor());
			}
			g.drawString(createSubtitle(), insets.left + 5, insets.top + TEXT_GAP);
		}
	}
	
	/** Creates the subtitle. */
	public String createSubtitle() {
		String s = "";
		int nSlices = imp.getStackSize();
		if (nSlices > 1) {
			ImageStack stack = imp.getStack();
			int currentSlice = imp.getCurrentSlice();
			s += currentSlice + "/" + nSlices;
			String label = stack.getShortSliceLabel(currentSlice);
			if (label != null && label.length() > 0) {
				if (imp.isHyperStack())
					label = label.replace(';', ' ');
				s += " (" + label + ")";
			}
			if ((this instanceof StackWindow) && running2) {
				return s;
			}
			s += "; ";
		} else {
			String label = (String) imp.getProperty("Label");
			if (label != null) {
				int newline = label.indexOf('\n');
				if (newline > 0)
					label = label.substring(0, newline);
				int len = label.length();
				if (len > 4 && label.charAt(len - 4) == '.' && !Character.isDigit(label.charAt(len - 1)))
					label = label.substring(0, len - 4);
				if (label.length() > 60)
					label = label.substring(0, 60);
				s = label + "; ";
			}
		}
		int type = imp.getType();
		Calibration cal = imp.getCalibration();
		if (cal.scaled()) {
			s += IJ.d2s(imp.getWidth() * cal.pixelWidth, 2) + "x" + IJ.d2s(imp.getHeight() * cal.pixelHeight, 2)
					+ " " + cal.getUnits() + " (" + imp.getWidth() + "x" + imp.getHeight() + "); ";
		} else
			s += imp.getWidth() + "x" + imp.getHeight() + " pixels; ";
		double size = ((double) imp.getWidth() * imp.getHeight() * imp.getStackSize()) / 1024.0;
		switch (type) {
			case ImagePlus.GRAY8:
			case ImagePlus.COLOR_256:
				s += "8-bit";
				break;
			case ImagePlus.GRAY16:
				s += "16-bit";
				size *= 2.0;
				break;
			case ImagePlus.GRAY32:
				s += "32-bit";
				size *= 4.0;
				break;
			case ImagePlus.COLOR_RGB:
				s += "RGB";
				size *= 4.0;
				break;
		}
		if (imp.isInvertedLut())
			s += " (inverting LUT)";
		String s2 = null, s3 = null;
		if (size < 1024.0) {
			s2 = IJ.d2s(size, 0);
			s3 = "K";
		} else
			if (size < 10000.0) {
				s2 = IJ.d2s(size / 1024.0, 1);
				s3 = "MB";
			} else
				if (size < 1048576.0) {
					s2 = IJ.d2s(Math.round(size / 1024.0), 0);
					s3 = "MB";
				} else {
					s2 = IJ.d2s(size / 1048576.0, 1);
					s3 = "GB";
				}
		if (s2.endsWith(".0"))
			s2 = s2.substring(0, s2.length() - 2);
		return s + "; " + s2 + s3;
	}
	
	@Override
	public void paint(Graphics g) {
		// if (IJ.debugMode) IJ.log("wPaint: " + imp.getTitle());
		drawInfo(g);
		Rectangle r = ic.getBounds();
		int extraWidth = MIN_WIDTH - r.width;
		int extraHeight = MIN_HEIGHT - r.height;
		if (extraWidth <= 0 && extraHeight <= 0 && !Prefs.noBorder && !IJ.isLinux())
			g.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
	}
	
	/**
	 * Removes this window from the window list and disposes of it.
	 * Returns false if the user cancels the "save changes" dialog.
	 */
	public boolean close() {
		boolean isRunning = running || running2;
		running = running2 = false;
		boolean virtual = imp.getStackSize() > 1 && imp.getStack().isVirtual();
		if (isRunning)
			IJ.wait(500);
		if (ij == null || IJ.getApplet() != null || Interpreter.isBatchMode() || IJ.macroRunning() || virtual)
			imp.changes = false;
		if (imp.changes) {
			String msg;
			String name = imp.getTitle();
			if (name.length() > 22)
				msg = "Save changes to\n" + "\"" + name + "\"?";
			else
				msg = "Save changes to \"" + name + "\"?";
			YesNoCancelDialog d = new YesNoCancelDialog(this, "ImageJ", msg);
			if (d.cancelPressed())
				return false;
			else
				if (d.yesPressed()) {
					FileSaver fs = new FileSaver(imp);
					if (!fs.save())
						return false;
				}
		}
		closed = true;
		if (WindowManager.getWindowCount() == 0) {
			xloc = 0;
			yloc = 0;
		}
		WindowManager.removeWindow(this);
		// setVisible(false);
		if (ij != null && ij.quitting()) // this may help avoid thread deadlocks
			return true;
		dispose();
		if (imp != null)
			imp.flush();
		imp = null;
		return true;
	}
	
	public ImagePlus getImagePlus() {
		return imp;
	}
	
	public void setImage(ImagePlus imp2) {
		ImageCanvas ic = getCanvas();
		if (ic == null || imp2 == null)
			return;
		imp = imp2;
		imp.setWindow(this);
		ic.updateImage(imp);
		ic.setImageUpdated();
		ic.repaint();
		repaint();
	}
	
	public void updateImage(ImagePlus imp) {
		if (imp != this.imp)
			throw new IllegalArgumentException("imp!=this.imp");
		this.imp = imp;
		ic.updateImage(imp);
		setLocationAndSize(true);
		if (this instanceof StackWindow) {
			StackWindow sw = (StackWindow) this;
			int stackSize = imp.getStackSize();
			int nScrollbars = sw.getNScrollbars();
			if (stackSize == 1 && nScrollbars > 0)
				sw.removeScrollbars();
			else
				if (stackSize > 1 && nScrollbars == 0)
					sw.addScrollbars(imp);
		}
		pack();
		repaint();
		maxBounds = getMaximumBounds();
		setMaximizedBounds(maxBounds);
		setMaxBoundsTime = System.currentTimeMillis();
	}
	
	public ImageCanvas getCanvas() {
		return ic;
	}
	
	static ImagePlus getClipboard() {
		return ImagePlus.getClipboard();
	}
	
	public Rectangle getMaximumBounds() {
		double width = imp.getWidth();
		double height = imp.getHeight();
		double iAspectRatio = width / height;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle maxWindow = ge.getMaximumWindowBounds();
		maxWindowBounds = maxWindow;
		if (iAspectRatio / ((double) maxWindow.width / maxWindow.height) > 0.75) {
			maxWindow.y += 22; // uncover ImageJ menu bar
			maxWindow.height -= 22;
		}
		Dimension extraSize = getExtraSize();
		double maxWidth = maxWindow.width - extraSize.width;
		double maxHeight = maxWindow.height - extraSize.height;
		double mAspectRatio = maxWidth / maxHeight;
		int wWidth, wHeight;
		double mag;
		if (iAspectRatio >= mAspectRatio) {
			mag = maxWidth / width;
			wWidth = maxWindow.width;
			wHeight = (int) (height * mag + extraSize.height);
		} else {
			mag = maxHeight / height;
			wHeight = maxWindow.height;
			wWidth = (int) (width * mag + extraSize.width);
		}
		int xloc = (int) (maxWidth - wWidth) / 2;
		if (xloc < 0)
			xloc = 0;
		return new Rectangle(xloc, maxWindow.y, wWidth, wHeight);
	}
	
	Dimension getExtraSize() {
		Insets insets = getInsets();
		int extraWidth = insets.left + insets.right + 10;
		int extraHeight = insets.top + insets.bottom + 10;
		if (extraHeight == 20)
			extraHeight = 42;
		int members = getComponentCount();
		// if (IJ.debugMode) IJ.log("getExtraSize: "+members+" "+insets);
		for (int i = 1; i < members; i++) {
			Component m = getComponent(i);
			Dimension d = m.getPreferredSize();
			extraHeight += d.height + 5;
			if (IJ.debugMode)
				IJ.log(i + "  " + d.height + " " + extraHeight);
		}
		return new Dimension(extraWidth, extraHeight);
	}
	
	@Override
	public Component add(Component comp) {
		comp = super.add(comp);
		maxBounds = getMaximumBounds();
		// if (!IJ.isLinux()) {
		setMaximizedBounds(maxBounds);
		setMaxBoundsTime = System.currentTimeMillis();
		// }
		return comp;
	}
	
	// public void setMaximizedBounds(Rectangle r) {
	// super.setMaximizedBounds(r);
	// IJ.log("setMaximizedBounds: "+r+" "+getMaximizedBounds());
	// if (getMaximizedBounds().x==0)
	// throw new IllegalArgumentException("");
	// }
	
	public void maximize() {
		if (maxBounds == null)
			return;
		int width = imp.getWidth();
		int height = imp.getHeight();
		double aspectRatio = (double) width / height;
		Dimension extraSize = getExtraSize();
		int extraHeight = extraSize.height;
		double mag = (double) (maxBounds.height - extraHeight) / height;
		if (IJ.debugMode)
			IJ.log("maximize: " + mag + " " + ic.getMagnification() + " " + maxBounds);
		setSize(getMaximizedBounds().width, getMaximizedBounds().height);
		if (mag > ic.getMagnification() || aspectRatio < 0.5 || aspectRatio > 2.0) {
			ic.setMagnification2(mag);
			ic.setSrcRect(new Rectangle(0, 0, width, height));
			ic.setDrawingSize((int) (width * mag), (int) (height * mag));
			validate();
			unzoomWhenMinimizing = true;
		} else
			unzoomWhenMinimizing = false;
	}
	
	public void minimize() {
		if (unzoomWhenMinimizing)
			ic.unzoom();
		unzoomWhenMinimizing = true;
	}
	
	/** Has this window been closed? */
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		// IJ.log("focusGained: "+imp.getTitle());
		if (!Interpreter.isBatchMode() && ij != null && !ij.quitting())
			WindowManager.setCurrentWindow(this);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
		if (IJ.debugMode)
			IJ.log("windowActivated: " + imp.getTitle());
		ImageJ ij = IJ.getInstance();
		boolean quitting = ij != null && ij.quitting();
		if (IJ.isMacintosh() && ij != null && !quitting) {
			IJ.wait(10); // may be needed for Java 1.4 on OS X
			setMenuBar(Menus.getMenuBar());
		}
		if (imp != null)
			imp.setActivated(); // notify ImagePlus that image has been activated
		if (!closed && !quitting && !Interpreter.isBatchMode())
			WindowManager.setCurrentWindow(this);
		Frame channels = Channels.getInstance();
		if (channels != null && imp.isComposite())
			((Channels) channels).update();
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		// IJ.log("windowClosing: "+imp.getTitle()+" "+closed);
		if (closed)
			return;
		if (ij != null) {
			WindowManager.setCurrentWindow(this);
			IJ.doCommand("Close");
		} else {
			// setVisible(false);
			dispose();
			WindowManager.removeWindow(this);
		}
	}
	
	@Override
	public void windowStateChanged(WindowEvent e) {
		int oldState = e.getOldState();
		int newState = e.getNewState();
		// IJ.log("WSC: "+getBounds()+" "+oldState+" "+newState);
		if ((oldState & Frame.MAXIMIZED_BOTH) == 0 && (newState & Frame.MAXIMIZED_BOTH) != 0)
			maximize();
		else
			if ((oldState & Frame.MAXIMIZED_BOTH) != 0 && (newState & Frame.MAXIMIZED_BOTH) == 0)
				minimize();
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	@Override
	public void focusLost(FocusEvent e) {
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		int rotation = event.getWheelRotation();
		int width = imp.getWidth();
		int height = imp.getHeight();
		Rectangle srcRect = ic.getSrcRect();
		int xstart = srcRect.x;
		int ystart = srcRect.y;
		if (IJ.spaceBarDown() || srcRect.height == height) {
			srcRect.x += rotation * Math.max(width / 200, 1);
			if (srcRect.x < 0)
				srcRect.x = 0;
			if (srcRect.x + srcRect.width > width)
				srcRect.x = width - srcRect.width;
		} else {
			srcRect.y += rotation * Math.max(height / 200, 1);
			if (srcRect.y < 0)
				srcRect.y = 0;
			if (srcRect.y + srcRect.height > height)
				srcRect.y = height - srcRect.height;
		}
		if (srcRect.x != xstart || srcRect.y != ystart)
			ic.repaint();
	}
	
	/**
	 * Copies the current ROI to the clipboard. The entire
	 * image is copied if there is no ROI.
	 */
	public void copy(boolean cut) {
		imp.copy(cut);
	}
	
	public void paste() {
		imp.paste();
	}
	
	/**
	 * This method is called by ImageCanvas.mouseMoved(MouseEvent).
	 * 
	 * @see ij.gui.ImageCanvas#mouseMoved
	 */
	public void mouseMoved(int x, int y) {
		imp.mouseMoved(x, y);
	}
	
	@Override
	public String toString() {
		return imp.getTitle();
	}
	
	/**
	 * Causes the next image to be opened to be centered on the screen
	 * and displayed without informational text above the image.
	 */
	public static void centerNextImage() {
		centerOnScreen = true;
	}
	
	/** Causes the next image to be displayed at the specified location. */
	public static void setNextLocation(Point loc) {
		nextLocation = loc;
	}
	
	/**
	 * Moves and resizes this window. Changes the
	 * magnification so the image fills the window.
	 */
	public void setLocationAndSize(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
		getCanvas().fitToWindow();
		pack();
	}
	
	/**
	 * Overrides the setBounds() method in Component so
	 * we can find out when the window is resized.
	 */
	// public void setBounds(int x, int y, int width, int height) {
	// super.setBounds(x, y, width, height);
	// ic.resizeSourceRect(width, height);
	// }
	
} // class ImageWindow

