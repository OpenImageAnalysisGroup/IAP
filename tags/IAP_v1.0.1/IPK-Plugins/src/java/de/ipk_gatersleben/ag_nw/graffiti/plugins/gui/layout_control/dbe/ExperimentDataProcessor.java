package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

public interface ExperimentDataProcessor {
	
	/**
	 * Perform data mapping (or something else).
	 * 
	 * @param doc
	 *           Experiment data (XML)
	 */
	public void setExperimentData(ExperimentInterface doc);
	
	/**
	 * Can be used to support the user specifying the annotation more easily (e.g. providing filled lists to choose from.
	 * 
	 * @param files
	 * @return
	 */
	public HashMap<File, ExperimentDataAnnotation> getAnnotations(Collection<File> files);
	
	public void setComponent(JComponent optSupplementaryPanel);
	
	public void addPostProcessor(List<Runnable> postProcessors);
	
	public boolean removePostProcessor(List<Runnable> postProcessors);
}