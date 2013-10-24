package de.ipk.ag_ba.image.operations.blocks.properties;

import iap.blocks.unused.RunnableOnImage;

import java.util.ArrayList;
import java.util.Set;

import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public interface BlockResultSet {
	
	/**
	 * @param searchIndex
	 *           0 == current block property, -1 = last property from any
	 *           previous block, -2 == property from block before last block 1
	 *           == search any previous value, 2 == search any previous value
	 *           which has been stored before any previous value.
	 * @param name
	 *           property name
	 * @return value NULL if no previous value is available, otherwise according
	 *         value.
	 */
	public BlockProperty getNumericProperty(int currentPositionInPipeline,
			int searchIndex, String name);
	
	/**
	 * @param position
	 *           0 == current block property
	 */
	public void setNumericProperty(int position, String name, double value);
	
	public int getBlockPosition();
	
	public int getNumberOfBlocksWithPropertyResults();
	
	public int getNumberOfBlocksWithThisProperty(String pName);
	
	/**
	 * @see PropertyNames
	 */
	public ArrayList<BlockPropertyValue> getPropertiesSearch(String search);
	
	public ArrayList<BlockPropertyValue> getPropertiesExactMatch(String search);
	
	void setNumericProperty(int position, String name, double value, String unit);
	
	void storeResults(String id_prefix,
			ResultsTableWithUnits numericResults,
			int position);
	
	void storeResults(String id_prefix, String id_postfix,
			ResultsTableWithUnits numericResults,
			int position);
	
	public void printAnalysisResults();
	
	public void setImage(String id, Image image);
	
	public Image getImage(String id);
	
	public void setVolume(String string, VolumeData volume);
	
	public Set<String> getVolumeNames();
	
	public VolumeData getVolume(String string);
	
	public void addImagePostProcessor(RunnableOnImageSet runnableOnImageSet);
	
	/**
	 * Returns the relevant list of post processors.
	 */
	public ArrayList<RunnableOnImageSet> getStoredPostProcessors(ImageConfiguration imageConfig);
	
	public void clearStore();
	
	public boolean isNumericStoreEmpty();
	
	public void addImagePostProcessor(ImageConfiguration imageType, RunnableOnImage runnableOnImage, RunnableOnImage runnableOnMask);
	
	public void clearStoredPostprocessors();
}
