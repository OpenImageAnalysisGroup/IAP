package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

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
		Image nir = input().images().nir();
		double blurRadius = getDouble("Blur Radius", 10.0);
		return process(nir, blurRadius);
	}
	
	@Override
	protected Image processNIRmask() {
		Image nir = input().masks().nir();
		double blurRadius = getDouble("Blur Radius", 10.0);
		return process(nir, blurRadius);
	}
	
	private Image process(Image image, double blurRadius) {
		Image blured = image.copy();
		blured = blured.io().histogramEqualisation().blur(blurRadius).invert().getImage();
		image = image.io().histogramEqualisation().getImage();
		return blured.io().add(image).histogramEqualisation().getImage();
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
