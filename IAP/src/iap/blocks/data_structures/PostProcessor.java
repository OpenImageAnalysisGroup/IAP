package iap.blocks.data_structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;

/**
 * It is recommended to base your code on AbstractPostProcessor.
 * 
 * @author Christian Klukas
 */
public interface PostProcessor {
	public ArrayList<BlockResultValue> postProcessCalculatedProperties(long time, String tray);
	
	public TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> getAllResults();
}
