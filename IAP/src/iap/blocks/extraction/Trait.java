package iap.blocks.extraction;

import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.structures.CameraType;

public class Trait {
	
	private final String name;
	
	public Trait(CameraPosition cameraPosition, CameraType ct, TraitCategory cat, String trait) {
		boolean v1 = true;
		if (v1)
			this.name = "RESULT_" + cameraPosition + "." + cat + "." + ct + "." + trait;
		else
			if (trait.contains("|"))
				this.name = "RESULT_" + cat + "." + trait.split("\\|")[0] + "." + ct + "."
						+ cameraPosition + "|" + trait.split("\\|")[1];
			else
				this.name = "RESULT_" + cat + "." + trait + "." + ct + "." + cameraPosition;
	}
	
	public Trait(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getNiceName() {
		String traitName = name;
		if (traitName == null)
			return null;
		
		String h = traitName;
		if (h != null && h.endsWith(")") && h.contains("(")) {
			h = h.substring(0, h.indexOf("(")).trim();
			traitName = h;
		}
		
		String[] name = traitName.split("\\.", 4);
		if (name.length == 4) {
			String cp = name[0];
			if (cp.startsWith("RESULT_"))
				cp = cp.substring("RESULT_".length());
			
			CameraPosition cpos = CameraPosition.fromString(cp);
			
			TraitCategory cat = TraitCategory.fromString(name[1]);
			
			CameraType ct = CameraType.fromString(name[2]);
			
			String trait = name[3];
			h = trait;
			if (h != null && h.endsWith(")") && h.contains("(")) {
				h = h.substring(0, h.indexOf("(")).trim();
				trait = h;
			}
			
			boolean normalized = false;
			if (trait.endsWith(".norm")) {
				normalized = true;
				trait = trait.substring(0, trait.length() - ".norm".length());
			} else
				if (trait.indexOf(".norm.") > 0) {
					normalized = true;
					trait = StringManipulationTools.stringReplace(traitName, ".norm.", ".");
				}
			
			boolean relativeR = false;
			if (trait.endsWith(".relative.raw")) {
				relativeR = true;
				trait = trait.substring(0, trait.length() - ".relative.raw".length());
			}
			
			boolean relativeP = false;
			if (trait.endsWith(".relative.percent")) {
				relativeP = true;
				trait = trait.substring(0, trait.length() - ".relative.percent".length());
			}
			
			boolean relativeL = false;
			if (trait.endsWith(".relative.log")) {
				relativeL = true;
				trait = trait.substring(0, trait.length() - ".relative.log".length());
			}
			
			trait = StringManipulationTools.stringReplace(trait, ".", " ");
			trait = StringManipulationTools.stringReplace(trait, "_", " ");
			
			return (relativeR ? "relative daily change of " : "")
					+ (relativeP ? "percentage of daily change of " : "")
					+ (relativeL ? "daily log-change rate of " : "")
					+ trait + " (" + (normalized ? "zoom-corrected " : "") + cat.getNiceString() + " trait based on "
					+ ct.getNiceName() + " " + cpos.getNiceName()
					+ " view)";
		} else
			return null;
	}
}
