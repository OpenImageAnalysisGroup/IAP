package de.ipk.ag_ba.image.operations.blocks.properties;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;

public interface RunnableOnImageSet {
	public Image postProcessImage(Image vis);

	public ImageConfiguration getConfig();
}
