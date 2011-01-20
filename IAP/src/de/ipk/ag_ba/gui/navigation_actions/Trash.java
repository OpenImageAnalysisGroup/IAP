package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;
import org.StringManipulationTools;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.ExperimentInfo;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class Trash extends AbstractNavigationAction {
	
	private final MongoDB m;
	private String experimentName;
	private Collection<ExperimentHeaderInterface> headers;
	private String message = "";
	private DeletionCommand cmd;
	
	public Trash(MongoDB m, String experimentName) {
		super("Mark experiment as deleted");
		this.m = m;
		this.experimentName = experimentName;
	}
	
	public Trash(ExperimentHeaderInterface header, DeletionCommand cmd, MongoDB m) {
		super("Perform '" + cmd + "'-operation");
		this.m = m;
		this.setHeader(header);
		this.cmd = cmd;
	}
	
	public Trash(Collection<ExperimentHeaderInterface> headers, DeletionCommand cmd, MongoDB m) {
		super("Perform '" + cmd + "'-operation");
		this.m = m;
		this.setHeader(headers);
		this.cmd = cmd;
	}
	
	@Override
	public String getDefaultTitle() {
		return cmd.toString();
	}
	
	@Override
	public String getDefaultImage() {
		return cmd.getImg();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		message = "<html><ul>";
		try {
			if (getHeader() != null)
				for (ExperimentHeaderInterface hhh : getHeader()) {
					ExperimentInfo ei = null;
					if (hhh == null)
						return;
					else
						ei = new ExperimentInfo(hhh);
					message += "<li>Process Experiment " + experimentName + ": ";
					if (cmd == DeletionCommand.DELETE || cmd == DeletionCommand.EMPTY_TRASH_DELETE_ALL_TRASHED_IN_LIST) {
						if (getHeader() != null) {
							m.deleteExperiment(hhh.getExcelfileid());
							message = "<html><b>" + "Experiment " + experimentName + " has been deleted.";
						} else {
							Object[] res = MyInputHelper.getInput("<html>"
												+ "You are about to delete a dataset from the database.<br>"
												+ "This action can not be undone.<br>"
												+ "Connected binary files are not immediately removed, but only<br>"
												+ "during the process of database maintanance procedures.",
												"Confirm final deletion operation", new Object[] {
																	"Remove experiment " + ei.experimentName + " from database?", false });
							if (res != null && (Boolean) res[0]) {
								// CallDBE2WebService.setDeleteExperiment(login, pass,
								// experimentName);
								// message = "<html><b>" + "Experiment " +
								// experimentName +
								// " has been removed from the database.";
								message = "Internal Error";
							} else {
								message = " has NOT been deleted.";
							}
						}
					}
					if (cmd == DeletionCommand.TRASH || cmd == DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS) {
						try {
							m.setExperimentType(hhh, "Trash" + ";" + hhh.getExperimentType());
							experimentName = hhh.getExperimentname();
							message += "has been marked as trashed!";
						} catch (Exception e) {
							message += "Error: " + e.getMessage();
						}
					}
					if (cmd == DeletionCommand.UNTRASH) {
						try {
							String type = hhh.getExperimentType();
							if (type.contains("Trash;"))
								type = StringManipulationTools.stringReplace(type, "Trash;", "");
							if (type.contains("Trash"))
								type = StringManipulationTools.stringReplace(type, "Trash", "");
							m.setExperimentType(hhh, type);
							experimentName = hhh.getExperimentname();
							message += "Experiment " + experimentName + " has been put out of trash!";
						} catch (Exception e) {
							message += "Error: " + e.getMessage();
						}
					}
				}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			message = "Error: " + e.getMessage();
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		if (res.size() > 1)
			res.remove(res.size() - 1);
		res.add(null);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(message);
	}
	
	public static NavigationButton getTrashEntity(final MongoDB m, final String experimentName,
						GUIsetting guiSetting) {
		NavigationAction trashAction = new Trash(m, experimentName);
		NavigationButton trash = new NavigationButton(trashAction, "Delete", "img/ext/edit-delete.png", guiSetting);
		trash.setRightAligned(true);
		return trash;
	}
	
	public static NavigationButton getTrashEntity(ExperimentHeaderInterface header, DeletionCommand cmd,
						GUIsetting guiSetting, MongoDB m) {
		NavigationAction trashAction = new Trash(header, cmd, m);
		NavigationButton trash = new NavigationButton(trashAction, cmd.toString(), cmd.getImg(), guiSetting);
		trash.setRightAligned(cmd != DeletionCommand.UNTRASH);
		return trash;
	}
	
	public static NavigationButton getTrashEntity(ArrayList<ExperimentHeaderInterface> trashed, DeletionCommand cmd,
						GUIsetting guiSetting, MongoDB m) {
		NavigationAction trashAction = new Trash(trashed, cmd, m);
		NavigationButton trash = new NavigationButton(trashAction, cmd.toString(), cmd.getImg(), guiSetting);
		trash.setRightAligned(true);
		return trash;
	}
	
	private void setHeader(Collection<ExperimentHeaderInterface> headers) {
		this.headers = headers;
	}
	
	private void setHeader(ExperimentHeaderInterface header) {
		Collection<ExperimentHeaderInterface> h = new ArrayList<ExperimentHeaderInterface>();
		h.add(header);
		this.headers = h;
	}
	
	private Collection<ExperimentHeaderInterface> getHeader() {
		return headers;
	}
}