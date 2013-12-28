package de.ipk.ag_ba.commands.experiment.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.ErrorMsg;
import org.IniIoProvider;
import org.StringManipulationTools;
import org.apache.commons.lang3.text.WordUtils;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.WebUrlAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.commands.settings.ActionSettingsEditor;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;

/**
 * @author klukas
 */
public class AbstractRscriptExecutionAction extends AbstractNavigationAction {
	
	private final String scriptIniLocation;
	private final ArrayList<String> res = new ArrayList<String>();
	private ArrayList<String> urls = new ArrayList<String>();
	private ArrayList<String> urlDescriptions = new ArrayList<String>();
	private String title;
	private String iconDef;
	String cmd;
	String[] params;
	boolean parameterDetermination = false;
	IniIoProvider iniIO = null;
	private boolean parameterRequested;
	private ArrayList<NavigationButton> resultActions = new ArrayList<NavigationButton>();
	ArrayList<ThreadSafeOptions> metaDataColumns = new ArrayList<ThreadSafeOptions>();
	ArrayList<ThreadSafeOptions> groupSelection = new ArrayList<ThreadSafeOptions>();
	private String exportFileName;
	private boolean allowGroupColumnSelection;
	private boolean allowGroupFiltering;
	private boolean allowDataColumnSelection;
	private final ExperimentReference experimentReference;
	private ActionFilterConditions actionGroupSelection;
	private ActionSettingsEditor ase;
	private ActionSelectDataColumns actionSelectDataColumns;
	private final ActionScriptBasedDataProcessing adp;
	
	public AbstractRscriptExecutionAction(ActionScriptBasedDataProcessing adp, String tooltip, String scriptIniLocation, ExperimentReference experimentReference)
			throws IOException {
		super(tooltip);
		this.adp = adp;
		this.scriptIniLocation = scriptIniLocation;
		this.experimentReference = experimentReference;
		readInfo();
		initMetaDataColumnSelection();
	}
	
	private void initMetaDataColumnSelection() {
		if (!parameterRequested) {
			iniIO = new VirtualIoProvider();
			
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, "Species").setParam(1, ConditionInfo.SPECIES).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, "Genotype").setParam(1, ConditionInfo.GENOTYPE).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, "Variety").setParam(1, ConditionInfo.VARIETY).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, "Sequence").setParam(1, ConditionInfo.SEQUENCE).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, "Treatment").setParam(1, ConditionInfo.TREATMENT).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, "Growth Conditions").setParam(1, ConditionInfo.GROWTHCONDITIONS).setBval(0, true));
		}
	}
	
	private void readInfo() throws IOException {
		ScriptHelper sh = new ScriptHelper(scriptIniLocation, adp);
		title = sh.getTitle();
		iconDef = sh.getIcon();
		cmd = sh.getCommand();
		params = sh.getParams();
		exportFileName = sh.getExportFileName();
		allowGroupColumnSelection = sh.isAllowGroupColumnSelection();
		allowGroupFiltering = sh.isAllowGroupFiltering();
		allowDataColumnSelection = sh.isAllowDataColumnSelection();
		tooltip = sh.getTooltip();
		urls = StringManipulationTools.getStringListFromArray(sh.getWebURLs());
		urlDescriptions = StringManipulationTools.getStringListFromArray(sh.getWebUrlTitles());
		if (params != null && !parameterRequested) {
			for (String s : params) {
				if (s.startsWith("[") && s.endsWith("]")) {
					parameterDetermination = true;
					break;
				}
			}
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		res.clear();
		resultActions.clear();
		if (parameterDetermination) {
			// empty
		} else {
			TreeMap<Long, String> ro = ScriptExecutor.start(getDefaultTitle(), cmd, params, getStatusProvider(), 1);
			res.add("<code>" + StringManipulationTools.getStringList(ro.values(), "<br>")
					+ "</code>");
		}
		
		ArrayList<NavigationButton> ra = new ArrayList<NavigationButton>();
		if (parameterDetermination) {
			AbstractRscriptExecutionAction ta;
			try {
				ta = new AbstractRscriptExecutionAction(adp, getDefaultTooltip(), scriptIniLocation, experimentReference);
				ta.parameterRequested = true;
				ta.parameterDetermination = false;
				ta.iniIO = iniIO;
				ta.metaDataColumns = metaDataColumns;
				ra.add(new NavigationButton(ta, guiSetting));
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (!parameterRequested) {
			if (exportFileName != null && !exportFileName.isEmpty()) {
				if (allowGroupColumnSelection)
					ra.add(new NavigationButton(new ActionSelectMetaDataColumns(metaDataColumns), guiSetting));
				if (allowGroupFiltering) {
					if (actionGroupSelection == null)
						actionGroupSelection = new ActionFilterConditions(metaDataColumns, groupSelection, experimentReference);
					ra.add(new NavigationButton(actionGroupSelection, guiSetting));
				}
				if (allowDataColumnSelection) {
					if (actionSelectDataColumns == null)
						actionSelectDataColumns = new ActionSelectDataColumns("Select Relevant Data Columns");
					ra.add(new NavigationButton(actionSelectDataColumns, guiSetting));
				}
			}
			for (int i = 0; i < urls.size(); i++) {
				if (urls.get(i) == null || urls.get(i).isEmpty())
					continue;
				if (urlDescriptions.get(i) == null || urlDescriptions.get(i).isEmpty())
					continue;
				ra.add(
						new NavigationButton(urlDescriptions.get(i),
								new WebUrlAction(new IOurl(urls.get(i)),
										"<html>Show " + urlDescriptions.get(i) + "<br>" +
												"(" + urls.get(i) + ")"), guiSetting));
			}
		}
		if (parameterDetermination) {
			if (params != null) {
				if (ase == null) {
					ase = new ActionSettingsEditor(null, iniIO, null, "Parameters");
					for (String s : params) {
						try {
							if (s.contains("[") && s.contains("]") && s.length() > 2) {
								s = s.substring(s.indexOf("[") + 1, s.indexOf("]")).trim();
								// [int|bootstrap sample size|1000]
								String a[] = s.split("\\|", 3);
								if (a.length == 3) {
									String type = a[0];
									String desc = a[1];
									String defaultValue = a[2].split("\\|")[0];
									if (a[2].split("\\|").length > 1) {
										boolean first = true;
										for (String help : a[2].split("\\|")) {
											if (!first) {
												desc = StringManipulationTools.stringReplace(desc, "_", "-");
												desc = WordUtils.capitalizeFully(desc, '-', ' ');
												res.add("<b>" + desc + "</b><br><br>" + StringManipulationTools.getWordWrap(help, 80));
											}
											first = false;
										}
									}
									if (type.equalsIgnoreCase("int")) {
										iniIO.getInstance().setInteger("Parameters", desc, Integer.parseInt(defaultValue));
									}
									
								}
							}
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
				}
				try {
					ase.performActionCalculateResults(src);
					for (NavigationButton r : ase.getResultNewActionSet()) {
						if (!r.isRightAligned())
							ra.add(r);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			
		}
		
		resultActions = ra;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return resultActions;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (parameterDetermination) {
			res.add(0, "<h3>Parameteriziation</h3>Click <i>Start Script</i> once parameters have been set according to your needs.");
			return new MainPanelComponent(res);
		} else {
			return new MainPanelComponent(res);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		if (parameterRequested)
			return "Start Script";
		else
			return title;
	}
	
	@Override
	public String getDefaultImage() {
		if (parameterRequested) {
			return "img/ext/gpl2/Gnome-Application-X-Executable-64.png";
		} else {
			if (iconDef != null && !iconDef.isEmpty())
				return iconDef;
			else
				return "img/ext/gpl2/Gnome-Dialog-Information-64.png";
		}
	}
}
