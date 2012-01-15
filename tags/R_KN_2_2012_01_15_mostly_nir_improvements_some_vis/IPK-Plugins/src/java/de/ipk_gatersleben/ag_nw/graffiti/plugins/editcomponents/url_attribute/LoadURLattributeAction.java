package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.actions.URLattributeAction;

public class LoadURLattributeAction implements URLattributeAction {
	
	public ActionListener getActionListener(final Attribute displayable,
						final Graph graph, final GraphElement ge, final boolean performAltCommand) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String val = (String) displayable.getValue();
				AttributeHelper.showInBrowser(val);
			}
		};
	}
	
	public String getPreIdentifyer() {
		return "";
	}
	
	public String getCommandDescription(boolean shortDesc, boolean altDesc) {
		if (shortDesc)
			return "Show in Browser: ";
		else
			return "Show in Browser";
	}
	
	public boolean supportsModifyCommand() {
		return false;
	}
}
