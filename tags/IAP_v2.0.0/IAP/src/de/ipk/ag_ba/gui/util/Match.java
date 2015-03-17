package de.ipk.ag_ba.gui.util;

import java.util.Collection;
import java.util.LinkedList;

import de.ipk.ag_ba.gui.picture_gui.CameraSelection;
import de.ipk.ag_ba.gui.picture_gui.DaySelection;
import de.ipk.ag_ba.gui.picture_gui.PlantSelection;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

/**
 * @author Christian Klukas
 */
public class Match {
	
	private final ExperimentInterface experiment;
	
	public Match(ExperimentInterface experiment) {
		this.experiment = experiment;
	}
	
	public Collection<NumericMeasurementInterface> getMatchForReference(
			CameraSelection cs, DaySelection ds, PlantSelection ps,
			NumericMeasurementInterface reference) {
		LinkedList<NumericMeasurementInterface> res = new LinkedList<NumericMeasurementInterface>();
		for (SubstanceInterface si : experiment) {
			if (match(cs, ds, ps, reference, si))
				for (ConditionInterface ci : si) {
					if (match(cs, ds, ps, reference, ci))
						for (SampleInterface sai : ci) {
							if (match(cs, ds, ps, reference, sai))
								for (NumericMeasurementInterface nmi : sai) {
									if (match(cs, ds, ps, reference, nmi)) {
										res.add(nmi);
									}
								}
						}
				}
		}
		return res;
	}
	
	private boolean match(CameraSelection cs, DaySelection ds, PlantSelection ps, NumericMeasurementInterface reference, NumericMeasurementInterface nmi) {
		switch (ps) {
			case ALL_PLANTS:
				return true;
			case THIS_PLANT:
				return nmi.getReplicateID() == reference.getReplicateID();
		}
		return false;
	}
	
	private boolean match(CameraSelection cs, DaySelection ds, PlantSelection ps, NumericMeasurementInterface reference, SampleInterface sai) {
		int day = sai.getTime();
		Long fineTime = sai.getSampleFineTimeOrRowId();
		if (fineTime == null)
			fineTime = -1l;
		SampleInterface refS = reference.getParentSample();
		switch (ds) {
			case ALL_DAYS:
				return true;
			case FROM_THIS_DAY:
				return day >= refS.getTime();
			case THIS_DAY:
				return day == refS.getTime();
			case THIS_SNAPSHOT:
				if (!(refS instanceof Sample3D))
					return false;
				Sample3D s3 = (Sample3D) refS;
				Long refFT = s3.getSampleFineTimeOrRowId();
				if (refFT == null)
					refFT = -1l;
				return fineTime == refFT;
			case UNTIL_THIS_DAY:
				return day <= refS.getTime();
		}
		return false;
	}
	
	private boolean match(CameraSelection cs, DaySelection ds, PlantSelection ps, NumericMeasurementInterface reference, ConditionInterface ci) {
		return true;
	}
	
	private boolean match(CameraSelection cs, DaySelection ds, PlantSelection ps, NumericMeasurementInterface reference, SubstanceInterface si) {
		if (cs == CameraSelection.THIS_CAMERA) {
			String info = si.getInfo() + "";
			SubstanceInterface refS = reference.getParentSample().getParentCondition().getParentSubstance();
			return si.getName().equals(refS.getName()) && (refS.getInfo() + "").equals(info);
		} else {
			return true;
		}
	}
}
