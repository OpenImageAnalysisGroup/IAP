package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImage;
import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.ReleaseInfo;
import org.Vector2i;
import org.apache.commons.io.IOUtils;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.PNMEncodeParam;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.Lab;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
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
	private String unit;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processVISimage() {
		if (input() == null || input().images() == null || input().images().vis() == null)
			return null;
		
		Image vis = input().images().vis().copy();
		
		// check white or black background and calculate thresholded image for text detection
		boolean white = false;
		int background = new Color(255, 255, 255).getRGB();
		
		Lab labAvg = ImageOperation.getLabAverage(vis.getAs1A());
		
		if (debug)
			System.out.println("Avg L*: " + labAvg.getAverageL());
		
		if (labAvg.getAverageL() > 128) {
			white = true;
			// background = Color.WHITE.getRGB();
		}
		
		if (debug)
			System.out.println("Is white background: " + white);
		
		Image filtered = null;
		int threshold = 20;
		
		if (!white) {
			filtered = vis.copy().io().invert().thresholdLabBrightness(threshold, ImageOperation.BACKGROUND_COLORint, false).dilateNG(2)
					.binary(Color.BLACK.getRGB(), Color.WHITE.getRGB()).getImage();
			filtered = vis.io().applyMask(filtered, background).replaceColor(ImageOperation.BACKGROUND_COLORint, Color.BLACK.getRGB()).invert().getImage();
		} else {
			filtered = vis.copy().io().thresholdLabBrightness(threshold, ImageOperation.BACKGROUND_COLORint, false).dilateNG(2)
					.binary(Color.BLACK.getRGB(), Color.WHITE.getRGB())
					.getImage();
			filtered = vis.io().applyMask(filtered, background).replaceColor(ImageOperation.BACKGROUND_COLORint, 33554431).getImage();
		}
		
		filtered.show("Input for text detection", debug);
		
		// distHorizontal => detect bar width
		ClusterDetection cd = new ClusterDetection(filtered, 33554431);
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
			
			int barx = centers[minRatioPositionInClusterArray].x;
			int bary = centers[minRatioPositionInClusterArray].y;
			int barwidth = clusterDimensions[minRatioPositionInClusterArray].x;
			int barheight = clusterDimensions[minRatioPositionInClusterArray].y;
			
			filtered = filtered.io().cropAbs(barx - barwidth, barx + barwidth, (int) (bary - barwidth / 1.), bary - barheight / 2 - 1).getImage();
			
			filtered.show("for ocr", debug);
			String scaleUnit = DetectScaleUnit(filtered);
			
			if (debug) {
				ImageCanvas ic = new ImageCanvas(filtered);
				ic.drawCircle(barx, bary, 10, Color.RED.getRGB(), 0.0, 3);
				ic.getImage().show("Detected bar");
			}
			
			int valrs = -1;
			
			if (!scaleUnit.isEmpty() && scaleUnit.length() > 1) {
				// realMarkerDist => detected value
				Matcher matcher = Pattern.compile("[0-9]").matcher(scaleUnit);
				
				while (matcher.find()) {
					valrs = Integer.parseInt(matcher.group());
				}
				
				// get unit
				Matcher matcher_cm = Pattern.compile("(cm)|(cn)").matcher(scaleUnit);
				String cm = "";
				while (matcher_cm.find()) {
					cm = matcher_cm.group();
				}
				
				Matcher matcher_mm = Pattern.compile("(mm)|(nm)|(nn)|(mn)").matcher(scaleUnit);
				String mm = "";
				while (matcher_mm.find()) {
					mm = matcher_mm.group();
				}
				
				boolean used_cm = false;
				
				if (cm.length() > 0 && mm.length() == 0)
					used_cm = true;
				
				if (used_cm) {
					valrs = (int) (valrs * 10d);
				}
				
				if (debug)
					System.out.println("Real Marker Distance: " + valrs + " | mm: " + mm + " cm: " + cm);
				
			}
			
			CameraPosition pos = optionsAndResults.getCameraPosition();
			
			if (valrs > 0) {
				optionsAndResults.setCalculatedBlueMarkerDistance(clusterDimensions[minRatioPositionInClusterArray].x
						* optionsAndResults.getREAL_MARKER_DISTANCE() / valrs);
				getResultSet()
						.setNumericResult(getBlockPosition(), new Trait(pos, CameraType.VIS, TraitCategory.OPTICS, "ruler_length.detected"),
								clusterDimensions[minRatioPositionInClusterArray].x, "px", this, input().images().getVisInfo());
				getResultSet().setNumericResult(getBlockPosition(), new Trait(pos, CameraType.VIS, TraitCategory.OPTICS, "ruler_length.real"),
						valrs, unit, this, input().images().getVisInfo());
				
				final int minRatioPositionInClusterArray_fin = minRatioPositionInClusterArray;
				final int valrs_fin = valrs;
				RunnableOnImage runnableOnMask = new RunnableOnImage() {
					@Override
					public Image postProcess(Image in) {
						
						return in
								.io()
								.canvas()
								.text(centers[minRatioPositionInClusterArray_fin].x, centers[minRatioPositionInClusterArray_fin].y,
										"" + valrs_fin + "mm#############################", Color.MAGENTA,
										100)
								.getImage();
					}
				};
				BlockResultSet br = getResultSet();
				if (br != null && vis != null && vis.getCameraType() != null)
					br.addImagePostProcessor(vis.getCameraType(), null, runnableOnMask);
			} else {
				// nothing detected
				RunnableOnImage runnableOnMask = new RunnableOnImage() {
					@Override
					public Image postProcess(Image in) {
						
						return in
								.io()
								.canvas()
								.text(200, 300, "NO TEXT DETECTED!###############", Color.RED, 100)
								.getImage();
					}
				};
				BlockResultSet br = getResultSet();
				if (br != null && vis != null && vis.getCameraType() != null)
					br.addImagePostProcessor(vis.getCameraType(), null, runnableOnMask);
			}
		} else {
			// nothing detected
			RunnableOnImage runnableOnMask = new RunnableOnImage() {
				@Override
				public Image postProcess(Image in) {
					
					return in
							.io()
							.canvas()
							.text(200, 300, "NO BAR DETECTED!###############", Color.BLUE, 100)
							.getImage();
				}
			};
			BlockResultSet br = getResultSet();
			if (br != null && vis != null && vis.getCameraType() != null)
				br.addImagePostProcessor(vis.getCameraType(), null, runnableOnMask);
		}
		return input().images().vis();
	}
	
	private String DetectScaleUnit(Image filtered) {
		String filename = UUID.randomUUID().toString();
		String out = ReleaseInfo.getAppSubdirFolderWithFinalSep("scratch");
		saveImage(out, filename, "PNM", filtered);
		String gocr = "gocr -a 50 " + filename + ".PNM";
		File dir = new File(out);
		
		String resFromShell = execute(dir, gocr);
		
		if (debug)
			System.out.println("RAW detected: " + resFromShell);
		
		String result = filterForLengthScale(resFromShell);
		
		new File(out + filename + ".PNM").delete();
		
		return result;
	}
	
	private String filterForLengthScale(String input) {
		String out = "";
		Matcher matcher_mm = Pattern.compile("[0-9]+[ \t]?[_]?((mn)|(nm)|(nn)|(mm)|(cn)|(cm))").matcher(input);
		while (matcher_mm.find()) {
			// System.out.printf("%s an Position [%d,%d]%n",
			// matcher.group(),
			// matcher.start(), matcher.end());
			out = matcher_mm.group();
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
		saveImage(outputPath, name, format, img.getAsBufferedImage(name != null && name.toUpperCase().contains("PNG")));
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
			if (format.toUpperCase().equals("PNM")) {
				// Create the OutputStream.
				OutputStream out = new FileOutputStream(outputfile);
				
				// Create the ParameterBlock.
				PNMEncodeParam param = new PNMEncodeParam();
				param.setRaw(true);
				
				// Create the PNM image encoder.
				ImageEncoder encoder = ImageCodec.createImageEncoder("PNM", out, param);
				
				ParameterBlock pb = new ParameterBlock();
				pb.add(img);
				PlanarImage tPlanarImage = JAI.create("awtImage", pb);
				encoder.encode(tPlanarImage);
				out.close();
			} else
				ImageIO.write(img, format, outputfile);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
