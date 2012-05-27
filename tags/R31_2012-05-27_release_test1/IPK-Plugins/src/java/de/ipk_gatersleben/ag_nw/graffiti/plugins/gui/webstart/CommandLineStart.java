package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.io.File;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter.PngJpegAlgorithm;

public class CommandLineStart {
	
	/**
	 * Example command line start method
	 * 
	 * @param args
	 */
	@SuppressWarnings("nls")
	public static void main(final String[] args) {
		ErrorMsg.addOnAddonLoadingFinishedAction(new Runnable() {
			public void run() {
				Graph graph;
				try {
					if (args.length > 0) {
						graph = MainFrame.getInstance().getGraph(new File(args[1]));
						PngJpegAlgorithm.createPNGimageFromGraph(graph);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				System.exit(0); // all OK
			}
		});
		new Main(false, (args.length > 0 ? args[0] : DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT), args, new String[] {});
	}
	
}
