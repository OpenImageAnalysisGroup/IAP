package de.ipk.ag_ba.image.operations.blocks.cmds.threeD;

import java.util.ArrayList;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.GenerationMode;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.MyPicture;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.ThreeDmodelGenerator;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;

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
		}
		return super.processVISimage();
	}
	
	@Override
	public void postProcessResultsForAllAngles(TreeMap<Double, BlockProperties> allResultsForSnapshot, BlockProperties summaryResult) {
		super.postProcessResultsForAllAngles(allResultsForSnapshot, summaryResult);
		
		int voxelresolution = 200;
		int widthFactor = 40;
		GenerationMode modeOfOperation = GenerationMode.GRAYSCALE_PROBABILITY; // GenerationMode.COLORED_RGBA;
		
		ThreeDmodelGenerator mg = new ThreeDmodelGenerator(voxelresolution, widthFactor);
		mg.setCameraDistance(1500);
		mg.setCubeSideLength(300, 300, 300);
		
		ArrayList<MyPicture> pictures = new ArrayList<MyPicture>();
		for (Double angle : allResultsForSnapshot.keySet()) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">Process image angle " + angle + " (TODO)");
			BlockProperties bp = allResultsForSnapshot.get(angle);
			FlexibleImage vis = bp.getImage("img.vis.3D");
			
			MyPicture p = new MyPicture();
			p.setPictureData(vis.getAsBufferedImage(), angle, mg);
			pictures.add(p);
		}
		
		mg.setRoundViewImages(pictures);
		mg.calculateModel(new BackgroundTaskConsoleLogger("", "", true), modeOfOperation, 0);
		
	}
}

// boolean createVolumeDataset = false; // does not work yet (sample info missing)
// if (createVolumeDataset) {
// Sample sample = null; // todo
// LoadedVolumeExtension volume = new LoadedVolumeExtension(sample, mg.getRGBcubeResult());
//
// int replicateID = -1; // todo
// volume.setReplicateID(replicateID);
//
// volume.setVoxelsizeX(25f * 400 / voxelresolution);
// volume.setVoxelsizeY(25f * 400 / voxelresolution * (100d / widthFactor));
// volume.setVoxelsizeZ(25f * 400 / voxelresolution);
//
// volume.setDimensionX(voxelresolution);
// volume.setDimensionY(voxelresolution);
// volume.setDimensionZ(voxelresolution);
//
// if (volume.getURL() == null)
// volume.setURL(new IOurl("loadedvolume", "", ""));
// volume.getURL().setFileName("IAP_reconstruction_" + System.currentTimeMillis() + ".argb_volume");
//
// volume.setColorDepth(VolumeColorDepth.RGBA.toString());
// }
