package org.graffiti.plugin.view;

import org.graffiti.graph.GraphElement;

public interface GraphView
					extends View {
	
	boolean isHidden(GraphElement ge);
}
