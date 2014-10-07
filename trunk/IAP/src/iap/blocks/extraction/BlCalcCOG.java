package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImage;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import org.Vector2d;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */
public class BlCalcCOG extends AbstractBlock implements CalculatesProperties {
	
	private static final String COG_Y = "cog.y";
	private static final String COG_X = "cog.x";
	private static final String COG_AVG_DISTANCE_TO_IMAGE_CENTER_POINT = "cog.avg_distance_to_image_center_point";
	private static final String COG_AVG_DISTANCE_TO_VERTICAL_IMAGE_CENTER_LINE = "cog.avg_distance_to_vertical_image_center_line";
	private static final String COG_AVG_DISTANCE_TO_CENTER = "cog.avg_distance_to_center";
	
	@Override
	protected Image processMask(Image img) {
		if (img == null)
			return null;
		if (getBoolean("Process " + img.getCameraType(), true)) {
			if (img != null) {
				final Vector2d cog = img.io().stat().getCOG();
				if (cog != null) {
					CameraPosition pos = optionsAndResults.getCameraPosition();
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(pos, img.getCameraType(), COG_X), cog.x, "px", this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(pos, img.getCameraType(), COG_Y), cog.y, "px", this);
					
					Double averageDistance = img.io().stat().calculateAverageDistanceTo(cog);
					if (averageDistance != null)
						getResultSet().setNumericResult(0, new Trait(pos, img.getCameraType(), COG_AVG_DISTANCE_TO_CENTER),
								averageDistance, "px", this);
					
					if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE) {
						double centerX = img.getWidth() / 2d;
						getResultSet().setNumericResult(getBlockPosition(),
								new Trait(pos, img.getCameraType(), COG_AVG_DISTANCE_TO_VERTICAL_IMAGE_CENTER_LINE),
								Math.abs(cog.x - centerX), "px", this);
					}
					if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
						double centerX = img.getWidth() / 2d;
						double centerY = img.getHeight() / 2d;
						getResultSet().setNumericResult(getBlockPosition(),
								new Trait(pos, img.getCameraType(), COG_AVG_DISTANCE_TO_IMAGE_CENTER_POINT),
								Math.sqrt(Math.abs(cog.x - centerX) * Math.abs(cog.x - centerX)
										+ Math.abs(cog.y - centerY) * Math.abs(cog.y - centerY)), "px", this);
					}
				}
				RunnableOnImage runnableOnMask = new RunnableOnImage() {
					@Override
					public Image postProcess(Image in) {
						if (in != null & cog != null)
							return in.io().canvas().drawCircle((int) cog.x, (int) cog.y, 5, Color.BLACK.getRGB(), 0.5d, 1).getImage();
						else
							return in;
					}
				};
				getResultSet().addImagePostProcessor(img.getCameraType(), null, runnableOnMask);
			}
		}
		return img;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	public String getName() {
		return "Calculate Center of Gravity";
	}
	
	@Override
	public String getDescription() {
		return "Calculates the center of gravity and the average distance of the plant pixels to the center.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty(COG_X,
						"X-coordinate of the center of gravity of the foreground pixels"),
				new CalculatedProperty(COG_Y,
						"Y-coordinate of the center of gravity of the foreground pixels"),
				new CalculatedProperty(COG_AVG_DISTANCE_TO_CENTER,
						"Average distance of foreground pixels to the center of gravity of the foreground pixels."),
				new CalculatedProperty(COG_AVG_DISTANCE_TO_VERTICAL_IMAGE_CENTER_LINE,
						"Average distance of foreground pixels to a vertical line along the middle axis of the image."),
				new CalculatedProperty(COG_AVG_DISTANCE_TO_IMAGE_CENTER_POINT,
						"Average distance of foreground pixels to the center of the image.")
		};
	}
}
