package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import javax.swing.JComboBox;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ComboBoxEditComponent;

public class NodePathwayLinkVisualizationAttributeEditor extends ComboBoxEditComponent {
	
	public NodePathwayLinkVisualizationAttributeEditor(Displayable disp) {
		super(disp);
		this.comboText = new String[] { "Off", "Segments", "Dots" };
		this.comboValue = new String[]
												{
																	"mode0",
																	"mode1",
																	"mode2"
												};
		this.comboBox = new JComboBox(this.comboText);
	}
}
