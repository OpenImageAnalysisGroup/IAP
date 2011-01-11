/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * Analysis of the image
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockDataAnalysis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		BufferedImage clrearRgbImage = dataAnalysis(getInput().getMasks().getVis().getBufferedImage(),
				ImageConfiguration.RgbTop,
				options.getRgbEpsilonA(), options.getRgbEpsilonB(), options.getMaxThreadsPerImage());
		return new FlexibleImage(clrearRgbImage, FlexibleImageType.VIS);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		BufferedImage clearFluorImage = dataAnalysis(getInput().getMasks().getFluo().getBufferedImage(),
				ImageConfiguration.FluoTop,
				options.getRgbEpsilonA(), options.getRgbEpsilonB(), options.getMaxThreadsPerImage());
		return new FlexibleImage(clearFluorImage, FlexibleImageType.FLUO);
		
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// FlexibleImage clearFluorImage = removeSmallObjects(getInput().getMasks().getNir());
	// return clearFluorImage;
	//
	// }
	
	private BufferedImage dataAnalysis(BufferedImage workImage, ImageConfiguration cameraTyp, double epsilonA,
			double epsiolonB, int maxThreadsPerImage) {
		
		SubstanceInterface substance = new Substance();
		substance.setName(cameraTyp.toString());
		ConditionInterface condition = new Condition(substance);
		Sample sample = new Sample(condition);
		LoadedImage limg = new LoadedImage(sample, workImage);
		limg.setURL(new IOurl(""));
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, maxThreadsPerImage, null, null, true, output, null,
				epsilonA, epsiolonB);
		
		return workImage;
	}
	
}