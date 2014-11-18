package de.ipk.ag_ba.commands.experiment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.ErrorMsg;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

public final class ActionShowDataWithinVANTED extends AbstractNavigationAction implements ActionDataProcessing {
	private final AbstractExperimentDataProcessor pp;
	private ExperimentReference experimentName;
	MainPanelComponent mpc;
	private NavigationButton src;
	
	public ActionShowDataWithinVANTED(String tooltip, AbstractExperimentDataProcessor pp) {
		super(tooltip);
		this.pp = pp;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		try {
			ExperimentInterface ed = experimentName.getData();
			Collection<NumericMeasurementInterface> md = Substance3D.getAllMeasurements(ed);
			ed = MappingData3DPath.merge(md, true);
			if (ed != null) {
				ed.numberConditions();
				SupplementaryFilePanelMongoDB optSupplementaryPanel = new SupplementaryFilePanelMongoDB(
						experimentName,
						experimentName.getExperimentName());
				ExperimentDataProcessingManager.getInstance().processData(ed, pp, null,
						optSupplementaryPanel, null);
				boolean showInline = SystemOptions.getInstance().getBoolean("VANTED", "debug-show-inline-iap", false);
				JComponent gui = IAPmain.showVANTED(showInline);
				// gui.setBorder(BorderFactory.createLoweredBevelBorder());
				if (gui != null)
					gui.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
				
				mpc = new MainPanelComponent(gui);
			}
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(
			ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return true;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experimentName = experimentReference;
	}
	
	@Override
	public String getDefaultTitle() {
		return pp.getName();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/vanted1_0.png";
	}
	
	@Override
	public NavigationImage getImageIconInactive() {
		try {
			return new NavigationImage((BufferedImage) pp.getIcon().getImage(), null);
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		try {
			return new NavigationImage((BufferedImage) pp.getIcon().getImage(), null);
		} catch (Exception e) {
			return null;
		}
	}
}