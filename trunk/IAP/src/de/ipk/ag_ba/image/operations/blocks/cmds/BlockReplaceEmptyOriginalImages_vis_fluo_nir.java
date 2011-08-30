package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class BlockReplaceEmptyOriginalImages_vis_fluo_nir extends AbstractBlock {
	
	private static final int sz = 128 - 5;
	
	public FlexibleImage processImage(FlexibleImage image) {
		FlexibleImageType it = image == null ? FlexibleImageType.UNKNOWN : image.getType();
		if (image != null) {
			if (image.getIO().countFilledPixels() == 0) {
				ImageData imageInfo = null;
				switch (it) {
					case FLUO:
						imageInfo = getInput().getImages().getFluoInfo();
						break;
					case NIR:
						imageInfo = getInput().getImages().getNirInfo();
						break;
					case VIS:
						imageInfo = getInput().getImages().getVisInfo();
						break;
					case UNKNOWN:
						break;
				}
				try {
					if (imageInfo != null) {
						image = new FlexibleImage(imageInfo.getURL());
						image.getIO().addBorder(50, 0, 0, Color.RED.getRGB());
					}
				} catch (Exception e) {
					image = null;
					imageInfo.addAnnotationField("loaderror", e.getMessage());
				}
			}
		}
		if (image == null) {
			int[] img = new int[sz * sz];
			img = ImageOperation.fillArray(img, Color.WHITE.getRGB());
			image = new FlexibleImage(sz, sz, img);
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
