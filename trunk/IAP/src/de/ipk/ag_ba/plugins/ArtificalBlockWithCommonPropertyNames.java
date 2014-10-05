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
		};
	}
	
	@Override
	protected Image processMask(Image mask) {
		return null;
	}
	
}
