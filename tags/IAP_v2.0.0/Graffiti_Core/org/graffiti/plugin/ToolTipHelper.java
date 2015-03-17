package org.graffiti.plugin;

import java.awt.Component;
import java.util.Stack;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.HelperClass;

/**
 * @author C. Klukas
 *         Adds recursively a tool tip text to a JComponent or to JPanels Sub-Components.
 *         If a JCheckbox is found with no Text, the given toolTipText is used as the new
 *         descriptive text.
 *         The first character of the toolTipText is changed to upper case.
 */
public class ToolTipHelper implements HelperClass {
	public static void addToolTip(JComponent jcomp, String toolTipText) {
		if (toolTipText == null)
			return;
		toolTipText = toolTipText.trim();
		
		String oldTooltip = "";
		if (jcomp.getToolTipText() != null && jcomp.getToolTipText().length() > 0)
			oldTooltip = jcomp.getToolTipText();
		
		while (toolTipText.startsWith("."))
			toolTipText = toolTipText.substring(1);
		
		while (toolTipText.endsWith("."))
			toolTipText = toolTipText.substring(0, toolTipText.length() - 1);
		
		if (toolTipText.length() >= 1) {
			toolTipText = toolTipText.substring(0, 1).toUpperCase() + toolTipText.substring(1);
		}
		
		if (toolTipText.contains(".") && !toolTipText.endsWith(".")) {
			String[] parts = toolTipText.split("\\.");
			String res = parts[0];
			int i;
			for (i = 1; i < parts.length - 1; i++) {
				if (parts[i].length() >= 1) {
					parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
				}
				res += "-" + parts[i];
			}
			if (parts[i].length() >= 1) {
				parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
			}
			res += ": " + parts[i];
			toolTipText = res;
		}
		
		if (oldTooltip.length() > 0)
			toolTipText = "<html>" + toolTipText + "<br><b>" + oldTooltip + "</b>";
		
		Stack<Component> s = new Stack<Component>();
		s.add(jcomp);
		while (!s.empty()) {
			Object se = s.pop();
			if (se instanceof JPanel) {
				Component[] cp = ((JPanel) se).getComponents();
				for (int i = 0; i < cp.length; i++) {
					s.add(cp[i]);
				}
			} else
				if (se instanceof JCheckBox) {
					JCheckBox jc = (JCheckBox) se;
					if (jc.getText().length() == 0) {
						jc.setToolTipText(toolTipText);
					}
				} else
					if (se instanceof JComponent) {
						String curr = ((JComponent) se).getToolTipText();
						if (curr == null || curr.length() <= 0)
							((JComponent) se).setToolTipText(toolTipText);
					}
		}
		
	}
}