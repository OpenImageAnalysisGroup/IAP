/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.03.2007 by Christian Klukas
 */
package scenario;

import java.util.Collection;

public interface ProvidesScenarioSupportCommands {
	
	Collection<String> getScenarioImports();
	
	Collection<String> getScenarioCommands();
	
}
