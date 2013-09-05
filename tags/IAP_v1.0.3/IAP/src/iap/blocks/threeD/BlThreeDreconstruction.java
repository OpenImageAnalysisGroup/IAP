package iap.blocks.threeD;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import info.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
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

public class BlThreeDreconstruction extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	protected Image processVISmask() {
		Image fi = input().masks() != null ? input().masks().vis() : null;
		if (fi != null) {
			getProperties().setImage("img.vis.3D", fi);
		}
		return fi;
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InterruptedException {
		synchronized (this.getClass()) {
			for (Long time : time2inSamples.keySet()) {
				Sample3D inSample = time2inSamples.get(time);
				TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
				if (!time2summaryResult.containsKey(time)) {
					time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
				}
				TreeSet<Integer> allTrays = new TreeSet<Integer>();
				for (String key : allResultsForSnapshot.keySet()) {
					allTrays.addAll(allResultsForSnapshot.get(key).keySet());
				}
				if (time2summaryResult.get(time).isEmpty())
					for (Integer knownTray : allTrays)
						time2summaryResult.get(time).put(knownTray, new BlockResults());
				for (Integer tray : time2summaryResult.get(time).keySet()) {
					for (String key : allResultsForSnapshot.keySet()) {
						BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
						if (rt == null || rt.isNumericStoreEmpty())
							continue;
						BlockResultSet summaryResult = time2summaryResult.get(time).get(tray);
						
						int voxelresolution = getInt("Voxel Resolution", 300);
						int widthFactor = getInt("Content Width", 40);
						GenerationMode modeOfOperation = GenerationMode.COLORED_RGBA;
						
						ThreeDmodelGenerator mg = new ThreeDmodelGenerator(voxelresolution, widthFactor);
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
									distHorizontal = options.getCalculatedBlueMarkerDistance();
								
								if (distHorizontal == null)
									if (angle.startsWith("side")) {
										BlockProperty val = bp.getNumericProperty(0, 0, "side" + ".optics.blue_marker_distance");
										if (val != null)
											distHorizontal = val.getValue();
									}
								realMarkerDistHorizontal = options.getREAL_MARKER_DISTANCE();
								Image vis = bp.getImage("img.vis.3D");
								bp.setImage("img.vis.3D", null);
								if (angle.startsWith("side"))
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
							for (int x = 0; x < voxelresolution; x++) {
								int[][] cubeYZ = cube[x];
								for (int y = 0; y < voxelresolution; y++) {
									int[] cubeZ = cubeYZ[y];
									for (int z = 0; z < voxelresolution; z++) {
										int c = cubeZ[z];
										// if voxel can be considered not transparent (solid)
										// add voxel volume to the result
										boolean solid = c != ImageOperation.BACKGROUND_COLORint;
										if (solid)
											solidVoxels++;
									}
								}
							}
							double vv = 1;
							double plantVolume = vv * solidVoxels;
							summaryResult.setNumericProperty(0,
									"RESULT_volume.plant3d.volume", plantVolume, "voxel");
							if (distHorizontal != null) {
								double corr = realMarkerDistHorizontal / distHorizontal;
								summaryResult.setNumericProperty(0, "RESULT_volume.plant3d.volume.norm",
										plantVolume * corr * corr * corr, "mm^3");
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
										+ replicateIDsOfSampleMeasurements.size() + " differing replicate IDs, instead! The generated volume possibly can't be related" +
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
							boolean create3Dskeleton = getBoolean("Calculate 3D Skeleton", true);
							boolean save3Dskeleton = getBoolean("Save 3D Skeleton", false);
							if (create3Dskeleton) {
								if (optStatus != null)
									optStatus.setCurrentStatusText1("Create 3-D skeleton");
								createSimpleDefaultSkeleton(summaryResult, voxelresolution, mg, distHorizontal, realMarkerDistHorizontal, cube,
										(LoadedVolume) volume.clone(volume.getParentSample()), save3Dskeleton);
							}
							boolean create3DadvancedProbabilitySkeleton = getBoolean("Calculate 3-D probability skeleton", true);
							boolean save3DadvancedProbabilitySkeleton = getBoolean("Save 3-D probability skeleton", false);
							if (create3DadvancedProbabilitySkeleton) {
								if (optStatus != null)
									optStatus.setCurrentStatusText1("Calculate 3-D probability skeleton");
								int[][][] probabilityCube = mg.getByteCubeResult();
								createAdvancedProbabilitySkeleton(
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
	private void createSimpleDefaultSkeleton(BlockResultSet summaryResult, int voxelresolution,
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
						}
					}
				}
			}
		}
		summaryResult.setNumericProperty(0,
				"RESULT_volume.plant3d.skeleton.length", skeletonLength, "px");
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal;
			summaryResult.setNumericProperty(0,
					"RESULT_volume.plant3d.skeleton.length.norm",
					skeletonLength * corr, "mm");
		}
		
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
	private void createAdvancedProbabilitySkeleton(BlockResultSet summaryResult, int voxelresolution,
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
		summaryResult.setNumericProperty(0,
				"RESULT_volume.plant3d.probability.skeleton.length", skeletonLength, "px");
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal;
			summaryResult.setNumericProperty(0,
					"RESULT_volume.plant3d.probability.skeleton.length.norm",
					skeletonLength * corr, "mm");
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
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
}
