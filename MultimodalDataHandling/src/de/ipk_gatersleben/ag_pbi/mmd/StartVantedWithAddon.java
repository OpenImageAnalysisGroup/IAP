/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
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

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.MainM;

public class StartVantedWithAddon {
	
	public static void main(String[] args) {
		System.out.println("Starting VANTED with Add-on " + getAddonName() + " for development...");
		MainM.startVanted(args, getAddonName());
	}
	
	public static String getAddonName() {
		return "MultimodalDataHandling.xml";
	}
	
}
