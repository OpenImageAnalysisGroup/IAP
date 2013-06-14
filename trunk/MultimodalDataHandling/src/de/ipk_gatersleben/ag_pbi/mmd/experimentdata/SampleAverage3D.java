package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.util.ArrayList;
import java.util.Map;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class SampleAverage3D extends SampleAverage {
	
	public SampleAverage3D(SampleInterface sample, Map map) {
		super(sample, map);
	}
	
	public SampleAverage3D(SampleInterface parent) {
		super(parent);
	}
	
	@Override
	public void calculateValuesFromSampleData() {
		ArrayList<NumericMeasurementInterface> measurements = new ArrayList<NumericMeasurementInterface>();
		for (NumericMeasurementInterface m : getParentSample())
			if (!(m instanceof VolumeData || m instanceof NetworkData || m instanceof ImageData))
				measurements.add(m);
		
		if (measurements.size() == 0) {
			setMin(Double.NaN);
			setMax(Double.NaN);
			setStddev(Double.NaN);
			setValue(Double.NaN);
		} else {
			int n = measurements.size();
			n = 0;
			
			setReplicateId(measurements.size());
			
			setMax(Double.NEGATIVE_INFINITY);
			setMin(Double.POSITIVE_INFINITY);
			double sum = 0;
			for (NumericMeasurementInterface m : measurements) {
				double v = m.getValue();
				
				if (Double.isNaN(v))
					continue;
				
				n++;
				if (v < getMin())
					setMin(v);
				if (v > getMax())
					setMax(v);
				sum += v;
			}
			
			if (n == 0) {
				setStddev(Double.NaN);
				setValue(Double.NaN);
				setMin(Double.NaN);
				setMax(Double.NaN);
				return;
			}
			if (n == 1) {
				setStddev(Double.NaN);
				setValue(sum);
				return;
			}
			
			double avg = sum / n;
			double sumDiff = 0;
			for (NumericMeasurementInterface m : measurements) {
				if (!Double.isNaN(m.getValue()))
					sumDiff += (m.getValue() - avg) * (m.getValue() - avg);
			}
			setStddev(Math.sqrt(sumDiff / (n - 1)));
			setValue(avg);
		}
	}
}
