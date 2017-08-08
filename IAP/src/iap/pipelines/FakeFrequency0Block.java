package iap.pipelines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.ImageAnalysisBlock;

public class FakeFrequency0Block implements ImageAnalysisBlock {
	
	private ImageAnalysisBlock template;
	
	public FakeFrequency0Block(ImageAnalysisBlock template) {
		this.template = template;
	}
	
	@Override
	public int compareTo(ImageAnalysisBlock o) {
		return template.compareTo(o);
	}
	
	@Override
	public void setInputAndOptions(String well, MaskAndImageSet input, ImageProcessorOptionsAndResults options, BlockResultSet settings, int blockPositionInPipeline, int blockFrequencyIndex, ImageStack debugStack) {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public MaskAndImageSet process() throws InterruptedException {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(TreeMap<String, TreeMap<Long, Double>> plandId2time2waterData, TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> summaryResult, BackgroundTaskStatusProviderSupportingExternalCall optStatus, CalculatesProperties propertyCalculator) throws InterruptedException {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public void setPreventDebugValues(boolean preventSecondShowingOfDebugWindows) {
		// empty
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public BlockType getBlockType() {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public String getName() {
		return template.getName();
	}
	
	@Override
	public String getDescription() {
		return template.getDescription();
	}
	
	@Override
	public String getDescriptionForParameters() {
		return template.getDescriptionForParameters();
	}
	
	@Override
	public boolean isChangingImages() {
		throw new RuntimeException("method not supported for this block instance");
	}
	
	@Override
	public int getBlockFrequencyIndex() {
		return 0; // MAIN FUNCTION OF THIS CLASS IS TO RETURN THIS VALUE
	}
	
	public ImageAnalysisBlock getTemplateObject() {
		return template;
	}
}
