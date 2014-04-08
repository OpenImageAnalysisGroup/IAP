package iap.blocks.extraction;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * This class assumes, that the blue markers are detected in the visible light images,
 * coordinates of other images are accordingly recalculated, depending on
 * resolution differences between the camera systems.
 * The camera is assumed to be oriented strictly horizontal or vertical (center of image is 'hit' in 90 deg).
 * 
 * @author Christian Klukas
 */
public class Normalisation {
	
	int imgWidth = -1;
	int imgHeight = -1;
	private double conversionFactorImageDist2realWorldDist;
	private boolean valid;
	
	public Normalisation(Double real_marker_distance, Double calculated_marker_distance, ImageSet masks, CameraType ct) {
		this.conversionFactorImageDist2realWorldDist = real_marker_distance / calculated_marker_distance;
		
		if (masks.getImage(ct) == null || masks.vis() == null || real_marker_distance == null || calculated_marker_distance == null) {
			conversionFactorImageDist2realWorldDist = Double.NaN;
			this.valid = false;
		} else {
			switch (ct) {
				case VIS:
					// no action needed, blue markers assumed to be in visible light images
					imgWidth = masks.vis().getWidth();
					imgHeight = masks.vis().getHeight();
					break;
				case FLUO:
					conversionFactorImageDist2realWorldDist = conversionFactorImageDist2realWorldDist * masks.vis().getWidth() / masks.fluo().getWidth();
					imgWidth = masks.fluo().getWidth();
					imgHeight = masks.fluo().getHeight();
					break;
				case NIR:
					conversionFactorImageDist2realWorldDist = conversionFactorImageDist2realWorldDist * masks.vis().getWidth() / masks.nir().getWidth();
					imgWidth = masks.nir().getWidth();
					imgHeight = masks.nir().getHeight();
					break;
				case IR:
					conversionFactorImageDist2realWorldDist = conversionFactorImageDist2realWorldDist * masks.vis().getWidth() / masks.ir().getWidth();
					imgWidth = masks.ir().getWidth();
					imgHeight = masks.ir().getHeight();
					break;
				case UNKNOWN:
				default:
					throw new UnsupportedOperationException("Cant process yet unknown camera type");
			}
			valid = true;
		}
	}
	
	public boolean isRealWorldCoordinateValid() {
		return valid;
	}
	
	public int convertImgXtoRealWorldX(double x) {
		return (int) ((x - imgWidth / 2d) * conversionFactorImageDist2realWorldDist);
	}
	
	public int convertImgYtoRealWorldY(double y) {
		return (int) ((y - imgHeight / 2d) * conversionFactorImageDist2realWorldDist);
	}
	
	public int getImageXfromRealWorldX(int x) {
		return (int) (x / conversionFactorImageDist2realWorldDist) + imgWidth / 2;
	}
	
	public int getImageYfromRealWorldY(int y) {
		return (int) (y / conversionFactorImageDist2realWorldDist) + imgHeight / 2;
	}
}
