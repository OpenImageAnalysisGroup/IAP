package ij.plugin;

import ij.*;
import ij.process.*;
import ij.gui.*;

/**
 * This plugin, which concatenates two images or stacks, implements the Image/Stacks/Concatenate command.
 * The images or stacks must have same width, height and data type.
 */
public class Concatenator implements PlugIn {
	
	ImagePlus imp1, imp2;
	static boolean keep;
	static String title = "Concatenated Stacks";
	
	public void run(String arg) {
		if (showDialog()) {
			ImagePlus imp3 = concatenate(imp1, imp2, keep);
			if (imp3 != null)
				imp3.show();
		}
	}
	
	public ImagePlus concatenate(ImagePlus imp1, ImagePlus imp2, boolean keep) {
		if (imp1.getType() != imp2.getType() || imp1.isHyperStack() || imp2.isHyperStack()) {
			error();
			return null;
		}
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		if (width != imp2.getWidth() || height != imp2.getHeight()) {
			error();
			return null;
		}
		ImageStack stack1 = imp1.getStack();
		ImageStack stack2 = imp2.getStack();
		int size1 = stack1.getSize();
		int size2 = stack2.getSize();
		ImageStack stack3 = imp1.createEmptyStack();
		int slice = 1;
		for (int i = 1; i <= size1; i++) {
			ImageProcessor ip = stack1.getProcessor(slice);
			String label = stack1.getSliceLabel(slice);
			if (keep || imp1 == imp2) {
				ip = ip.duplicate();
				slice++;
			} else
				stack1.deleteSlice(slice);
			stack3.addSlice(label, ip);
		}
		slice = 1;
		for (int i = 1; i <= size2; i++) {
			ImageProcessor ip = stack2.getProcessor(slice);
			String label = stack2.getSliceLabel(slice);
			if (keep || imp1 == imp2) {
				ip = ip.duplicate();
				slice++;
			} else
				stack2.deleteSlice(slice);
			stack3.addSlice(label, ip);
		}
		ImagePlus imp3 = new ImagePlus(title, stack3);
		imp3.setCalibration(imp1.getCalibration());
		if (!keep) {
			imp1.changes = false;
			if (imp1.getWindow() != null)
				imp1.getWindow().close();
			if (imp1 != imp2) {
				imp2.changes = false;
				if (imp2.getWindow() != null)
					imp2.getWindow().close();
			}
		}
		return imp3;
	}
	
	boolean showDialog() {
		int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.noImage();
			return false;
		}
		
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp != null ? imp.getTitle() : "";
		}
		
		GenericDialog gd = new GenericDialog("Concatenator");
		gd.addChoice("Stack1:", titles, titles[0]);
		gd.addChoice("Stack2:", titles, wList.length > 1 ? titles[1] : titles[0]);
		gd.addStringField("Title:", title, 16);
		gd.addCheckbox("Keep Source Stacks", keep);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		int index1 = gd.getNextChoiceIndex();
		int index2 = gd.getNextChoiceIndex();
		title = gd.getNextString();
		keep = gd.getNextBoolean();
		
		imp1 = WindowManager.getImage(wList[index1]);
		imp2 = WindowManager.getImage(wList[index2]);
		return true;
	}
	
	void error() {
		IJ.showMessage("Concatenator", "This command requires two stacks that have\n" +
				"the same width, height and data type.");
	}
	
}
