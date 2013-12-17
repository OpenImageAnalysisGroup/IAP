package de.ipk.ag_ba.image.structures;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.FolderPanel;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.utils.MyFileSaver;

public class ImageStack {
	
	ij.ImageStack stack;
	
	boolean sizeKnown = false;
	
	private int w;
	
	private int h;
	
	private final ArrayList<String> settingsPaths = new ArrayList<String>();
	
	public void addImage(String label, Image image) {
		addImage(label, image, null);
	}
	
	public void addImage(String label, Image image, String optSettingsPath) {
		image = image.copy();
		if (!sizeKnown) {
			sizeKnown = true;
			this.w = image.getWidth();
			this.h = image.getHeight();
			stack = new ij.ImageStack(w, h);
		} else {
			if (w > 1 && h > 1)
				image = image.resize(w, h);
			if (image.getWidth() != w)
				ErrorMsg.addErrorMessage("mismatching image size: " + w + " <> " + image.getWidth());
			if (image.getHeight() != h)
				ErrorMsg.addErrorMessage("mismatching image size: " + h + " <!> " + image.getHeight());
		}
		try {
			stack.addSlice(label + "//Settings: " + optSettingsPath + "//", image.getAsImagePlus().getProcessor());
			settingsPaths.add(optSettingsPath);
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
	
	public void show(final String title) {
		if (SystemAnalysis.isHeadless())
			return;
		final ImagePlus image = new ImagePlus();
		image.setStack(stack);
		if (!SwingUtilities.isEventDispatchThread()) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					image.show(title + " (" + stack.getSize() + ")");
					IAPservice.showImageJ();
				}
			};
			SwingUtilities.invokeLater(r);
		} else {
			image.show(title + " (" + stack.getSize() + ")");
			IAPservice.showImageJ();
		}
	}
	
	public void print(String title, final Runnable actionCmd, String buttonTitle, JComponent optSideComponent) {
		if (SystemAnalysis.isHeadless())
			return;
		ImagePlus image = new ImagePlus();
		image.setStack(stack);
		if (image.getWidth() > 0 && image.getHeight() > 0) {
			image.show(title + " (" + stack.getSize() + ")");
			ImageWindow win = image.getWindow();
			JButton jb = new JButton(buttonTitle);
			jb.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Engineering-64.png").getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH)));
			
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					actionCmd.run();
				}
			});
			JComponent ccc = FolderPanel.getBorderedComponent(
					TableLayout.get3Split(jb, null, optSideComponent, TableLayout.PREFERRED, 5, TableLayout.PREFERRED), 5, 5, 5, 5);
			win.add(ccc);
			win.pack();
			IAPservice.showImageJ();
		}
	}
	
	public int size() {
		return stack == null ? 0 : stack.getSize();
	}
}
