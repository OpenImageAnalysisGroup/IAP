package de.ipk.ag_ba.image.operations.blocks.properties;

import de.ipk.ag_ba.commands.ImageConfiguration;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public interface RunnableOnImageSet {
	public FlexibleImage postProcessVis(FlexibleImage vis);

	public ImageConfiguration getConfig();
}
