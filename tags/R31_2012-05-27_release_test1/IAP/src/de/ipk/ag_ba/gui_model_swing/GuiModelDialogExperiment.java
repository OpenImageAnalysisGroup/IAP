package de.ipk.ag_ba.gui_model_swing;

import javax.swing.JComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class GuiModelDialogExperiment extends GuiModelDialogSwing {
	
	ExperimentHeaderInterface experimentHeader;
	
	public GuiModelDialogExperiment(ExperimentHeaderInterface header) {
		experimentHeader = header;
	}
	
	@Override
	public void updateGUI() {
		super.updateGUI();
		// set field values and labels
	}
	
	@Override
	public JComponent getGUI() {
		/*
		 * FolderPanel fp = new FolderPanel("Experiment " + experimentHeader.getExperimentname(), false, false, false, null);
		 * Color c = new Color(220, 220, 220);
		 * fp.setFrameColor(c, Color.BLACK, 4, 8);
		 * editName = new JTextField(experimentHeader.getExperimentname());
		 * coordinator = new JTextField(experimentHeader.getCoordinator());
		 * groupVisibility = new JTextField(experimentHeader.getImportusergroup());
		 * // getGroups(login, pass, experimentHeader.getImportusergroup(),
		 * // editPossible);
		 * experimentTypeSelection = getExperimentTypes(m, experimentHeader.getExperimentType(), editPossible);
		 * expStart = new JDateChooser(experimentHeader.getStartdate());
		 * expEnd = new JDateChooser(experimentHeader.getImportdate());
		 * remark = new JTextField(experimentHeader.getRemark());
		 * fp.addGuiComponentRow(new JLabel("Name"), editName, false);
		 * fp.addGuiComponentRow(new JLabel("ID"), disable(new JTextField(experimentHeader.getDatabaseId() + "")), false);
		 * fp.addGuiComponentRow(new JLabel("Import by"), disable(new JTextField(experimentHeader.getImportusername())),
		 * false);
		 * fp.addGuiComponentRow(new JLabel("Coordinator"), coordinator, false);
		 * fp.addGuiComponentRow(new JLabel("Group"), groupVisibility, false);
		 * fp.addGuiComponentRow(new JLabel("Experiment-Type"), experimentTypeSelection, false);
		 * fp.addGuiComponentRow(new JLabel("Start-Time"), expStart, false);
		 * fp.addGuiComponentRow(new JLabel("End-Time"), expEnd, false);
		 * fp.addGuiComponentRow(new JLabel("Remark"), remark, false);
		 * fp.addGuiComponentRow(new JLabel("Connected Files"), new JLabel(niceValue(experimentHeader.getNumberOfFiles(), null)
		 * + " (" + niceValue(experimentHeader.getSizekb(), "KB") + ")"), false);
		 * fp.addGuiComponentRow(new JLabel("Show XML"), getShowDataButton(experiment), false);
		 */
		return null;
	}
}
