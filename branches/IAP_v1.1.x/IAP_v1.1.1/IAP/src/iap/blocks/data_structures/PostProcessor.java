package iap.blocks.data_structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;

/**
 * It is recommended to base your code on AbstractPostProcessor.
 * 
 * @author Christian Klukas
 */
public interface PostProcessor {
	public ArrayList<BlockPropertyValue> postProcessCalculatedProperties(long time, int tray);
	
	public TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> getAllResults();
}
