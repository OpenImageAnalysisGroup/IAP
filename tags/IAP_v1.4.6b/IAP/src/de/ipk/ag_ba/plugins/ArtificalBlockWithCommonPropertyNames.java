package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class ArtificalBlockWithCommonPropertyNames extends AbstractBlock implements CalculatesProperties {
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return null;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.UNDEFINED;
	}
	
	@Override
	public String getName() {
		return "common or imported property";
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("water_weight", "Imported property, observed as a difference of the weight before and after watering."),
				new CalculatedProperty(
						"water_sum",
						"Calculated property from watering information. May not be accurate as it depends on calibration data and other factors which influence this value."),
				new CalculatedProperty("weight_before", "Imported property, weight data observed before a possible watering."),
				new CalculatedProperty("top.vis", "Data categorized as being sourced from a visible-light camera from top-view."),
				new CalculatedProperty("top.fluo", "Data categorized as being sourced from a fluorescence camera from top-view."),
				new CalculatedProperty("top.nir", "Data categorized as being sourced from a near-infrared camera from top-view."),
				new CalculatedProperty("top.ir", "Data categorized as being sourced from a infrared camera from top-view."),
				new CalculatedProperty("side.vis", "Data categorized as being sourced from a visible-light camera from side-view."),
				new CalculatedProperty("side.fluo", "Data categorized as being sourced from a fluorescence camera from side-view."),
				new CalculatedProperty("side.nir", "Data categorized as being sourced from a near-infrared camera from side-view."),
				new CalculatedProperty("side.ir", "Data categorized as being sourced from a infrared camera from side-view."),
				
				new CalculatedProperty(
						"Row Type",
						"Either 'side' or 'top' for data directly related to top or side images, or 'combined' for data, derived from multiple top and/or side images."),
				new CalculatedProperty("Angle", "Rotation angle for top and side view images and connected derived data."),
				new CalculatedProperty("Plant ID",
						"The identifier of the plant (corresponds to the plant carrier). If the image contains several plants, and "
								+ "the processing of these parts has been performed independently, the particular image segment is indicated with the suffix '_x', "
								+ "where 'x' is user defined ID (can be changed in the settings)."),
				new CalculatedProperty("Condition",
						"A combination of available metadata information for the different experimental factors (genotypes, treatments)."),
				new CalculatedProperty("Species", "The plant species (user provided metadata)."),
				new CalculatedProperty("Genotype", "The plant genotype (user provided metadata)."),
				new CalculatedProperty("Variety", "The plant species variety (user provided metadata)."),
				new CalculatedProperty("GrowthCondition", "The growth condition (user provided metadata)."),
				new CalculatedProperty("Treatment", "The plant treatment (user provided metadata)."),
				new CalculatedProperty("Sequence",
						"Metadata field, which can be used to store additional information, e.g. information about a particular experiment, "
								+ "consisting to a sequence of experiment runs."),
				new CalculatedProperty("Day", "A textual representation of the sample times including time point as a whole number and time unit (e.g. 'day 1')."),
				new CalculatedProperty("Time", "Exact sample time and date."),
				new CalculatedProperty("Day (Int)", "A whole number indicating the relative sample time (e.g. day '5')."),
				new CalculatedProperty("Day (Float)",
						"A floating point number, indicating the relative sample time. It is constructed from "
								+ "the whole day information and the relative sample time within the day. E.g. for day 3 10:00 AM, the value would be 3+10/24."),
				new CalculatedProperty("Weight A (g)", "If weight data is available, this value contains the measured weight before the watering."),
				new CalculatedProperty("Weight B (g)", "If weight data is available, this value contains the measured weight after the watering."),
				new CalculatedProperty("Water (weight-diff)",
						"If weight data is available, this value is the weight difference, observed as the result of the watering."),
				new CalculatedProperty(
						"Water (sum of day)",
						"Calculated property from watering information. May not be accurate as it depends on calibration data and other factors which influence this value."),
				new CalculatedProperty("RGB", "Name of the export file name of the corresponding visible-light camera image."),
				new CalculatedProperty("FLUO", "Name of the export file name of the corresponding fluresence camera image."),
				new CalculatedProperty("NIR", "Name of the export file name of the corresponding near-infrared camera image."),
				new CalculatedProperty("IR", "Name of the export file name of the corresponding infrared camera image."),
				new CalculatedProperty("OTHER", "Name of the export file name of camera image of unknown type."),
				new CalculatedProperty("RGB Config", "Name of the visible-light camera configuration."),
				new CalculatedProperty("FLUO Config", "Name of the fluresence camera configuration."),
				new CalculatedProperty("NIR Config", "Name of the near-infrared camera configuration."),
				new CalculatedProperty("IR Config", "Name of the infrared camera configuration."),
				new CalculatedProperty("OTHER Config", "Name of a camera configuration of an unknown type."),
		
		};
	}
	
	@Override
	protected Image processMask(Image mask) {
		return null;
	}
	
}
