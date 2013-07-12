package org.graffiti.plugin.algorithm;

import java.util.Collection;

public interface ProvidesAccessToOtherAlgorithms {
	
	public Collection<Algorithm> getAlgorithmList();
	
	public boolean closeDialogBeforeExecution(Algorithm algorithm);
	
}
