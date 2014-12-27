package de.ipk.ag_ba.gui.util;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;

/**
 * @author klukas
 */
public interface ConditionVisitor {
	public void visit(ConditionInterface ci);
}
