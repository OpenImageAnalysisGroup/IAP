package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.ErrorMsg;
import org.StringManipulationTools;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.ExperimentInfo;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 * 
 */
public class Trash extends AbstractNavigationAction {

	private String pass;
	private String login;
	private String experimentName;
	private ExperimentHeaderInterface header;
	private String message = "";
	private DeletionCommand cmd;

	public Trash(String pass, String login, String experimentName) {
		super("Mark experiment as deleted");
		this.pass = pass;
		this.login = login;
		this.experimentName = experimentName;
	}

	public Trash(ExperimentHeaderInterface header, DeletionCommand cmd) {
		super("Perform '" + cmd + "'-operation");
		this.header = header;
		this.cmd = cmd;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
		try {
			ExperimentInfo ei = null;
			if (header == null)
				;
			// ei = CallDBE2WebService.getExperimentInfos(login, pass,
			// experimentName)[0];
			else
				ei = new ExperimentInfo(header);
			if (cmd == DeletionCommand.DELETE) {
				if (header != null) {
					message = "<html><b>" + "Not Yet Implemented: Experiment " + experimentName + " has not been deleted.";
				} else {
					Object[] res = MyInputHelper.getInput("<html>"
							+ "You are about to delete a dataset from the database.<br>"
							+ "This action can not be undone.<br>"
							+ "Connected binary files are not immediately removed, but only<br>"
							+ "during the process of database maintanance procedures.", "Confirm final deletion operation",
							new Object[] { "Remove experiment " + ei.experimentName + " from database?", false });
					if (res != null && (Boolean) res[0]) {
						// CallDBE2WebService.setDeleteExperiment(login, pass,
						// experimentName);
						// message = "<html><b>" + "Experiment " + experimentName +
						// " has been removed from the database.";
						message = "<html><b>" + "Internal Error";
					} else {
						message = "<html><b>" + "Experiment " + experimentName + " has not been deleted.";
					}
				}
			}
			if (cmd == DeletionCommand.TRASH) {
				try {
					if (header != null) {
						new MongoDB().setExperimentType(header, "Trash" + ";" + header.getExperimentType());
						experimentName = header.getExperimentname();
					} else {
						// ExperimentInfo ni = new ExperimentInfo(ei.experimentName,
						// ei.experimentID, ei.importUser,
						// ei.importUser, "Trash", ei.dateExperimentStart,
						// ei.dateExperimentImport, ei.remark,
						// ei.coordinator, ei.excelFileMd5, ei.fileCount,
						// ei.byteSize);
						// CallDBE2WebService.setChangeExperimentMetaData(login, pass,
						// experimentName, ni);
					}
					message = "<html><b>" + "Experiment " + experimentName + " has been marked as trashed!";
				} catch (Exception e) {
					message = "Error: " + e.getMessage();
				}
			}
			if (cmd == DeletionCommand.UNTRASH) {
				try {
					if (header != null) {
						String type = header.getExperimentType();
						if (type.contains("Trash;"))
							type = StringManipulationTools.stringReplace(type, "Trash;", "");
						if (type.contains("Trash"))
							type = StringManipulationTools.stringReplace(type, "Trash", "");
						new MongoDB().setExperimentType(header, type);
						experimentName = header.getExperimentname();
					} else {
						// ExperimentInfo ni = new ExperimentInfo(ei.experimentName,
						// ei.experimentID, ei.importUser,
						// ei.importUser, "Restored", ei.dateExperimentStart,
						// ei.dateExperimentImport, ei.remark,
						// ei.coordinator, ei.excelFileMd5, ei.fileCount,
						// ei.byteSize);
						// CallDBE2WebService.setChangeExperimentMetaData(login, pass,
						// experimentName, ni);
					}
					message = "<html><b>" + "Experiment " + experimentName + " has been put out of trash!";
				} catch (Exception e) {
					message = "Error: " + e.getMessage();
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			message = "Error: " + e.getMessage();
		}
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		while (res.size() > 2)
			res.remove(res.size() - 1);
		return res;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return new Phenotyping().getResultNewActionSet();
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(message);
	}

	public static NavigationGraphicalEntity getTrashEntity(final String login, final String pass,
			final String experimentName) {
		NavigationAction trashAction = new Trash(pass, login, experimentName);
		NavigationGraphicalEntity trash = new NavigationGraphicalEntity(trashAction, "Delete", "img/ext/edit-delete.png");
		trash.setRightAligned(true);
		return trash;
	}

	public static NavigationGraphicalEntity getTrashEntity(ExperimentHeaderInterface header, DeletionCommand cmd) {
		NavigationAction trashAction = new Trash(header, cmd);
		NavigationGraphicalEntity trash = new NavigationGraphicalEntity(trashAction, cmd.toString(), cmd.getImg());
		trash.setRightAligned(cmd != DeletionCommand.UNTRASH);
		return trash;
	}
}