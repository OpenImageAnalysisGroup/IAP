package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation;

import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

public class CubeGraphGenerator extends AbstractAlgorithm {
	int width = 5;
	int height = 5;
	int depth = 5;
	boolean fillSurface = true;
	boolean fillInside = false;
	boolean rgbColors = true;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(width, "Steps in X direction", null),
							new IntegerParameter(height, "Steps in Y direction", null),
							new IntegerParameter(depth, "Steps in Z direction", null),
							new BooleanParameter(fillSurface, "Fill Surface", "If enabled, the surface is filled with nodes"),
							new BooleanParameter(fillInside, "Fill Inside", "If enabled, the inner parts are filled with nodes"),
							new BooleanParameter(fillInside, "RGB Colors", "If enabled, the node color is dependent on the X/Y/Z value") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		//
		super.setParameters(params);
	}
	
	public void execute() {
		
	}
	
	public String getName() {
		return "Generate Grid / Cube";
	}
	
}
