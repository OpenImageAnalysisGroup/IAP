package de.ipk.ag_ba.image.operations.blocks.cmds.threeD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
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
			getProperties().setImage("img.vis.3D", fi);
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
		
		int voxelresolution = 200;
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
				p.setPictureData(vis.getAsBufferedImage(), angle, mg);
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
			getProperties().setNumericProperty(0, "RESULT_plant.volume", plantVolume);
			
			boolean createVolumeDataset = false;
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
			// the volume needs to be added to the output set
			// it will be saved e.g. by MongoDB, where the side view animated GIF will be rendered as
			// the preview icon
			//
			// the actual volume should probably not be saved, because it is too large,
			// instead the MongoDB volume storage code should conditionally, depending on some flag
			// not save the volume, but a larger animated gif of the side view as the actual content,
			// turning the output effectively into a stored image
			//
			// an alternative is to save a compressed surface volume (oct-tree!?), which could be expanded
			// into full volume structure on demand, or depending code (image GIF) could be adapted to accept
			// a "virtual cube", which provides XYZ access to the colored voxels, similar to the int[][][] cube
		}
		
	}
}
