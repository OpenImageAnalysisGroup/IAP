package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;

import org.ReleaseInfo;
import org.Vector2i;
import org.apache.commons.io.IOUtils;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.Lab;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Detects Scale (like a map key) for example "2mm" in an image (only works for this style of naming). The scale should be orientated in a horizontal direction.
 * The legend should be added by digital image editing tool and is recommended to use a "straight" font like "Arial". The block is tested for linux system and
 * also requires the package GOCR for text detection (available for linux and windows, project webpage: http://jocr.sourceforge.net/).
 * 
 * @author Pape
 */
public class BlDetectScaleforNormalization extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processVISimage() {
		if (input() == null || input().images() == null)
			return null;
		
		Image vis = input().images().vis().copy();
		
		// check white or black background and calculate thresholded image for text detection
		boolean white = false;
		int background = Color.BLACK.getRGB();
		
		Lab labAvg = ImageOperation.getLabAverage(vis.getAs1A());
		
		if (debug)
			System.out.println("Avg L*: " + labAvg.getAverageL());
		
		if (labAvg.getAverageL() > 128) {
			white = true;
			background = Color.WHITE.getRGB();
		}
		
		if (debug)
			System.out.println("Is white background: " + white);
		
		Image filtered = null;
		
		if (!white)
			filtered = vis.io().thresholdLabBrightness(240, background, true).getImage();
		else
			filtered = vis.io().thresholdLabBrightness(10, background, false).getImage();
		
		filtered.show("Input for text detection", debug);
		String scaleUnit = DetectScaleUnit(filtered);
		
		if (!scaleUnit.isEmpty() && scaleUnit.length() > 1) {
			// realMarkerDist => detected value
			Matcher matcher = Pattern.compile("[0-9]").matcher(scaleUnit);
			int valrs = -1;
			while (matcher.find()) {
				valrs = Integer.parseInt(matcher.group());
			}
			
			if (debug)
				System.out.println("Real Marker Distance: " + valrs);
			
			if (valrs != -1) {
				
				// distHorizontal => detect bar width
				ClusterDetection cd = new ClusterDetection(filtered, background);
				cd.detectClusters();
				
				if (cd.getClusterCount() > 0) {
					Vector2i[] clusterDimensions = cd.getClusterDimension();
					Vector2i[] centers = cd.getClusterCenterPoints();
					
					// object with min w / h ratio is some kind of longest bar
					double minRatio = Double.MAX_VALUE;
					int minRatioPositionInClusterArray = -1;
					for (int i = 0; i < clusterDimensions.length; i++) {
						// watch out cluster dimensions changed!!
						double ratio = clusterDimensions[i].y / (double) clusterDimensions[i].x;
						if (ratio < minRatio && clusterDimensions[i].y > 0 && clusterDimensions[i].x > 0) {
							minRatio = ratio;
							minRatioPositionInClusterArray = i;
						}
					}
					
					if (debug) {
						ImageCanvas ic = new ImageCanvas(filtered);
						ic.drawCircle(centers[minRatioPositionInClusterArray].x, centers[minRatioPositionInClusterArray].y, 10, Color.RED.getRGB(), 0.0, 3);
						ic.getImage().show("Detected bar");
					}
					
					CameraPosition pos = optionsAndResults.getCameraPosition();
					
					optionsAndResults.setCalculatedBlueMarkerDistance(clusterDimensions[minRatioPositionInClusterArray].x
							* optionsAndResults.getREAL_MARKER_DISTANCE() / valrs);
					getResultSet()
							.setNumericResult(getBlockPosition(), new Trait(pos, CameraType.VIS, TraitCategory.OPTICS, "ruler_length.detected"),
									clusterDimensions[minRatioPositionInClusterArray].x, "px", this, input().images().getVisInfo());
					getResultSet().setNumericResult(getBlockPosition(), new Trait(pos, CameraType.VIS, TraitCategory.OPTICS, "ruler_length.real"),
							valrs, "mm", this, input().images().getVisInfo());
				}
			}
			
		}
		return input().images().vis();
	}
	
	private synchronized String DetectScaleUnit(Image filtered) {
		String out = ReleaseInfo.getAppSubdirFolderWithFinalSep("scratch");
		saveImage(out, "temp", "png", filtered);
		String gocr = "gocr temp.png";
		File dir = new File(out);
		
		String resFromShell = execute(dir, gocr);
		
		if (debug)
			System.out.println("RAW detected: " + resFromShell);
		
		String result = filterForLengthScale(resFromShell);
		
		new File(out + "temp.png").delete();
		
		return result;
	}
	
	private String filterForLengthScale(String input) {
		String out = "";
		Matcher matcher = Pattern.compile("[0-9][ \t]?[mn][mn]").matcher(input);
		while (matcher.find()) {
			// System.out.printf("%s an Position [%d,%d]%n",
			// matcher.group(),
			// matcher.start(), matcher.end());
			out = matcher.group();
		}
		return out;
	}
	
	private String execute(File dir, String cmd) {
		Runtime shell = Runtime.getRuntime();
		Process prozess = null;
		InputStream inp = null;
		
		try {
			prozess = shell.exec(cmd, null, dir);
			inp = prozess.getInputStream();
		} catch (IOException ioe) {
			throw new RuntimeErrorException(new Error(ioe), "Cmd command brocken: " + cmd);
		}
		
		String readFromShell = "";
		
		if (inp != null) {
			try {
				readFromShell = IOUtils.toString(inp, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeErrorException(new Error(e), e.getMessage());
			}
		} else
			throw new RuntimeErrorException(new Error("No input stream from external program."));
		
		return readFromShell;
	}
	
	private static synchronized void saveImage(String outputPath, String name, String format, Image img) {
		saveImage(outputPath, name, format, img.getAsBufferedImage());
	}
	
	private static synchronized void saveImage(String outputPath, String name, String format, BufferedImage img) {
		File path = new File(outputPath);
		boolean pathOK = true;
		if (!path.exists())
			pathOK = path.mkdirs();
		if (pathOK == false) {
			System.out.println("Path incorrect, no image has been written!");
			return;
		}
		if (!outputPath.endsWith("/"))
			outputPath = outputPath + "/";
		File outputfile = new File(outputPath + name + "." + format);
		try {
			ImageIO.write(img, format, outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Detect Legend";
	}
	
	@Override
	public String getDescription() {
		return "Detects Legend in an image (for example \"2mm\")";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("optics.ruler_length.detected", "If scale is visible in the image, this value saves the length in pixel."),
				new CalculatedProperty("optics.ruler_length.real",
						"If scale is visible in the image, this value saves the detected real length (for example 3 mm).")
		};
	}
}
