package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author klukas
 */
public class BlSaveResultImages extends AbstractBlock {
	
	@Override
	protected Image processImage(Image image) {
		if (image != null) {
			boolean manyWells = optionsAndResults.getWellCnt() > 1;
			ImageData outImageReference = (ImageData) input().images().getImageInfo(image.getCameraType())
					.clone(input().images().getImageInfo(image.getCameraType()).getParentSample());
			if (manyWells)
				outImageReference.setQualityAnnotation(outImageReference.getQualityAnnotation() + "_"
						+ getWellIdx());
			try {
				LoadedImage res = processAndOrSaveResultImage(outImageReference, image);
				if (res != null) {
					if (!res.getParentSample().getParentCondition().getParentSubstance().getName().contains(image.getCameraType() + "")) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Saved camera type " + image.getCameraType() + " to substance "
								+ res.getParentSample().getParentCondition().getParentSubstance().getName());
					}
					getResultSet().setImage(getBlockPosition(), "RESULT_" + res.getSubstanceName(),
							new ImageAndImageData(
									null,// new Image(res.getLoadedImage()),
									res.getImageDataReference()),
							false);
				}
			} catch (Exception e) {
				throw new RuntimeException("Could not save result image", e);
			}
		}
		return image;
	}
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	private LoadedImage processAndOrSaveResultImage(ImageData outImageReference, Image resImage) throws Exception {
		String tray = getWellIdx();
		
		if (optionsAndResults.forceDebugStack) {
			return null;
		} else {
			if (outImageReference != null && outImageReference.getLabelURL() != null)
				outImageReference.addAnnotationField("oldreference", outImageReference.getLabelURL().toString());
			
			if (outImageReference != null && outImageReference.getURL() != null)
				outImageReference.setLabelURL(outImageReference.getURL().copy());
			
			outImageReference.setURL(new IOurl(null, null, outImageReference.getURL().getFileName()));
			
			return saveImage(tray, outImageReference, resImage);
		}
	}
	
	private LoadedImage saveImage(
			final String tray,
			final ImageData id, final Image image) throws Exception {
		if (id != null && id.getParentSample() != null) {
			LoadedImage loadedImage = new LoadedImage(id, image.getAsBufferedImage());
			// loadedImage.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
			return saveImageAndUpdateURL(loadedImage, optionsAndResults.databaseTarget, false, tray);
		} else
			return null;
	}
	
	private String addTrayInfo(String tray, String fileName, ImageData image) {
		if (tray != null && tray.length() > 0) {
			String extension = fileName.substring(fileName.lastIndexOf(".") + ".".length());
			
			String replace;
			
			replace = "." + tray + ".";
			fileName = StringManipulationTools.stringReplace(fileName,
					"." + extension, replace
							+ extension);
		}
		return fileName;
	}
	
	protected LoadedImage saveImageAndUpdateURL(LoadedImage result,
			DatabaseTarget databaseTarget, boolean processLabelUrl,
			String tray) throws Exception {
		if (result.getURL() == null)
			result.setURL(new IOurl(null, StringManipulationTools.removeFileExtension(result.getURL().getFileName())
					+ SystemOptions.getInstance().getString("IAP", "Result File Type", "png")));
		// System.out.println("CT=" + cameraType + ", WxH=" + result.getLoadedImage().getWidth() + " x " + result.getLoadedImage().getHeight());
		// if (cameraType == CameraType.NIR && result.getLoadedImage().getWidth() > 570) {
		// new Image(result.getLoadedImage()).show(cameraType + " ?");
		// System.out.println("CT=" + cameraType + ", W=" + result.getLoadedImage().getWidth());
		// }
		
		result.getURL().setFileName(addTrayInfo(tray, result.getURL().getFileName(), result));
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (result.getLabelURL() != null && processLabelUrl) {
			result.getLabelURL().setFileName(
					addTrayInfo(tray,
							result.getLabelURL().getFileName(), result));
			result.getLabelURL().setPrefix(LoadedDataHandler.PREFIX);
		}
		
		if (databaseTarget != null) {
			return databaseTarget.saveImage(new String[] { "", "label_" }, result, false, true);
		} else {
			boolean clearmemory = true;
			if (clearmemory) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">Image result not saved and removed from result set: " + result.getURL().toString());
				return null;
			} else {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">Image result kept in memory: " + result.getURL().toString());
				return result;
			}
		}
	}
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		for (CameraType ct : CameraType.values())
			res.add(ct);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.POSTPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Save result images";
	}
	
	@Override
	public String getDescription() {
		return "Saves result images (results need first to be moved from the mask-image-set to the image-set)";
	}
	
}
