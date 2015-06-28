package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class BlSpotMatcher extends AbstractBlock {
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		for (CameraType ct : CameraType.values())
			res.add(ct);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Circular Spot Matcher (L*a*b*)";
	}
	
	@Override
	public String getDescription() {
		return "Detect round spots in L/a/b color space. Spot size can be specified.";
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null && getBoolean("Process " + mask.getCameraType(), false)) {
			Image lu = mask.io().channels().getLabL().getImage();
			int w = mask.getWidth();
			int h = mask.getHeight();
			int[][] img = lu.getAs2A();
			
			int foreground = 0;
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int color = img[x][y];
					if (color != ImageOperation.BACKGROUND_COLORint) {
						foreground++;
					}
				}
			}
			
			double scanRadius = getDouble("Scan Radius Per Mille of Forground Area", 10);
			scanRadius = Math.sqrt(scanRadius / 1000 * foreground / Math.PI);
			
			float[] lValues = mask.io().channels().getLabFloatArray(Channel.LAB_L);
			float[] aValues = mask.io().channels().getLabFloatArray(Channel.LAB_A);
			float[] bValues = mask.io().channels().getLabFloatArray(Channel.LAB_B);
			
			float[] values = null;
			ArrayList<String> validStrings = new ArrayList<>(3);
			validStrings.add("L*a*b - L");
			validStrings.add("L*a*b - a");
			validStrings.add("L*a*b - b");
			String sel = getStringRadioSelection("Color Channel", "L*a*b - L", validStrings);
			if (sel.endsWith("L"))
				values = lValues;
			else
				if (sel.endsWith("a"))
					values = aValues;
				else
					if (sel.endsWith("b"))
						values = bValues;
			
			boolean darkSpots = getBoolean("Look for Dark Spots", true);
			double max = 0;
			
			int sr = (int) scanRadius;
			boolean[][] okMaskO = new boolean[(int) (scanRadius * 2) + 1][(int) (scanRadius * 2) + 1];
			boolean[][] okMaskI = new boolean[(int) (scanRadius * 2) + 1][(int) (scanRadius * 2) + 1];
			for (int offX = -sr; offX <= sr; offX++) {
				for (int offY = -sr; offY <= scanRadius; offY++) {
					if ((offX + offY) % 10 != 0)
						continue;
					double dist = Math.sqrt(offX * offX + offY * offY);
					if (dist < scanRadius)
						okMaskO[offX + sr][offY + sr] = true;
					if (dist < scanRadius / 2)
						okMaskI[offX + sr][offY + sr] = true;
					
				}
			}
			DescriptiveStatistics statI = new DescriptiveStatistics();
			DescriptiveStatistics statO = new DescriptiveStatistics();
			int sr2 = sr * 2;
			int colSkip = new Color(0, 0, 0).getRGB();
			for (int x = sr; x < w - sr; x++) {
				for (int y = sr; y < h - sr; y++) {
					// if (x % 20 != 0 || y % 20 != 0) {
					// if ((x + y) % 3 != 0) {
					// img[x][y] = colSkip;
					// continue;
					// }
					int color = img[x][y];
					if (color != ImageOperation.BACKGROUND_COLORint) {
						// float innerN = 0;
						// float outerN = 0;
						// float innerSum = 0;
						// float outerSum = 0;
						statI.clear();
						statO.clear();
						for (int offX = 0; offX <= sr2; offX++) {
							for (int offY = 0; offY <= sr2; offY++) {
								if (okMaskO[offX][offY]) {
									int xi = x + offX - sr;
									int yi = y + offY - sr;
									float v = values[xi + yi * w];
									if (okMaskI[offX][offY]) {
										// inner circle
										// innerN++;
										// innerSum += v;
										statI.addValue(v);
									} else {
										// outer circle
										// outerN++;
										// outerSum += v;
										statO.addValue(v);
									}
									if (v > max)
										max = v;
								}
							}
						}
						float averageInner = (float) statI.getPercentile(50);// innerSum / innerN;
						float averageOuter = (float) statO.getPercentile(50);// outerSum / outerN;
						
						float r;
						if (darkSpots) {
							r = (float) (Math.log(averageOuter) - Math.log(averageInner));
						} else {
							r = (float) (Math.log(averageInner) - Math.log(averageOuter));
						}
						r = r * 4;
						// r = (float) (r / statO.getStandardDeviation());
						if (r < 0)
							r = 0;
						else
							if (r > 1f)
								r = 1f;
						
						img[x][y] = new Color(r, r, r).getRGB();
						
					}
				}
			}
			
			final double srf = scanRadius;
			getResultSet().addImagePostProcessor(mask.getCameraType(), (RunnableOnImage) null, new RunnableOnImage() {
				@Override
				public Image postProcess(Image in) {
					int sr = (int) srf;
					in = in.io().canvas().fillCircle(w - sr, sr, sr, Color.YELLOW.getRGB(), 0, 1)
							.fillCircle(w - sr / 2, sr / 2, sr, Color.RED.getRGB(), 0, 1).getImage();
					return in;
				}
			});
			
			return new Image(img);
		} else
			return mask;
	}
}
