package de.ipk.ag_ba.server.datastructures;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import org.graffiti.plugin.io.resources.MyByteArrayInputStream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

public class LoadedImageStream extends LoadedImage {
	
	private final byte[] bufferOfLabelImage;
	
	public LoadedImageStream(SampleInterface parent, BufferedImage image, byte[] bufferOfLabelImage) {
		super(parent, image);
		this.bufferOfLabelImage = bufferOfLabelImage;
	}
	
	public LoadedImageStream(ImageData id, BufferedImage image, byte[] bufferOfLabelImage) {
		super(id, image);
		this.bufferOfLabelImage = bufferOfLabelImage;
	}
	
	@Override
	public BufferedImage getLoadedImageLabelField() {
		throw new UnsupportedOperationException(
				"This variant of the LoadedImage class can't return the loaded image as an BufferedImage (only as a data stream).");
	}
	
	@Override
	public InputStream getInputStreamLabelField() {
		MyByteArrayInputStream isLabel = null;
		if (bufferOfLabelImage != null)
			isLabel = new MyByteArrayInputStream(bufferOfLabelImage, bufferOfLabelImage.length);
		
		return isLabel;
	}
}
