// ==============================================================================
// Copyright (c) 2006 IPK
// ==============================================================================

package org.graffiti.plugins.editcomponents.defaults;

import java.awt.Dimension;

import javax.swing.JComboBox;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ComboBoxEditComponent;

/**
 * @author klukas
 */
public class EdgeArrowShapeEditComponent
					extends ComboBoxEditComponent {
	public static String standardArrow = "org.graffiti.plugins.views.defaults.StandardArrowShape";
	public static String standardArrowLeft = "org.graffiti.plugins.views.defaults.StandardArrowShapeLeft";
	public static String standardArrowRight = "org.graffiti.plugins.views.defaults.StandardArrowShapeRight";
	public static String thinStandardArrow = "org.graffiti.plugins.views.defaults.ThinStandardArrowShape";
	public static String circleArrow = "org.graffiti.plugins.views.defaults.CircleArrowShape";
	public static String thinCircleArrow = "org.graffiti.plugins.views.defaults.ThinCircleArrowShape";
	public static String circleConnectArrow = "org.graffiti.plugins.views.defaults.CircleConnectArrowShape";
	public static String diamondArrow = "org.graffiti.plugins.views.defaults.DiamondArrowShape";
	public static String thinDiamondArrow = "org.graffiti.plugins.views.defaults.ThinDiamondArrowShape";
	public static String inhibitorArrow = "org.graffiti.plugins.views.defaults.InhibitorArrowShape";
	public static String triggerArrow = "org.graffiti.plugins.views.defaults.ThinTriggerArrowShape";
	public static String absoluteInhibitorArrow = "org.graffiti.plugins.views.defaults.AbsoluteInhibitorArrowShape";
	public static String absoluteStimulationArrow = "org.graffiti.plugins.views.defaults.ThinAbsoluteStimulationArrowShape";
	public static String assignmentArrow = "org.graffiti.plugins.views.defaults.AssignmentArrowShape";
	
	public EdgeArrowShapeEditComponent(Displayable disp) {
		super(disp);
		this.comboText = new String[]
												{
																	"-->",
																	"--l>",
																	"--r>",
																	"-+>",
																	"-<+>",
																	"--<>",
																	"--(+)",
																	"--o",
																	"-o|",
																	"--|",
																	"-||",
																	"->>",
																	"--/",
																	"-|>",
																	"---"
												};
		this.comboValue = new String[]
												{
														standardArrow,
														standardArrowLeft,
														standardArrowRight,
																	thinStandardArrow,
																	thinDiamondArrow,
																	diamondArrow,
																	thinCircleArrow,
																	circleArrow,
																	circleConnectArrow,
																	inhibitorArrow,
																	absoluteInhibitorArrow,
																	absoluteStimulationArrow,
																	assignmentArrow,
																	triggerArrow,
																	""
												};
		this.comboBox = new JComboBox(this.comboText) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMinimumSize() {
				Dimension res = super.getMinimumSize();
				res.setSize(20, res.getHeight());
				return res;
			}
			
			@Override
			public Dimension getPreferredSize() {
				Dimension res = super.getPreferredSize();
				res.setSize(20, res.getHeight());
				return res;
			}
		};
		this.comboBox.setRenderer(new ArrowShapeCellRenderer());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
