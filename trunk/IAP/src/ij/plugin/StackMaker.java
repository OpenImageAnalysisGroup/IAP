package ij.plugin;

import ij.*;
import ij.gui.*;
import ij.process.*;

/**
 * The plugin implements the Image/Stacks/Montage to Stack command.
 * It creates a w*h image stack from an wxh image montage.
 * This is the opposite of what the "Make Montage" command does.
 * 2010.04.20,TF: Final stack can be cropped to remove border around frames.
 */
public class StackMaker implements PlugIn {
	private static int w = 2, h = 2, b = 0;
	
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.noImage();
			return;
		}
		if (imp.getStackSize() > 1) {
			IJ.error("This command requires a montage");
			return;
		}
		GenericDialog gd = new GenericDialog("Stack Maker");
		gd.addNumericField("Images_per_row: ", w, 0);
		gd.addNumericField("Images_rer_column: ", h, 0);
		gd.addNumericField("Border width: ", b, 0);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		w = (int) gd.getNextNumber();
		h = (int) gd.getNextNumber();
		b = (int) gd.getNextNumber();
		ImageStack stack = makeStack(imp.getProcessor(), w, h, b);
		new ImagePlus("Stack", stack).show();
	}
	
	public ImageStack makeStack(ImageProcessor ip, int w, int h, int b) {
		int width = ip.getWidth() / w;
		int height = ip.getHeight() / h;
		ImageStack stack = new ImageStack(width, height);
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				ip.setRoi(x * width, y * height, width, height);
				stack.addSlice(null, ip.crop());
			}
		if (b > 0) {
			int cropwidth = width - b - b / 2;
			int cropheight = height - b - b / 2;
			StackProcessor sp = new StackProcessor(stack, ip);
			stack = sp.crop(b, b, cropwidth, cropheight);
		}
		return stack;
	}
}
