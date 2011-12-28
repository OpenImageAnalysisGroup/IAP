package de.ipk.ag_ba.image.operations.blocks;

import ij.measure.ResultsTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.StringManipulationTools;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class BlockResults implements BlockResultSet {
	
	private final TreeMap<Integer, TreeMap<String, Double>> storedNumerics = new TreeMap<Integer, TreeMap<String, Double>>();
	private final HashMap<String, FlexibleImage> storedImages = new HashMap<String, FlexibleImage>();
	private final HashMap<String, VolumeData> storedVolumes = new HashMap<String, VolumeData>();
	private final ArrayList<RunnableOnImageSet> storedPostProcessors = new ArrayList<RunnableOnImageSet>();
	
	@Override
	public synchronized BlockProperty getNumericProperty(
			int currentPositionInPipeline, int searchIndex, Enum<?> pName) {
		String name = pName.name();
		if (searchIndex <= 0
				&& !storedNumerics.containsKey(currentPositionInPipeline
						+ searchIndex))
			return null;
		else {
			if (searchIndex > 0) {
				// search property
				int foundCount = 0;
				for (int index = currentPositionInPipeline; index >= 0; index--) {
					if (storedNumerics.containsKey(index)) {
						if (storedNumerics.get(index).containsKey(name)) {
							foundCount++;
							if (foundCount == searchIndex) {
								Double d = storedNumerics.get(index).get(name);
								return new BlockProperty(d, index);
							}
						}
					}
				}
				return null;
			} else {
				Double d = storedNumerics.get(
						currentPositionInPipeline + searchIndex).get(name);
				if (d == null)
					return null;
				else
					return new BlockProperty(d, currentPositionInPipeline
							+ searchIndex);
			}
		}
	}
	
	@Override
	public synchronized void setNumericProperty(int position, Enum<?> name,
			double value) {
		if (!storedNumerics.containsKey(position))
			storedNumerics.put(position, new TreeMap<String, Double>());
		
		storedNumerics.get(position).put(name.name(), value);
	}
	
	@Override
	public synchronized void setNumericProperty(int position, String name,
			double value) {
		if (!storedNumerics.containsKey(position))
			storedNumerics.put(position, new TreeMap<String, Double>());
		
		storedNumerics.get(position).put(name, value);
	}
	
	@Override
	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Integer index : storedNumerics.keySet()) {
			sb.append("BLOCK: " + index + ": \n");
			for (String property : storedNumerics.get(index).keySet()) {
				sb.append("- " + property + " = "
						+ storedNumerics.get(index).get(property) + "\n");
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public synchronized int getBlockPosition() {
		return storedNumerics.lastKey();
	}
	
	@Override
	public synchronized int getNumberOfBlocksWithPropertyResults() {
		return storedNumerics.size();
	}
	
	@Override
	public synchronized int getNumberOfBlocksWithThisProperty(Enum<?> pName) {
		String name = pName.name();
		int foundCount = 0;
		for (int index = getBlockPosition(); index >= 0; index--) {
			if (storedNumerics.containsKey(index)) {
				if (storedNumerics.get(index).containsKey(name)) {
					foundCount++;
				}
			}
		}
		return foundCount;
	}
	
	@Override
	public synchronized ArrayList<BlockPropertyValue> getPropertiesSearch(
			String search) {
		ArrayList<BlockPropertyValue> result = new ArrayList<BlockPropertyValue>();
		Collection<TreeMap<String, Double>> sv = storedNumerics.values();
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
								// ignore, not a parameter which has an enum
								// constant
							}
							if (pn == null) {
								if (tm.get(key) != null) {
									String name = key
											.substring(search.length());
									BlockPropertyValue p = new BlockPropertyValue(
											name, getUnitFromName(name),
											tm.get(key));
									result.add(p);
								}
							} else {
								BlockPropertyValue p = new BlockPropertyValue(
										pn.getName(), pn.getUnit(), tm.get(key));
								result.add(p);
							}
						}
					}
			}
		return result;
	}
	
	@Override
	public synchronized ArrayList<BlockPropertyValue> getPropertiesExactMatch(
			String match) {
		ArrayList<BlockPropertyValue> result = new ArrayList<BlockPropertyValue>();
		Collection<TreeMap<String, Double>> sv = storedNumerics.values();
		if (sv != null)
			for (TreeMap<String, Double> tm : sv) {
				Set<String> ks = tm.keySet();
				if (ks != null)
					for (String key : ks) {
						if (key.equals(match)) {
							PropertyNames pn = null;
							try {
								pn = PropertyNames.valueOf(key);
							} catch (Exception e) {
								// ignore, not a parameter which has an enum
								// constant
							}
							if (pn == null) {
								if (tm.get(key) != null) {
									String name = key
											.substring(match.length());
									BlockPropertyValue p = new BlockPropertyValue(
											name, getUnitFromName(name),
											tm.get(key));
									result.add(p);
								}
							} else {
								BlockPropertyValue p = new BlockPropertyValue(
										pn.getName(), pn.getUnit(), tm.get(key));
								result.add(p);
							}
						}
					}
			}
		return result;
	}
	
	TreeMap<String, String> name2unit = getUnits();
	
	private String getUnitFromName(String name) {
		if (name2unit.containsKey(name))
			return name2unit.get(name);
		else
			return "";
	}
	
	private TreeMap<String, String> getUnits() {
		TreeMap<String, String> res = new TreeMap<String, String>();
		res.put("top.fluo.intensity.average", "relative / pix");
		res.put("top.fluo.normalized.histogram.bin.1.0_36", "px");
		res.put("top.fluo.normalized.histogram.bin.2.36_72", "px");
		res.put("top.fluo.normalized.histogram.bin.3.72_109", "px");
		res.put("top.fluo.normalized.histogram.bin.4.109_145", "px");
		res.put("top.fluo.normalized.histogram.bin.5.145_182", "px");
		res.put("top.fluo.normalized.histogram.bin.6.182_218", "px");
		res.put("top.fluo.normalized.histogram.bin.7.218_255", "px");
		res.put("top.ndvi", "relative");
		res.put("top.ndvi.vis.blue.intensity.average", "relative");
		res.put("top.ndvi.vis.green.intensity.average", "relative");
		res.put("top.ndvi.vis.red.intensity.average", "relative");
		res.put("top.nir.intensity.average", "relative / pix");
		res.put("top.nir.normalized.histogram.bin.1.0_36", "px");
		res.put("top.nir.normalized.histogram.bin.2.36_72", "px");
		res.put("top.nir.normalized.histogram.bin.3.72_109", "px");
		res.put("top.nir.normalized.histogram.bin.4.109_145", "px");
		res.put("top.nir.normalized.histogram.bin.5.145_182", "px");
		res.put("top.nir.normalized.histogram.bin.6.182_218", "px");
		res.put("top.nir.normalized.histogram.bin.7.218_255", "px");
		res.put("top.nir.wetness.average", "percent");
		res.put("vis.side", "images");
		res.put("fluo.top", "images");
		res.put("nir.side", "images");
		res.put("RESULT_VIS_MARKER_POS_1_LEFT_X", "null");
		res.put("", "");
		res.put("RESULT_VIS_MARKER_POS_1_RIGHT_X", "null");
		res.put("RESULT_VIS_MARKER_POS_2_LEFT_X", "null");
		res.put("", "");
		res.put("RESULT_VIS_MARKER_POS_2_RIGHT_X", "null");
		res.put("side.height", "mm");
		res.put("side.height.norm", "mm");
		res.put("side.width", "mm");
		res.put("side.width.norm", "mm");
		res.put("side.fluo.histogram.bin.1.0_36", "px");
		res.put("side.fluo.histogram.bin.2.36_72", "px");
		res.put("side.fluo.histogram.bin.3.72_109", "px");
		res.put("side.fluo.histogram.bin.4.109_145", "px");
		res.put("side.fluo.histogram.bin.5.145_182", "px");
		res.put("side.fluo.histogram.bin.6.182_218", "px");
		res.put("side.fluo.histogram.bin.7.218_255", "px");
		res.put("side.fluo.intensity.average", "relative");
		res.put("side.fluo.intensity.chlorophyl.average", "relative");
		res.put("side.fluo.intensity.phenol.average", "relative");
		res.put("side.fluo.intensity.phenol.chlorophyl.ratio", "c/p");
		res.put("side.fluo.normalized.histogram.bin.1.0_36", "px");
		res.put("side.fluo.normalized.histogram.bin.2.36_72", "px");
		res.put("side.fluo.normalized.histogram.bin.3.72_109", "px");
		res.put("side.fluo.normalized.histogram.bin.4.109_145", "px");
		res.put("side.fluo.normalized.histogram.bin.5.145_182", "px");
		res.put("side.fluo.normalized.histogram.bin.6.182_218", "px");
		res.put("side.fluo.normalized.histogram.bin.7.218_255", "px");
		res.put("side.ndvi", "relative");
		res.put("side.ndvi.vis.blue.intensity.average", "relative");
		res.put("side.ndvi.vis.green.intensity.average", "relative");
		res.put("side.ndvi.vis.red.intensity.average", "relative");
		res.put("side.nir.histogram.bin.1.0_36", "px");
		res.put("side.nir.histogram.bin.2.36_72", "px");
		res.put("side.nir.histogram.bin.3.72_109", "px");
		res.put("side.nir.histogram.bin.4.109_145", "px");
		res.put("side.nir.histogram.bin.5.145_182", "px");
		res.put("side.nir.histogram.bin.6.182_218", "px");
		res.put("side.nir.histogram.bin.7.218_255", "px");
		res.put("side.nir.intensity.average", "relative");
		res.put("side.nir.normalized.histogram.bin.1.0_36", "px");
		res.put("side.nir.normalized.histogram.bin.2.36_72", "px");
		res.put("side.nir.normalized.histogram.bin.3.72_109", "px");
		res.put("side.nir.normalized.histogram.bin.4.109_145", "px");
		res.put("side.nir.normalized.histogram.bin.5.145_182", "px");
		res.put("side.nir.normalized.histogram.bin.6.182_218", "px");
		res.put("side.nir.normalized.histogram.bin.7.218_255", "px");
		res.put("side.nir.skeleton.intensity.average", "relative");
		res.put("side.nir.wetness.average", "percent");
		res.put("side.bloom", "0/1");
		res.put("side.bloom.count", "tassel");
		res.put("side.fluo.bloom.area.size", "mm^2");
		res.put("side.leaf.count", "leafs");
		res.put("side.leaf.length.avg", "px");
		res.put("side.leaf.length.avg.norm", "mm");
		res.put("side.leaf.length.sum", "px");
		res.put("side.leaf.length.sum.norm", "mm");
		res.put("volume.iap", "px^3");
		res.put("volume.lt", "px^3");
		res.put("side.area", "px");
		res.put("side.area.norm", "mm^2");
		res.put("side.border.length", "px");
		res.put("side.border.length.norm", "mm");
		res.put("side.compactness.01", "relative");
		res.put("side.compactness.16", "relative");
		res.put("side.hull.area", "px");
		res.put("side.hull.area.norm", "mm^2");
		res.put("side.hull.centroid.x", "px");
		res.put("side.hull.centroid.x.norm", "mm");
		res.put("side.hull.centroid.y", "px");
		res.put("side.hull.centroid.y.norm", "mm");
		res.put("side.hull.circularity", "relative");
		res.put("side.hull.circumcircle.d", "px");
		res.put("side.hull.circumcircle.d.norm", "mm");
		res.put("side.hull.circumcircle.x", "px");
		res.put("side.hull.circumcircle.x.norm", "mm");
		res.put("side.hull.circumcircle.y", "px");
		res.put("side.hull.circumcircle.y.norm", "mm");
		res.put("side.hull.fillgrade", "percent");
		res.put("side.hull.points", "hullpoints");
		res.put("fluo.side", "images");
		res.put("side.leaf.count.max", "leafs");
		res.put("side.leaf.count.median", "leafs");
		res.put("side.leaf.length.sum.max", "px");
		res.put("side.leaf.length.sum.norm.max", "mm");
		res.put("RESULT_VIS_MARKER_POS_3_LEFT_X", "null");
		res.put("", "");
		res.put("RESULT_VIS_MARKER_POS_3_RIGHT_X", "null");
		res.put("top.main.axis.normalized.distance.avg", "mm");
		res.put("top.main.axis.rotation", "degree");
		res.put("top.area", "px");
		res.put("top.border.length", "px");
		res.put("top.compactness.01", "relative");
		res.put("top.compactness.16", "relative");
		res.put("top.hull.area", "px");
		res.put("top.hull.centroid.x", "px");
		res.put("top.hull.centroid.y", "px");
		res.put("top.hull.circularity", "relative");
		res.put("top.hull.circumcircle.d", "px");
		res.put("top.hull.circumcircle.x", "px");
		res.put("top.hull.circumcircle.y", "px");
		res.put("top.hull.fillgrade", "percent");
		res.put("top.hull.points", "hullpoints");
		res.put("side.leaf.count.best", "leafs");
		res.put("top.fluo.intensity.chlorophyl.average", "relative");
		res.put("top.fluo.intensity.phenol.average", "relative");
		res.put("top.fluo.intensity.phenol.chlorophyl.ratio", "relative");
		
		return res;
	}
	
	@Override
	public synchronized void storeResults(String id_prefix,
			ResultsTable numericResults, int position) {
		for (int row = 0; row < numericResults.getCounter(); row++) {
			for (int col = 0; col <= numericResults.getLastColumn(); col++) {
				String id = numericResults.getColumnHeading(col);
				double val = numericResults.getValueAsDouble(col, row);
				if (!Double.isNaN(val))
					setNumericProperty(position, id_prefix + id, val);
			}
		}
	}
	
	@Override
	public synchronized void printAnalysisResults() {
		for (BlockPropertyValue bpv : getPropertiesSearch("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			System.out.println(bpv.getName()
					+ "="
					+ StringManipulationTools.formatNumber(bpv.getValue(),
							"#.###") + " " + bpv.getUnit());
		}
	}
	
	@Override
	public void setImage(String id, FlexibleImage image) {
		storedImages.put(id, image);
	}
	
	@Override
	public FlexibleImage getImage(String id) {
		FlexibleImage res = storedImages.get(id);
		storedImages.remove(id);
		return res;
	}
	
	@Override
	public void setVolume(String string, VolumeData volume) {
		storedVolumes.put(string, volume);
	}
	
	@Override
	public Set<String> getVolumeNames() {
		return storedVolumes.keySet();
	}
	
	@Override
	public VolumeData getVolume(String string) {
		return storedVolumes.get(string);
	}
	
	@Override
	public synchronized void addImagePostProcessor(
			RunnableOnImageSet runnableOnImageSet) {
		storedPostProcessors.add(runnableOnImageSet);
	}
	
	@Override
	public synchronized ArrayList<RunnableOnImageSet> getStoredPostProcessors(
			ImageConfiguration conf) {
		ArrayList<RunnableOnImageSet> res = new ArrayList<RunnableOnImageSet>();
		for (RunnableOnImageSet ros : storedPostProcessors) {
			if (ros.getConfig() == conf)
				res.add(ros);
		}
		return res;
	}
}
