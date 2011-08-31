package de.ipk.ag_ba.image.operations.blocks;

import ij.measure.ResultsTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class BlockPropertiesImpl implements BlockProperties {
	
	private final TreeMap<Integer, TreeMap<String, Double>> store = new TreeMap<Integer, TreeMap<String, Double>>();
	private final HashMap<String, FlexibleImage> storedImg = new HashMap<String, FlexibleImage>();
	private final HashMap<String, VolumeData> storedVol = new HashMap<String, VolumeData>();
	
	@Override
	public synchronized BlockProperty getNumericProperty(int currentPositionInPipeline, int searchIndex, Enum<?> pName) {
		String name = pName.name();
		if (searchIndex <= 0 && !store.containsKey(currentPositionInPipeline + searchIndex))
			return null;
		else {
			if (searchIndex > 0) {
				// search property
				int foundCount = 0;
				for (int index = currentPositionInPipeline; index >= 0; index--) {
					if (store.containsKey(index)) {
						if (store.get(index).containsKey(name)) {
							foundCount++;
							if (foundCount == searchIndex) {
								Double d = store.get(index).get(name);
								return new BlockProperty(d, index);
							}
						}
					}
				}
				return null;
			} else {
				Double d = store.get(currentPositionInPipeline + searchIndex).get(name);
				if (d == null)
					return null;
				else
					return new BlockProperty(d, currentPositionInPipeline + searchIndex);
			}
		}
	}
	
	@Override
	public synchronized void setNumericProperty(int position, Enum<?> name, double value) {
		if (!store.containsKey(position))
			store.put(position, new TreeMap<String, Double>());
		
		store.get(position).put(name.name(), value);
	}
	
	@Override
	public synchronized void setNumericProperty(int position, String name, double value) {
		if (!store.containsKey(position))
			store.put(position, new TreeMap<String, Double>());
		
		store.get(position).put(name, value);
	}
	
	@Override
	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Integer index : store.keySet()) {
			sb.append("BLOCK: " + index + ": \n");
			for (String property : store.get(index).keySet()) {
				sb.append("- " + property + " = " + store.get(index).get(property) + "\n");
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public synchronized int getBlockPosition() {
		return store.lastKey();
	}
	
	@Override
	public synchronized int getNumberOfBlocksWithPropertyResults() {
		return store.size();
	}
	
	@Override
	public synchronized int getNumberOfBlocksWithThisProperty(Enum<?> pName) {
		String name = pName.name();
		int foundCount = 0;
		for (int index = getBlockPosition(); index >= 0; index--) {
			if (store.containsKey(index)) {
				if (store.get(index).containsKey(name)) {
					foundCount++;
				}
			}
		}
		return foundCount;
	}
	
	@Override
	public synchronized ArrayList<BlockPropertyValue> getProperties(String search) {
		ArrayList<BlockPropertyValue> result = new ArrayList<BlockPropertyValue>();
		Collection<TreeMap<String, Double>> sv = store.values();
		if (sv != null)
			for (TreeMap<String, Double> tm : sv) {
				Set<String> ks = tm.keySet();
				if (ks != null)
					for (String key : ks) {
						if (key.startsWith(search)) {
							PropertyNames pn = null;
							try {
								pn = PropertyNames.valueOf(key);
							} catch (Exception e) {
								// ignore, not a parameter which has an enum constant
							}
							if (pn == null) {
								if (tm.get(key) != null) {
									BlockPropertyValue p = new BlockPropertyValue(key.substring(search.length()), "", tm.get(key));
									result.add(p);
								}
							} else {
								BlockPropertyValue p = new BlockPropertyValue(pn.getName(), pn.getUnit(), tm.get(key));
								result.add(p);
							}
						}
					}
			}
		return result;
	}
	
	@Override
	public synchronized void storeResults(String id_prefix, ResultsTable numericResults, int position) {
		for (int row = 0; row < numericResults.getCounter(); row++) {
			for (int col = 0; col <= numericResults.getLastColumn(); col++) {
				String id = numericResults.getColumnHeading(col);
				double val = numericResults.getValueAsDouble(col, row);
				setNumericProperty(position, id_prefix + id, val);
			}
		}
	}
	
	@Override
	public synchronized void printAnalysisResults() {
		for (BlockPropertyValue bpv : getProperties("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			System.out.println(bpv.getName() + "=" + StringManipulationTools.formatNumber(bpv.getValue(), "#.###") + " " + bpv.getUnit());
		}
	}
	
	@Override
	public void setImage(String id, FlexibleImage image) {
		storedImg.put(id, image);
	}
	
	@Override
	public FlexibleImage getImage(String id) {
		return storedImg.get(id);
	}
	
	@Override
	public void setVolume(String string, LoadedVolumeExtension volume) {
		storedVol.put(string, volume);
	}
	
	@Override
	public Set<String> getVolumeNames() {
		return storedVol.keySet();
	}
	
	@Override
	public VolumeData getVolume(String string) {
		return storedVol.get(string);
	}
}
