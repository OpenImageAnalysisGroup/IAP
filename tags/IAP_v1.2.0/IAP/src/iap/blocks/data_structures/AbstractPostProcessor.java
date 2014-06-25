package iap.blocks.data_structures;

import iap.blocks.extraction.ConfigNameAndAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;

/**
 * @author Christian Klukas
 */
public abstract class AbstractPostProcessor implements PostProcessor {
	
	private final TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot;
	
	public AbstractPostProcessor(TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot) {
		this.time2allResultsForSnapshot = time2allResultsForSnapshot;
	}
	
	@Override
	public TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> getAllResults() {
		return time2allResultsForSnapshot;
	}
	
	protected Set<Long> getAllTimePoints() {
		return time2allResultsForSnapshot.keySet();
	}
	
	protected ArrayList<BlockResultValue> getCalculatedValues(String name, boolean exactSearch,
			long time, Integer tray, String angleConfigName) {
		ArrayList<BlockResultValue> result = new ArrayList<BlockResultValue>();
		TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
		for (String key : allResultsForSnapshot.keySet()) {
			if (angleConfigName != null && !key.equals(angleConfigName))
				continue;
			BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
			for (BlockResultValue v : rt.searchResults(exactSearch, name, false)) {
				result.add(v);
			}
		}
		return result;
	}
	
	protected ArrayList<BlockResultValue> getCalculatedValues(String name, boolean exactSearch,
			long time, Integer tray) {
		return getCalculatedValues(name, exactSearch, time, tray, null);
	}
	
	protected ArrayList<ConfigNameAndAngle> getSideAngles(long time, int tray) {
		ArrayList<ConfigNameAndAngle> res = new ArrayList<ConfigNameAndAngle>();
		for (String dc : time2allResultsForSnapshot.get(time).keySet()) {
			if (dc.startsWith(AbstractPhenotypingTask._2ND_SIDE)) {
				double d = Double.parseDouble(dc.substring(dc.indexOf(";") + ";".length()));
				res.add(new ConfigNameAndAngle(dc, d));
				
			}
		}
		return res;
	}
}
