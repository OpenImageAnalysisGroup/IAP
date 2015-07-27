package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.ArrayList;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.ImageTexture;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ColorSpace;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

/**
 * R/G/B analysis for foreground pixels.
 * 
 * @author pape, klukas
 */
public class BlCalcColorfeatures extends AbstractBlock implements CalculatesProperties {
	
	@Override
	protected Image processMask(Image mask) {
		calcTextureFeatures(mask, mask.getCameraType(), optionsAndResults.getCameraPosition(), input().images().getImageInfo(mask.getCameraType()));
		return mask;
	}
	
	private void calcTextureFeatures(Image img, CameraType ct, CameraPosition cp, NumericMeasurement3D imageRef) {
		for (Channel c : Channel.values()) {
			boolean validChannel = false;
			if (c.getColorSpace() == ColorSpace.RGB) {
				if (ct == CameraType.VIS) {
					if (getBoolean("Process " + ct.getNiceName() + " channel " + c.getNiceName(), c == Channel.RGB_R || c == Channel.RGB_G || c == Channel.RGB_B))
						validChannel = true;
				}
				if (ct == CameraType.FLUO) {
					if (getBoolean("Process " + ct.getNiceName() + " channel " + c.getNiceName(), c == Channel.RGB_R || c == Channel.RGB_G))
						validChannel = true;
				}
				if (ct == CameraType.NIR) {
					if (c == Channel.RGB_R)
						if (getBoolean("Process " + ct.getNiceName() + " channel " + c.getNiceName(), true))
							validChannel = true;
				}
			} else {
				if (getBoolean("Process Colorspace " + c.getColorSpace().getNiceName(), false))
					validChannel = true;
			}
			if (validChannel) {
				ImageOperation ch_img = img.io().channels().get(c);
				calcTextureForImage(ch_img, c, ct, cp, imageRef);
			}
		}
	}
	
	private void calcTextureForImage(ImageOperation img, Channel c, CameraType cameraType, CameraPosition cp, NumericMeasurement3D imageRef) {
		ImageTexture it = new ImageTexture(img.getImage());
		it.calcTextureFeatures();
		
		for (FirstOrderTextureFeatures tf : FirstOrderTextureFeatures.values()) {
			// if (getBoolean("Calculate " + tf.getNiceName(), tf == FirstOrderTextureFeatures.MEAN || tf == FirstOrderTextureFeatures.STD))
			if (tf == FirstOrderTextureFeatures.MEAN || tf == FirstOrderTextureFeatures.STD)
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cp, cameraType, TraitCategory.TEXTURE, c + "." + tf),
						it.firstOrderFeatures.get(tf), null, this, imageRef);
		}
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
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Color Intensities";
	}
	
	@Override
	public String getDescription() {
		return "Calculates R/G/B intensities for all camera types besides IR. "
				+ "For FLUO images, the blue channel is not considered. "
				+ "For gray scale NIR images, only the red channel is considered. "
				+ "The used channels and features can be modified.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul><li>You may modify the considered color channel and features.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		ArrayList<CalculatedPropertyDescription> desList = new ArrayList<CalculatedPropertyDescription>();
		for (Channel c : Channel.values()) {
			for (FirstOrderTextureFeatures tf : FirstOrderTextureFeatures.values()) {
				if (tf == FirstOrderTextureFeatures.MEAN || tf == FirstOrderTextureFeatures.STD)
					desList.add(new CalculatedProperty(c + "." + tf, tf.getNiceName()
							+ " - first order texture property (independent of pixel neighbors). Calculated on grayscale image derived from channel "
							+ c.getNiceName()
							+ "." +
							(tf.getReferenceLink() != null ? " Further information: <a href='" + tf.getReferenceLink() + "'>Link</a>." : "")));
			}
		}
		return desList.toArray(new CalculatedPropertyDescription[desList.size()]);
	}
}
