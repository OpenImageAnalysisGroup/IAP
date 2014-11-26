package de.ipk.ag_ba.image.operations.blocks;

import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImage;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

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
import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author Christian Klukas
 */
public class BlockResults implements BlockResultSet {
	
	private final TreeMap<Integer, TreeMap<String, DoubleAndImageData>> storedNumerics = new TreeMap<Integer, TreeMap<String, DoubleAndImageData>>();
	private final TreeMap<Integer, TreeMap<String, ThreadSafeOptions>> storedObjects = new TreeMap<Integer, TreeMap<String, ThreadSafeOptions>>();
	private TreeMap<Integer, TreeMap<String, ImageAndImageData>> storedImages = new TreeMap<Integer, TreeMap<String, ImageAndImageData>>();
	private final HashMap<String, VolumeData> storedVolumes = new HashMap<String, VolumeData>();
	private final ArrayList<RunnableOnImageSet> storedPostProcessors = new ArrayList<RunnableOnImageSet>();
	private final Double cameraAngle;
	
	public BlockResults(Double cameraAngle) {
		this.cameraAngle = cameraAngle;
	}
	
	@Override
	public synchronized BlockResult searchNumericResult(
			int currentPositionInPipeline, int searchIndex, Trait pName) {
		return searchNumericResult(currentPositionInPipeline, searchIndex, pName.toString(), true);
	}
	
	@Override
	public synchronized BlockResultObject searchObjectResult(
			int currentPositionInPipeline, int searchIndex, String pName) {
		return (BlockResultObject) searchNumericResult(currentPositionInPipeline, searchIndex, pName, false);
	}
	
	public synchronized BlockResult searchNumericResult(
			int currentPositionInPipeline, int searchIndex, String trait, boolean searchNumericValue_true_searchObject_false) {
		String name = trait;
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
									DoubleAndImageData d = storedNumerics.get(index).get(name);
									return new BlockResult(d.getValue(), index);
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
					DoubleAndImageData d = storedNumerics.get(
							currentPositionInPipeline + searchIndex).get(name);
					if (d == null)
						return null;
					else
						return new BlockResult(d.getValue(), currentPositionInPipeline + searchIndex);
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
	public synchronized void setNumericResult(int position, Trait name,
			double value, CalculatesProperties descriptionProvider, NumericMeasurement3D imageRef) {
		if (descriptionProvider == null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No valid property-calculator object provided. Trait: " + name
					+ " Indicating possibly incomplete trait description.");
		if (!storedNumerics.containsKey(position))
			storedNumerics.put(position, new TreeMap<String, DoubleAndImageData>());
		
		storedNumerics.get(position).put(name.toString(), new DoubleAndImageData(value, imageRef));
	}
	
	@Override
	public synchronized void setNumericResult(int position, Trait trait,
			double value, String unit, CalculatesProperties descriptionProvider, NumericMeasurement3D imageRef) {
		String name = trait.toString();
		if (descriptionProvider == null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No valid property-calculator object provided. Trait: " + name
					+ " Indicating possibly incomplete trait description.");
		if (!storedNumerics.containsKey(position))
			storedNumerics.put(position, new TreeMap<String, DoubleAndImageData>());
		
		storedNumerics.get(position).put(name, new DoubleAndImageData(value, imageRef));
		
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
			Collection<TreeMap<String, DoubleAndImageData>> sv = storedNumerics.values();
			if (sv != null)
				for (TreeMap<String, DoubleAndImageData> tm : sv) {
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
												tm.get(key).getValue(), cameraAngle,
												tm.get(key).getImageData());
										result.add(p);
										toRemove = key;
									}
								} else {
									BlockResultValue p = new BlockResultValue(
											pn.getName(null).toString(), pn.getUnit(),
											tm.get(key).getValue(), cameraAngle,
											tm.get(key).getImageData());
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
			Collection<TreeMap<String, ImageAndImageData>> sv = storedImages.values();
			if (sv != null)
				for (TreeMap<String, ImageAndImageData> tm : sv) {
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
									BlockResultValue p = new BlockResultValue(pn.getName(null).toString(), tm.get(key));
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
									BlockResultValue p = new BlockResultValue(pn.getName(null).toString(), tm.get(key));
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
	public synchronized ArrayList<BlockResultValue> searchResults(Trait trait) {
		return searchResults(false, trait.toString(), false);
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
	public synchronized void storeResults(CameraPosition cp, CameraType ct, TraitCategory cat,
			ResultsTableWithUnits numericResults, int position, CalculatesProperties description, NumericMeasurement3D imageRef) {
		storeResults(cp, ct, cat, null, numericResults, position, description, imageRef);
	}
	
	@Override
	public synchronized void storeResults(CameraPosition cp, CameraType ct, TraitCategory cat,
			String id_postfix,
			ResultsTableWithUnits numericResults, int position,
			CalculatesProperties description, NumericMeasurement3D imageRef) {
		if (description == null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No valid property-calculator object provided. "
					+ "Indicating possibly incomplete trait description.");
		for (int row = 0; row < numericResults.getCounter(); row++) {
			for (int col = 0; col <= numericResults.getLastColumn(); col++) {
				String id = numericResults.getColumnHeading(col);
				double val = numericResults.getValueAsDouble(col, row);
				String unit = numericResults.getColumnHeadingUnit(col);
				if (!Double.isNaN(val))
					setNumericResult(position, new Trait(cp, ct, cat,
							id + (id_postfix != null && !id_postfix.isEmpty() ?
									(id_postfix.startsWith("|") ? "" : ".") + id_postfix : "")),
							val, unit, description, imageRef);
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
	public void setImage(int position, String id, ImageAndImageData image, boolean deleteAtPipelineCompletion) {
		synchronized (storedImages) {
			if (!storedImages.containsKey(position))
				storedImages.put(position, new TreeMap<String, ImageAndImageData>());
			
			storedImages.get(position).put(id, image);
			if (deleteAtPipelineCompletion) {
				synchronized (deleteAtPipelineFinishing) {
					deleteAtPipelineFinishing.add(id);
				}
			}
		}
	}
	
	@Override
	public Image getImage(int blockPosition, String id) {
		synchronized (storedImages) {
			if (!storedImages.containsKey(blockPosition))
				return null;
			ImageAndImageData res = storedImages.get(blockPosition).get(id);
			if (res == null)
				return null;
			else {
				storedImages.get(blockPosition).remove(id);
				return res.getImage();
			}
		}
	}
	
	@Override
	public ImageAndImageData getImage(String id) {
		return getImage(id, true);
	}
	
	@Override
	public ImageAndImageData getImage(String id, boolean remove) {
		synchronized (storedImages) {
			for (Integer blockPosition : storedImages.keySet()) {
				ImageAndImageData res = storedImages.get(blockPosition).get(id);
				if (res == null)
					continue;
				else {
					if (remove)
						storedImages.get(blockPosition).remove(id);
					return res;
				}
			}
			return new ImageAndImageData(null, null);
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
	public TreeMap<Integer, TreeMap<String, ImageAndImageData>> getImages() {
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
	public void setImages(TreeMap<Integer, TreeMap<String, ImageAndImageData>> storedImages) {
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
	
	@Override
	public void setImage(int currentPositionInPipeline, String id, LoadedImage image, boolean deleteAtPipelineCompletion) {
		setImage(currentPositionInPipeline, id, new ImageAndImageData(
				new Image(image.getLoadedImage()), image.getImageDataReference()), deleteAtPipelineCompletion);
	}
}
