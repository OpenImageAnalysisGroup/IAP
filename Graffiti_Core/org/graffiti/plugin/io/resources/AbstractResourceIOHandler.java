package org.graffiti.plugin.io.resources;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

public abstract class AbstractResourceIOHandler implements ResourceIOHandler {
	
	public IOurl saveAs(IOurl source, String targetFilename) throws Exception {
		throw new UnsupportedOperationException("Save not implemented for IO handler " + this.getClass().getCanonicalName());
	}
	
	public IOurl save(IOurl source) throws Exception {
		return saveAs(source, source.getFileName());
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		
		BufferedImage i = null;
		InputStream is = url.getInputStream();
		is = ResourceIOManager.getInputStreamMemoryCached(is);
		i = ImageIO.read(is);
		int maxS = i.getHeight() > i.getWidth() ? i.getHeight() : i.getWidth();
		double ICON_HEIGHT = 128;
		double factor = ICON_HEIGHT / maxS;
		i = resize(i, (int) (i.getWidth() * factor), (int) (i.getHeight() * factor));
		
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ImageIO.write(i, "png", output);
		
		MyByteArrayInputStream result = new MyByteArrayInputStream(output.getBuff(), output.size());
		
		return result;
	}
	
	private static BufferedImage resize(BufferedImage image, int width, int height) {
		int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
	
}
