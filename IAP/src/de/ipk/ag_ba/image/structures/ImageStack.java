package de.ipk.ag_ba.image.structures;

import ij.ImagePlus;
import ij.gui.StackWindow;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Graphics;
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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.FolderPanel;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.util.IAPservice;

/**
 * @author klukas
 */
public class ImageStack {
	
	ij.ImageStack stack;
	
	boolean sizeKnown = false;
	
	private int w;
	
	private int h;
	
	private final ArrayList<String> settingsPaths = new ArrayList<String>();
	
	private ImageSetConfig debugConfig;
	
	private int well;
	
	private int wellCnt;
	
	public ImageStack() {
		this.well = -1;
	}
	
	public ImageStack(int well) {
		this.well = well;
	}
	
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
		}
		if (image.getWidth() != w || image.getHeight() != h) {
			System.out.println(SystemAnalysis.getCurrentTime() + "ERROR: mismatching image size: " + h + " <!> " + image.getHeight());
			return;
		}
		try {
			stack.addSlice(label + (optSettingsPath != null ? "//Settings: " + optSettingsPath + "//" : ""),
					image.getAsImagePlus().getProcessor());
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
		throw new UnsupportedOperationException("TODO NOT YET IMPLEMENTED");
		// new MyFileSaver(image).saveAsTiffStack(os);
	}
	
	public void show(final String title) {
		if (SystemAnalysis.isHeadless())
			return;
		for (int i = 1; i <= size(); i++) {
			stack.setSliceLabel(title + ": " + stack.getSliceLabel(i), i);
		}
		final ImagePlus image = new ImagePlus();
		image.setStack(stack);
		if (!SwingUtilities.isEventDispatchThread()) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					IAPservice.showImageJ(true);
					image.show(title + " (" + stack.getSize() + ")");
				}
			};
			SwingUtilities.invokeLater(r);
		} else {
			IAPservice.showImageJ(true);
			image.show(title + " (" + stack.getSize() + ")");
		}
	}
	
	public void show(String title, final Runnable actionCmd, String buttonTitle, JComponent optSideComponent) {
		show(title, actionCmd, buttonTitle, optSideComponent, null);
	}
	
	public void show(String title, final Runnable actionCmd, String buttonTitle, final JComponent optSideComponent,
			final ThreadSafeOptions tsoCurrentImageDisplayPage) {
		if (SystemAnalysis.isHeadless())
			return;
		ImagePlus image = new ImagePlus() {
			
			@Override
			public void updatePosition(int c, int z, int t) {
				super.updatePosition(c, z, t);
				if (tsoCurrentImageDisplayPage != null) {
					try {
						String tt = getStack().getSliceLabel(z);
						if (tt != null && tt.startsWith("Result of ")) {
							tt = tt.substring("Result of ".length());
						} else
							tsoCurrentImageDisplayPage.setParam(1, null);
						tsoCurrentImageDisplayPage.setParam(0, tt);
					} catch (Exception e) {
						// empty
					}
				}
			}
			
		};
		image.setStack(stack);
		if (image.getWidth() > 0 && image.getHeight() > 0) {
			final JButton jb = new JButton(buttonTitle);
			jb.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Engineering-64.png").getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH)));
			
			final java.awt.Image iconff = IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Engineering-64.png");
			
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					actionCmd.run();
				}
			});
			final String titlef = title + " (" + SystemAnalysis.getCurrentTime() + ")";
			final JComponent ccc = FolderPanel.getBorderedComponent(
					TableLayout.get3Split(jb, null,
							optSideComponent, TableLayout.PREFERRED, 5, TableLayout.PREFERRED), 5, 5, 5, 5);
			
			StackWindow win = new StackWindow(image) {
				
				@Override
				public void setIconImage(java.awt.Image image) {
					super.setIconImage(iconff);
				}
				
				@Override
				public void paint(Graphics arg0) {
					super.paint(arg0);
					jb.repaint();
					if (optSideComponent != null) {
						if (optSideComponent instanceof JPanel) {
							JPanel jp = (JPanel) optSideComponent;
							for (Component j : jp.getComponents())
								j.repaint();
						} else
							optSideComponent.repaint();
					}
				}
				
				@Override
				public String getTitle() {
					return titlef;
				}
			};
			win.add(ccc);
			win.pack();
			
			IAPservice.showImageJ(true);
			
			image.show(title + " (" + stack.getSize() + ")");
			
		}
	}
	
	public int size() {
		return stack == null ? 0 : stack.getSize();
	}
	
	public synchronized ImageSetConfig getDebugConfig() {
		if (this.debugConfig == null)
			debugConfig = new ImageSetConfig();
		return debugConfig;
	}
	
	public int getWell() {
		return well;
	}
	
	public int getWellCnt() {
		return wellCnt;
	}
	
	public void setWell(int wellIdx, int wellCnt) {
		well = wellIdx;
		this.wellCnt = wellCnt;
	}
	
	public ij.ImageStack getStack() {
		return stack;
	}
	
	public void setStack(ij.ImageStack stack) {
		this.stack = stack;
	}
}
