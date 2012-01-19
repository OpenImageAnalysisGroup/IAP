package de.ipk_gatersleben.ag_pbi.mmd.visualisations.fluxdata;

import java.awt.Shape;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugins.views.defaults.StraightLineEdgeShape;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class DynamicStraightLineEdgeShape extends StraightLineEdgeShape {
	
	@Override
	protected double getEdgeThickness() {
		return getMappingValue();
	}
	
	@Override
	protected double getFrameThickness() {
		return getMappingValue();
	}
	
	@Override
	public Shape getHeadArrow() {
		if (getMappingValue() < 0)
			return tailArrow;
		else
			return headArrow;
	}
	
	@Override
	public Shape getTailArrow() {
		if (getMappingValue() < 0)
			return headArrow;
		else
			return tailArrow;
	}
	
	private double getMappingValue() {
		double multiplicator = (Double) AttributeHelper.getAttributeValue(((GraphElement) this.graphicsAttr
							.getAttributable()).getGraph(), "flux", "multiplicator", new Double(1d), new Double(1d));
		String seriesname = (String) AttributeHelper.getAttributeValue(((GraphElement) this.graphicsAttr
							.getAttributable()).getGraph(), "flux", "selectedcondition", "<none selected>", "");
		ExperimentInterface e = new GraphElementHelper((GraphElement) this.graphicsAttr.getAttributable())
							.getDataMappings();
		for (SubstanceInterface sub : e)
			for (ConditionInterface c : sub)
				if (c.getName().equals(seriesname))
					for (SampleInterface s : c)
						if (s.getSampleAverage() != null)
							return multiplicator * s.getSampleAverage().getValue() * graphicsAttr.getFrameThickness();
						else
							return multiplicator * s.iterator().next().getValue() * graphicsAttr.getFrameThickness();
		return 5;
	}
	
}