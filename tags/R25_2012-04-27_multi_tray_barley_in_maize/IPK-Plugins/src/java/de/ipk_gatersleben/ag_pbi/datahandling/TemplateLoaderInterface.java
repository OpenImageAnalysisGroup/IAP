/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jul 17, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_pbi.datahandling;

import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;

public interface TemplateLoaderInterface {
	
	public abstract JPanel getAttributeDialog(int filenumber) throws Exception;
	
	public abstract List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname);
	
	public abstract String getSubstance();
	
	public abstract void setFormularData(TemplateLoaderInterface loader, Object[] formularData);
	
	public abstract File getFile();
	
	public abstract Object[] getFormData();
	
	public abstract String toString();
	
	public abstract void setAnnotation(ExperimentDataAnnotation ed);
	
}