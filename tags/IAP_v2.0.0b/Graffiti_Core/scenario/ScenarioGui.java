/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.03.2007 by Christian Klukas
 */
package scenario;

import javax.swing.Action;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;

public interface ScenarioGui {
	
	void postWorkflowStep(Algorithm algorithm, Parameter[] params);
	
	void postWorkflowStep(Action action);
	
	void postWorkflowStep(String title, String[] imports, String[] commands);
}
