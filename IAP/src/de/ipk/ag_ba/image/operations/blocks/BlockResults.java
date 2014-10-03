package de.ipk.ag_ba.image.operations.blocks;

import iap.blocks.data_structures.RunnableOnImage;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.pipelines.ImageProcessorOptionsAndResults;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResult;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageInMemory;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author Christian Klukas
 */
public class BlockResults implements BlockResultSet {
	
	private final TreeMap<Integer, TreeMap<String, Double>> storedNumerics = new TreeMap<Integer, TreeMap<String, Double>>();
	private final TreeMap<Integer, TreeMap<String, ThreadSafeOptions>> storedObjects = new TreeMap<Integer, TreeMap<String, ThreadSafeOptions>>();
	private TreeMap<Integer, TreeMap<String, ImageData>> storedImages = new TreeMap<Integer, TreeMap<String, ImageData>>();
	private final HashMap<String, VolumeData> storedVolumes = new HashMap<String, VolumeData>();
	private final ArrayList<RunnableOnImageSet> storedPostProcessors = new ArrayList<RunnableOnImageSet>();
	private final Double cameraAngle;
	
	public BlockResults(Double cameraAngle) {
		this.cameraAngle = cameraAngle;
	}
	
	@Override
	public synchronized BlockResult searchNumericResult(
			int currentPositionInPipeline, int searchIndex, String pName) {
		return searchNumericResult(currentPositionInPipeline, searchIndex, pName, true);
	}
	
	@Override
	public synchronized BlockResultObject searchObjectResult(
			int currentPositionInPipeline, int searchIndex, String pName) {
		return (BlockResultObject) searchNumericResult(currentPositionInPipeline, searchIndex, pName, false);
	}
	
	public synchronized BlockResult searchNumericResult(
			int currentPositionInPipeline, int searchIndex, String pName, boolean searchNumericValue_true_searchObject_false) {
		String name = pName;
		if (searchIndex <= 0 &&
				((searchNumericValue_true_searchObject_false && !storedNumerics.containsKey(currentPositionInPipeline + searchIndex))
				|| (!searchNumericValue_true_searchObject_false && !storedObjects.containsKey(currentPositionInPipeline + searchIndex))))
			return null;
		else {
			if (searchIndex > 0) {
				// search property
				int foundCount = 0;
				for (int index = currentPositionInPipeline; index >= 0; index--) {
					if (searchNumericValue_true_searchObject_false) {
						if (storedNumerics.containsKey(index)) {
							if (storedNumerics.get(index).containsKey(name)) {
								foundCount++;
								if (foundCount == searchIndex) {
									Double d = storedNumerics.get(index).get(name);
									return new BlockResult(d, index);
								}
							}
						}
					} else {
						if (storedObjects.containsKey(index)) {
							if (storedObjects.get(index).containsKey(name)) {
								foundCount++;
								if (foundCount == searchIndex) {
									ThreadSafeOptions d = storedObjects.get(index).get(name);
									return new BlockResultObject((String) d.getParam(0, null), d.getParam(1, null), index);
								}
							}
						}
					}
				}
				return null;
			} else {
				if (searchNumericValue_true_searchObject_false) {
					Double d = storedNumerics.get(
							currentPositionInPipeline + searchIndex).get(name);
					if (d == null)
						return null;
					else
						return new BlockResult(d, currentPositionInPipeline + searchIndex);
				} else {
					ThreadSafeOptions d = storedObjects.get(
							currentPositionInPipeline + searchIndex).get(name);
					if (d == null)
						return null;
					else
						return new BlockResultObject((String) d.getParam(0, null), d.getParam(1, null), currentPositionInPipeline + searchIndex);
				}
			}
		}
	}
	
	@Override
	public synchronized void setNumericResult(int position, String name,
			double value) {
		if (!storedNumerics.containsKey(position))
			storedNumerics.put(position, new TreeMap<String, Double>());
		
		storedNumerics.get(position).put(name, value);
	}
	
	@Override
	public synchronized void setNumericResult(int position, String name,
			double value, String unit) {
		if (!storedNumerics.containsKey(position))
			storedNumerics.put(position, new TreeMap<String, Double>());
		
		storedNumerics.get(position).put(name, value);
		
		if (unit != null) {
			name2unit.put(name, unit);
			if (name.startsWith("RESULT_")) {
				name2unit.put(name.substring("RESULT_".length()), unit);
			}
		}
	}
	
	@Override
	public synchronized void setObjectResult(int position, String name,
			Object value) {
		if (!storedObjects.containsKey(position))
			storedObjects.put(position, new TreeMap<String, ThreadSafeOptions>());
		ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setParam(0, name);
		tso.setParam(1, value);
		
		storedObjects.get(position).put(name, tso);
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
		sb.append("\n");
		for (Integer index : storedObjects.keySet()) {
			sb.append("BLOCK: " + index + ": \n");
			for (String property : storedObjects.get(index).keySet()) {
				sb.append("- " + property + " = "
						+ storedObjects.get(index).get(property).getParam(0, null) + "=" + storedObjects.get(index).get(property).getParam(1, null) + "\n");
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public synchronized int getBlockPosition() {
		return storedNumerics.lastKey();
	}
	
	@Override
	public synchronized int getNumberOfBlocksWithNumericResults() {
		return storedNumerics.size();
	}
	
	@Override
	public synchronized int getNumberOfBlocksWithGivenName(String pName) {
		String name = pName;
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
	public synchronized ArrayList<BlockResultValue> searchResults(boolean exact,
			String search, boolean removeReturnedValue) {
		ArrayList<BlockResultValue> result = new ArrayList<BlockResultValue>();
		{
			// analyse numeric store
			Collection<TreeMap<String, Double>> sv = storedNumerics.values();
			if (sv != null)
				for (TreeMap<String, Double> tm : sv) {
					String toRemove = null;
					Set<String> ks = tm.keySet();
					if (ks != null)
						for (String key : ks) {
							if ((!exact && key.startsWith(search)) || (exact && key.equals(search))) {
								PropertyNames pn = null;
								try {
									pn = PropertyNames.valueOf(key);
								} catch (Exception e) {
									// ignore, not a parameter which has an enum
									// constant
								}
								if (pn == null) {
									if (tm.get(key) != null) {
										String name = key.substring(search.length());
										BlockResultValue p = new BlockResultValue(
												name, getUnitFromName(key),
												tm.get(key), cameraAngle);
										result.add(p);
										toRemove = key;
									}
								} else {
									BlockResultValue p = new BlockResultValue(
											pn.getName(null), pn.getUnit(), tm.get(key), cameraAngle);
									result.add(p);
									toRemove = key;
								}
							}
						}
					if (toRemove != null && removeReturnedValue)
						tm.remove(toRemove);
				}
		}
		{
			// analyse image store
			Collection<TreeMap<String, ImageData>> sv = storedImages.values();
			if (sv != null)
				for (TreeMap<String, ImageData> tm : sv) {
					String toRemove = null;
					Set<String> ks = tm.keySet();
					if (ks != null)
						for (String key : ks) {
							if ((!exact && key.startsWith(search)) || (exact && key.equals(search))) {
								PropertyNames pn = null;
								try {
									pn = PropertyNames.valueOf(key);
								} catch (Exception e) {
									// ignore, not a parameter which has an enum
									// constant
								}
								if (pn == null) {
									if (tm.get(key) != null) {
										String name = key.substring(search.length());
										BlockResultValue p = new BlockResultValue(name, tm.get(key));
										result.add(p);
										toRemove = key;
									}
								} else {
									BlockResultValue p = new BlockResultValue(pn.getName(null), tm.get(key));
									result.add(p);
									toRemove = key;
								}
							}
						}
					if (toRemove != null && removeReturnedValue)
						tm.remove(toRemove);
				}
		}
		{
			// analyse object store
			Collection<TreeMap<String, ThreadSafeOptions>> sv = storedObjects.values();
			if (sv != null)
				for (TreeMap<String, ThreadSafeOptions> tm : sv) {
					String toRemove = null;
					Set<String> ks = tm.keySet();
					if (ks != null)
						for (String key : ks) {
							if ((!exact && key.startsWith(search)) || (exact && key.equals(search))) {
								PropertyNames pn = null;
								try {
									pn = PropertyNames.valueOf(key);
								} catch (Exception e) {
									// ignore, not a parameter which has an enum
									// constant
								}
								if (pn == null) {
									if (tm.get(key) != null) {
										String name = key.substring(search.length());
										BlockResultValue p = new BlockResultValue(name, tm.get(key).getParam(1, null));
										result.add(p);
										toRemove = key;
									}
								} else {
									BlockResultValue p = new BlockResultValue(pn.getName(null), tm.get(key));
									result.add(p);
									toRemove = key;
								}
							}
						}
					if (toRemove != null && removeReturnedValue)
						tm.remove(toRemove);
				}
		}
		return result;
	}
	
	@Override
	public synchronized ArrayList<BlockResultValue> searchResults(String search) {
		return searchResults(false, search, false);
	}
	
	TreeMap<String, String> name2unit = getUnits();
	private final HashSet<String> deleteAtPipelineFinishing = new HashSet<String>();
	
	private String getUnitFromName(String name) {
		if (name2unit.containsKey(name))
			return name2unit.get(name);
		else
			return "";
	}
	
	private TreeMap<String, String> getUnits() {
		TreeMap<String, String> res = new TreeMap<String, String>();
		return res;
	}
	
	@Override
	public synchronized void storeResults(String id_prefix,
			ResultsTableWithUnits numericResults, int position) {
		storeResults(id_prefix, null, numericResults, position);
	}
	
	@Override
	public synchronized void storeResults(String id_prefix, String id_postfix,
			ResultsTableWithUnits numericResults, int position) {
		for (int row = 0; row < numericResults.getCounter(); row++) {
			for (int col = 0; col <= numericResults.getLastColumn(); col++) {
				String id = numericResults.getColumnHeading(col);
				double val = numericResults.getValueAsDouble(col, row);
				String unit = numericResults.getColumnHeadingUnit(col);
				if (!Double.isNaN(val))
					setNumericResult(position, id_prefix + id + (id_postfix != null ? id_postfix : ""), val, unit);
			}
		}
	}
	
	@Override
	public synchronized void printAnalysisResults() {
		for (BlockResultValue bpv : searchResults("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			System.out.println(bpv.getName()
					+ "="
					+ StringManipulationTools.formatNumber(bpv.getValue(),
							"#.###") + " " + bpv.getUnit());
		}
	}
	
	@Override
	public void setImage(int blockPosition, String id, ImageData image, boolean deleteAtPipelineCompletion) {
		synchronized (storedImages) {
			if (!storedImages.containsKey(blockPosition))
				storedImages.put(blockPosition, new TreeMap<String, ImageData>());
			else
				if (storedImages.get(blockPosition).containsKey(id))
					System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Result set already contains image with ID '" + id
							+ "', overwriting previous information!");
			storedImages.get(blockPosition).put(id, image);
		}
		if (deleteAtPipelineCompletion)
			synchronized (deleteAtPipelineFinishing) {
				deleteAtPipelineFinishing.add(id);
			}
	}
	
	@Override
	public void setImage(int position, String id, Image image, boolean deleteAtPipelineCompletion) {
		synchronized (storedImages) {
			if (!storedImages.containsKey(position))
				storedImages.put(position, new TreeMap<String, ImageData>());
			
			storedImages.get(position).put(id, new ImageInMemory(null, image));
			synchronized (deleteAtPipelineFinishing) {
				deleteAtPipelineFinishing.add(id);
			}
		}
	}
	
	@Override
	public Image getImage(int blockPosition, String id) {
		synchronized (storedImages) {
			if (!storedImages.containsKey(blockPosition))
				return null;
			ImageData res = storedImages.get(blockPosition).get(id);
			if (res == null)
				return null;
			else {
				storedImages.get(blockPosition).remove(id);
				return ((ImageInMemory) res).getImageData();
			}
		}
	}
	
	@Override
	public Image getImage(String id) {
		synchronized (storedImages) {
			for (Integer blockPosition : storedImages.keySet()) {
				ImageData res = storedImages.get(blockPosition).get(id);
				if (res == null)
					continue;
				else {
					storedImages.get(blockPosition).remove(id);
					return ((ImageInMemory) res).getImageData();
				}
			}
			return null;
		}
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
			CameraType conf) {
		ArrayList<RunnableOnImageSet> res = new ArrayList<RunnableOnImageSet>();
		for (RunnableOnImageSet ros : storedPostProcessors) {
			if (ros.getConfig() == conf) {
				res.add(ros);
			}
		}
		return res;
	}
	
	@Override
	public boolean isNumericStoreEmpty() {
		return storedNumerics.isEmpty();
	}
	
	public Rectangle2D.Double getRelativeBlueMarkerRectangle(ImageProcessorOptionsAndResults options) {
		return BlDetectBlueMarkers.getRelativeBlueMarkerRectangle(this, options);
	}
	
	@Override
	public void addImagePostProcessor(final CameraType imageType, final RunnableOnImage runnableOnImage, final RunnableOnImage runnableOnMask) {
		if (imageType == null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Internal error: Defined CameraType is NULL for given Image-Postprocessor!");
		addImagePostProcessor(new RunnableOnImageSet() {
			@Override
			public Image postProcessImage(Image image) {
				if (runnableOnImage != null)
					return runnableOnImage.postProcess(image);
				else
					return image;
			}
			
			@Override
			public CameraType getConfig() {
				return imageType;
			}
			
			@Override
			public Image postProcessMask(Image mask) {
				if (runnableOnMask != null)
					return runnableOnMask.postProcess(mask);
				else
					return mask;
			}
		});
	}
	
	@Override
	public void clearStoredPostprocessors() {
		storedPostProcessors.clear();
	}
	
	@Override
	public TreeMap<Integer, TreeMap<String, ImageData>> getImages() {
		return storedImages;
	}
	
	@Override
	public void addImagePostProcessor(ImageConfiguration imgConfig, RunnableOnImage runnableOnImage, RunnableOnImage runnableOnMask) {
		addImagePostProcessor(imgConfig.getCameraType(), runnableOnImage, runnableOnMask);
	}
	
	@Override
	public void removeResultObject(BlockResultObject obj) {
		for (Entry<Integer, TreeMap<String, ThreadSafeOptions>> u : storedObjects.entrySet()) {
			TreeMap<String, ThreadSafeOptions> i = u.getValue();
			String toRemove = null;
			for (Entry<String, ThreadSafeOptions> o : i.entrySet()) {
				if (obj.getObject() == o.getValue().getParam(1, null)) {
					toRemove = o.getKey();
					break;
				}
			}
			if (toRemove != null)
				i.remove(toRemove);
		}
	}
	
	@Override
	public void setImages(TreeMap<Integer, TreeMap<String, ImageData>> storedImages) {
		this.storedImages = storedImages;
	}
	
	@Override
	public void clearNotUsedResults() {
		synchronized (deleteAtPipelineFinishing) {
			for (String id : deleteAtPipelineFinishing) {
				for (Integer bp : storedImages.keySet()) {
					if (storedImages.get(bp).containsKey(id))
						storedImages.get(bp).remove(id);
				}
			}
		}
	}
}
