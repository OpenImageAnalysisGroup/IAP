package de.ipk.ag_ba.commands;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.FolderPanel;
import org.FolderPanel.Iconsize;
import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.util.InstanceLoader;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.settings.ActionSettingsEditor;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.Unicode;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class ActionSettings extends AbstractNavigationAction {
	
	ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	ArrayList<String> allSettings = new ArrayList<String>();
	private final String iniFileName;
	private final String title;
	private NavigationButton src;
	private final IniIoProvider iniIO;
	private String debugLastSystemOptionStorageGroup;
	
	private boolean clickedOnce = false;
	private String debugDesiredSettingsBlock;
	
	public ActionSettings(String iniFileName, IniIoProvider iniIO, String tooltip, String title) {
		super(tooltip);
		this.iniIO = iniIO;
		this.iniFileName = iniFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		res.clear();
		if (iniIO == null)
			res.add(new NavigationButton(new AbstractNavigationAction(
					"Open file explorer and show settings file (" +
							(iniFileName == null ? "iap.ini" : iniFileName) + ")") {
				@Override
				public void performActionCalculateResults(NavigationButton src) throws Exception {
					AttributeHelper.showInFileBrowser(ReleaseInfo.getAppFolder(), iniFileName == null ? "iap.ini" : iniFileName);
				}
				
				@Override
				public String getDefaultTitle() {
					return "Show Config-File";
				}
				
				@Override
				public String getDefaultImage() {
					return "img/ext/gpl2/Gnome-Document-Open-64.png";
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
					return currentSet;
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewActionSet() {
					return null;
				}
			}, src.getGUIsetting()));
		
		SystemOptions so = SystemOptions.getInstance(iniFileName, iniIO);
		ArrayList<String> ss = so.getSectionTitles();
		Collections.sort(ss);
		allSettings.clear();
		String currentPrefix = "";
		ArrayList<NavigationButton> toBeAdded = new ArrayList<>();
		for (String s : ss) {
			if (s == null)
				s = "";
			
			for (String setting : so.getSectionSettings(s)) {
				String bln = null;
				try {
					ImageAnalysisBlock inst = (ImageAnalysisBlock) Class.forName(setting.split("//")[0], true, InstanceLoader.getCurrentLoader()).newInstance();
					String v = so.internalGetString(s, setting);
					bln = inst.getName() + "//" + setting.split("//")[1] + (v != null ? " <b>&#9655;</b> " + v : "");
				} catch (Exception e) {
					try {
						String v = so.internalGetString(s, setting);
						bln = setting + (v != null ? " <b>&#9655;</b> " + v : "");
					} catch (Exception er) {
						// empty
					}
				}
				allSettings.add(s + " |||<small>" + Unicode.ARROW_RIGHT + "</small> "
						+ StringManipulationTools.stringReplace(
								bln == null || bln.isEmpty() ? setting : bln,
								"//", " <small>" + Unicode.ARROW_RIGHT + "</small> "));
			}
			
			final ActionSettingsEditor ac = new ActionSettingsEditor(iniFileName, iniIO,
					"Change settings of section " + s, s);
			final NavigationButton nb = new NavigationButton(ac, src.getGUIsetting());
			String prefix = s;
			if (s != null && s.contains("-") && StringManipulationTools.isNumeric(s.substring(s.lastIndexOf("-")))) {
				prefix = s.substring(0, s.lastIndexOf("-"));
			}
			if (!currentPrefix.equals(prefix)) {
				if (toBeAdded.size() > 1) {
					groupAdd(res, toBeAdded);
				} else
					res.addAll(toBeAdded);
				toBeAdded.clear();
				toBeAdded.add(nb);
			} else
				toBeAdded.add(nb);
			currentPrefix = prefix;
			
			if (!clickedOnce)
				if (s != null && debugLastSystemOptionStorageGroup != null && !s.isEmpty() && s.equals(debugLastSystemOptionStorageGroup)) {
					BackgroundTaskHelper.executeLaterOnSwingTask(10, new Runnable() {
						@Override
						public void run() {
							if (debugDesiredSettingsBlock != null && !debugDesiredSettingsBlock.isEmpty()) {
								ac.setDesiredSettingsBlock(debugDesiredSettingsBlock);
							}
							nb.performAction();
						}
					});
				}
		}
		if (toBeAdded.size() > 1)
			groupAdd(res, toBeAdded);
		else
			res.addAll(toBeAdded);
		
		if (iniIO == null)
			if (iniFileName == null) {
				ActionFolder af = new ActionFolder("Analysis Template Settings");
				int n = 0;
				for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates()) {
					af.addAdditionalEntity(new NavigationButton(
							new ActionSettings(pd.getIniFileName(), null,
									"Change settings of " + StringManipulationTools.removeHTMLtags(pd.getName()) + " analysis pipeline",
									"" + StringManipulationTools.removeHTMLtags(pd.getName()) + ""),
							src.getGUIsetting()));
					n++;
				}
				if (n > 0) {
					af.setTitle("Analysis Templates (" + n + ")");
					af.setIcon("img/ext/gpl2/collection_settings.png");
					res.add(new NavigationButton(af, src.getGUIsetting()));
				}
			}
		clickedOnce = true;
	}
	
	private void groupAdd(ArrayList<NavigationButton> res2, ArrayList<NavigationButton> toBeAdded) {
		ActionFolder af = new ActionFolder("Analysis Template Settings");
		int n = 0;
		for (NavigationButton nb : toBeAdded) {
			af.addAdditionalEntity(nb);
			n++;
		}
		if (n > 0) {
			af.setTitle(toBeAdded.iterator().next().getTitle() + "...");
			af.setIcon("img/ext/gpl2/collection_settings_box.png");
			res.add(new NavigationButton(af, src.getGUIsetting()));
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> rr = new ArrayList<NavigationButton>(res);
		Book book = new Book(null, "Help", new IOurl("http://iapg2p.sourceforge.net/#workflow"), "img/dataset.png");
		rr.add(0, book.getNavigationButton(src));
		return rr;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<JComponent> rr = new ArrayList<JComponent>();
		FolderPanel fp = new FolderPanel("Setting Fields", true, true, true, null);
		fp.setFrameColor(IAPnavigationPanel.getTabColor(), IAPnavigationPanel.getTabColor(), 5, 5);
		for (String s : allSettings)
			fp.addGuiComponentRow(new JLabel("<html><font color='#222222'>" + s.split("\\|\\|\\|")[0]), new JLabel("<html><font color='#222222'>"
					+ s.split("\\|\\|\\|")[1]), false);
		fp.setMaximumRowCount(15);
		fp.setBackground(IAPnavigationPanel.getTabColor());
		fp.enableSearch(true);
		fp.setSearchLeftAligned(true, 400);
		fp.setIconSize(Iconsize.MIDDLE);
		fp.setShowCondenseButton(false);
		fp.addSearchFilter(fp.getDefaultSearchFilter(null),
				"<html>"
						+ "Enter text into the search field to filter the list content,<br>"
						+ "use either & or | for logic operations to use multiple search terms.");
		fp.layoutRows();
		rr.add(fp);
		if (false)
			rr.add(new JLabel("<html><b>Click on a action button above to open a<br>"
					+ "settings-group or to edit the corresponding setting.</b><br><br>" +
					"Most settings are active immediately, but some  options<br>"
					+ "may require a restart of the progam upon setting change."));
		if (false)
			rr.add(new JLabel("<html><b>The values within a specific group may<br>"
					+ "be removed/reverted to their defaults:</b><br><br>" +
					"The &quot;Defaults (delayed)&quot; command, shown for a selected<br>"
					+ "settings-group, removes the shown settings and their values.<br>" +
					"The settings will re-appear as soon as they are needed, and will<br>"
					+ "be reverted to the programmed defaults."));
		return new MainPanelComponent(rr, 25);
	}
	
	@Override
	public String getDefaultTitle() {
		return title;
	}
	
	@Override
	public String getDefaultImage() {
		if (iniFileName == null && iniIO == null)
			return IAPimages.getToolbox();
		else
			return "img/ext/gpl2/Gnome-Applications-Science-64.png";
	}
	
	public void setInitialNavigationPath(String debugLastSystemOptionStorageGroup) {
		this.debugLastSystemOptionStorageGroup = debugLastSystemOptionStorageGroup;
	}
	
	public void setInitialNavigationSubPath(String debugDesiredSettingsBlock) {
		this.debugDesiredSettingsBlock = debugDesiredSettingsBlock;
	}
}
