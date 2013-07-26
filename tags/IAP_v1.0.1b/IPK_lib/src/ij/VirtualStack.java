package ij;

import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Font;
import java.awt.image.ColorModel;
import java.io.File;

/** This class represents an array of disk-resident images. */
public class VirtualStack extends ImageStack {
	private static final int INITIAL_SIZE = 100;
	private String path;
	private int nSlices;
	private String[] names;
	private String[] labels;
	private int bitDepth;
	
	/** Default constructor. */
	public VirtualStack() {
	}
	
	/** Creates a new, empty virtual stack. */
	public VirtualStack(int width, int height, ColorModel cm, String path) {
		super(width, height, cm);
		this.path = path;
		names = new String[INITIAL_SIZE];
		labels = new String[INITIAL_SIZE];
		// IJ.log("VirtualStack: "+path);
	}
	
	/** Adds an image to the end of the stack. */
	public void addSlice(String name) {
		if (name == null)
			throw new IllegalArgumentException("'name' is null!");
		nSlices++;
		// IJ.log("addSlice: "+nSlices+"	"+name);
		if (nSlices == names.length) {
			String[] tmp = new String[nSlices * 2];
			System.arraycopy(names, 0, tmp, 0, nSlices);
			names = tmp;
			tmp = new String[nSlices * 2];
			System.arraycopy(labels, 0, tmp, 0, nSlices);
			labels = tmp;
		}
		names[nSlices - 1] = name;
	}
	
	/** Does nothing. */
	@Override
	public void addSlice(String sliceLabel, Object pixels) {
	}
	
	/** Does nothing.. */
	@Override
	public void addSlice(String sliceLabel, ImageProcessor ip) {
	}
	
	/** Does noting. */
	@Override
	public void addSlice(String sliceLabel, ImageProcessor ip, int n) {
	}
	
	/** Deletes the specified slice, were 1<=n<=nslices. */
	@Override
	public void deleteSlice(int n) {
		if (n < 1 || n > nSlices)
			throw new IllegalArgumentException("Argument out of range: " + n);
		if (nSlices < 1)
			return;
		for (int i = n; i < nSlices; i++)
			names[i - 1] = names[i];
		names[nSlices - 1] = null;
		nSlices--;
	}
	
	/** Deletes the last slice in the stack. */
	@Override
	public void deleteLastSlice() {
		if (nSlices > 0)
			deleteSlice(nSlices);
	}
	
	/** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
	@Override
	public Object getPixels(int n) {
		ImageProcessor ip = getProcessor(n);
		if (ip != null)
			return ip.getPixels();
		else
			return null;
	}
	
	/**
	 * Assigns a pixel array to the specified slice,
	 * were 1<=n<=nslices.
	 */
	@Override
	public void setPixels(Object pixels, int n) {
	}
	
	/**
	 * Returns an ImageProcessor for the specified slice,
	 * were 1<=n<=nslices. Returns null if the stack is empty.
	 */
	@Override
	public ImageProcessor getProcessor(int n) {
		// IJ.log("getProcessor: "+n+"  "+names[n-1]+"  "+bitDepth);
		Opener opener = new Opener();
		opener.setSilentMode(true);
		IJ.redirectErrorMessages(true);
		ImagePlus imp = opener.openImage(path, names[n - 1]);
		IJ.redirectErrorMessages(false);
		ImageProcessor ip = null;
		int depthThisImage = 0;
		if (imp != null) {
			int w = imp.getWidth();
			int h = imp.getHeight();
			int type = imp.getType();
			ColorModel cm = imp.getProcessor().getColorModel();
			labels[n - 1] = (String) imp.getProperty("Info");
			depthThisImage = imp.getBitDepth();
			ip = imp.getProcessor();
		} else {
			File f = new File(path, names[n - 1]);
			String msg = f.exists() ? "Error opening " : "File not found: ";
			ip = new ByteProcessor(getWidth(), getHeight());
			ip.invert();
			int size = getHeight() / 20;
			if (size < 9)
				size = 9;
			Font font = new Font("Helvetica", Font.PLAIN, size);
			ip.setFont(font);
			ip.setAntialiasedText(true);
			ip.setColor(0);
			ip.drawString(msg + names[n - 1], size, size * 2);
			depthThisImage = 8;
		}
		if (depthThisImage != bitDepth) {
			switch (bitDepth) {
				case 8:
					ip = ip.convertToByte(true);
					break;
				case 16:
					ip = ip.convertToShort(true);
					break;
				case 24:
					ip = ip.convertToRGB();
					break;
				case 32:
					ip = ip.convertToFloat();
					break;
			}
		}
		if (ip.getWidth() != getWidth() || ip.getHeight() != getHeight()) {
			ImageProcessor ip2 = ip.createProcessor(getWidth(), getHeight());
			ip2.insert(ip, 0, 0);
			ip = ip2;
		}
		return ip;
	}
	
	/** Currently not implemented */
	public int saveChanges(int n) {
		return -1;
	}
	
	/** Returns the number of slices in this stack. */
	@Override
	public int getSize() {
		return nSlices;
	}
	
	/** Returns the label of the Nth image. */
	@Override
	public String getSliceLabel(int n) {
		String label = labels[n - 1];
		if (label == null)
			return names[n - 1];
		else
			if (label.length() <= 60)
				return label;
			else
				return names[n - 1] + "\n" + label;
	}
	
	/** Returns null. */
	@Override
	public Object[] getImageArray() {
		return null;
	}
	
	/** Does nothing. */
	@Override
	public void setSliceLabel(String label, int n) {
	}
	
	/** Always return true. */
	@Override
	public boolean isVirtual() {
		return true;
	}
	
	/** Does nothing. */
	@Override
	public void trim() {
	}
	
	/** Returns the path to the directory containing the images. */
	public String getDirectory() {
		return path;
	}
	
	/** Returns the file name of the specified slice, were 1<=n<=nslices. */
	public String getFileName(int n) {
		return names[n - 1];
	}
	
	/** Sets the bit depth (8, 16, 24 or 32). */
	public void setBitDepth(int bitDepth) {
		this.bitDepth = bitDepth;
	}
	
	/** Returns the bit depth (8, 16, 24 or 32), or 0 if the bit depth is not known. */
	public int getBitDepth() {
		return bitDepth;
	}
	
	public ImageStack sortDicom(String[] strings, String[] info, int maxDigits) {
		int n = getSize();
		String[] names2 = new String[n];
		for (int i = 0; i < n; i++)
			names2[i] = names[i];
		for (int i = 0; i < n; i++) {
			int slice = (int) Tools.parseDouble(strings[i].substring(strings[i].length() - maxDigits), 0.0);
			if (slice == 0)
				return null;
			names[i] = names2[slice - 1];
			labels[i] = info[slice - 1];
		}
		return this;
	}
	
}
