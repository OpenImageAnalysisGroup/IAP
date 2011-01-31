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

import java.util.ArrayList;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class MappingData3DPath {
	
	private final NumericMeasurementInterface measnew;
	private final SubstanceInterface mdnew;
	private final ConditionInterface condnew;
	private final SampleInterface sampnew;
	private final boolean isValid = true;
	
	public NumericMeasurementInterface getMeasurement() {
		return measnew;
	}
	
	public SubstanceInterface getSubstance() {
		return mdnew;
	}
	
	public ConditionInterface getConditionData() {
		return condnew;
	}
	
	public SampleInterface getSampleData() {
		return sampnew;
	}
	
	public MappingData3DPath(NumericMeasurementInterface meas) {
		// try {
		SampleInterface sample = meas.getParentSample();
		
		ConditionInterface cond = sample.getParentCondition();
		SubstanceInterface md = cond.getParentSubstance();
		
		mdnew = md.clone();
		condnew = cond.clone(mdnew);
		mdnew.add(condnew);
		sampnew = sample.clone(condnew);
		condnew.add(sampnew);
		measnew = meas.clone(sampnew);
		sampnew.add(measnew);
		// } catch (NullPointerException e) {
		// ErrorMsg.addErrorMessage(e);
		// measnew = null;
		// mdnew = null;
		// condnew = null;
		// sampnew = null;
		// isValid = false;
		// }
	}
	
	public static ExperimentInterface merge(ArrayList<MappingData3DPath> mappingpaths) {
		
		Experiment experiment = new Experiment();
		
		for (MappingData3DPath p : mappingpaths)
			Substance.addAndMerge(experiment, p.getSubstance());
		
		return experiment;
	}
	
	public boolean isValid() {
		return isValid;
	}
	
}
