package iap.blocks.image_analysis_tools.cvppp_2014;

import iap.blocks.image_analysis_tools.leafClustering.Feature;
import ij.ImagePlus;
import ij.process.ImageConverter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.Colors;
import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

public class LeafSegmentationCvppp {
	
	Image[] segmentedImages;
	Image[] segmentedAndNotSplitImages;
	HashMap<String, ArrayList<Feature>> centerPoints = new HashMap<String, ArrayList<Feature>>();
	String outputPath;
	boolean markAndSaveResultImages = false;
	boolean evaluation = false;
	double overallScore;
	private Image labeledResultImage;
	
	public LeafSegmentationCvppp(HashMap<String, ArrayList<Feature>> centerPoints,
			Image[] segmentedImages,
			Image[] segmentedAndNotSplitImages) {
		this.segmentedImages = segmentedImages;
		this.segmentedAndNotSplitImages = segmentedAndNotSplitImages;
		this.centerPoints = centerPoints;
		this.outputPath = outputPath + this.getClass().getName() + "/";
	}
	
	public Image[] segmentLeaves() throws InterruptedException {
		
		final Image[] segmentedLeavesImages = new Image[segmentedImages.length];
		
		for (int idx = 0; idx < segmentedImages.length; idx++) {
			final Image segmented = segmentedImages[idx];
			final Image segmentedAndNotSplit = segmentedAndNotSplitImages[idx];
			final int fi = idx;
			runS(segmentedLeavesImages, fi, segmented, segmentedAndNotSplit);
		}
		
		return segmentedLeavesImages;
	}
	
	private void runS(Image[] segmentedLeavesImages, int idx, Image segmented, Image segmentedAndNotSplit) {
		// Load only one image => 1
		String id = "1";
		
		Firewalk fw = new Firewalk(segmented, false);
		fw.setSimpleFillMode(true);
		ArrayList<Feature> leafCenterPoints = centerPoints.get(id);
		ArrayList<Vector2i> startPoints = new ArrayList<Vector2i>();
		
		for (Feature p : leafCenterPoints) {
			startPoints.add(new Vector2i((int) p.getPosition().getX(), (int) p.getPosition().getY()));
		}
		ArrayList<Color> startColors = Colors.get(startPoints.size(), 1);
		
		Image seg = fw.igniteFireAndBurnColorsDown(startPoints, startColors, 4, 0);// A2: 6, 1); // 4, 2// ? , ? // A1: 4, 0
		
		seg = seg.io().medianColorFilterForBackgroundPixels(Color.BLACK.getRGB(), segmentedAndNotSplit).getImage();
		segmentedLeavesImages[idx] = seg;
		
		seg = seg.io().replaceColor(ImageOperation.BACKGROUND_COLORint, Color.BLACK.getRGB()).getImage().setFilename(id);
		
		ImagePlus ip = seg.getAsImagePlus();
		ImageConverter imgc = new ImageConverter(ip);
		imgc.convertRGBtoIndexedColor(256);
		labeledResultImage = new Image(ip);
	}
	
	public Image getResultImage() {
		return labeledResultImage;
	}
}
