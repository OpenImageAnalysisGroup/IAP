package de.ipk.ag_ba.image.operations.blocks.cmds.threeD;

import info.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.GenerationMode;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.MyPicture;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.ThreeDmodelGenerator;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;

public class BlockThreeDgeneration extends AbstractBlock {
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		return mask;
	}
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage fi = getInput().getImages() != null ? getInput().getImages().getVis() : null;
		if (fi != null) {
			getProperties().setImage("img.vis.3D", fi.print("CLEARED", false));
			getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_REAL_VALUE, options.getDoubleSetting(Setting.REAL_MARKER_DISTANCE));
		} else {
			System.out.println();
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">WARNING: NO VIS IMAGE TO BE STORED FOR LATER 3D GENRATION!");
		}
		return super.processVISimage();
	}
	
	@Override
	public void postProcessResultsForAllAngles(
			Sample3D inSample,
			TreeMap<String, ImageData> inImages,
			TreeMap<String, BlockProperties> allResultsForSnapshot, BlockProperties summaryResult) throws InterruptedException {
		int voxelresolution = 500;
		int widthFactor = 40;
		GenerationMode modeOfOperation = GenerationMode.COLORED_RGBA;
		
		ThreeDmodelGenerator mg = new ThreeDmodelGenerator(voxelresolution, widthFactor);
		mg.setCameraDistance(1500);
		mg.setCubeSideLength(300, 300, 300);
		
		ArrayList<MyPicture> pictures = new ArrayList<MyPicture>();
		BlockProperty distHorizontal = null;
		double realMarkerDistHorizontal = Double.NaN;
		for (String angle : allResultsForSnapshot.keySet()) {
			// System.out.println(SystemAnalysisExt.getCurrentTime() + ">Process image angle " + angle + " (TODO)");
			BlockProperties bp = allResultsForSnapshot.get(angle);
			distHorizontal = bp.getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
			BlockProperty bpv = bp.getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_REAL_VALUE);
			if (bpv != null)
				realMarkerDistHorizontal = bpv.getValue();
			FlexibleImage vis = bp.getImage("img.vis.3D");
			bp.setImage("img.vis.3D", null);
			if (vis != null) {
				
				MyPicture p = new MyPicture();
				double ang = Double.parseDouble(angle.substring(angle.indexOf(";") + ";".length()));
				p.setPictureData(vis, ang / 180d * Math.PI, mg);
				pictures.add(p);
			}
		}
		if (pictures.size() > 0) {
			mg.setRoundViewImages(pictures);
			mg.calculateModel(new BackgroundTaskConsoleLogger("", "", true), modeOfOperation, 0);
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
			summaryResult.setNumericProperty(0, "RESULT_plant3d.volume", plantVolume);
			
			if (distHorizontal != null) {
				double corr = realMarkerDistHorizontal / distHorizontal.getValue();
				summaryResult.setNumericProperty(0, "RESULT_plant3d.volume.norm",
						plantVolume * corr * corr * corr);
			}
			
			boolean createVolumeDataset = true;
			LoadedVolumeExtension volume = null;
			if (createVolumeDataset) {
				Sample sample = inSample;
				volume = new LoadedVolumeExtension(sample, mg.getRGBcubeResultCopy());
				
				HashSet<Integer> replicateIDsOfSampleMeasurements = new HashSet<Integer>();
				for (Measurement m : sample) {
					replicateIDsOfSampleMeasurements.add(m.getReplicateID());
				}
				
				int replicateID = -1;
				if (replicateIDsOfSampleMeasurements.size() == 1)
					replicateID = replicateIDsOfSampleMeasurements.iterator().next();
				else
					System.out.println(SystemAnalysisExt.getCurrentTime()
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
				volume.getURL().setFileName("IAP_reconstruction_" + System.currentTimeMillis() + ".argb_volume");
				
				volume.setColorDepth(VolumeColorDepth.RGBA.toString());
				summaryResult.setVolume("RESULT_plant_model", volume);
			}
			boolean create3Dskeleton = true;
			if (create3Dskeleton) {
				createSimpleDefaultSkeleton(summaryResult, voxelresolution, mg, distHorizontal, realMarkerDistHorizontal, cube,
						(LoadedVolume) volume.clone(volume.getParentSample()));
			}
			boolean create3DadvancedProbabilitySkeleton = true;
			if (create3DadvancedProbabilitySkeleton) {
				int[][][] probabilityCube = mg.getByteCubeResult();
				createAdvancedProbabilitySkeleton(summaryResult, voxelresolution, mg, distHorizontal, realMarkerDistHorizontal, probabilityCube,
						(LoadedVolume) volume.clone(volume.getParentSample()));
			}
		}
	}
	
	/**
	 * The "fire" burns down each solid voxel with fixed speed.
	 */
	private void createSimpleDefaultSkeleton(BlockProperties summaryResult, int voxelresolution, ThreeDmodelGenerator mg, BlockProperty distHorizontal,
			double realMarkerDistHorizontal, int[][][] cube, LoadedVolume volume) {
		int fire = ImageOperation.BACKGROUND_COLORint;
		StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">Create simple 3D skeleton", false);
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
		summaryResult.setNumericProperty(0, "RESULT_plant3d.skeleton.length", skeletonLength);
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal.getValue();
			summaryResult.setNumericProperty(0, "RESULT_plant3d.skeleton.length.norm",
					skeletonLength * corr);
		}
		
		LoadedVolumeExtension lve = new LoadedVolumeExtension(volume);
		lve.setVolume(new ByteShortIntArray(cube));
		String n = lve.getURL().getFileName();
		if (n == null)
			n = SystemAnalysisExt.getCurrentTime() + " (NO VOLUME NAME, NULL ERROR 1)";
		n = StringManipulationTools.stringReplace(n, ".argb_volume", "");
		lve.getURL().setFileName(n + ".(plant skeleton).argb_volume");
		summaryResult.setVolume("RESULT_plant_skeleton", lve);
		
		s.printTime();
	}
	
	/**
	 * The "fire" slowly burns down the cube, based on each voxel's probability
	 */
	private void createAdvancedProbabilitySkeleton(BlockProperties summaryResult, int voxelresolution, ThreeDmodelGenerator mg, BlockProperty distHorizontal,
			double realMarkerDistHorizontal, int[][][] probabilityCube, LoadedVolume volume) {
		int empty = 0;
		StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">Create advanced probablity 3D skeleton", false);
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
		summaryResult.setNumericProperty(0, "RESULT_plant3d.probability-skeleton.length", skeletonLength);
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal.getValue();
			summaryResult.setNumericProperty(0, "RESULT_plant3d.probability-skeleton.length.norm",
					skeletonLength * corr);
		}
		
		LoadedVolumeExtension lve = new LoadedVolumeExtension(volume);
		lve.setVolume(new ByteShortIntArray(probabilityCube));
		String n = lve.getURL().getFileName();
		if (n == null)
			n = SystemAnalysisExt.getCurrentTime() + " (NO VOLUME NAME, NULL ERROR 2)";
		n = StringManipulationTools.stringReplace(n, ".argb_volume", "");
		lve.getURL().setFileName(n + ".(plant probability skeleton).argb_volume");
		
		summaryResult.setVolume("RESULT_plant_probability-skeleton", lve);
		
		s.printTime();
	}
	
	private void addSkeleton(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton, int x, int y, int z, int c) {
		if (!x2y2z2colorSkeleton.containsKey(x))
			x2y2z2colorSkeleton.put(x, new HashMap<Integer, HashMap<Integer, Integer>>());
		if (!x2y2z2colorSkeleton.get(x).containsKey(y))
			x2y2z2colorSkeleton.get(x).put(y, new HashMap<Integer, Integer>());
		x2y2z2colorSkeleton.get(x).get(y).put(z, c);
	}
}
