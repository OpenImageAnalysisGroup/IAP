/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 21, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.JMButton;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public abstract class AbstractAnnotationProvider implements AnnotationProvider {
	
	public JButton getButton(final ExperimentInterface md, final ExperimentDataInfoPane resultPane) {
		JButton r = new JMButton(getTitle());
		r.setOpaque(false);
		r.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
								"Request", "In Process. Please wait.");
				
				if (requestUserData(md)) {
					BackgroundTaskHelper.issueSimpleTask(getTitle(), "Please wait", getAnnotations(md, status, resultPane), null, status, 0);
				}
				
			}
		});
		return r;
	}
	
	protected abstract Runnable getAnnotations(ExperimentInterface md, BackgroundTaskStatusProviderSupportingExternalCall status,
			ExperimentDataInfoPane resultPane);
	
}
