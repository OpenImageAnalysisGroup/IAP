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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataPresenter;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface;

public class NetworkLoader extends TemplateLoaderMMD {
	
	@Override
	public String toString() {
		return "Network";
	}
	
	@Override
	protected List<ExperimentInterface> process(List<File> files, ExperimentDataPresenter receiver, BackgroundTaskStatusProviderSupportingExternalCall status) {
		Collection<ExperimentInterface> c = new DataImportDialog().getExperimentMetadataFromUserByDialog(files, this, null);
		if (c == null)
			return null;
		else
			return new ArrayList<ExperimentInterface>(c);
	}
	
	@Override
	public String[] getValidExtensions() {
		return new String[] { "gml", "graphml" };// MainFrame.getInstance().getIoManager().getGraphFileExtensions().toArray(new
		// String[]{});
	}
	
	@Override
	public TemplateLoaderInterface createInstance(File f) {
		return new NetworkLoaderInstance(f, this);
	}
	
}
