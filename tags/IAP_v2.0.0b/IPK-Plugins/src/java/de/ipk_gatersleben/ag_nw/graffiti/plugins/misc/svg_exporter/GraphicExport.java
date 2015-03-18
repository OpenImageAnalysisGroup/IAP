package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.util.ArrayList;
import java.util.Collection;

import org.AttributeHelper;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class GraphicExport extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new PngJpegAlgorithm(true));
		res.add(new PngJpegAlgorithm(false));
		// res.add(new PDFSVGAlgorithm());
		
		boolean addAutomaticallyBorder = false;
		if (graph != null)
			addAutomaticallyBorder = (Boolean) AttributeHelper.getAttributeValue(graph, "", "background_coloring", new Boolean(false), new Boolean(false), false);
		
		res.add(new SVGAlgorithm(addAutomaticallyBorder));
		res.add(new PDFAlgorithm(addAutomaticallyBorder));
		return res;
	}
	
	@Override
	public String getName() {
		return "Create Image File...";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
}