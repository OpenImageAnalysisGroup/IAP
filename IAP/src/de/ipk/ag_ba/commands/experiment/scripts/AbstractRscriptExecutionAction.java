package de.ipk.ag_ba.commands.experiment.scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.commons.lang3.text.WordUtils;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.WebUrlAction;
import de.ipk.ag_ba.commands.experiment.ExportSetting;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.commands.experiment.process.report.SnapshotVisitor;
import de.ipk.ag_ba.commands.experiment.scripts.helperClasses.FileSaver;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.commands.settings.ActionSettingsEditor;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
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
	String[] params = null;
	boolean parameterDetermination = false;
	IniIoProvider iniIO = null;
	private boolean parameterRequested;
	private ArrayList<NavigationButton> resultActions = new ArrayList<NavigationButton>();
	ThreadSafeOptions metaDataColumnsReady = new ThreadSafeOptions();
	ArrayList<ThreadSafeOptions> metaDataColumns = new ArrayList<ThreadSafeOptions>();
	ArrayList<ThreadSafeOptions> groupSelection = new ArrayList<ThreadSafeOptions>();
	private String exportFileName;
	private boolean allowGroupColumnSelection;
	private boolean allowGroupFiltering;
	private boolean allowDataColumnSelection;
	private boolean createClusteringDataset;
	final ExperimentReference experimentReference;
	private ActionFilterConditions actionGroupSelection;
	private ActionSettingsEditor ase;
	private ActionSelectDataColumns actionSelectDataColumns;
	private final ActionScriptBasedDataProcessing adp;
	private HashMap<String, String> knownSettings = new HashMap<String, String>();
	private long startTime;
	private ArrayList<String> desiredColumns;
	private ArrayList<ThreadSafeOptions> listOfDataColumns = new ArrayList<ThreadSafeOptions>();
	private ArrayList<String> scriptFileNames;
	private int timeoutInMinutes;
	
	public AbstractRscriptExecutionAction(ActionScriptBasedDataProcessing adp, String tooltip, String scriptIniLocation, ExperimentReference experimentReference)
			throws Exception {
		super(tooltip);
		this.adp = adp;
		this.scriptIniLocation = scriptIniLocation;
		this.experimentReference = experimentReference;
		readInfo();
		initMetaDataColumnSelection();
		experimentReference.runAsDataBecomesAvailable(new InitStructuresAsDataBecomesAvailable(this));
	}
	
	private void initMetaDataColumnSelection() {
		if (!parameterRequested) {
			iniIO = new VirtualIoProvider();
			
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, ConditionInfo.SPECIES.toString()).setParam(10, ConditionInfo.SPECIES.toString())
					.setParam(1, ConditionInfo.SPECIES).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, ConditionInfo.GENOTYPE.toString()).setParam(10, ConditionInfo.GENOTYPE.toString())
					.setParam(1, ConditionInfo.GENOTYPE).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, ConditionInfo.VARIETY.toString()).setParam(10, ConditionInfo.VARIETY.toString())
					.setParam(1, ConditionInfo.VARIETY).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, ConditionInfo.SEQUENCE.toString()).setParam(10, ConditionInfo.SEQUENCE.toString())
					.setParam(1, ConditionInfo.SEQUENCE).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, ConditionInfo.TREATMENT.toString()).setParam(10, ConditionInfo.TREATMENT.toString())
					.setParam(1, ConditionInfo.TREATMENT).setBval(0, true));
			metaDataColumns.add(new ThreadSafeOptions().setParam(0, ConditionInfo.GROWTHCONDITIONS.toString())
					.setParam(10, ConditionInfo.GROWTHCONDITIONS.toString()).setParam(1, ConditionInfo.GROWTHCONDITIONS)
					.setBval(0, true));
			if (createClusteringDataset)
				metaDataColumns
						.add(new ThreadSafeOptions().setParam(0, "Plant ID").setParam(10, "Plant ID").setParam(1, ConditionInfo.IGNORED_FIELD).setBval(0, true));
			metaDataColumnsReady.setBval(0, true);
		}
	}
	
	private void readInfo() throws Exception {
		ScriptHelper sh = new ScriptHelper(scriptIniLocation, adp);
		title = sh.getTitle();
		iconDef = sh.getIcon();
		cmd = sh.getCommand();
		params = sh.getParams();
		exportFileName = sh.getExportFileName();
		allowGroupColumnSelection = sh.isAllowGroupColumnSelection();
		allowGroupFiltering = sh.isAllowGroupFiltering();
		allowDataColumnSelection = sh.isAllowDataColumnSelection();
		createClusteringDataset = sh.isCreateClusteringDataset();
		tooltip = sh.getTooltip();
		urls = StringManipulationTools.getStringListFromArray(sh.getWebURLs());
		urlDescriptions = StringManipulationTools.getStringListFromArray(sh.getWebUrlTitles());
		desiredColumns = StringManipulationTools.getStringListFromArray(sh.getDesiredDataColumns());
		scriptFileNames = StringManipulationTools.getStringListFromArray(sh.getExportScriptFileNames());
		timeoutInMinutes = sh.getTimeoutInMinutes();
		if (allowGroupColumnSelection || allowGroupFiltering || allowDataColumnSelection)
			parameterDetermination = true;
		else
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
		this.startTime = System.currentTimeMillis();
		res.clear();
		resultActions.clear();
		
		ArrayList<NavigationButton> ra = new ArrayList<NavigationButton>();
		
		if (parameterDetermination) {
			// empty
		} else {
			String expDir = null;
			if (exportFileName != null) {
				expDir = ReleaseInfo.getAppSubdirFolderWithFinalSep("scratch", "script_call_" + System.currentTimeMillis());
				
				if (scriptFileNames != null && scriptFileNames.size() > 0) {
					// copy files from script bundle to scratch file location (scriptIniLocation ==> expDir)
					for (String sf : scriptFileNames) {
						if (sf == null || sf.isEmpty())
							continue;
						FileSaver.copy(ReleaseInfo.getAppFolderWithFinalSep()
								+ new File(scriptIniLocation).getParent()
								+ File.separator + sf, expDir + sf);
					}
				}
				
				ArrayList<ThreadSafeOptions> togglesDivideDataSetBy = new ArrayList<ThreadSafeOptions>(metaDataColumns);
				togglesDivideDataSetBy.add(new ThreadSafeOptions().setParam(0, "Clustering").setBval(0, true));
				ArrayList<ThreadSafeOptions> togglesMetaDataFiltering = groupSelection;
				
				/*
				 * String field = (String) t.getParam(0, "");
				 * String content = (String) t.getParam(1, "");
				 * String value = null;
				 * if (field.equals("Condition"))
				 */
				
				ArrayList<ThreadSafeOptions> togglesDataColumns = listOfDataColumns;
				ActionPdfCreation3 expCommand = new ActionPdfCreation3(
						experimentReference,
						togglesDivideDataSetBy,
						new ThreadSafeOptions() /* false */, // all side angles?
						new ThreadSafeOptions() /* false */, // all replicates or sample averge?
						new ThreadSafeOptions(), // images?
						new ThreadSafeOptions(),
						false, // xlsx?
						togglesMetaDataFiltering,
						togglesDataColumns,
						null, null, null, "complete, high mem requ.", ExportSetting.ALL, true);
				expCommand.setDisableNiceNameMapping(allowDataColumnSelection);
				if (!createClusteringDataset) {
					if (allowGroupColumnSelection) {
						final ArrayList<ThreadSafeOptions> togglesDivideDataSetByF = togglesDivideDataSetBy;
						SnapshotVisitor sv = new SnapshotVisitor() {
							@Override
							public void visit(SnapshotDataIAP s) {
								StringBuilder sb = new StringBuilder();
								for (ThreadSafeOptions tso : togglesDivideDataSetByF) {
									if (!tso.getBval(0, false))
										continue;
									String v = s.getFieldValue((ConditionInfo) tso.getParam(1, ConditionInfo.IGNORED_FIELD));
									if (v != null && !v.isEmpty()) {
										if (sb.length() > 0)
											sb.append("/");
										sb.append(v);
									}
								}
								if (sb.length() > 0)
									s.setCondition(sb.toString());
								else
									s.setCondition("(no annotation)");
							}
						};
						expCommand.setSnapshotVisitor(sv);
					}
				}
				expCommand.setClustering(createClusteringDataset);
				expCommand.setPreventMainCSVexport(createClusteringDataset);
				if (createClusteringDataset) {
					expCommand.setCustomClusterTargetFileName(expDir + exportFileName);
					expCommand.setUseIndividualReportNames(true);
				} else {
					expCommand.setCustomTargetFileName(expDir + exportFileName);
					expCommand.setCustomTargetFileName2(expDir + exportFileName);
				}
				expCommand.setExperimentReference(experimentReference);
				expCommand.setStatusProvider(getStatusProvider());
				expCommand.performActionCalculateResults(src);
			}
			ArrayList<String> pl = new ArrayList<String>();
			if (params != null) {
				for (String s : params) {
					if (s.contains("[") && s.contains("]") && s.length() > 2) {
						String spec = s.substring(s.indexOf("[") + 1, s.indexOf("]")).trim();
						String desc = knownSettings.get(spec);
						int value = iniIO.getInstance().getInteger("Parameters", desc, -1);
						String pre = s.substring(0, s.indexOf("["));
						String post = s.substring(s.indexOf("]") + 1);
						pl.add(pre + value + post);
					} else
						pl.add(s);
				}
			}
			String[] params2 = pl.toArray(new String[] {});
			if (params == null)
				params2 = null;
			TreeMap<Long, String> ro = ScriptExecutor.start(
					getDefaultTitle(), cmd, params2, getStatusProvider(), timeoutInMinutes,
					expDir != null ? new File(expDir) : null, true
					);
			res.add("<code>" + StringManipulationTools.getStringList(ro.values(), "<br>")
					+ "</code>");
			
			HashSet<String> knownInputFiles = new HashSet<String>();
			if (exportFileName != null && !exportFileName.isEmpty())
				knownInputFiles.add(exportFileName);
			if (scriptFileNames != null && scriptFileNames.size() > 0)
				for (String s : scriptFileNames)
					if (s != null && !s.isEmpty())
						knownInputFiles.add(s);
			ArrayList<String> newFiles = new ArrayList<String>();
			if (expDir != null)
				for (String f : new File(expDir).list()) {
					if (!knownInputFiles.contains(f))
						newFiles.add(f);
				}
			if (expDir != null && new File(expDir).exists()) {
				final String expDirF = expDir;
				ra.add(new NavigationButton(new AbstractNavigationAction(
						"Open file explorer") {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						AttributeHelper.showInFileBrowser(expDirF, null);
					}
					
					@Override
					public String getDefaultTitle() {
						return "Show Output Folder";
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
			}
			for (String fn : newFiles) {
				IOurl u = FileSystemHandler.getURL(new File(expDir + File.separator + fn));
				String icon = null;
				if (fn != null && fn.contains("."))
					icon = IAPimages.getImageFromFileExtensionGenericIfNotKnown(fn.substring(fn.lastIndexOf(".")));
				if (icon != null)
					ra.add(new Book("", fn.substring(0, fn.lastIndexOf(".")), u, icon).getNavigationButton(src));
				else
					ra.add(new Book("", fn.substring(0, fn.lastIndexOf(".")), u).getNavigationButton(src));
				
			}
		}
		
		if (parameterDetermination) {
			AbstractRscriptExecutionAction ta;
			try {
				ta = new AbstractRscriptExecutionAction(adp, getDefaultTooltip(), scriptIniLocation, experimentReference);
				ta.parameterRequested = true;
				ta.parameterDetermination = false;
				ta.iniIO = iniIO;
				ta.metaDataColumns = metaDataColumns;
				ta.groupSelection = groupSelection;
				ta.listOfDataColumns = listOfDataColumns;
				ta.knownSettings = knownSettings;
				ra.add(new NavigationButton(ta, guiSetting));
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (!parameterRequested) {
			if (exportFileName != null && !exportFileName.isEmpty()) {
				if (allowGroupColumnSelection)
					ra.add(new NavigationButton(new ActionSelectMetaDataColumns(metaDataColumns, experimentReference), guiSetting));
				if (allowGroupFiltering) {
					if (actionGroupSelection == null)
						actionGroupSelection = new ActionFilterConditions(metaDataColumnsReady, metaDataColumns, groupSelection, experimentReference);
					ra.add(new NavigationButton(actionGroupSelection, guiSetting));
				}
				if (allowDataColumnSelection) {
					if (actionSelectDataColumns == null) {
						actionSelectDataColumns = new ActionSelectDataColumns("Select Relevant Data Columns", desiredColumns, experimentReference, listOfDataColumns);
						listOfDataColumns = actionSelectDataColumns.getDataColumnList();
					}
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
			if (ase == null)
				ase = new ActionSettingsEditor(null, iniIO, null, "Parameters");
			for (String s : params) {
				try {
					if (s.contains("[") && s.contains("]") && s.length() > 2) {
						s = s.substring(s.indexOf("[") + 1, s.indexOf("]")).trim();
						String paramSpec = s;
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
								iniIO.getInstance().setInteger("Parameters", desc,
										iniIO.getInstance().getInteger("Parameters", desc, Integer.parseInt(defaultValue)));
								knownSettings.put(paramSpec, desc);
							}
							if (type.equalsIgnoreCase("float")) {
								iniIO.getInstance().setDouble("Parameters", desc,
										iniIO.getInstance().getDouble("Parameters", desc, Double.parseDouble(defaultValue)));
								knownSettings.put(paramSpec, desc);
							}
						}
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
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
			long endTime = System.currentTimeMillis();
			// res.add(0, "<code><font face='Arial' color='gray'>Started " + SystemAnalysis.getCurrentTime(startTime) + "</font></code>");
			res.add("<code><font face='Arial' color='gray'>Run time " + SystemAnalysis.getWaitTime(endTime - startTime)
					+ "</font></code>");
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
