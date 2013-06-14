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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.util.Map;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class Condition3D extends Condition {
	
	public Condition3D(SubstanceInterface md) {
		super(md);
	}
	
	@Override
	public String getCoordinator() {
		String coord = super.getCoordinator();
		if (coord == null)
			coord = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return coord;
	}
	
	public Condition3D(SubstanceInterface s3d, @SuppressWarnings("rawtypes") Map attributemap) {
		super(s3d, attributemap);
	}
	
}
