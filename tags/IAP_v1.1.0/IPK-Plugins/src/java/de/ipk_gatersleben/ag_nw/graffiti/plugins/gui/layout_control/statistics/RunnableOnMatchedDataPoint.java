package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;

/**
 * @author Christian Klukas
 */
public interface RunnableOnMatchedDataPoint {
	
	void storeMatchingInformation(Measurement calculatedData,
			double manualMeasurement);
	
	void postProcessStoredInformationOnMatchingPairs(
			ExperimentInterface experiment, SimpleRegression sr,
			String matchWithSubstance, String corrDesc);
	
	void reset();
	
}
