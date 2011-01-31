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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.ErrorMsg;
import org.jdom.Attribute;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;

public class NumericMeasurement3D extends NumericMeasurement {
	
	private String positionUnit = null;
	private Double position = null;
	
	public NumericMeasurement3D(SampleInterface parent) {
		super(parent);
	}
	
	public NumericMeasurement3D(SampleInterface sample, Map<?, ?> map) {
		super(sample, map);
		if (map.containsKey("position"))
			setPosition((Double) map.get("position"));
		if (map.containsKey("positionUnit"))
			setPositionUnit((String) map.get("positionUnit"));
		
	}
	
	public NumericMeasurement3D(Measurement copyFrom, String newSubstanceName, String optNewExperimentName) {
		super(copyFrom, newSubstanceName, optNewExperimentName);
	}
	
	@Override
	public String toString() {
		if (position != null && positionUnit != null)
			return "Omics-value: " + getValue() + " (" + position + " " + positionUnit + ")";
		else
			return "Omics-value: " + getValue();
	}
	
	public MeasurementNodeType getType() {
		return MeasurementNodeType.OMICS;
	}
	
	public String getExperimentName() {
		try {
			return getParentSample().getParentCondition().getExperimentName();
		} catch (NullPointerException e) {
			System.err.println("No experiment name set, using standard name...");
			return Experiment.UNSPECIFIED_EXPERIMENTNAME;
		}
	}
	
	public String getSubstanceName() {
		try {
			return getParentSample().getParentCondition().getParentSubstance().getName();
		} catch (NullPointerException e) {
			System.err.println("No substance name set, using standard name...");
			return Experiment.UNSPECIFIED_SUBSTANCE;
		}
	}
	
	public static HashMap<String, ArrayList<NumericMeasurementInterface>> getAllExperiments(
						Collection<NumericMeasurementInterface> mappingfiles) {
		return getAllExperiments(mappingfiles, null);
	}
	
	public static HashMap<String, ArrayList<NumericMeasurementInterface>> getAllExperiments(
						Collection<NumericMeasurementInterface> mappingfiles, MeasurementNodeType type) {
		HashMap<String, ArrayList<NumericMeasurementInterface>> exps = new HashMap<String, ArrayList<NumericMeasurementInterface>>();
		
		for (NumericMeasurementInterface md : mappingfiles)
			if ((type == null ? true : ((NumericMeasurement3D) md).getType() == type)) {
				if (exps.containsKey(((NumericMeasurement3D) md).getExperimentName()))
					exps.get(((NumericMeasurement3D) md).getExperimentName()).add(md);
				else {
					ArrayList<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
					list.add(md);
					exps.put(((NumericMeasurement3D) md).getExperimentName(), list);
				}
			}
		
		return exps;
	}
	
	public static ExperimentInterface getExperiment(ArrayList<NumericMeasurementInterface> measurements) {
		return getExperiment(measurements, false);
	}
	
	public static ExperimentInterface getExperiment(ArrayList<NumericMeasurementInterface> measurements, boolean sortConditionsByName) {
		ArrayList<MappingData3DPath> mappingpaths = new ArrayList<MappingData3DPath>();
		
		for (NumericMeasurementInterface meas : measurements)
			mappingpaths.add(new MappingData3DPath(meas));
		
		if (sortConditionsByName)
			Collections.sort(mappingpaths, new Comparator<MappingData3DPath>() {
				@Override
				public int compare(MappingData3DPath arg0, MappingData3DPath arg1) {
					return arg0.getConditionData().toString().compareTo(arg1.getConditionData().toString());
				}
			});
		
		return new Experiment(MappingData3DPath.merge(mappingpaths));
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		super.getXMLAttributeString(r);
		Substance.getAttributeString(r, new String[] {
				"position", "positionUnit"
		}, new Object[] {
				position, positionUnit
		});
	}
	
	@Override
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		
		else
			if (attr.getName().equals("value"))
				setValue(Double.parseDouble(attr.getValue()));
			else
				if (attr.getName().equals("position"))
					try {
						setPosition(Double.parseDouble(attr.getValue()));
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				else
					if (attr.getName().equals("positionUnit"))
						setPositionUnit(attr.getValue());
					else
						super.setAttribute(attr);
	}
	
	public NumericMeasurementInterface copyDataAndPath() {
		return new MappingData3DPath(this).getMeasurement();
		
	}
	
	public void setPosition(Double position) {
		this.position = position;
	}
	
	public Double getPosition() {
		return position;
	}
	
	public void setPositionUnit(String positionUnit) {
		this.positionUnit = positionUnit;
	}
	
	public String getPositionUnit() {
		return positionUnit;
	}
	
	public boolean equalNumericMeasurement(NumericMeasurementInterface meas) {
		String u1 = getReplicateID() + " " + getValue() + " " + getUnit() + " " + getPositionUnit() + " " + getPosition();
		String u2 = meas.getReplicateID() + " " + meas.getValue() + " " + meas.getUnit() + " "
							+ ((NumericMeasurement3D) meas).getPositionUnit() + " " + ((NumericMeasurement3D) meas).getPosition();
		return u1.equals(u2);
	}
	
	public boolean isPositionSet() {
		return getPosition() != null && getPositionUnit() != null;
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		super.fillAttributeMap(attributes);
		attributes.put("position", position);
		attributes.put("positionUnit", positionUnit);
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		NumericMeasurement3D m = (NumericMeasurement3D) super.clone(parent);
		m.setPosition(getPosition());
		m.setPositionUnit(getPositionUnit());
		return m;
	}
	
}
