/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

/**
 * @author klukas
 */
public class DataExportAction extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	// private JTable table;
	
	public DataExportAction(String tooltip) {
		super(tooltip);
	}
	
	public DataExportAction(MongoDB m, ExperimentReference experimentReference) {
		this("Export Data");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export Data";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveAsArchive();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		
		// Create a buffer for reading the files
		byte[] buf = new byte[1024 * 1024];
		
		// Create the ZIP file
		String outFilename = "outfile.zip";
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename)));
		
		status.setCurrentStatusText1("Load Experiment");
		ExperimentInterface experiment = experimentReference.getData(m);
		status.setCurrentStatusText1("Analyze Content");
		
		HashSet<String> usedFileNames = new HashSet<String>();
		
		// filename:
		// SNAPSHOTNAME=Image Config_[GRAD]Grad
		// plantID SNAPSHOTNAME DATUM ZEIT.png
		
		for (SubstanceInterface su : experiment)
			for (ConditionInterface co : su)
				for (SampleInterface sa : co) {
					for (NumericMeasurementInterface nm : sa) {
						if (nm instanceof BinaryMeasurement) {
							BinaryMeasurement bm = (BinaryMeasurement) nm;
							
							out.putNextEntry(new ZipEntry(bm.getURL().getFileName()));
							
							InputStream in = bm.getURL().getInputStream();
							
							int len;
							while ((len = in.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
							
							// Complete the entry
							out.closeEntry();
							in.close();
							
						}
					}
				}
		
		out.close();
		
		// ArrayList<String> cols = new ArrayList<String>();
		// cols.add("Plant");
		// cols.add("Carrier");
		// cols.add("Experiment");
		// cols.add("Time");
		// cols.add("Weight (before watering)");
		// cols.add("Weight (after watering)");
		// cols.add("Water");
		// Object[] columns = cols.toArray();
		//
		// ExperimentInterface experiment = experimentReference.getData(m);
		// ArrayList<ReportRow> rows = new ArrayList<ReportRow>();
		// for (SubstanceInterface su : experiment) {
		// if (su.getName() == null)
		// continue;
		//
		// if (su.getName().equals("weight_before")) {
		//
		// }
		// if (su.getName().equals("water_weight")) {
		//
		// }
		// if (su.getName().equals("water_amount")) {
		//
		// }
		// for (ConditionInterface c : su) {
		// for (SampleInterface sa : c) {
		// for (Measurement m : sa) {
		// ReportRow r = new ReportRow();
		// r.setPlant(c.getConditionId() + ": " + c.getConditionName());
		// r.setCarrier(m.getReplicateID());
		// r.setExperiment(experiment.getHeader().getExperimentname());
		// r.setTime(sa.getSampleTime());
		// }
		// }
		// }
		// }
		//
		// ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();
		//
		// Object[][] rowdata = new Object[rows.size()][cols.size()];
		//
		// table = new JTable(rowdata, columns);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("ToDo: this function is not yet available");
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
}
