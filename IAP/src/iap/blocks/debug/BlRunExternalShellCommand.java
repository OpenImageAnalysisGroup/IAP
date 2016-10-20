package iap.blocks.debug;

import iap.blocks.data_structures.AbstractBlock;
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
 * Executes a customized command in shell. IAP block n => image (vis, fluo, nir) -> external command => IAP block m 
 * 
 * @author Pape
 */
public class BlRunExternalShellCommand extends AbstractBlock implements CalculatesProperties {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processImage(Image image) {
		if (image == null)
			return image;
		
		CameraType ct = image.getCameraType();
		String filename = UUID.randomUUID().toString();
		String tempDir = ReleaseInfo.getAppSubdirFolderWithFinalSep("scratch");
		String format = "png";
		saveImage(tempDir, filename, format , image);
		File dir = new File(tempDir);
		String cmd = getString("Cmd " + ct.getNiceName(), "");
		
		Image manipulated_image = null;
		
		// System.out.println("Command: " + cmd  + " " + filename + "." + format + " " + filename + "_processed." + format);
		
		if (!cmd.contains("") || cmd.length() > 0)
			manipulated_image = execute(dir, cmd, filename + "." + format, filename + "_processed." + format);
		
		new File(tempDir + "/" + filename + "." + format).delete();
		
		if (manipulated_image != null)
			return manipulated_image;
		else
			return image;
	}
	
	private Image execute(File dir, String cmd, String inputfile, String outputfile) {
		Runtime shell = Runtime.getRuntime();
		Process process = null;
		InputStream inpSt = null;
		
		try {
			process = shell.exec(cmd + " " + inputfile + " " + outputfile, null, dir);
			inpSt = process.getInputStream();
		} catch (IOException ioe) {
			throw new RuntimeErrorException(new Error(ioe), "Cmd command brocken: " + cmd);
		}
		
		String readFromShell = "";
		
		if (inpSt != null) {
			try {
				readFromShell = IOUtils.toString(inpSt, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeErrorException(new Error(e), e.getMessage());
			}
		} else
			throw new RuntimeErrorException(new Error("No input stream from external program."));
		
		// System.out.println("Shell output: " + readFromShell);
		
		Image output = null;
		
		// System.out.println("load: " + dir + outputfile);
		
		try {
			File f = new File(dir + "/" + outputfile);
			output = new Image(ImageIO.read(f));
			f.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output;
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
	protected Image processMask(Image mask) {
		return mask;
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
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Run External Shell Command";
	}
	
	@Override
	public String getDescription() {
		return "Executes a custom command in local shell.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {};
	}

}
