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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Executes a customized command in shell. IAP block n => image (vis, fluo, nir) -> external command => IAP block m.
 * The pattern for execution "command options [input] [output]".
 * In- and output images will attached automatically, it is not necessary to specify these.
 * The result string from external cmd must have a specified structure,
 * following the common IAP standard:
 * 
 * camera_position.trait_category.camera_type.traitname xyz [unit]
 * top.geometry.vis.area xyz [px]
 * 
 * @author Jean-Michel Pape
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
		Image manipulated_image = null;
		String string_output = "";
		boolean isFirst = true;
		
		for (int i = 1; i <= getInt("Number of individual Commands for " + ct.getNiceName(), 1); i++) {
			// generate random name
			String filename = UUID.randomUUID().toString();
			String tempDirPath = ReleaseInfo.getAppSubdirFolderWithFinalSep("scratch");
			String format = "png";
			
			// check actual run
			if (isFirst)
				saveImage(tempDirPath, filename, format , image);
			else
				saveImage(tempDirPath, filename, format , manipulated_image);
			
			File dir = new File(tempDirPath);
			String cmd = getString("Cmd " + ct.getNiceName() + " " + i, "");
			
			String inputNameForExternal = filename + "." + format;
			String outputNameForExternal = filename + "_processed." + format;
			
			boolean saveNumericResults = getBoolean("Save numeric results for cmd " + i, false);
			
			// command inserted?
			if (!cmd.contains("") || cmd.length() > 0) {
				if (debug)
					System.out.println("Run external Command " + ct.getNiceName() + " " + i + ": " + cmd  + " " + inputNameForExternal + " " + outputNameForExternal);
				
				Object[] exec_output = execute(dir, cmd, inputNameForExternal, outputNameForExternal, saveNumericResults );
				manipulated_image = (Image) exec_output[0];
				string_output = (String) exec_output[1];
			}
			
			if (manipulated_image != null)
				isFirst = false;
			
			// delete temp input
			new File(tempDirPath + "/" + filename + "." + format).delete();
			
			if (saveNumericResults)
				saveNumericOutputforCmd(string_output, getCameraPosition(), ct);
		}
		
		if (manipulated_image != null)
			return manipulated_image;
		else
			return image;
	}
	
	/**
	 * Parse result string and save to iap results. The result string from external cmd must have a specified structure,
	 * following the common IAP standard:
	 * 
	 * camera_position.trait_category.camera_type.traitname xyz [unit]
	 * top.geometry.vis.area xyz [px]
	 * 
	 * Enter null if not specified.
	 * 
	 * null.intensity.null...
	 * 
	 * In this case camera position and camera type will be replaced by the current information of the pipeline.
	 * 
	 * @param string_output - includes external results
	 * @param camera_type 
	 * @param cameraPosition 
	 */
	private void saveNumericOutputforCmd(String string_output, CameraPosition camera_position_iap, CameraType camera_type_iap) {
		// 
		String[] output_split = string_output.split("\n");
		for (String s : output_split) {
			String[] trait_entry = s.split("\\.");
			String camera_position_ex = trait_entry[0];
			String trait_category_ex = trait_entry[1];
			String camera_type_ex = trait_entry[2];
			String trait_name_ex = "";
			
			// add remaining to trait name ...
			for (int idx = 3; idx < trait_entry.length - 1; idx++)
				trait_name_ex += trait_entry[idx] + ".";

			// split last
			String[] last_vals = trait_entry[trait_entry.length - 1].split("\\s+");
			
			trait_name_ex += last_vals[0].replace(":", "");
			double value = new Double(last_vals[1]);
			String unit = last_vals[2];
			
			String camera_position = "";
			String camera_type = "";
			
			// compare and check values
			if (camera_position_ex.contains("null"))
				camera_position = camera_position_iap.toString();
			else
				camera_position = camera_position_ex;
			
			if (camera_type_ex.contains("null"))
				camera_type = camera_type_iap.toString();
			else
				camera_type = camera_type_ex;
				
			CameraPosition cp = CameraPosition.fromString(camera_position);
			CameraType ct = CameraType.fromString(camera_type);
			String u = (String) unit.subSequence(1, unit.length() - 1);
			
			ImageData info = (ct == CameraType.VIS) ? input().images().getVisInfo() :
				(ct == CameraType.FLUO) ? input().images().getFluoInfo() :
						(ct == CameraType.NIR) ? input().images().getNirInfo() :
								(ct == CameraType.IR) ? null : null;
						
			getResultSet().setNumericResult(getBlockPosition(),
				new Trait(cp, ct,
						TraitCategory.fromString(trait_category_ex), trait_name_ex), 
				value, 
				u,
				this,
				info);
		}
	}

	private Object[] execute(File dir, String cmd, String inputfile, String outputfile, boolean saveShellOutput) {
		Runtime shell = Runtime.getRuntime();
		Process process = null;
		InputStream inpSt = null;
		
		try {
			process = shell.exec(cmd + " " + inputfile + " " + outputfile, null, dir);
			inpSt = process.getInputStream();
		} catch (IOException ioe) {
			throw new RuntimeErrorException(new Error(ioe), "Cmd command brocken: " + cmd + " " + inputfile + " " + outputfile);
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
		
		if (debug)
			System.out.println("Shell output for " + cmd + " : " + readFromShell);
		
		Image output_image = null;
		
		// run external command, load and delete temp output image
		File f = new File(dir + "/" + outputfile);
		try {
			output_image = new Image(ImageIO.read(new File(dir + "/" + outputfile)));
			f.delete();
		} catch (IOException e) {
			try {
				output_image = new Image(ImageIO.read(new File(dir + "/" + inputfile)));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			f.delete();
		}
		
		Object[] obj = new Object[]{output_image, readFromShell};
		
		return obj;
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
		return "Run External Shell Command(s)";
	}
	
	@Override
	public String getDescription() {
		return "Executes a custom command in local shell. The command can be specified in a string option for each image modality, nothing will be executed in case of an empty String."
				+ " ('custom_command options ...' [inputpath] [outputpath] -- In- and output is handled by IAP, it has not to be specified."
				+ "Example: 'convert -negate' runs the invert script of ImageMagick®.)";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {};
	}

}
