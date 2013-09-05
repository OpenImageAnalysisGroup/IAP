/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import qmwi.kseg.som.DataSet;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SOMprintDataset extends AbstractAlgorithm {
	int numberOfNeuronsParm = 8;
	int widthOfSOMparm = 0; // 0=quadratic
	double maxNeighbourHoodParm = -1;
	int decreaseNeighbourhoodAfterXiterationsParam = 0; // 0=off
	int typeOfNeighbourhoodFunctionParam = 1;
	int numberLearnIterationsParam = 5;
	double betaParam = 0.1;
	double gammaParam = 2;
	boolean returnNaN;
	private boolean useSampleAverage;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
			return "Print Dataset as CSV to Console";
		else
			return null; // "Print Dataset as CSV to Console";
	}
	
	@Override
	public void reset() {
		super.reset();
		// numberOfNeuronsParm=8;
		// widthOfSOMparm=0; // 0=quadratic
		// maxNeighbourHoodParm=-1;
		// decreaseNeighbourhoodAfterXiterationsParam=0; // 0=off
		// typeOfNeighbourhoodFunctionParam=1;
		// numberLearnIterationsParam=5;
		// betaParam=0.1;
		// gammaParam=2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(returnNaN, "Disable Interpolation",
												"If selected, missing time points are not filled with the value from the previous time point."),
							new BooleanParameter(SOMplugin.getLastUseAverageSetting(), "Use Sample Average Values",
												"If selected, the sample average values will be used, otherwise the replicate values will be used.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		returnNaN = ((BooleanParameter) params[0]).getBoolean().booleanValue();
		useSampleAverage = ((BooleanParameter) params[1]).getBoolean().booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		SOMplugin.initDataSetWithSelection(SOMplugin.getDataSet(false), getSelectedOrAllGraphElements(), returnNaN, useSampleAverage);
		DataSet ds = SOMplugin.getDataSet(false);
		System.out.println("=================== DATA SET ===================");
		for (int i = 0; i < ds.getGroupSize(); i++) {
			System.out.print(ds.getColumnNameAt(i) + ";");
		}
		System.out.println();
		String[][] data = ds.getData();
		for (int r = 0; r < data.length; r++) {
			for (int c = 0; c < data[r].length; c++)
				System.out.print(data[r][c] + ";");
			System.out.println();
		}
		System.out.println("================================================");
	}
}