/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ipk.ag_ba.commands.analysis.ActionPhytochamberBlueRubberAnalysis;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.maize.ImageAnalysisAction;
import de.ipk.ag_ba.gui.navigation_actions.maize.Maize3DanalysisAction;
import de.ipk.ag_ba.gui.navigation_actions.maize.MaizeAnalysisAction;
import de.ipk.ag_ba.gui.navigation_actions.roots.RootScannAnalysisAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ImageAnalysis {
	
	public static NavigationButton getPhytochamberEntityBlueRubber(final MongoDB m,
			final ExperimentReference experiment, final double epsilon, final double epsilon2, GUIsetting guiSetting) {
		
		NavigationAction phenotypeAnalysisAction = new ActionPhytochamberBlueRubberAnalysis(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(phenotypeAnalysisAction, guiSetting);
		return resultTaskButton;
	}
	
	public static NavigationButton getMaizeEntity(final MongoDB m,
			final ExperimentReference experiment, final double epsilon, final double epsilon2, GUIsetting guiSetting) {
		
		NavigationAction maizeAnalysisAction = new MaizeAnalysisAction(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(maizeAnalysisAction, guiSetting);
		return resultTaskButton;
	}
	
	public static NavigationButton getMaize3dEntity(final MongoDB m,
			final ExperimentReference experiment, final double epsilon, final double epsilon2, GUIsetting guiSetting) {
		
		NavigationAction maizeAnalysisAction = new Maize3DanalysisAction(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(maizeAnalysisAction, guiSetting);
		return resultTaskButton;
	}
	
	public static NavigationButton getPipelineEntity(PipelineDesc pd,
			final MongoDB m, final ExperimentReference experiment,
			final double epsilon, final double epsilon2, GUIsetting guiSetting) {
		NavigationAction imageAnalysisAction = new ImageAnalysisAction(pd, m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(imageAnalysisAction, guiSetting);
		return resultTaskButton;
	}
	
	public static NavigationButton getRootScannEntity(final MongoDB m,
			final ExperimentReference experiment, GUIsetting guiSetting) {
		
		NavigationAction rootAnalysisAction = new RootScannAnalysisAction(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(rootAnalysisAction, guiSetting);
		return resultTaskButton;
	}
	
	public static JComponent getImageZoomSlider(final ArrayList<ZoomedImage> zoomedImages) {
		
		int FPS_MIN = 0;
		int FPS_MAX = 400;
		int FPS_INIT = 100;
		
		final JSlider sliderZoom = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);
		
		final JLabel lbl = new JLabel("Zoom (100%)");
		
		// Turn on labels at major tick marks.
		sliderZoom.setMajorTickSpacing(100);
		sliderZoom.setMinorTickSpacing(10);
		sliderZoom.setPaintTicks(true);
		sliderZoom.setPaintLabels(true);
		sliderZoom.setOpaque(false);
		sliderZoom.setVisible(false);
		BackgroundTaskHelper.executeLaterOnSwingTask(200, new Runnable() {
			@Override
			public void run() {
				sliderZoom.setVisible(true);
			}
		});
		
		for (ZoomedImage zoomedImage : zoomedImages) {
			if (zoomedImage.getInt() == 0)
				zoomedImage.setInt(100);
			else {
				updateZoom(zoomedImage, lbl, sliderZoom, zoomedImage.getInt());
				sliderZoom.setValue(zoomedImage.getInt());
			}
		}
		
		sliderZoom.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider s = (JSlider) e.getSource();
				int val = s.getValue() - s.getValue() % 5;
				if (val < 5)
					val = 5;
				for (ZoomedImage zoomedImage : zoomedImages) {
					if (val != zoomedImage.getInt()) {
						zoomedImage.setInt(val);
						updateZoom(zoomedImage, lbl, s, val);
					}
				}
			}
		});
		
		return TableLayout.getSplitVertical(lbl, sliderZoom, TableLayout.PREFERRED, TableLayout.PREFERRED);
	}
	
	private static void updateZoom(final ZoomedImage zoomedImage, final JLabel lbl, JSlider s, int val) {
		lbl.setText("Zoom (" + val + "%)");
		zoomedImage.setInt(val);
	}
	
}
