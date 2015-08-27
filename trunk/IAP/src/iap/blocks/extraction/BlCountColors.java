package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;

import java.util.BitSet;
import java.util.HashSet;
import java.util.TreeMap;

import org.AttributeHelper;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape, klukas
 */
public class BlCountColors extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	@Override
	protected Image processVISmask() {
		Image image = input().masks().vis();
		if (getBoolean("count colors (VIS)", true))
			countColors(CameraType.VIS, image, input().images().getVisInfo(),
					getBoolean("calculate histogram (VIS)", true), getBoolean("use color names instead of RGB code (VIS)", true));
		return image;
	}
	
	private void countColors(CameraType ct, Image image, ImageData visInfo, boolean createHistogram, boolean useColorNames) {
		if (image == null)
			return;
		
		int[] img = image.getAs1A();
		BitSet colors = new BitSet(16777216);
		TreeMap<String, Integer> colorHistogram = new TreeMap<>();
		for (int pix : img) {
			int c = pix & 0xffffff;
			colors.set(c);
			if (createHistogram) {
				int rf = (c & 0xff0000) >> 16;
				int gf = (c & 0x00ff00) >> 8;
				int bf = (c & 0x0000ff);
				
				String name;
				if (useColorNames)
					name = AttributeHelper.getColorName(rf, gf, bf);
				else {
					String rs = rf + "";
					String gs = gf + "";
					String bs = bf + "";
					while (rs.length() < 3)
						rs = "0" + rs;
					while (gs.length() < 3)
						gs = "0" + gs;
					while (bs.length() < 3)
						bs = "0" + bs;
					name = rs + "_" + gs + "_" + bs;
				}
				if (!colorHistogram.containsKey(name))
					colorHistogram.put(name, 0);
				
				colorHistogram.put(name, colorHistogram.get(name) + 1);
			}
		}
		
		double count = colors.cardinality();
		double count_d = count;
		
		getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), ct, TraitCategory.INTENSITY, "color.count"),
				count_d,
				"", this, visInfo);
		
		if (createHistogram) {
			for (String color : colorHistogram.keySet()) {
				getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), ct, TraitCategory.INTENSITY,
						"color.histogram." + color),
						colorHistogram.get(color),
						"px", this, visInfo);
			}
		}
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
		return "Count number of different colors in an image. Optionally, create (named) color histogram.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("color.count", "Number of different colors in the image."),
				new CalculatedProperty("color.histogram", "Number of pixels with a given color.")
		};
	}
}
