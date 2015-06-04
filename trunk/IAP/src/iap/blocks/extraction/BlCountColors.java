package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class BlCountColors extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	@Override
	protected Image processVISmask() {
		Image image = input().masks().vis();
		if (getBoolean("process VIS mask", true))
			countColors(CameraType.VIS, image, input().images().getVisInfo());
		return image;
	}
	
	private void countColors(CameraType ct, Image image, ImageData visInfo) {
		if (image == null)
			return;
		
		int[] img = image.getAs1A();
		int[] colors = new int[16777216];
		
		for (int pix : img) {
			colors[pix & 0xffffff]++;
		}
		
		double count = 0;
		
		for (int c : colors) {
			if (c > 0)
				count++;
		}
		
		double count_d = count;
		
		getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), ct, TraitCategory.INTENSITY, "Color_Count"),
				count_d,
				"", this, visInfo);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
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
		return "Count Colors";
	}
	
	@Override
	public String getDescription() {
		return "Count number of different colors in an image.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
