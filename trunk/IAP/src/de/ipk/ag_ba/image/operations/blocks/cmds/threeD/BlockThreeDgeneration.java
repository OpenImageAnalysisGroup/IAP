package de.ipk.ag_ba.image.operations.blocks.cmds.threeD;

import info.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
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
		} else {
			System.out.println();
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">WARNING: NO VIS IMAGE TO BE STORED FOR LATER 3D GENRATION!");
		}
		return super.processVISimage();
	}
	
	@Override
	public void postProcessResultsForAllAngles(
			Sample3D inSample,
			TreeMap<Double, ImageData> inImages,
			TreeMap<Double, BlockProperties> allResultsForSnapshot, BlockProperties summaryResult) {
		// super.postProcessResultsForAllAngles(inSample, inImages, allResultsForSnapshot, summaryResult);
		
		int voxelresolution = 500;
		int widthFactor = 40;
		GenerationMode modeOfOperation = GenerationMode.COLORED_RGBA;
		
		ThreeDmodelGenerator mg = new ThreeDmodelGenerator(voxelresolution, widthFactor);
		mg.setCameraDistance(1500);
		mg.setCubeSideLength(300, 300, 300);
		
		ArrayList<MyPicture> pictures = new ArrayList<MyPicture>();
		for (Double angle : allResultsForSnapshot.keySet()) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">Process image angle " + angle + " (TODO)");
			BlockProperties bp = allResultsForSnapshot.get(angle);
			FlexibleImage vis = bp.getImage("img.vis.3D");
			if (vis != null) {
				
				MyPicture p = new MyPicture();
				p.setPictureData(vis, angle / 180d * Math.PI, mg);
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
			
			BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
			double realMarkerDistHorizontal = options.getIntSetting(Setting.REAL_MARKER_DISTANCE);
			if (distHorizontal != null) {
				double corr = realMarkerDistHorizontal / distHorizontal.getValue();
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_plant3d.volume.norm",
								plantVolume * corr * corr * corr);
			}
			
			boolean createVolumeDataset = true;
			if (createVolumeDataset) {
				Sample sample = inSample;
				LoadedVolumeExtension volume = new LoadedVolumeExtension(sample, mg.getRGBcubeResult());
				
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
					volume.setURL(new IOurl("loadedvolume", "", ""));
				volume.getURL().setFileName("IAP_reconstruction_" + System.currentTimeMillis() + ".argb_volume");
				
				volume.setColorDepth(VolumeColorDepth.RGBA.toString());
			}
			boolean create3Dskeleton = true;
			if (create3Dskeleton) {
				int fire = ImageOperation.BACKGROUND_COLORint;
				StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">Create 3D Skeleton", true);
				HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
				boolean foundBorderVoxel = false;
				do {
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
								for (int z : y2z.keySet()) {
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
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_plant3d.skeleton.length.norm",
							skeletonLength * corr);
				}
				
				s.printTime();
			}
		}
		
	}
	
	private void addSkeleton(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton, int x, int y, int z, int c) {
		if (!x2y2z2colorSkeleton.containsKey(x))
			x2y2z2colorSkeleton.put(x, new HashMap<Integer, HashMap<Integer, Integer>>());
		if (!x2y2z2colorSkeleton.get(x).containsKey(y))
			x2y2z2colorSkeleton.get(x).put(y, new HashMap<Integer, Integer>());
		x2y2z2colorSkeleton.get(x).get(y).put(z, c);
	}
}
