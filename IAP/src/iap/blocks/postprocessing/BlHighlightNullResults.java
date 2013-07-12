package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Replaces the null result or empty images with the input image, with a red border, to indicate a processing problem.
 * 
 * @author klukas
 */
public class BlHighlightNullResults extends AbstractBlock {
	
	private static final int sz = 128 - 5;
	
	private final Color lr = new Color(255, 127, 127);
	private final int lri = lr.getRGB();
	
	@Override
	public Image processImage(Image image) {
		CameraType it = image == null ? CameraType.UNKNOWN : image.getCameraType();
		if (image != null) {
			if (image.io().countFilledPixels() == 0) {
				ImageData imageInfo = null;
				switch (it) {
					case FLUO:
						imageInfo = input().images().getFluoInfo();
						break;
					case NIR:
						imageInfo = input().images().getNirInfo();
						break;
					case VIS:
						imageInfo = input().images().getVisInfo();
						break;
					case UNKNOWN:
						break;
				}
				try {
					if (imageInfo != null) {
						image = new Image(imageInfo.getURL());
						int x = 0, y = 0, w = image.getWidth(), h = image.getHeight();
						double alpha = 0.5;
						int color = new Color(50, 50, 50).getRGB();
						image = image.io().canvas().fillRect(x, y, w, h, color, alpha).getImage().io().addBorder(120, 0, 0, lri).getImage();
						image = image.io().drawLine(0, 0, sz, sz, lr, 5).drawLine(sz, 0, 0, sz, lr, 5).addBorder(5, 0, 0, lri)
								.getImage();
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
			image = new Image(sz, sz, img);
			image = image.io().drawLine(0, 0, sz, sz, lr, 5).drawLine(sz, 0, 0, sz, lr, 5).addBorder(5, 0, 0, lri)
					.getImage();
		}
		return image;
	}
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.POSTPROCESSING;
	}
	
}
