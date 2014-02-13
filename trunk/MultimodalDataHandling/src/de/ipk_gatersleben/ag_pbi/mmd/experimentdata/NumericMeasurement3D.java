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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MyAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author rohn, klukas
 */
public class NumericMeasurement3D extends NumericMeasurement {
	
	private String positionUnit = null;
	private Double position = null;
	private String annotation;
	
	public NumericMeasurement3D(SampleInterface parent) {
		super(parent);
	}
	
	public NumericMeasurement3D(SampleInterface sample, Map<?, ?> map) {
		super(sample, map);
		if (map.containsKey("position"))
			setPosition((Double) map.get("position"));
		if (map.containsKey("positionUnit"))
			setPositionUnit((String) map.get("positionUnit"));
		if (map.containsKey("annotation"))
			setAnnotation((String) map.get("annotation"));
		
	}
	
	public NumericMeasurement3D(Measurement copyFrom, String newSubstanceName, String optNewExperimentName) {
		super(copyFrom, newSubstanceName, optNewExperimentName);
		if (copyFrom instanceof NumericMeasurement3D) {
			setPositionUnit(((NumericMeasurement3D) copyFrom).getPositionUnit());
			setPosition(((NumericMeasurement3D) copyFrom).getPosition());
			setAnnotation(((NumericMeasurement3D) copyFrom).getAnnotation());
		}
	}
	
	@Override
	public String toString() {
		if (getQualityAnnotation() != null && getQualityAnnotation().length() > 0) {
			if (position != null && positionUnit != null)
				return getValue() + "  +/-" + (getParentSample().getSampleAverage().getStdDev()) + (getUnit() != null ? " " + getUnit() : "") + " (" + position
						+ " " + positionUnit + ", "
						+ getQualityAnnotation() + ")";
			else
				return getValue() + "  +/-" + (getParentSample().getSampleAverage().getStdDev()) + (getUnit() != null ? " " + getUnit() : "") + " ("
						+ getQualityAnnotation() + ")";
		} else {
			if (position != null && positionUnit != null)
				return getValue() + "  +/-" + (getParentSample().getSampleAverage().getStdDev()) + (getUnit() != null ? " " + getUnit() : "") + " (" + position
						+ " " + positionUnit + ")";
			else
				return getValue() + "  +/-" + (getParentSample().getSampleAverage().getStdDev()) + (getUnit() != null ? " " + getUnit() : "");
		}
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
	
	public static ExperimentInterface getExperiment(ArrayList<NumericMeasurementInterface> measurements, boolean ignoreSnapshotFineTime) {
		return getExperiment(measurements, false, ignoreSnapshotFineTime);
	}
	
	public static ExperimentInterface getExperiment(ArrayList<NumericMeasurementInterface> measurements, boolean sortConditionsByName,
			boolean ignoreSnapshotFineTime) {
		return getExperiment(measurements, sortConditionsByName, ignoreSnapshotFineTime, true, null);
	}
	
	public static ExperimentInterface getExperiment(final ArrayList<NumericMeasurementInterface> measurements, boolean sortConditionsByName,
			boolean ignoreSnapshotFineTime, final boolean cloneElements,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		final ThreadSafeOptions nextObjectIndex = new ThreadSafeOptions();
		nextObjectIndex.setInt(-1);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Create Path Objects");
		
		int n = SystemAnalysis.getNumberOfCPUs();
		final Semaphore lock = BackgroundTaskHelper.lockGetSemaphore(null, n);
		
		final TreeMap<String, ArrayList<MappingData3DPath>> result = new TreeMap<String, ArrayList<MappingData3DPath>>();
		// sortConditionsByName is ignored! (always true)
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					int nextObject = nextObjectIndex.addInt(1);
					int measurementCnt = measurements.size();
					while (nextObject < measurementCnt) {
						NumericMeasurementInterface meas = measurements.get(nextObject);
						MappingData3DPath res = new MappingData3DPath(meas, cloneElements);
						String key = res.getConditionData().toString();
						synchronized (result) {
							if (!result.containsKey(key))
								result.put(key, new ArrayList<MappingData3DPath>());
							result.get(key).add(res);
						}
						nextObject = nextObjectIndex.addInt(1);
					}
				} finally {
					lock.release();
				}
			}
		};
		
		try {
			for (int i = 0; i < n; i++) {
				lock.acquire();
				Thread t = new Thread(r);
				t.setName("MappingData3DPath construction");
				t.start();
			}
			lock.acquire(n);
			lock.release(n);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Merge path objects...");
		
		int initialCapacity = 0;
		for (ArrayList<MappingData3DPath> values : result.values())
			initialCapacity += values.size();
		
		ArrayList<MappingData3DPath> mappingpaths = new ArrayList<MappingData3DPath>(initialCapacity);
		for (ArrayList<MappingData3DPath> values : result.values())
			mappingpaths.addAll(values);
		
		Experiment res = new Experiment(MappingData3DPath.merge(mappingpaths, ignoreSnapshotFineTime, optStatus));
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Finished Construction");
		return res;
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		super.getXMLAttributeString(r);
		Substance.getAttributeString(r, new String[] {
				"position", "positionUnit", "annotation"
		}, new Object[] {
				position, positionUnit, annotation
		});
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		else {
			String name = attr.getName();
			if (name.startsWith("v")) // equals("value"))
				setValue(Double.parseDouble(attr.getValue()));
			else
				if (name.equals("position"))
					try {
						setPosition(Double.parseDouble(attr.getValue()));
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				else
					if (name.equals("positionUnit"))
						setPositionUnit(attr.getValue());
					else
						if (name.startsWith("a")) // equals("annotation"))
							setAnnotation(attr.getValue());
						else
							super.setAttribute(attr);
		}
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
		this.positionUnit = positionUnit;// != null ? positionUnit.intern() : null;
	}
	
	public String getPositionUnit() {
		return positionUnit;
	}
	
	private static HashMap<String, String> annotations = new HashMap<String, String>();
	
	public void setAnnotation(String annotation) {
		synchronized (annotations) {
			if (!annotations.containsKey(annotation))
				annotations.put(annotation, annotation);
			this.annotation = annotations.get(annotation);
		}
		// this.annotation = annotation != null ? annotation.intern() : null;
	}
	
	public String getAnnotation() {
		return annotation;
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
		if (position != null)
			attributes.put("position", position);
		if (positionUnit != null && positionUnit.length() > 0)
			attributes.put("positionUnit", positionUnit);
		if (annotation != null && annotation.length() > 0)
			attributes.put("annotation", annotation);
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		NumericMeasurement3D m = (NumericMeasurement3D) super.clone(parent);
		m.setPosition(getPosition());
		m.setPositionUnit(getPositionUnit());
		m.setAnnotation(getAnnotation());
		return m;
	}
	
	public synchronized String getAnnotationField(String key) {
		String a = getAnnotation();
		if (a != null) {
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				String[] nn = f.split("#", 2);
				if (nn[0].equals(key))
					return nn[1];
			}
		}
		return null;
	}
	
	public synchronized ArrayList<String> getAnnotationKeys(String search) {
		ArrayList<String> result = new ArrayList<String>();
		String a = getAnnotation();
		if (a != null) {
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				String[] nn = f.split("#", 2);
				if (nn[0].contains(search))
					result.add(nn[0]);
			}
		}
		return result;
	}
	
	public synchronized boolean addAnnotationField(String key, String value) {
		if (key == null || value == null)
			return false;
		if (value.contains(";"))
			throw new UnsupportedOperationException(
					"Annotation field value must not contain the ;-character!");
		String a = getAnnotation();
		if (a == null)
			a = key + "#" + value;
		else
			a += ";" + key + "#" + value;
		setAnnotation(a);
		return true;
	}
	
	public synchronized boolean setAnnotationField(String key, String value) {
		if (key == null || value == null)
			return false;
		if (getAnnotationField(key) != null) {
			return replaceAnnotationField(key, value);
		} else {
			return addAnnotationField(key, value);
		}
	}
	
	public synchronized boolean replaceAnnotationField(String key, String value) {
		if (key == null || value == null)
			return false;
		boolean found = false;
		StringBuilder res = new StringBuilder();
		String a = getAnnotation();
		if (value.contains(";"))
			throw new UnsupportedOperationException(
					"Annotation field value must not contain a commata character!");
		if (a != null) {
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				if (f.length() == 0)
					continue;
				String[] nn = f.split("#", 2);
				if (res.length() > 0)
					res.append(";");
				res.append(nn[0]);
				if (nn[0].equals(key)) {
					res.append("#" + value);
					found = true;
				} else
					res.append("#" + nn[1]);
			}
		}
		setAnnotation(res.toString());
		return found;
	}
	
	public boolean removeAnnotationField(String key) {
		String a = getAnnotation();
		if (a == null)
			return false;
		else {
			boolean found = false;
			StringBuilder res = new StringBuilder();
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				String[] nn = f.split("#", 2);
				if (!nn[0].equals(key)) {
					if (res.length() > 0)
						res.append(";");
					res.append(f);
				} else
					found = true;
			}
			return found;
		}
	}
}
