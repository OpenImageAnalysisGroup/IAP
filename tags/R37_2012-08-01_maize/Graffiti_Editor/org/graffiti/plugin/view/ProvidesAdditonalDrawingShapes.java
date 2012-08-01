package org.graffiti.plugin.view;

import java.awt.Shape;
import java.util.Collection;

public interface ProvidesAdditonalDrawingShapes {
	Collection<Shape> getPreBorderShapes();
	
	Collection<Shape> getPostBorderShapes();
}
