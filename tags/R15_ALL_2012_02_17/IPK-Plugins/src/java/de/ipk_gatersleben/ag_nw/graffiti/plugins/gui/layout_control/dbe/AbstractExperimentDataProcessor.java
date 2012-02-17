package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * Management of action commands for data mapping.
 * 
 * @author rohn, klukas
 */
public abstract class AbstractExperimentDataProcessor
					extends AbstractEditorAlgorithm
					implements ExperimentDataProcessor {
	
	protected LinkedList<Runnable> postProcessors = new LinkedList<Runnable>();
	
	public AbstractExperimentDataProcessor() {
		this(true);
	}
	
	public AbstractExperimentDataProcessor(boolean register) {
		if (register)
			ExperimentDataProcessingManager.addExperimentDataProcessor(this);
	}
	
	/**
	 * Should not be overridden, only in case processData() uses a background thread.
	 * In this case the postProcessors and the setExperimentData() call should be executed
	 * by your code as soon as the background processing is finished.
	 */
	public void execute() {
		processData();
		for (Runnable r : postProcessors)
			r.run();
		setExperimentData(null);
	}
	
	protected abstract void processData();
	
	public abstract void setExperimentData(ExperimentInterface mappingData);
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.
	 * ExperimentDataProcessorInterface#getAnnotations()
	 */
	public HashMap<File, ExperimentDataAnnotation> getAnnotations(Collection<File> files) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.
	 * ExperimentDataProcessorInterface#setComponent(javax.swing.JComponent)
	 */
	public void setComponent(JComponent optSupplementaryPanel) {
		// ignore
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.
	 * ExperimentDataProcessorInterface#addPostProcessor(java.lang.Runnable)
	 */
	public void addPostProcessor(List<Runnable> postProcessors) {
		this.postProcessors.addAll(postProcessors);
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.
	 * ExperimentDataProcessorInterface#removePostProcessor(java.lang.Runnable)
	 */
	public boolean removePostProcessor(List<Runnable> postProcessors) {
		return this.postProcessors.removeAll(postProcessors);
	}
}
