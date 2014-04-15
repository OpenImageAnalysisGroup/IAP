package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import ij.gui.Roi;

import java.awt.Rectangle;
import java.util.HashSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlColorCorrectionNir extends AbstractSnapshotAnalysisBlock {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processNIRimage() {
		if (input().images().nir() != null) {
			Image nir = input().images().nir();
			double blurRadius = getDouble("Blur Radius", 100.0);
			return process(nir, blurRadius);
		} else
			return input().images().nir();
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() != null) {
			Image nir = input().masks().nir();
			double blurRadius = getDouble("Blur Radius", 100.0);
			return process(nir, blurRadius);
		} else
			return input().masks().nir();
	}
	
	/**
	 * @param Saturated
	 *           (for Normalization) - 0.35 is equivalent to ImageJ´s 'Adjust Brightness/Contrast' method
	 * @return
	 */
	private Image process(Image image, double blurRadius) {
		String nm = optionsAndResults.getStringSettingRadio(this, "Mode", "Normalization",
				StringManipulationTools.getStringListFromArray(new String[] { "Normalization", "Equalization" }));
		ImageOperation image_io = image.io();
		ImageOperation filteredImage = image_io.copy();
		Roi bb = filteredImage.getBoundingBox();
		Rectangle br = bb.getBounds();
		filteredImage = filteredImage.crop(bb);
		filteredImage = filteredImage.blurImageJ(blurRadius).invert();
		int borderSizeLeftRight = (image.getWidth() - br.width) / 2;
		int borderSizeTopBottom = (image.getHeight() - br.height) / 2;
		return filteredImage
				.addBorder(borderSizeLeftRight, borderSizeTopBottom, (int) br.getMinX() - borderSizeLeftRight, (int) br.getMinY() - borderSizeTopBottom,
						ImageOperation.BACKGROUND_COLORint)
				.add(image_io.getImage())
				.crop(bb)
				.histogramEqualisation(nm.equalsIgnoreCase("Normalization"), getDouble("Saturated (for Normalization)", 0.35))
				.addBorder(borderSizeLeftRight, borderSizeTopBottom, (int) br.getMinX() - borderSizeLeftRight, (int) br.getMinY() - borderSizeTopBottom,
						ImageOperation.BACKGROUND_COLORint)
				.getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Background Correction NIR-Image";
	}
	
	@Override
	public String getDescription() {
		return "Corrects illumination of the Nir-image by modelling shading by gaussian blur.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul>" +
				"<li>Saturated (for Normalization) - overall percentage of the most bright and most dark pixels which are used for the threshold determination." +
				"</ul>";
	}
}
