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
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import java.io.File;
import java.util.List;

import javax.swing.JLabel;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoader;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface;

public abstract class TemplateLoaderMMD extends TemplateLoader {
	
	public abstract TemplateLoaderInterface createInstance(File f);
	
	@Override
	public boolean process(List<File> files) {
		if (receiver != null) {
			List<ExperimentInterface> l = process(files, receiver, null);
			if (l != null)
				for (ExperimentInterface exp : l)
					receiver.processReceivedData(null, exp.getName(), exp, new JLabel(toString()));
			return true;
		}
		return false;
	}
	
}
