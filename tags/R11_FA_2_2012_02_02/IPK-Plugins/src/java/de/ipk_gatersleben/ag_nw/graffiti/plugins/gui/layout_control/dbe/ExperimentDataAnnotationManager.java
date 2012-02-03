package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ExperimentDataAnnotationManager {
	
	private static ExperimentDataAnnotationManager instance;
	
	private ExperimentDataAnnotationManager() {
		super();
	}
	
	public static ExperimentDataAnnotationManager getInstance() {
		if (instance == null)
			instance = new ExperimentDataAnnotationManager();
		return instance;
	}
	
	public HashMap<File, ExperimentDataAnnotation> getExperimentAnnotation(
						List<ExperimentDataProcessor> optUseTheseProcessors, Collection<File> files) {
		HashMap<File, ExperimentDataAnnotation> res = new HashMap<File, ExperimentDataAnnotation>();
		Collection<ExperimentDataProcessor> processors = optUseTheseProcessors;
		if (processors == null)
			processors = ExperimentDataProcessingManager.getExperimentDataProcessors();
		HashMap<ExperimentDataProcessor, HashMap<File, ExperimentDataAnnotation>> annos = new HashMap<ExperimentDataProcessor, HashMap<File, ExperimentDataAnnotation>>();
		for (ExperimentDataProcessor ep : processors) {
			if (files != null && ep != null) {
				HashMap<File, ExperimentDataAnnotation> a = ep.getAnnotations(files);
				if (a != null)
					annos.put(ep, a);
			}
		}
		for (File f : files) {
			ExperimentDataAnnotation ea = null;
			for (ExperimentDataProcessor ep : processors) {
				HashMap<File, ExperimentDataAnnotation> file2annotation = annos.get(ep);
				if (file2annotation != null)
					if (ea == null)
						ea = file2annotation.get(f);
					else
						ea.mergeAnnotations(file2annotation.get(f));
			}
			if (ea != null)
				res.put(f, ea);
		}
		
		return res;
	}
}
