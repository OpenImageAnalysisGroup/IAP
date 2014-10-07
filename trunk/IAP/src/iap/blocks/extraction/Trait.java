package iap.blocks.extraction;

import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import de.ipk.ag_ba.image.structures.CameraType;

public class Trait {
	
	private final String name;
	
	public Trait(CameraPosition cameraPosition, CameraType ct, TraitCategory cat, String trait) {
		boolean v1 = true;
		if (v1)
			this.name = "RESULT_" + cat + "." + cameraPosition + "." + ct + "." + trait;
		else
			if (trait.contains("|"))
				this.name = "RESULT_" + trait.split("\\|")[0] + "." + ct + "."
						+ cameraPosition + "|" + trait.split("\\|")[1];
			else
				this.name = "RESULT_" + trait + "." + ct + "." + cameraPosition + "." + cat;
	}
	
	public Trait(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
