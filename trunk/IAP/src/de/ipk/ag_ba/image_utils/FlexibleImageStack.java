package de.ipk.ag_ba.image_utils;

import ij.ImagePlus;
import ij.ImageStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FlexibleImageStack {
	
	ImageStack stack;
	
	boolean sizeKnown = false;
	
	private int w;
	
	private int h;
	
	public void addImage(String label, FlexibleImage image) {
		if (!sizeKnown) {
			sizeKnown = true;
			this.w = image.getWidth();
			this.h = image.getHeight();
			stack = new ImageStack(w, h);
		} else {
			image = image.resize(w, h);
			if (image.getWidth() != w)
				System.out.println("naja...: " + w + " <> " + image.getWidth());
			if (image.getHeight() != h)
				System.out.println("naja, naja..." + h + " <!> " + image.getHeight());
		}
		stack.addSlice(label, image.getConvertAsImagePlus().getProcessor());
	}
	
	public void saveAsLayeredTif(File file) throws FileNotFoundException {
		saveAsLayeredTif(new FileOutputStream(file));
	}
	
	public void saveAsLayeredTif(OutputStream os) {
		ImagePlus image = new ImagePlus();
		image.setStack(stack);
		
		new MyFileSaver(image).saveAsTiffStack(os);
	}
	
	public void print(String title) {
		ImagePlus image = new ImagePlus();
		image.setStack(stack);
		image.show(title + " (" + stack.getSize() + ")");
	}
}
