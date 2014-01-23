package iap;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 *         Special commands of IAP which can be executed from the command line.
 */
public class Util {
	public static void main(String[] args) throws IOException {
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
				System.out.println("   [BlockClassName] [numerics output filename (CSV/XLSX) or . or NULL] "
						+ "([vis/fluo/nir/ir/vismask/fluomask/nirmask/irmask] [inputImageFileName] [outputImageFileName or . or NULL])*");
				System.out.println("MODE 2 convert R statistics file (specially formatted to an image:");
				System.out.println("   s2i ([inputStatisticsFile] [outputImageFileName] [{width}x{height} or NULL (detect size from input)])*");
				System.exit(1);
			case "s2i":
				convertStatisticsFilesToImages(args);
		}
		
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
		UtilText2Image.convertRstatFileToImage(inputFiles, outputFiles, imageSizeDefinition, ImageOperation.BACKGROUND_COLORint,
				Color.GREEN.getRGB(), Color.YELLOW.getRGB(),
				Color.PINK.getRGB(), Color.RED.getRGB());
	}
}
