package iap.blocks.threeD;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import info.StopWatch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResult;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.GenerationMode;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.MyPicture;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.ThreeDmodelGenerator;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;

/**
 * @author Christian Klukas
 *         Todo: Calculated properties should be renamed from volume.xy to vis.volume.xy or fluo.volume.xy.
 */
public class BlThreeDreconstruction extends AbstractBlock implements CalculatesProperties {
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	protected Image processVISmask() {
		Image fi = input().masks() != null ? input().masks().vis() : null;
		if (!getBoolean("Process Fluo Instead of Vis", false)) {
			if (fi != null) {
				getResultSet().setImage(getBlockPosition(), "img.3D." + CameraType.VIS, fi, false);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fi = input().masks() != null ? input().masks().fluo() : null;
		if (getBoolean("Process Fluo Instead of Vis", false)) {
			if (fi != null) {
				getResultSet().setImage(getBlockPosition(), "img.3D." + CameraType.FLUO, fi, false);
			}
		}
		return fi;
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, CalculatesProperties propertyCalculator) throws InterruptedException {
		if (optStatus != null)
			optStatus.setCurrentStatusText1("3D-Generation in progress, waiting");
		synchronized (this.getClass()) {
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Generate 3D-Model");
			for (CameraType ct : new CameraType[] { CameraType.VIS, CameraType.FLUO })
				for (Long time : time2inSamples.keySet()) {
					Sample3D inSample = time2inSamples.get(time);
					TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
					if (!time2summaryResult.containsKey(time)) {
						time2summaryResult.put(time, new TreeMap<String, HashMap<Integer, BlockResultSet>>());
						time2summaryResult.get(time).put("-720", new HashMap<Integer, BlockResultSet>());
					}
					TreeSet<Integer> allTrays = new TreeSet<Integer>();
					for (String key : allResultsForSnapshot.keySet()) {
						allTrays.addAll(allResultsForSnapshot.get(key).keySet());
					}
					if (time2summaryResult.get(time).get("-720").isEmpty())
						for (Integer knownTray : allTrays)
							time2summaryResult.get(time).get("-720").put(knownTray, new BlockResults(null));
					for (Integer tray : time2summaryResult.get(time).get("-720").keySet()) {
						for (String key : allResultsForSnapshot.keySet()) {
							BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
							if (rt == null || rt.isNumericStoreEmpty())
								continue;
							BlockResultSet summaryResult = time2summaryResult.get(time).get("-720").get(tray);
							
							int voxelresolution = getInt("Voxel Resolution", 500);
							int widthFactor = getInt("Content Width", 40);
							GenerationMode modeOfOperation = GenerationMode.COLORED_RGBA;
							
							ThreeDmodelGenerator mg = new ThreeDmodelGenerator(voxelresolution, widthFactor,
									getInt("Probability Threshold", 100));
							mg.setCameraDistance(getInt("Camera Distance", 3200));
							mg.setCubeSideLength(getInt("Visible Box Size X", 300), getInt("Visible Box Size Y", 300), getInt("Visible Box Size Z", 300));
							
							ArrayList<MyPicture> pictures = new ArrayList<MyPicture>();
							Double distHorizontal = null;
							Double realMarkerDistHorizontal = null;
							if (allResultsForSnapshot != null && allResultsForSnapshot.keySet() != null)
								for (String angle : allResultsForSnapshot.keySet()) {
									if (allResultsForSnapshot.get(angle) == null)
										continue;
									BlockResultSet bp = allResultsForSnapshot.get(angle).get(tray);
									if (bp == null)
										continue;
									if (distHorizontal == null)
										distHorizontal = optionsAndResults.getCalculatedBlueMarkerDistance();
									
									if (distHorizontal == null)
										if (angle.startsWith("2nd_side")) {
											BlockResult val = bp.searchNumericResult(0, 0, new Trait(cp(), ct, TraitCategory.OPTICS, "marker.horizontal_distance"));
											if (val != null)
												distHorizontal = val.getValue();
										}
									realMarkerDistHorizontal = optionsAndResults.getREAL_MARKER_DISTANCE();
									Image vis = bp.getImage("img.3D." + ct);
									bp.setImage(getBlockPosition(), "img.3D." + ct, (Image) null, true);
									if (angle.startsWith("2nd_side"))
										if (vis != null) {
											MyPicture p = new MyPicture();
											double ang = Double.parseDouble(angle.substring(angle.indexOf(";") + ";".length()));
											p.setPictureData(vis, ang / 180d * Math.PI, mg);
											pictures.add(p);
										}
								}
							if (pictures.size() > 2) {
								mg.setRoundViewImages(pictures);
								mg.calculateModel(null/* optStatus */, modeOfOperation, 0, false);
								// the cube is a true cube (dim X,Y,Z are equal), the
								// input images are stretched to the target square
								// therefore, the actual volume calculation needs to consider
								// the source dimensions of the voxels, and therefore a un-stretch
								// calculation needs to be performed to error-correct the calculated volume
								int[][][] cube = mg.getRGBcubeResult();
								
								int solidVoxels = 0;
								long cogX = 0;
								long cogY = 0;
								long cogZ = 0;
								for (int x = 0; x < voxelresolution; x++) {
									int[][] cubeYZ = cube[x];
									for (int y = 0; y < voxelresolution; y++) {
										int[] cubeZ = cubeYZ[y];
										for (int z = 0; z < voxelresolution; z++) {
											int c = cubeZ[z];
											// if voxel can be considered not transparent (solid)
											// add voxel volume to the result
											boolean solid = c != ImageOperation.BACKGROUND_COLORint;
											if (solid) {
												solidVoxels++;
												cogX += x;
												cogY += y;
												cogZ += z;
											}
										}
									}
								}
								double vv = 1;
								double plantVolume = vv * solidVoxels;
								summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.volume"), plantVolume, "voxel", this);
								summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.cog.x"), cogX / solidVoxels, "voxel", this);
								summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.cog.y"), cogY / solidVoxels, "voxel", this);
								summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.cog.z"), cogZ / solidVoxels, "voxel", this);
								if (distHorizontal != null) {
									double corr = realMarkerDistHorizontal / distHorizontal;
									summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.volume.norm"), plantVolume * corr * corr
											* corr, "mm^3", this);
								}
								
								boolean saveVolumeDataset = getBoolean("Save Volume Dataset", false);
								LoadedVolumeExtension volume = null;
								Sample sample = inSample;
								volume = new LoadedVolumeExtension(sample, mg.getRGBcubeResultCopy());
								
								HashSet<Integer> replicateIDsOfSampleMeasurements = new HashSet<Integer>();
								for (Measurement m : sample) {
									replicateIDsOfSampleMeasurements.add(m.getReplicateID());
									if (volume.getQualityAnnotation() == null && m instanceof NumericMeasurement3D
											&& ((NumericMeasurement3D) m).getQualityAnnotation() != null)
										volume.setQualityAnnotation(((NumericMeasurement3D) m).getQualityAnnotation());
								}
								
								int replicateID = -1;
								if (replicateIDsOfSampleMeasurements.size() == 1)
									replicateID = replicateIDsOfSampleMeasurements.iterator().next();
								else
									System.out.println(SystemAnalysis.getCurrentTime()
											+ ">ERROR: 3D Volume generation block didn't find the expected single sample measurement replicate ID. It found "
											+ replicateIDsOfSampleMeasurements.size()
											+ " differing replicate IDs, instead! The generated volume possibly can't be related" +
											"to a single set of side views of a single Snapshot. This is a internal error.");
								volume.setReplicateID(replicateID);
								
								volume.setVoxelsizeX(25f * 400 / voxelresolution);
								volume.setVoxelsizeY(25f * 400 / voxelresolution * (100d / widthFactor));
								volume.setVoxelsizeZ(25f * 400 / voxelresolution);
								
								volume.setDimensionX(voxelresolution);
								volume.setDimensionY(voxelresolution);
								volume.setDimensionZ(voxelresolution);
								
								if (volume.getURL() == null)
									volume.setURL(new IOurl(LoadedDataHandler.PREFIX, "", ""));
								volume.getURL().setFileName(
										""
												+ volume.getQualityAnnotation()
												+ "_"
												+ StringManipulationTools.getFileSystemName(SystemAnalysis.getCurrentTimeInclSec(volume.getParentSample()
														.getSampleFineTimeOrRowId())) + ".argb_volume");
								
								volume.setColorDepth(VolumeColorDepth.RGBA.toString());
								if (saveVolumeDataset) {
									summaryResult.setVolume("RESULT_volume.plant3d.cube", volume);
								}
								if (getBoolean("Debug - Save 3D-Render to Desktop", false)) {
									try {
										File f = new File(SystemAnalysis.getDesktopFolder() + "/render_" + time + "_" + volume.getURL().getFileName() + ".gif");
										MyByteArrayInputStream cnt = volume.getSideViewGif(800, 600, optStatus);
										ResourceIOManager.copyContent(cnt, new FileOutputStream(f));
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
								}
								boolean create3Dskeleton = getBoolean("Calculate 3D Skeleton", true);
								boolean save3Dskeleton = getBoolean("Save 3D Skeleton", false);
								if (create3Dskeleton) {
									if (optStatus != null)
										optStatus.setCurrentStatusText1("Create 3-D skeleton");
									createSimpleDefaultSkeleton(ct, summaryResult, voxelresolution, mg, distHorizontal, realMarkerDistHorizontal, cube,
											(LoadedVolume) volume.clone(volume.getParentSample()), save3Dskeleton);
								}
								boolean create3DadvancedProbabilitySkeleton = getBoolean("Calculate 3-D probability skeleton", true);
								boolean save3DadvancedProbabilitySkeleton = getBoolean("Save 3-D probability skeleton", false);
								if (create3DadvancedProbabilitySkeleton) {
									if (optStatus != null)
										optStatus.setCurrentStatusText1("Calculate 3-D probability skeleton");
									int[][][] probabilityCube = mg.getByteCubeResult();
									createAdvancedProbabilitySkeleton(ct,
											summaryResult, voxelresolution, mg, distHorizontal, realMarkerDistHorizontal, probabilityCube,
											(LoadedVolume) volume.clone(volume.getParentSample()), save3DadvancedProbabilitySkeleton);
								}
							}
						}
					}
				}
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Finished construction");
		} // synchronized
	}
	
	/**
	 * The "fire" burns down each solid voxel with fixed speed.
	 */
	private void createSimpleDefaultSkeleton(CameraType ct, BlockResultSet summaryResult, int voxelresolution,
			ThreeDmodelGenerator mg,
			Double distHorizontal,
			Double realMarkerDistHorizontal, int[][][] cube, LoadedVolume volume, boolean save3Dskeleton) {
		int fire = ImageOperation.BACKGROUND_COLORint;
		StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">Create simple 3D skeleton", false);
		HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
		boolean foundBorderVoxel = false;
		do {
			foundBorderVoxel = false;
			for (int x = 1; x < voxelresolution - 1; x++) {
				for (int y = 1; y < voxelresolution - 1; y++) {
					for (int z = 1; z < voxelresolution - 1; z++) {
						int c = cube[x][y][z];
						boolean filled = c != fire;
						if (filled) {
							boolean left = cube[x - 1][y][z] != fire;
							boolean right = cube[x + 1][y][z] != fire;
							boolean above = cube[x][y - 1][z] != fire;
							boolean below = cube[x][y + 1][z] != fire;
							boolean behind = cube[x][y][z + 1] != fire;
							boolean before = cube[x][y][z - 1] != fire;
							if (!left || !right || !above || !below || !behind || !before) {
								// border voxel
								foundBorderVoxel = true;
								int filledSurrounding = 0;
								if (left)
									filledSurrounding++;
								if (right)
									filledSurrounding++;
								if (above)
									filledSurrounding++;
								if (below)
									filledSurrounding++;
								if (behind)
									filledSurrounding++;
								if (before)
									filledSurrounding++;
								if (filledSurrounding <= 2)
									addSkeleton(x2y2z2colorSkeleton, x, y, z, c);
							}
							cube[x][y][z] = fire;
						}
					}
				}
			}
		} while (foundBorderVoxel);
		long skeletonLength = 0;
		long skeletonX = 0;
		long skeletonY = 0;
		long skeletonZ = 0;
		for (int x = 1; x < voxelresolution - 1; x++) {
			if (x2y2z2colorSkeleton.containsKey(x)) {
				HashMap<Integer, HashMap<Integer, Integer>> y2z = x2y2z2colorSkeleton.get(x);
				for (int y = 1; y < voxelresolution - 1; y++) {
					if (y2z.containsKey(y)) {
						HashMap<Integer, Integer> z2c = y2z.get(y);
						for (int z : z2c.keySet()) {
							Integer c = z2c.get(z);
							cube[x][y][z] = c;
							skeletonLength++;
							skeletonX += x;
							skeletonY += y;
							skeletonZ += z;
						}
					}
				}
			}
		}
		summaryResult.setNumericResult(0,
				new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.skeleton.length"), skeletonLength, "px", this);
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal;
			summaryResult.setNumericResult(0,
					new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.skeleton.length.norm"),
					skeletonLength * corr, "mm", this);
		}
		summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.skeleton.cog.x"), skeletonX / skeletonLength, "voxel", this);
		summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.skeleton.cog.y"), skeletonY / skeletonLength, "voxel", this);
		summaryResult.setNumericResult(0, new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.skeleton.cog.z"), skeletonZ / skeletonLength, "voxel", this);
		
		LoadedVolumeExtension lve = new LoadedVolumeExtension(volume);
		lve.setVolume(new ByteShortIntArray(cube));
		String n = lve.getURL().getFileName();
		if (n == null)
			n = SystemAnalysis.getCurrentTime() + " (NO VOLUME NAME, NULL ERROR 1)";
		n = StringManipulationTools.stringReplace(n, ".argb_volume", "");
		lve.getURL().setFileName(n + ".(plant skeleton).argb_volume");
		if (save3Dskeleton)
			summaryResult.setVolume("RESULT_volume.plant3d.skeleton.cube", lve);
		
		s.printTime();
	}
	
	/**
	 * The "fire" slowly burns down the cube, based on each voxel's probability
	 */
	private void createAdvancedProbabilitySkeleton(CameraType ct, BlockResultSet summaryResult, int voxelresolution,
			ThreeDmodelGenerator mg,
			Double distHorizontal,
			Double realMarkerDistHorizontal,
			int[][][] probabilityCube, LoadedVolume volume,
			boolean save3DadvancedProbabilitySkeleton) {
		int empty = 0;
		StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">Create advanced probablity 3D skeleton", false);
		HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
		boolean foundBorderVoxel = false;
		do {
			foundBorderVoxel = false;
			for (int x = 1; x < voxelresolution - 1; x++) {
				for (int y = 1; y < voxelresolution - 1; y++) {
					for (int z = 1; z < voxelresolution - 1; z++) {
						int c = probabilityCube[x][y][z];
						boolean filled = c > empty;
						if (filled) {
							boolean left = probabilityCube[x - 1][y][z] != empty;
							boolean right = probabilityCube[x + 1][y][z] != empty;
							boolean above = probabilityCube[x][y - 1][z] != empty;
							boolean below = probabilityCube[x][y + 1][z] != empty;
							boolean behind = probabilityCube[x][y][z + 1] != empty;
							boolean before = probabilityCube[x][y][z - 1] != empty;
							if (!left || !right || !above || !below || !behind || !before) {
								// border voxel
								foundBorderVoxel = true;
								int filledSurrounding = 0;
								if (left)
									filledSurrounding++;
								if (right)
									filledSurrounding++;
								if (above)
									filledSurrounding++;
								if (below)
									filledSurrounding++;
								if (behind)
									filledSurrounding++;
								if (before)
									filledSurrounding++;
								if (filledSurrounding <= 2)
									addSkeleton(x2y2z2colorSkeleton, x, y, z, c);
							}
							probabilityCube[x][y][z]--;
						}
					}
				}
			}
		} while (foundBorderVoxel);
		long skeletonLength = 0;
		for (int x = 1; x < voxelresolution - 1; x++) {
			if (x2y2z2colorSkeleton.containsKey(x)) {
				HashMap<Integer, HashMap<Integer, Integer>> y2z = x2y2z2colorSkeleton.get(x);
				for (int y = 1; y < voxelresolution - 1; y++) {
					if (y2z.containsKey(y)) {
						HashMap<Integer, Integer> z2c = y2z.get(y);
						for (int z : z2c.keySet()) {
							Integer c = z2c.get(z);
							probabilityCube[x][y][z] = c;
							skeletonLength++;
						}
					}
				}
			}
		}
		summaryResult.setNumericResult(0,
				new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.probability.skeleton.length"), skeletonLength, "px", this);
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal;
			summaryResult.setNumericResult(0,
					new Trait(cp(), ct, TraitCategory.GEOMETRY, "plant3d.probability.skeleton.length.norm"),
					skeletonLength * corr, "mm", this);
		}
		
		LoadedVolumeExtension lve = new LoadedVolumeExtension(volume);
		lve.setVolume(new ByteShortIntArray(probabilityCube));
		String n = lve.getURL().getFileName();
		if (n == null)
			n = SystemAnalysis.getCurrentTime() + " (NO VOLUME NAME, NULL ERROR 2)";
		n = StringManipulationTools.stringReplace(n, ".argb_volume", "");
		lve.getURL().setFileName(n + ".(plant probability skeleton).argb_volume");
		if (save3DadvancedProbabilitySkeleton)
			summaryResult.setVolume("RESULT_volume.plant3d.probability.skeleton.cube", lve);
		
		s.printTime();
	}
	
	private void addSkeleton(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton, int x, int y, int z, int c) {
		if (!x2y2z2colorSkeleton.containsKey(x))
			x2y2z2colorSkeleton.put(x, new HashMap<Integer, HashMap<Integer, Integer>>());
		if (!x2y2z2colorSkeleton.get(x).containsKey(y))
			x2y2z2colorSkeleton.get(x).put(y, new HashMap<Integer, Integer>());
		x2y2z2colorSkeleton.get(x).get(y).put(z, c);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "3D-Volume-Generation";
	}
	
	@Override
	public String getDescription() {
		return "Perform space carving operation on fluo (default) or visible light side images.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("volume.plant3d.volume",
						"Estimated plant volume in voxels, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.volume.norm",
						"Estimated plant volume in real-world coordinates, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.cog.x",
						"Center of gravity (X-axis) of the plant volume, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.cog.y",
						"Center of gravity (Y-axis) of the plant volume, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.cog.z",
						"Center of gravity (Z-axis) of the plant volume, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.cube",
						"Calculated plant volume (colored voxel cube)."),
				new CalculatedProperty("volume.plant3d.skeleton.length",
						"Length of the 3-D-skeleton, based on the plant volume cube, obtained by applying the space-carving algorithm "
								+ "to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.skeleton.length.norm",
						"Length (normalized to real-world coordinates) of the 3-D-skeleton, based on the plant volume cube, "
								+ "obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.skeleton.cog.x",
						"Center of gravity (X-axis) of the plant volume skeleton, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.skeleton.cog.y",
						"Center of gravity (Y-axis) of the plant volume skeleton, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.skeleton.cog.z",
						"Center of gravity (Z-axis) of the plant volume skeleton, obtained by applying the space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.probability.skeleton.length",
						"Length plant volume skeleton, obtained by applying a special probability-based space-carving algorithm to multiple side-view images."),
				new CalculatedProperty("volume.plant3d.probability.skeleton.length.norm",
						"Length plant volume skeleton in real-world coordinates, obtained by applying a special probability-based space-carving "
								+ "algorithm to multiple side-view images.")
		};
	}
}
