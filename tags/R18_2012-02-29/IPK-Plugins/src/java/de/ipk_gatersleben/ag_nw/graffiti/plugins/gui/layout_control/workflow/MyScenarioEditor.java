package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.AttributeHelper;
import org.JMButton;

import scenario.Scenario;

public class MyScenarioEditor extends JMButton {
	private static final long serialVersionUID = 1L;
	
	Scenario s = null;
	
	public MyScenarioEditor(final Scenario s) {
		this.s = s;
		setIcon(WorkflowHelper.getInstance().myGetIcon("images/edit.png"));
		setText(s.getName());
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AttributeHelper.showInBrowser(s.getFileName());
			}
		});
		if (s.getMenu().length() > 0)
			setToolTipText("Shown in main menu " + s.getMenu());
		else
			setToolTipText("Shown in contextmenu");
	}
	
}
