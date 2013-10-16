package de.ipk.ag_ba.mongo;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public interface RunnableProcessingSubstance {
	public void visit(SubstanceInterface substance);
}
