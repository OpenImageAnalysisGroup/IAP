package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionTrash extends AbstractNavigationAction {
	
	private final MongoDB m;
	private String experimentName;
	private Set<ExperimentHeaderInterface> headers;
	private String message = "";
	private DeletionCommand cmd;
	
	public ActionTrash(MongoDB m, String experimentName) {
		super("Mark experiment as deleted");
		this.m = m;
		this.experimentName = experimentName;
	}
	
	public ActionTrash(ExperimentHeaderInterface header, DeletionCommand cmd, MongoDB m) {
		super("Perform '" + cmd + "'-operation");
		this.m = m;
		this.setHeader(header);
		this.cmd = cmd;
	}
	
	public ActionTrash(Collection<ExperimentHeaderInterface> headers, DeletionCommand cmd, MongoDB m) {
		super("Perform '" + cmd + "'-operation");
		this.m = m;
		LinkedHashSet<ExperimentHeaderInterface> set = new LinkedHashSet<ExperimentHeaderInterface>();
		set.addAll(headers);
		this.setHeader(set);
		this.cmd = cmd;
	}
	
	public ActionTrash(Set<ExperimentHeaderInterface> headers, DeletionCommand cmd, MongoDB m) {
		super("Perform '" + cmd + "'-operation");
		this.m = m;
		this.setHeader(headers);
		this.cmd = cmd;
	}
	
	@Override
	public String getDefaultTitle() {
		String desc = "";
		if (headers.size() == 1)
			desc = "";// headers.iterator().next().getExperimentName();
		else
			desc = headers.size() + " experiments";
		if (!desc.isEmpty())
			return "<html><center>" + cmd.toString() + "<br>(" + desc + ")</center>";
		else
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
			Collection<ExperimentHeaderInterface> list = getHeader();
			if (list != null && list.size() > 0) {
				int idx = 0;
				for (ExperimentHeaderInterface hhh : list) {
					status.setCurrentStatusText1("Processing");
					idx++;
					status.setCurrentStatusText2(idx + "/" + list.size());
					processExperiment(hhh);
					status.setCurrentStatusValueFine(100d * idx / list.size());
				}
				status.setCurrentStatusText1("Finished");
				status.setCurrentStatusText2("");
				message = "Finished processing of " + list.size() + " experiments!";
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			message = "Error: " + e.getMessage();
			status.setCurrentStatusText1("Error:");
			status.setCurrentStatusText2(e.getMessage());
		}
	}
	
	private void processExperiment(ExperimentHeaderInterface hhh) throws Exception {
		experimentName = hhh.getExperimentName();
		message += "<li>Process Experiment " + experimentName + ": ";
		if (cmd == DeletionCommand.DELETE || cmd == DeletionCommand.EMPTY_TRASH_DELETE_ALL_TRASHED_IN_LIST) {
			if (m != null && getHeader() != null) {
				m.deleteExperiment(hhh.getDatabaseId());
				message += "<html><b>" + "Experiment " + experimentName + " has been deleted.";
			} else {
				ResourceIOHandler h = ResourceIOManager.getInstance().getHandlerFromPrefix(new IOurl(hhh.getDatabaseId()).getPrefix());
				if (h != null)
					h.deleteResource(new IOurl(hhh.getDatabaseId()));
			}
		}
		if (cmd == DeletionCommand.TRASH || cmd == DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS) {
			try {
				if (m != null)
					m.setExperimentType(hhh, "Trash" + ";" + hhh.getExperimentType());
				else {
					hhh.setExperimenttype("Trash" + ";" + hhh.getExperimentType());
					hhh.getExperimentHeaderHelper().saveUpdatedProperties(null);
				}
				message += "has been marked as trashed!";
			} catch (Exception e) {
				message += "Error: " + e.getMessage();
			}
		}
		if (cmd == DeletionCommand.UNTRASH || cmd == DeletionCommand.UNTRASH_ALL) {
			try {
				String type = hhh.getExperimentType();
				while (type.contains("Trash;"))
					type = StringManipulationTools.stringReplace(type, "Trash;", "");
				while (type.contains("Trash"))
					type = StringManipulationTools.stringReplace(type, "Trash", "");
				if (m != null)
					m.setExperimentType(hhh, type);
				else {
					hhh.setExperimenttype(type);
					hhh.getExperimentHeaderHelper().saveUpdatedProperties(null);
				}
				message += "Experiment " + experimentName + " has been put out of trash!";
			} catch (Exception e) {
				message += "Error: " + e.getMessage();
			}
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
		NavigationAction trashAction = new ActionTrash(m, experimentName);
		NavigationButton trash = new NavigationButton(trashAction, "Delete", "img/ext/edit-delete.png", guiSetting);
		trash.setRightAligned(true);
		return trash;
	}
	
	public static NavigationButton getTrashEntity(ExperimentHeaderInterface header, DeletionCommand cmd,
			GUIsetting guiSetting, MongoDB m) {
		LinkedHashSet<ExperimentHeaderInterface> expAndHistory = new LinkedHashSet<ExperimentHeaderInterface>();
		expAndHistory.add(header);
		expAndHistory.addAll(header.getHistory().values());
		NavigationAction trashAction = new ActionTrash(header, cmd, m);
		NavigationButton trash = new NavigationButton(trashAction, cmd.toString(), cmd.getImg(),
				guiSetting);
		// trash.setRightAligned(cmd != DeletionCommand.UNTRASH);
		return trash;
	}
	
	public static NavigationButton getTrashEntity(Set<ExperimentHeaderInterface> trashed, DeletionCommand cmd,
			GUIsetting guiSetting, MongoDB m) {
		NavigationAction trashAction = new ActionTrash(trashed, cmd, m);
		NavigationButton trash = new NavigationButton(trashAction, cmd.toString(), cmd.getImg(), guiSetting);
		trash.setRightAligned(true);
		return trash;
	}
	
	private void setHeader(Set<ExperimentHeaderInterface> headers) {
		this.setHeaders(headers);
	}
	
	private void setHeader(ExperimentHeaderInterface header) {
		LinkedHashSet<ExperimentHeaderInterface> h = new LinkedHashSet<ExperimentHeaderInterface>();
		h.add(header);
		h.addAll(header.getHistory().values());
		this.setHeaders(h);
	}
	
	private Collection<ExperimentHeaderInterface> getHeader() {
		return getHeaders();
	}
	
	private Set<ExperimentHeaderInterface> getHeaders() {
		return headers;
	}
	
	private void setHeaders(Set<ExperimentHeaderInterface> headers) {
		this.headers = headers;
		LinkedHashSet<ExperimentHeaderInterface> toBeAdded = new LinkedHashSet<ExperimentHeaderInterface>();
		for (ExperimentHeaderInterface h : this.headers)
			toBeAdded.addAll(h.getHistory().values());
		for (ExperimentHeaderInterface h : toBeAdded)
			this.headers.add(h);
	}
}