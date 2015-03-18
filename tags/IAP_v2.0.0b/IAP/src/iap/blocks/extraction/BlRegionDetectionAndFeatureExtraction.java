package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import org.GapList;
import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape
 */
@Deprecated
public class BlRegionDetectionAndFeatureExtraction extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	@Override
	protected Image processVISmask() {
		
		// options
		boolean markResults = getBoolean("Mark Results", true);
		boolean saveResults = getBoolean("Save Results", true);
		boolean trackResults = getBoolean("Track Results", true);
		int minimumSizeOfRegion = getInt("Minimum Region Size", 15);
		
		int background = ImageOperation.BACKGROUND_COLORint;
		
		Image img = input().masks().vis();
		
		if (img == null)
			return null;
		
		// Region detection
		ClusterDetection cd = new ClusterDetection(img, background);
		cd.detectClusters();
		
		// get features (size, angle, center of gravity)
		GapList<Feature> featureList = getFeaturesFromClusters(cd, minimumSizeOfRegion);
		Image marked = saveAndMarkResults(img, featureList, markResults, saveResults, input().images().getVisInfo());
		
		return marked;
	}
	
	private GapList<Feature> getFeaturesFromClusters(ClusterDetection cd, int minimumSizeOfRegion) {
		GapList<Feature> flist = new GapList<Feature>();
		boolean[] deleted;
		
		int[] areas = cd.getClusterSize();
		Vector2i[] CoGs = cd.getClusterCenterPoints();
		
		deleted = new boolean[areas.length];
		
		for (int i = 0; i < areas.length; i++) {
			if (areas[i] > minimumSizeOfRegion) {
				Vector2i pos = CoGs[i];
				Feature tempFeature = new Feature(new Integer(pos.x), new Integer(pos.y));
				tempFeature.addFeature("size", areas[i], FeatureObjectType.NUMERIC);
				flist.add(tempFeature);
			}
			else
				deleted[i] = true;
		}
		
		return flist;
	}
	
	private Image saveAndMarkResults(Image img, GapList<Feature> featureList, boolean markResults, boolean saveResults, ImageData imageRef) {
		
		boolean saveResultObject = true;
		
		if (markResults) {
			ImageCanvas ic = new ImageCanvas(img);
			for (Feature p : featureList) {
				ic.drawRectangle((int) p.getPosition().getX() - 10, (int) p.getPosition().getY() - 10, 21, 21, Color.RED, 3);
			}
			img = ic.getImage();
		}
		
		if (saveResults) {
			CameraPosition pos = optionsAndResults.getCameraPosition();
			// save leaf count
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower.count"), featureList.size(), "flower", this, imageRef);
			
			// save x and y position
			int num = 1;
			for (Feature p : featureList) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".position.x"),
						(int) p.getPosition().getX(), "flower", this, imageRef);
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".position.y"),
						(int) p.getPosition().getY(), "flower", this, imageRef);
				num++;
			}
		}
		
		if (saveResultObject) {
			String name = this.getClass().getSimpleName();
			name = name.toLowerCase();
			getResultSet().setObjectResult(getBlockPosition(), "leaftiplist" + "_" + img.getCameraType(), featureList);
		}
		
		return img;
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
		return "Detect Image Regions (Objects)";
	}
	
	@Override
	public String getDescription() {
		return "Detects Image Region and extract Features.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("flower.count", "Number of detected flowers."),
				new CalculatedProperty("flower.*.position.x", "Position (X-axis) of a detected flower."),
				new CalculatedProperty("flower.*.position.y", "Position (Y-axis) of a detected flower."),
		};
	}
}
