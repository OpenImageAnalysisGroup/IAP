package de.ipk.ag_ba.image.operations.blocks.properties;

import ij.measure.ResultsTable;

import java.util.ArrayList;
import java.util.Set;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.structures.FlexibleImage;
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
			int searchIndex, Enum<?> name);
	
	/**
	 * @param position
	 *           0 == current block property
	 */
	public void setNumericProperty(int position, Enum<?> name, double value);
	
	public int getBlockPosition();
	
	public int getNumberOfBlocksWithPropertyResults();
	
	public int getNumberOfBlocksWithThisProperty(Enum<?> pName);
	
	/**
	 * @see PropertyNames
	 */
	public ArrayList<BlockPropertyValue> getPropertiesSearch(String search);
	
	public ArrayList<BlockPropertyValue> getPropertiesExactMatch(String search);
	
	void setNumericProperty(int position, String name, double value);
	
	void storeResults(String id_prefix, ResultsTable numericResults,
			int position);
	
	public void printAnalysisResults();
	
	public void setImage(String id, FlexibleImage image);
	
	public FlexibleImage getImage(String id);
	
	public void setVolume(String string, VolumeData volume);
	
	public Set<String> getVolumeNames();
	
	public VolumeData getVolume(String string);
	
	public void addImagePostProcessor(RunnableOnImageSet runnableOnImageSet);
	
	public ArrayList<RunnableOnImageSet> getStoredPostProcessors(
			ImageConfiguration rgbside);
}
