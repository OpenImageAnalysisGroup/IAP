package iap.blocks.preprocessing;

import java.util.HashMap;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

/**
 * @author Christian Klukas
 */
public class BlExtractChannels extends AbstractBlock {
	
	private HashMap<CameraType, Image> resultImages;
	
	public BlExtractChannels() {
		super();
		this.resultImages = new HashMap<CameraType, Image>();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		return CameraType.getHashSet(CameraType.values());
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return CameraType.getHashSet(CameraType.values());
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Extract color channels";
	}
	
	@Override
	public String getDescription() {
		return "Extract desired color channels";
	}
	
	@Override
	protected Image processMask(Image mask) {
		boolean process = getBoolean("Process " + mask.getCameraType(), false);
		if (process) {
			for (int i = 1; i <= 3; i++) {
				String channel = getStringRadioSelection(mask.getCameraType() + " extraction " + i, Channel.RGB_R.name(), Channel.getListOfNames(Channel.values()));
				String target = getStringRadioSelection(mask.getCameraType() + " target " + i, mask.getCameraType().name(), CameraType.getListOfNames(CameraType.values()));
				Channel c = Channel.valueOf(channel);
				CameraType ct = CameraType.fromString(target);
				
				Image res = mask.io().channels().get(c).getImage();
				resultImages.put(ct, res);
			}
			
			return mask;
		} else
			return mask;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		for (CameraType ct : resultImages.keySet())
			processedMasks.setImage(ct, resultImages.get(ct));
	}
	
}
