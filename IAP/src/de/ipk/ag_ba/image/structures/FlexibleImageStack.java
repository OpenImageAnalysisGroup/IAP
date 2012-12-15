package de.ipk.ag_ba.image.structures;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JButton;

import org.ErrorMsg;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.utils.MyFileSaver;

public class FlexibleImageStack {
	
	ImageStack stack;
	
	boolean sizeKnown = false;
	
	private int w;
	
	private int h;
	
	public void addImage(String label, FlexibleImage image) {
		image = image.copy();
		if (!sizeKnown) {
			sizeKnown = true;
			this.w = image.getWidth();
			this.h = image.getHeight();
			stack = new ImageStack(w, h);
		} else {
			if (w > 1 && h > 1)
				image = image.resize(w, h);
			if (image.getWidth() != w)
				ErrorMsg.addErrorMessage("mismatching image size: " + w + " <> " + image.getWidth());
			if (image.getHeight() != h)
				ErrorMsg.addErrorMessage("mismatching image size: " + h + " <!> " + image.getHeight());
		}
		try {
			stack.addSlice(label, image.getAsImagePlus().getProcessor());
		} catch (Exception e) {
			System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: COULD NOT ADD IMAGE TO IMAGE-STACK: " +
					e.getMessage());
		}
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
		if (SystemAnalysis.isHeadless())
			return;
		ImagePlus image = new ImagePlus();
		image.setStack(stack);
		image.show(title + " (" + stack.getSize() + ")");
		IAPservice.showImageJ();
	}
	
	public void print(String title, final Runnable actionCmd, String buttonTitle) {
		if (SystemAnalysis.isHeadless())
			return;
		ImagePlus image = new ImagePlus();
		image.setStack(stack);
		if (image.getWidth() > 0 && image.getHeight() > 0) {
			image.show(title + " (" + stack.getSize() + ")");
			ImageWindow win = image.getWindow();
			JButton jb = new JButton(buttonTitle);
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					actionCmd.run();
				}
			});
			win.add(jb);
			win.pack();
			IAPservice.showImageJ();
		}
	}
	
}
