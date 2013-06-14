/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd;

import java.net.URL;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TemplateFile;
import de.ipk_gatersleben.ag_pbi.datahandling.Template;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoader;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.fluxdata.FluxExperimentDataLoader;
import de.ipk_gatersleben.ag_pbi.mmd.fluxdata.FluxreactionAttribute;
import de.ipk_gatersleben.ag_pbi.mmd.fluxdata.FluxreactionAttributeComponent;
import de.ipk_gatersleben.ag_pbi.mmd.fluxdata.VisualiseFluxDataAlgorithm;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.ImageLoader;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.NetworkLoader;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.SpatialExperimentDataLoader;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.VolumeLoader;
import de.ipk_gatersleben.ag_pbi.mmd.visualisations.gradient.GradientCharts;

public class MultimodalDataHandlingAddon extends EditorPluginAdapter {
	
	@SuppressWarnings("unchecked")
	public MultimodalDataHandlingAddon() {
		
		registerHandlersAndTemplates();
		
		// an attribute to make the flux reaction nodes nicer
		attributeComponents.put(FluxreactionAttribute.class, FluxreactionAttributeComponent.class);
		StringAttribute.putAttributeType(FluxreactionAttribute.name, FluxreactionAttribute.class);
		
		// attributes for the gradient chart
		this.attributeDescriptions = new AttributeDescription[] {
							new AttributeDescription(
												"useCustomDomainSteps", BooleanAttribute.class,
												"<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>:" +
																	"<html>Domain Axis: <br>&nbsp;&nbsp;&nbsp;<small><!--A-->Custom Step Size",
												true, true, null),
							new AttributeDescription(
												"customDomainStepSize", DoubleAttribute.class,
												"<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>:" +
																	"<html>Domain Axis:  <br>&nbsp;&nbsp;&nbsp;<small><!--A-->Step Size",
												true, true, null),
								new AttributeDescription(
													"useCustomDomainBounds", BooleanAttribute.class,
													"<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>:" +
																		"<html>Domain Axis: <br>&nbsp;&nbsp;&nbsp;<small>Custom Min/Max",
													true, true, null),
										new AttributeDescription(
															"minBoundDomain", DoubleAttribute.class,
															"<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>:" +
																				"<html>Domain Axis: <br>&nbsp;&nbsp;&nbsp;<small>Minimum",
															true, true, null),
												new AttributeDescription(
																	"maxBoundDomain", DoubleAttribute.class,
																	"<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>:" +
																						"<html>Domain Axis: <br>&nbsp;&nbsp;&nbsp;Maximum",
																	true, true, null),
														new AttributeDescription(
																			"diagramTransparency", DoubleAttribute.class,
																			"<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>:" +
																								"Background Transparency",
																			true, true, null),
																new AttributeDescription(
																					"chartresolution", DoubleAttribute.class,
																					"Gradientchart:Resolution Factor",
																					true, true, null),
																		new AttributeDescription(
																							"plotlinethickness", DoubleAttribute.class,
																							"Gradientchart:Line Thickness",
																							true, true, null),
																							new AttributeDescription(
																									FluxreactionAttribute.name, StringAttribute.class,
																									"Flux:Size",
																									true, true, null)
		};
		
		algorithms = new Algorithm[] {
							new VisualiseFluxDataAlgorithm()
		};
		
		// does not work as intended
		// AttributeHelper.addEdgeShape("Dynamic Flux",
		// "de.ipk_gatersleben.ag_pbi.inputoutput.fluxdata.DynamicStraightLineEdgeShape");
		
		// this.views = new String[1];
		// this.views[0] =
		// "de.ipk_gatersleben.ag_pbi.inputoutput.fluxdata.FluxDataView";
		
	}
	
	private void registerHandlersAndTemplates() {
		// extend vanted experimentdatahierarchy
		DataMappingTypeManager3D.replaceVantedMappingTypeManager();
		
		// //gradient experiment data template (two columns) -> is not needed
		// anymore, use spatial experiment data loader
		// Template gradient = new Template();
		// gradient.setTemplateFile(new TemplateFile("Substance Gradient",
		// GravistoService.getResource(this.getClass(), "gradient", "xls"),
		// null));
		//
		// TemplateLoader tl = new GradientLoader();
		// tl.registerLoader();
		// gradient.setTemplateLoader(tl);
		// gradient.registerTemplate();
		
		// load images, networks and volumes
		Template volume = new Template();
		TemplateLoader tl = new VolumeLoader();
		tl.registerLoader();
		volume.setTemplateLoader(tl);
		volume.registerTemplate();
		
		Template network = new Template();
		tl = new NetworkLoader();
		tl.registerLoader();
		network.setTemplateLoader(tl);
		network.registerTemplate();
		
		Template image = new Template();
		tl = new ImageLoader();
		tl.registerLoader();
		image.setTemplateLoader(tl);
		image.registerTemplate();
		
		ResourceIOManager.registerIOHandler(LoadedDataHandler.getInstance());
		
		// spatial experiment data loader (includes also the possibility to
		// specify gradients)
		Template spatial = new Template();
		URL url = GravistoService.getResource(this.getClass(), "spatial_template", "xls");
		spatial.addTemplateFile(new TemplateFile("Spatial Experiment Data", url, null));
		url = GravistoService.getResource(this.getClass(), "spatial_template_transposed", "xls");
		spatial.addTemplateFile(new TemplateFile("Spatial Experiment Data (transposed)", url, null));
		
		tl = new SpatialExperimentDataLoader();
		spatial.setTemplateLoader(tl);
		for (GradientCharts gc : GradientCharts.values())
			spatial.addTemplateChartComponent(gc);
		tl.registerLoader();
		spatial.registerTemplate();
		
		// fluxes visualisation and template
		Template fluxtempalte = new Template();
		url = GravistoService.getResource(this.getClass(), "fluxdata/flux_template", "xls");
		fluxtempalte.addTemplateFile(new TemplateFile("Flux Data", url, null));
		
		FluxExperimentDataLoader fl = new FluxExperimentDataLoader();
		fluxtempalte.setTemplateLoader(fl);
		fl.registerLoader();
		fluxtempalte.registerTemplate();
		
		algorithms = new Algorithm[] {
							new VisualiseFluxDataAlgorithm()
		};
		
		// does not work as intended
		// AttributeHelper.addEdgeShape("Dynamic Flux",
		// "de.ipk_gatersleben.ag_pbi.inputoutput.fluxdata.DynamicStraightLineEdgeShape");
		
		// this.views = new String[1];
		// this.views[0] =
		// "de.ipk_gatersleben.ag_pbi.inputoutput.fluxdata.FluxDataView";
		
	}
	
	@Override
	public ImageIcon getIcon() {
		try {
			ImageIcon icon = new ImageIcon(GravistoService.getResource(this.getClass(), "icon", "png"));
			if (icon != null)
				return icon;
			else
				return super.getIcon();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return super.getIcon();
		}
	}
}
