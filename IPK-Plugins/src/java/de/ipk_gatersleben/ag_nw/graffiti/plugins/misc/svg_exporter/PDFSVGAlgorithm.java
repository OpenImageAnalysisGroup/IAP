package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

public class PDFSVGAlgorithm extends AbstractAlgorithm {
	
	private static ExportType type = ExportType.PDF;
	private static Boolean addBorder = false;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new ObjectListParameter(type, "Vectorgraphics type", "Choose the type of vector graphics to be used", ExportType.values()),
							new BooleanParameter(addBorder, "Add image border (empty space around the image)",
												"<html>"
																	+ "If enabled, the graph will be surrounded by a border. The size of the border<br>" +
																	"will be the distance between the origin (0,0) and the most top-left graphelement.<br>" +
																	"To increase border size move the graph to lower-right") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int idx = 0;
		type = (ExportType) params[idx++].getValue();
		addBorder = ((BooleanParameter) params[idx++]).getBoolean();
	}
	
	@Override
	public void execute() {
		switch (type) {
			case PDF:
				GravistoService.getInstance().runAlgorithm(new PDFAlgorithm(addBorder), null);
				break;
			case SVG:
				GravistoService.getInstance().runAlgorithm(new SVGAlgorithm(addBorder), null);
				break;
		}
		
	}
	
	@Override
	public String getName() {
		return "Create PDF/SVG";
	}
	
	private enum ExportType {
		PDF, SVG
	}
	
}
