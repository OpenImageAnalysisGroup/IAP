package org.graffiti.plugin.io.resources;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

public abstract class AbstractResourceIOHandler implements ResourceIOHandler {
	
	@Override
	public IOurl saveAs(IOurl source, String targetFilename) throws Exception {
		throw new UnsupportedOperationException("Save not implemented for IO handler " + this.getClass().getCanonicalName());
	}
	
	@Override
	public IOurl save(IOurl source) throws Exception {
		return saveAs(source, source.getFileName());
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		return getPreviewInputStream(url, 128);
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url, int size) throws Exception {
		if (url != null && url.getFileName() != null && url.getFileName().contains(".argb_volume"))
			return null;
		if (url == null) {
			System.out.println("ERROR: Request for InputStream of NULL-URL");
			return null;
		}
		BufferedImage i = null;
		InputStream is = new MyByteArrayInputStream(ResourceIOManager.getPreviewImageContent(url));
		i = ImageIO.read(is);
		if (i == null) {
			return null;
		}
		int maxS = i.getHeight() > i.getWidth() ? i.getHeight() : i.getWidth();
		double ICON_HEIGHT = size;
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
