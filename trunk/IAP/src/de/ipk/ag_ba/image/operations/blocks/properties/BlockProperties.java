package de.ipk.ag_ba.image.operations.blocks.properties;

import java.util.ArrayList;

import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;

public interface BlockProperties {
	
	/**
	 * @param searchIndex
	 *           0 == current block property, -1 = last property from any previous block, -2 == property from block before last block
	 *           1 == search any previous value, 2 == search any previous value which has been stored before any previous value.
	 * @param name
	 *           property name
	 * @return value NULL if no previous value is available, otherwise according value.
	 */
	public BlockProperty getNumericProperty(int currentPositionInPipeline, int searchIndex, Enum<?> name);
	
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
	public ArrayList<BlockPropertyValue> getProperties(String search);
	
}
