package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockReplaceEmptyOriginalImage extends AbstractBlock {
	
	private static final int sz = 128 - 5;
	
	@Override
	protected FlexibleImage processVISimage() throws InterruptedException {
		FlexibleImage image = getInput().getImages().getVis();
		IOurl infoUrl = getInput().getImages().getVisInfo().getURL();
		image = processImage(image, infoUrl);
		return image;
	}
	
	@Override
	protected FlexibleImage processFLUOimage() throws InterruptedException {
		FlexibleImage image = getInput().getImages().getFluo();
		IOurl infoUrl = getInput().getImages().getFluoInfo().getURL();
		image = processImage(image, infoUrl);
		return image;
	}
	
	@Override
	protected FlexibleImage processNIRimage() throws InterruptedException {
		FlexibleImage image = getInput().getImages().getNir();
		IOurl infoUrl = getInput().getImages().getNirInfo().getURL();
		image = processImage(image, infoUrl);
		return image;
	}
	
	public FlexibleImage processImage(FlexibleImage image, IOurl infoUrl) {
		if (image != null) {
			if (image.getIO().countFilledPixels() == 0) {
				try {
					image = new FlexibleImage(infoUrl);
					image.getIO().addBorder(50, 0, 0, Color.RED.getRGB());
				} catch (Exception e) {
					image = null;
					getInput().getImages().getVisInfo().addAnnotationField("loaderror", e.getMessage());
				}
			}
		}
		if (image == null) {
			int[] img = new int[sz * sz];
			img = ImageOperation.fillArray(img, Color.WHITE.getRGB());
			image = new FlexibleImage(img, sz, sz);
			image = image.getIO().drawLine(0, 0, sz, sz, Color.RED, 5).drawLine(sz, 0, 0, sz, Color.RED, 5).addBorder(5, 0, 0, Color.RED.getRGB())
					.getImage();
		}
		return image;
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		return mask;
	}
	
}
