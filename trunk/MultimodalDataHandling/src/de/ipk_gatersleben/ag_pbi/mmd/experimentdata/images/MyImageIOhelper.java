/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
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
 * Created on Jun 15, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.ErrorMsg;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;

/**
 * @author klukas
 */
public class MyImageIOhelper {
	
	public static BufferedImage getPreviewImage(BufferedImage image) {
		int width = 128, height = 128;
		
		int oldWidth = image.getWidth();
		int oldHeight = image.getHeight();
		int maxSource = (oldWidth > oldHeight) ? oldWidth : oldHeight;
		int minTarget = (width < height) ? width : height;
		double factor = (double) minTarget / (double) maxSource;
		width = (int) (oldWidth * factor);
		height = (int) (oldHeight * factor);
		BufferedImage previewImage = resize(image, width, height, (int) (1.2d / factor) + 1);
		
		return previewImage;
	}
	
	public static MyByteArrayInputStream getPreviewImageStream(BufferedImage image) throws IOException {
		try {
			BufferedImage previewImage = getPreviewImage(image);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
			
			ImageIO.write(previewImage, SystemOptions.getInstance().getString("IAP", "Preview File Type", "png"), ios);
			byte[] content = bos.toByteArray();
			MyByteArrayInputStream is = new MyByteArrayInputStream(content, content.length);
			return is;
		} catch (Exception e) {
			System.out.println("Warning: Couldn't create preview image for a certain image.");
			return null;
		}
	}
	
	private static BufferedImage resize(BufferedImage image, int width, int height, int blur) {
		boolean goodQuality = false;
		if (goodQuality && image.getWidth() > width && image.getHeight() > height) {
			image = blurImage(image, blur);
		}
		
		image = resize(image, width, height);
		return image;
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage createCompatibleImage(BufferedImage image) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage result = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics2D g2 = result.createGraphics();
		g2.drawRenderedImage(image, null);
		g2.dispose();
		return result;
	}
	
	private static BufferedImage resize(BufferedImage image, int width, int height) {
		int type = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		// g.setComposite(AlphaComposite.Clear);
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
	
	@SuppressWarnings("unchecked")
	public static BufferedImage blurImage(BufferedImage image, int blurRadius) {
		float[] matrix = new float[blurRadius * blurRadius];
		for (int i = 0; i < blurRadius * blurRadius; i++)
			matrix[i] = 1.0f / blurRadius / blurRadius;
		
		Map map = new HashMap();
		
		map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		RenderingHints hints = new RenderingHints(map);
		BufferedImageOp op = new ConvolveOp(new Kernel(blurRadius, blurRadius, matrix), ConvolveOp.EDGE_NO_OP, hints);
		try {
			BufferedImage bi = op.filter(image, null);
			return bi;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return image;
		}
	}
}
