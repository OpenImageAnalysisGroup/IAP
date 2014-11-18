package de.ipk.ag_ba.image.operations.blocks.properties;

import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImage;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.BlockResultObject;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public interface BlockResultSet {
	
	/**
	 * @param searchIndex
	 *           0 == current block property, -1 = last property from any
	 *           previous block, -2 == property from block before last block, 1
	 *           == search any previous value, 2 == search any previous value
	 *           which has been stored before any previous value.
	 * @param name
	 *           property name
	 * @return value NULL if no previous value is available, otherwise according
	 *         value.
	 */
	public BlockResult searchNumericResult(int currentPositionInPipeline,
			int searchIndex, Trait name);
	
	/**
	 * @param position
	 *           0 == current block property
	 */
	public void setNumericResult(int currentPositionInPipeline, Trait name, double value, CalculatesProperties descriptionProvider, NumericMeasurement3D imageRef);
	
	void setNumericResult(int currentPositionInPipeline, Trait name, double value, String unit, CalculatesProperties descriptionProvider,
			NumericMeasurement3D imageRef);
	
	public int getBlockPosition();
	
	public int getNumberOfBlocksWithNumericResults();
	
	public int getNumberOfBlocksWithGivenName(String pName);
	
	/**
	 * @see PropertyNames
	 */
	public ArrayList<BlockResultValue> searchResults(String search);
	
	public ArrayList<BlockResultValue> searchResults(Trait trait);
	
	public ArrayList<BlockResultValue> searchResults(boolean exact, String search, boolean removeReturnedValue);
	
	void storeResults(CameraPosition cp, CameraType ct, TraitCategory cat,
			ResultsTableWithUnits numericResults,
			int position, CalculatesProperties description, NumericMeasurement3D imageRef);
	
	void storeResults(CameraPosition cp, CameraType ct, TraitCategory cat, String id_postfix,
			ResultsTableWithUnits numericResults,
			int position, CalculatesProperties description, NumericMeasurement3D imageRef);
	
	public void printAnalysisResults();
	
	public void setImage(int currentPositionInPipeline, String id, LoadedImage image, boolean deleteAtPipelineCompletion);
	
	public void setImage(int currentPositionInPipeline, String id, ImageAndImageData image, boolean deleteAtPipelineCompletion);
	
	public Image getImage(int currentPositionInPipeline, String id);
	
	public void setVolume(String string, VolumeData volume);
	
	public Set<String> getVolumeNames();
	
	public VolumeData getVolume(String string);
	
	public void addImagePostProcessor(RunnableOnImageSet runnableOnImageSet);
	
	/**
	 * Returns the relevant list of post processors.
	 */
	public ArrayList<RunnableOnImageSet> getStoredPostProcessors(CameraType imageConfig);
	
	public boolean isNumericStoreEmpty();
	
	/**
	 * @param imageType
	 *           Camera type (vis/fluo/nir/ir).
	 */
	public void addImagePostProcessor(CameraType cameraType, RunnableOnImage runnableOnImage, RunnableOnImage runnableOnMask);
	
	public void clearStoredPostprocessors();
	
	public TreeMap<Integer, TreeMap<String, ImageAndImageData>> getImages();
	
	public void setImages(TreeMap<Integer, TreeMap<String, ImageAndImageData>> storedImages);
	
	public void addImagePostProcessor(ImageConfiguration imgConfig, RunnableOnImage roi, RunnableOnImage runnableOnMask);
	
	BlockResultObject searchObjectResult(int currentPositionInPipeline, int searchIndex, String pName);
	
	void setObjectResult(int currentPositionInPipeline, String name, Object value);
	
	public void removeResultObject(BlockResultObject result1);
	
	ImageAndImageData getImage(String id);
	
	public void clearNotUsedResults();
	
}
