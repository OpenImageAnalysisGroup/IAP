/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
/*
 * Created on Jun 11, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.SystemOptions;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;

/**
 * @author klukas
 */
public class LoadedImage extends ImageData implements LoadedData {
	
	private final BufferedImage image, imageLabel;
	
	public LoadedImage(SampleInterface parent, BufferedImage image) {
		super(parent);
		LoadedDataHandler.registerObject(this);
		this.image = image;
		this.imageLabel = null;
	}
	
	public LoadedImage(ImageData id, BufferedImage image) {
		super(id.getParentSample(), id);
		LoadedDataHandler.registerObject(this);
		this.image = image;
		this.imageLabel = null;
	}
	
	public LoadedImage(ImageData id, BufferedImage image, BufferedImage optLabelImage) {
		super(id.getParentSample(), id);
		LoadedDataHandler.registerObject(this);
		this.image = image;
		this.imageLabel = optLabelImage;
	}
	
	public BufferedImage getLoadedImage() {
		return image;
	}
	
	public BufferedImage getLoadedImageLabelField() {
		return imageLabel;
	}
	
	// /**
	// * Use getInputStream() if you don't want memory overhead.
	// *
	// * @return
	// * @throws IOException
	// */
	// public ImageUploadData getImageUploadData() throws IOException {
	//
	// MyByteArrayInputStream is = getStream(image);
	// MyByteArrayInputStream isLabel = null;
	// if (imageLabel != null)
	// isLabel = getStream(imageLabel);
	//
	// ByteArrayInputStream bis_preview = null;// MyImageIOhelper.getPreviewImageStream(image);
	//
	// long len = is.getBuff().length;
	// return new ImageUploadData(len, is, isLabel, bis_preview);
	// }
	
	protected MyByteArrayInputStream getStream(BufferedImage img) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
		ImageIO.write(img, "png", ios);
		byte[] content = bos.toByteArray();
		MyByteArrayInputStream is = new MyByteArrayInputStream(content, content.length);
		return is;
	}
	
	/**
	 * Consumes less memory than getStream(), but if you want to do read the
	 * stream several times (e.g.reading image and generating md5) it is useful
	 * to cache the data.
	 * 
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream() {
		return getInputStreamFromImage(image);
	}
	
	public BufferedImage getPreviewImage() {
		return MyImageIOhelper.getPreviewImage(image);
	}
	
	private static JFrame lastWindow;
	
	public void showImageWindow() {
		BufferedImage img = GravistoService.getScaledImage(image, 800, 800);
		
		synchronized (LoadedImage.class) {
			if (lastWindow != null) {
				lastWindow.setVisible(false);
				lastWindow.dispose();
				int x = lastWindow.getX();
				int y = lastWindow.getX();
				lastWindow = MainFrame.showMessageWindow(getURL().toString(), new JLabel(new ImageIcon(img)), false);
				lastWindow.setBounds(x, y, lastWindow.getWidth(), lastWindow.getHeight());
				lastWindow.setVisible(true);
			} else
				lastWindow = MainFrame.showMessageWindow(getURL().toString(), new JLabel(new ImageIcon(img)));
		}
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		return new LoadedImage((ImageData) super.clone(parent), getLoadedImage(), getLoadedImageLabelField());
	}
	
	@Override
	public InputStream getInputStreamLabelField() {
		return getInputStreamFromImage(imageLabel);
	}
	
	private InputStream getInputStreamFromImage(final BufferedImage image) {
		if (image == null)
			return null;
		
		boolean pipe = false;
		
		if (!pipe) {
			MyByteArrayOutputStream mos = new MyByteArrayOutputStream();
			try {
				ImageIO.write(image, SystemOptions.getInstance().getString("IAP", "Result File Type", "png"), mos);
				return new MyByteArrayInputStream(mos.getBuff(), mos.size());
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
		
		try {
			final PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream pin = new PipedInputStream(pout);
			
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ImageIO.write(image, SystemOptions.getInstance().getString("IAP", "Result File Type", "png"), pout);
						pout.close();
					} catch (IOException e) {
						ErrorMsg.addErrorMessage(e);
						System.out.println("Could not write to piped output stream!");
						try {
							pout.close();
						} catch (IOException e1) {
							ErrorMsg.addErrorMessage(e1);
						}
					}
				}
			});
			t.setName("Write image to piped output stream");
			t.start();
			return pin;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
}
