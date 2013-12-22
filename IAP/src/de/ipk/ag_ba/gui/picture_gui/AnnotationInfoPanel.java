package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.MeasurementFilter;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class AnnotationInfoPanel extends JPanel {
	private static final long serialVersionUID = 7793767687607473484L;
	
	private final DataSetFileButton imageButton;
	private final MongoTreeNode mt;
	private JCheckBox cbO;
	private JCheckBox cbF;
	private JLabel annotationLabel;
	
	private final MeasurementFilter mf;
	
	public AnnotationInfoPanel(DataSetFileButton imageButton, MongoTreeNode mt, MeasurementFilter mf) {
		this.imageButton = imageButton;
		this.mt = mt;
		this.mf = mf;
		addGui(true);
	}
	
	public void removeGui() {
		addGui(true);
	}
	
	public void addGui() {
		addGui(false);
	}
	
	public void addGui(boolean onlyChecked) {
		removeAll();
		cbO = null;
		cbF = null;
		ArrayList<JComponent> anno = getAnnotationElements(onlyChecked);
		setLayout(TableLayout.getLayout(TableLayout.PREFERRED,
				TableLayout.PREFERRED, 1, anno.size()));
		int idx = 0;
		for (JComponent a : anno)
			add(a, "0," + (idx++));
		revalidate();
		
	}
	
	private ArrayList<JComponent> getAnnotationElements(boolean onlyChecked) {
		if (cbO == null && imageButton.imageResult != null) {
			cbO = new JCheckBox("Outlier");
			MappingDataEntity mde = imageButton.imageResult.getBinaryFileInfo().entity;
			if (mde != null && mde instanceof ImageData) {
				modifyFlagGui((ImageData) mde, "outlier", cbO);
			}
		}
		if (cbF == null && imageButton.imageResult != null) {
			cbF = new JCheckBox("Flagged");
			MappingDataEntity mde = imageButton.imageResult.getBinaryFileInfo().entity;
			if (mde != null && mde instanceof ImageData) {
				modifyFlagGui((ImageData) mde, "flagged", cbF);
			}
		}
		if (annotationLabel == null && imageButton.imageResult != null) {
			annotationLabel = new JLabel("");
			MappingDataEntity mde = imageButton.imageResult.getBinaryFileInfo().entity;
			if (mde != null && mde instanceof ImageData) {
				modifyFlagGui((ImageData) mde, "rem:", annotationLabel);
			}
		}
		ArrayList<JComponent> res = new ArrayList<JComponent>();
		if (cbO != null && (!onlyChecked || cbO.isSelected()))
			res.add(cbO);
		if (cbF != null && (!onlyChecked || cbF.isSelected()))
			res.add(cbF);
		if (annotationLabel != null && annotationLabel.getText().length() > 0)
			res.add(annotationLabel);
		return res;
	}
	
	private void modifyFlagGui(final ImageData id, final String key,
			final JCheckBox cb) {
		boolean isGlobalOutlier = mf.filterOut(id.getQualityAnnotation(), id.getParentSample().getTime());
		String f = id.getAnnotationField(key);
		if (f != null && f.equals("1")) {
			cb.setSelected(true);
		}
		if (key.equals("outlier") && isGlobalOutlier) {
			cb.setSelected(true);
			cb.setEnabled(false);
			cb.setText("Defined Outlier");
		} else {
			ActionListener[] al = cb.getActionListeners();
			if (al != null)
				for (ActionListener l : al)
					cb.removeActionListener(l);
			cb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String a = id.getAnnotationField(key);
					// System.out.println("GET " + key + ": " + a);
					if (cb.isSelected()) {
						if (a == null)
							id.addAnnotationField(key, "1");
						else
							id.replaceAnnotationField(key, "1");
					} else {
						if (a == null)
							id.addAnnotationField(key, "0");
						else
							id.replaceAnnotationField(key, "0");
					}
					// System.out.println("ANNO: " + id.getAnnotation());
				}
			});
		}
	}
	
	private void modifyFlagGui(final ImageData id, final String keySearch,
			final JLabel cb) {
		ArrayList<String> display = new ArrayList<String>();
		for (String key : id.getAnnotationKeys(keySearch)) {
			String f = id.getAnnotationField(key);
			if (f != null && f.length() > 0)
				display.add(StringManipulationTools.stringReplace(key,
						keySearch, "") + ": " + f);
		}
		if (display.size() > 0)
			cb.setText("<html>"
					+ StringManipulationTools.getStringList(display, "<br>"));
	}
	
	private long callTime = 0;
	
	public void removeGuiLater() {
		callTime = System.currentTimeMillis();
		final long callTime2 = callTime;
		Timer rt = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (callTime2 == callTime)
					removeGui();
			}
		});
		rt.start();
	}
}
