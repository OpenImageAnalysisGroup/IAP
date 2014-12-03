package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operation.ImageHistogram;
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
				LoadedImage res = processAndOrSaveResultImage(image.getCameraType(), getCameraPosition(), outImageReference, image);
				if (res != null) {
					if (!res.getParentSample().getParentCondition().getParentSubstance().getName().contains(image.getCameraType() + "")) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Saved camera type " + image.getCameraType() + " to substance "
								+ res.getParentSample().getParentCondition().getParentSubstance().getName());
					}
					if (image.getCameraType() != CameraType.NIR) {
						ImageHistogram ih = image.io().histogram(false);
						if (ih.getMostCommonValueR() == ih.getMostCommonValueG() && ih.getMostCommonValueG() == ih.getMostCommonValueB())
							System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Saved camera type " + image.getCameraType() + " to substance "
									+ res.getParentSample().getParentCondition().getParentSubstance().getName() + ". R=G=B, so NIR image suspected! Image Size: "
									+ image.getWidth() + "x" + image.getHeight() + ", not transparent pixels: " + image.io().countFilledPixels());
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
	
	private LoadedImage processAndOrSaveResultImage(CameraType ct, CameraPosition cp, ImageData outImageReference, Image resImage) throws Exception {
		String tray = getWellIdx();
		
		if (optionsAndResults.forceDebugStack) {
			return null;
		} else {
			if (outImageReference != null && outImageReference.getLabelURL() != null)
				outImageReference.addAnnotationField("oldreference", outImageReference.getLabelURL().toString());
			
			if (outImageReference != null && outImageReference.getURL() != null)
				outImageReference.setLabelURL(outImageReference.getURL().copy());
			
			outImageReference.setURL(new IOurl(null, null, outImageReference.getURL().getFileName()));
			
			return saveImage(ct, cp, tray, outImageReference, resImage);
		}
	}
	
	private LoadedImage saveImage(CameraType ct, CameraPosition cp,
			final String tray,
			final ImageData id, Image image) throws Exception {
		if (id != null && id.getParentSample() != null) {
			image = markWithImageInfos(image, id, optionsAndResults, getWellIdx());
			LoadedImage loadedImage = new LoadedImage(id, image.getAsBufferedImage(true));
			// loadedImage.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
			return saveImageAndUpdateURL(ct, cp, loadedImage, optionsAndResults.databaseTarget, false, tray);
		} else
			return null;
	}
	
	public static Image markWithImageInfos(Image image, ImageData id, ImageProcessorOptionsAndResults optionsAndResults, String wellidx) {
		Long tt = id.getParentSample().getSampleFineTimeOrRowId();
		String t = "";
		if (tt != null) {
			Date date = new Date(tt);
			t = new SimpleDateFormat("HH:mm").format(date);
		}
		String wells = "";
		if (optionsAndResults.getWellCnt() > 1) {
			wells = " " + wellidx + " " + (optionsAndResults.getWellIdx() + 1) + "/" + optionsAndResults.getWellCnt();
		}
		String r = id.getPosition() != null ? id.getPosition().intValue() + "" : "0";
		image = image.io().canvas()
				.text(5, 15, "IAP V" + ReleaseInfo.IAP_VERSION_STRING, Color.RED)
				.text(5, 30, id.getQualityAnnotation() + wells + " R" + id.getReplicateID(), Color.ORANGE)
				.text(5, 45, image.getCameraType() + " " + optionsAndResults.getCameraPosition() + " " + r, Color.YELLOW)
				.text(5, 60, id.getParentSample().getSampleTime() + " " + t, Color.GREEN)
				.getImage();
		return image;
	}
	
	private String addTrayInfo(CameraType ct, CameraPosition cp, String tray, String fileName, ImageData image) {
		if (tray != null && tray.length() > 0) {
			String extension = fileName.substring(fileName.lastIndexOf(".") + ".".length());
			
			String replace;
			
			replace = "." + ct + "." + cp + "." + tray + "." + image.getParentSample().getSampleTime() + ".";
			fileName = StringManipulationTools.stringReplace(fileName,
					"." + extension, replace
							+ extension);
		} else {
			String extension = fileName.substring(fileName.lastIndexOf(".") + ".".length());
			
			String replace;
			
			replace = "." + ct + "." + cp + "." + image.getParentSample().getSampleTime() + ".";
			fileName = StringManipulationTools.stringReplace(fileName,
					"." + extension, replace
							+ extension);
		}
		return fileName;
	}
	
	protected LoadedImage saveImageAndUpdateURL(CameraType ct, CameraPosition cp, LoadedImage result,
			DatabaseTarget databaseTarget, boolean processLabelUrl,
			String tray) throws Exception {
		if (result.getURL() == null)
			result.setURL(new IOurl(null, StringManipulationTools.removeFileExtension(result.getURL().getFileName())
					+ SystemOptions.getInstance().getString("IAP", "Result File Type", "png")));
		
		result.getURL().setFileName(addTrayInfo(ct, cp, tray, result.getURL().getFileName(), result));
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (result.getLabelURL() != null && processLabelUrl) {
			result.getLabelURL().setFileName(
					addTrayInfo(ct, cp, tray,
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
	public boolean isChangingImages() {
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
