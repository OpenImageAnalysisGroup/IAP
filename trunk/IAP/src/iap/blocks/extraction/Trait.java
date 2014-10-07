package iap.blocks.extraction;

import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import de.ipk.ag_ba.image.structures.CameraType;

public class Trait {
	
	private final String name;
	
	public Trait(CameraPosition cameraPosition, CameraType ct, String trait) {
		// this.name = "RESULT_" + cameraPosition + "." + ct + "." + trait;
		this.name = "RESULT_" + trait + "." + ct + "." + cameraPosition;
	}
	
	public Trait(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
