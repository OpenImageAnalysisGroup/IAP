package de.ipk.ag_ba.image.structures;

import java.util.ArrayList;

import org.StringManipulationTools;

public class ImageSetDescription {
	
	private final ImageDescription visDesc;
	private final ImageDescription fluoDesc;
	private final ImageDescription nirDesc;
	private final ImageDescription irDesc;
	
	private final ImageDescription visMaskDesc;
	private final ImageDescription fluoMaskDesc;
	private final ImageDescription nirMaskDesc;
	private final ImageDescription irMaskDesc;
	
	public ImageSetDescription(MaskAndImageSet baseForComparison) {
		visDesc = new ImageDescription(baseForComparison.images().vis());
		fluoDesc = new ImageDescription(baseForComparison.images().fluo());
		nirDesc = new ImageDescription(baseForComparison.images().nir());
		irDesc = new ImageDescription(baseForComparison.images().ir());
		
		visMaskDesc = new ImageDescription(baseForComparison.masks().vis());
		fluoMaskDesc = new ImageDescription(baseForComparison.masks().fluo());
		nirMaskDesc = new ImageDescription(baseForComparison.masks().nir());
		irMaskDesc = new ImageDescription(baseForComparison.masks().ir());
	}
	
	public boolean isDifferentTo(MaskAndImageSet compareWith) {
		return !getDifferenceDescription(compareWith).isEmpty();
	}
	
	public String getDifferenceDescription(MaskAndImageSet compareWith) {
		ArrayList<String> changes = new ArrayList<String>();
		StringManipulationTools.addIfNotEmpty(changes, visDesc.getChange("vis img", compareWith.images().vis()));
		StringManipulationTools.addIfNotEmpty(changes, fluoDesc.getChange("fluo img", compareWith.images().fluo()));
		StringManipulationTools.addIfNotEmpty(changes, nirDesc.getChange("nir img", compareWith.images().nir()));
		StringManipulationTools.addIfNotEmpty(changes, irDesc.getChange("ir img", compareWith.images().ir()));
		
		StringManipulationTools.addIfNotEmpty(changes, visMaskDesc.getChange("vis msk", compareWith.masks().vis()));
		StringManipulationTools.addIfNotEmpty(changes, fluoMaskDesc.getChange("fluo msk", compareWith.masks().fluo()));
		StringManipulationTools.addIfNotEmpty(changes, nirMaskDesc.getChange("nir msk", compareWith.masks().nir()));
		StringManipulationTools.addIfNotEmpty(changes, irMaskDesc.getChange("ir msk", compareWith.masks().ir()));
		
		return StringManipulationTools.getStringList(changes, "\n");
	}
	
}
