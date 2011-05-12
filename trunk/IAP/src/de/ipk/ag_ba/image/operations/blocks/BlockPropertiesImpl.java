package de.ipk.ag_ba.image.operations.blocks;

import java.util.ArrayList;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;

public class BlockPropertiesImpl implements BlockProperties {
	
	private final TreeMap<Integer, TreeMap<String, Double>> store = new TreeMap<Integer, TreeMap<String, Double>>();
	
	@Override
	public BlockProperty getNumericProperty(int currentPositionInPipeline, int searchIndex, Enum<?> pName) {
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
	public void setNumericProperty(int position, Enum<?> name, double value) {
		if (!store.containsKey(position))
			store.put(position, new TreeMap<String, Double>());
		
		store.get(position).put(name.name(), value);
	}
	
	@Override
	public String toString() {
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
	public int getBlockPosition() {
		return store.lastKey();
	}
	
	@Override
	public int getNumberOfBlocksWithPropertyResults() {
		return store.size();
	}
	
	@Override
	public int getNumberOfBlocksWithThisProperty(Enum<?> pName) {
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
	public ArrayList<BlockPropertyValue> getProperties(String search) {
		ArrayList<BlockPropertyValue> result = new ArrayList<BlockPropertyValue>();
		
		for (TreeMap<String, Double> tm : store.values()) {
			for (String key : tm.keySet()) {
				if (key.startsWith(search)) {
					PropertyNames pn = PropertyNames.valueOf(key);
					if (pn == null)
						continue;
					
					BlockPropertyValue p = new BlockPropertyValue(pn.getName(), pn.getUnit(), tm.get(key));
					result.add(p);
				}
			}
		}
		return result;
	}
}
