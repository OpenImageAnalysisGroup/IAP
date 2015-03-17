package iap;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.pipelines.ImageProcessorOptionsAndResults;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.StringManipulationTools;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.util.InstanceLoader;

import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk.ag_ba.postgresql.CommandLineBackgroundTaskStatusProvider;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.OptionsGenerator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 *         Special commands of IAP which can be executed from the command line.
 */
public class Util {
	public static void main(String[] args) throws Exception {
		for (String info : IAPmain.getMainInfoLines())
			System.out.println(info);
		
		if (args == null || args.length == 0)
			args = new String[] { "help" };
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Execute command: " + StringManipulationTools.getStringList(args, " "));
		String cmd = args[0];
		switch (cmd) {
			case "help":
				System.out.println("Filenames: ");
				System.out.println("   [filename] - output to named file, [.] output to stdout, [NULL] no output");
				System.out.println("Multiple parameters:");
				System.out.println("   (...)* parameter sequence can be repeated multiple times or can be omitted");
				System.out.println("MODE 1 execute an image analysis block: ");
				System.out.println("   [BlockClassName1;BlockClassName2] [numerics output filename (CSV/XLSX) or . or NULL] "
						+ "([vis/fluo/nir/ir/vismask/fluomask/nirmask/irmask] [inputImageFileName] [outputImageFileName or . or NULL])*");
				System.out.println("MODE 2 convert R statistics file (specially formatted to an image:");
				System.out.println("   s2i ([inputStatisticsFile] [outputImageFileName] [{width}x{height} or NULL (detect size from input)])*");
				System.out.println("   - mode 2 currently not available (in development) -");
				System.exit(1);
			case "s2i":
				convertStatisticsFilesToImages(args);
				System.exit(0);
		}
		ArrayList<String> params = StringManipulationTools.getStringListFromArray(args);
		String blockNames = params.remove(0);
		OutputType ot = new OutputType(params.remove(0));
		BlockPipeline p = new BlockPipeline();
		for (String b : blockNames.split(";")) {
			if (b != null && !b.startsWith("#") && !b.trim().isEmpty()) {
				Class<?> c = Class.forName(b, true, InstanceLoader.getCurrentLoader());
				if (ImageAnalysisBlock.class.isAssignableFrom(c))
					p.add((Class<? extends ImageAnalysisBlock>) c);
				else
					System.out.println("WARNING: ImageAnalysisBlock " + b + " is not assignable to " + ImageAnalysisBlock.class.getCanonicalName()
							+ "! (block is not added to pipeline!)");
			}
		}
		SystemOptions settings = null;
		TreeMap<String, HashMap<String, BlockResultSet>> previousResultsForThisTimePoint = null;
		TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> plantResults = null;
		final ImageProcessorOptionsAndResults options = new ImageProcessorOptionsAndResults(settings, previousResultsForThisTimePoint, plantResults);
		final MaskAndImageSet input = new MaskAndImageSet(
				new ImageSet((Image) null, (Image) null, (Image) null, (Image) null),
				new ImageSet((Image) null, (Image) null, (Image) null, (Image) null));
		while (!params.isEmpty()) {
			// load and assign input images
			String inputTarget = params.remove(0);
			String inputFileName = params.remove(0);
			OutputType outputTarget = new OutputType(params.remove(0));
			
			CameraType cameraType = null;
			boolean mask = false;
			switch (inputTarget) {
				case "vismask":
					mask = true;
				case "vis":
					cameraType = CameraType.VIS;
					break;
				case "fluomask":
					mask = true;
				case "fluo":
					cameraType = CameraType.FLUO;
					break;
				case "nirmask":
					mask = true;
				case "nir":
					cameraType = CameraType.NIR;
					break;
				case "irmask":
					mask = true;
				case "ir":
					cameraType = CameraType.IR;
					break;
			
			}
			Image img = new Image(FileSystemHandler.getURL(new File(inputFileName)));
			img.setCameraType(cameraType);
			if (mask)
				input.masks().set(img);
			else
				input.images().set(img);
		}
		HashMap<String, BlockResultSet> blockResults = null;
		OptionsGenerator og = new OptionsGenerator() {
			
			@Override
			public ImageProcessorOptionsAndResults getOptions() {
				return options;
			}
			
			@Override
			public ImageSet getImageSet() {
				return input.images();
			}
			
			@Override
			public ImageSet getMaskSet() {
				return input.masks();
			}
		};
		p.execute(og, blockResults, new CommandLineBackgroundTaskStatusProvider(true));
	}
	
	private static void convertStatisticsFilesToImages(String[] args) throws IOException {
		ArrayList<String> inputFiles = new ArrayList<String>();
		ArrayList<String> outputFiles = new ArrayList<String>();
		ArrayList<String> imageSizeDefinition = new ArrayList<String>();
		int idx = -1;
		for (String a : args) {
			if (idx >= 0) {
				if (idx % 3 == 0)
					inputFiles.add(a);
				if (idx % 3 == 1)
					outputFiles.add(a);
				if (idx % 3 == 2)
					imageSizeDefinition.add(a);
			}
			idx++;
		}
		UtilText2Image.convertRstatFileToImage(inputFiles, outputFiles, imageSizeDefinition, Color.black.getRGB(),
				Color.BLUE.getRGB(), Color.YELLOW.getRGB(),
				Color.GREEN.getRGB(), Color.PINK.getRGB());
	}
}
