package de.ipk.ag_ba.image.operations.blocks.properties;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;

public interface RunnableOnImageSet {
	public FlexibleImage postProcessVis(FlexibleImage vis);

	public ImageConfiguration getConfig();
}
