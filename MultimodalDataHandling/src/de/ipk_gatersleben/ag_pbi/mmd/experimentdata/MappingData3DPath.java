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
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

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
		this(meas, true);
	}
	
	public MappingData3DPath(NumericMeasurementInterface meas, boolean clone) {
		// try {
		SampleInterface sample = meas.getParentSample();
		
		ConditionInterface cond = sample.getParentCondition();
		SubstanceInterface md = cond.getParentSubstance();
		
		mdnew = clone ? md.clone() : md;
		condnew = clone ? cond.clone(mdnew) : cond;
		mdnew.add(condnew);
		sampnew = clone ? sample.clone(condnew) : sample;
		condnew.add(sampnew);
		measnew = clone ? meas.clone(sampnew) : meas;
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
	
	public static ExperimentInterface merge(ArrayList<MappingData3DPath> mappingpaths, boolean ignoreSnapshotFineTime) {
		return merge(mappingpaths, ignoreSnapshotFineTime, null);
	}
	
	public static ExperimentInterface merge(ArrayList<MappingData3DPath> mappingpaths, final boolean ignoreSnapshotFineTime,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		final Experiment experiment = new Experiment();
		// try {
		final ThreadSafeOptions idx = new ThreadSafeOptions();
		final int max = mappingpaths.size();
		final TreeMap<String, LinkedList<MappingData3DPath>> data = new TreeMap<String, LinkedList<MappingData3DPath>>();
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Process " + mappingpaths.size() + " mapping path objects");
		for (MappingData3DPath p : mappingpaths) {
			if (!data.containsKey(p.getSubstance().getName()))
				data.put(p.getSubstance().getName(), new LinkedList<MappingData3DPath>());
			data.get(p.getSubstance().getName()).add(p);
		}
		
		// final Semaphore lock = BackgroundTaskHelper.lockGetSemaphore(null, SystemAnalysis.getNumberOfCPUs());
		for (String substance : data.keySet()) {
			// System.out.println("MERGE SUBSET SUBSTANCE " + substance + " (" + data.get(substance).size() + " values)...");
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Process " + substance);
			try {
				while (!data.get(substance).isEmpty()) {
					MappingData3DPath p = data.get(substance).poll();
					Substance.addAndMerge(experiment, p.getSubstance(), ignoreSnapshotFineTime);
					idx.addInt(1);
					if (optStatus != null)
						optStatus.setCurrentStatusValueFine(100d / max * idx.getInt());
					if (optStatus != null)
						optStatus.setCurrentStatusText2("Path Object " + idx.getInt() + "/" + max);
				}
				for (SubstanceInterface si : experiment)
					for (ConditionInterface ci : si)
						for (SampleInterface s : ci)
							s.recalculateSampleAverage();
			} finally {
				// lock.release();
			}
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Merged Mapping Paths");
		return experiment;
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public static ArrayList<MappingData3DPath> get(ExperimentInterface e) {
		ArrayList<MappingData3DPath> res = new ArrayList<MappingData3DPath>();
		for (NumericMeasurementInterface nmi : Substance3D.getAllMeasurements(e)) {
			res.add(new MappingData3DPath(nmi));
		}
		return res;
	}
	
	public static ArrayList<MappingData3DPath> get(ExperimentInterface e, boolean clone) {
		ArrayList<MappingData3DPath> res = new ArrayList<MappingData3DPath>();
		for (NumericMeasurementInterface nmi : Substance3D.getAllMeasurements(e)) {
			res.add(new MappingData3DPath(nmi, clone));
		}
		return res;
	}
	
	public static ExperimentInterface merge(Collection<NumericMeasurementInterface> md, boolean ignoreSnapshotFineTime) {
		ArrayList<MappingData3DPath> mmd = new ArrayList<MappingData3DPath>();
		for (NumericMeasurementInterface nmi : md)
			mmd.add(new MappingData3DPath(nmi));
		return merge(mmd, ignoreSnapshotFineTime);
	}
	
}
