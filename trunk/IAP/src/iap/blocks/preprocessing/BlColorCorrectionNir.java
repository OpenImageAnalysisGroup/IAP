package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

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
			double blurRadius = getDouble("Blur Radius", 10.0);
			return process(nir, blurRadius);
		} else
			return input().images().nir();
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() != null) {
			Image nir = input().masks().nir();
			double blurRadius = getDouble("Blur Radius", 10.0);
			return process(nir, blurRadius);
		} else
			return input().masks().nir();
	}
	
	private Image process(Image image, double blurRadius) {
		ImageOperation image_io = image.io().histogramEqualisation(true);
		ImageOperation blured = image_io.copy();
		double avgValBlured = blured.getMedian();
		blured = blured.blur(blurRadius).subtract(avgValBlured).invert();
		return blured.add(image_io.getImage()).histogramEqualisation(true).getImage();
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
}
