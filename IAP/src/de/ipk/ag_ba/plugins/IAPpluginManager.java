package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.pipelines.ImageProcessorOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.ErrorMsg;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginEntry;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.TemplatePhenotypingTask;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;

/**
 * @author Christian Klukas
 */
public class IAPpluginManager {
	private static IAPpluginManager instance = new IAPpluginManager();
	
	private IAPpluginManager() {
		instance = this;
	}
	
	public static IAPpluginManager getInstance() {
		return instance;
	}
	
	private void processPlugins(RunnableOnIAPplugin r) {
		while (!ErrorMsg.areApploadingAndFinishActionsCompleted())
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			}
		for (PluginEntry pe : DefaultPluginManager.lastInstance.getPluginEntries()) {
			if (pe.getPlugin() instanceof IAPplugin) {
				IAPplugin ip = (IAPplugin) pe.getPlugin();
				r.processPlugin(ip);
			}
		}
	}
	
	public Collection<NavigationAction> getHomeActions() {
		final Collection<NavigationAction> actions = new ArrayList<NavigationAction>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				for (NavigationAction na : p.getHomeNavigationActions()) {
					actions.add(na);
				}
			}
		};
		processPlugins(r);
		return actions;
	}
	
	public Collection<DataSource> getHomeDataSources() {
		final Collection<DataSource> datasources = new ArrayList<DataSource>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				for (DataSource ds : p.getDataSources()) {
					datasources.add(ds);
				}
			}
		};
		processPlugins(r);
		return datasources;
	}
	
	public Collection<ActionDataProcessing> getExperimentProcessingActions(
			final ExperimentReference experimentReference,
			final boolean imageAnalysis) {
		final Collection<ActionDataProcessing> dataProcessingActions = new ArrayList<ActionDataProcessing>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				for (ActionDataProcessing dp : p.getDataProcessingActions(experimentReference)) {
					if ((imageAnalysis && dp.isImageAnalysisCommand()) || !dp.isImageAnalysisCommand())
						dataProcessingActions.add(dp);
				}
			}
		};
		processPlugins(r);
		return dataProcessingActions;
	}
	
	public Collection<ActionDataProcessing> getExperimentToolActions(
			final ExperimentReference experimentReference) {
		final Collection<ActionDataProcessing> dataProcessingActions = new ArrayList<ActionDataProcessing>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				for (ActionDataProcessing dp : p.getDataProcessingTools(experimentReference)) {
					dataProcessingActions.add(dp);
				}
			}
		};
		processPlugins(r);
		return dataProcessingActions;
	}
	
	public Collection<ActionScriptBasedDataProcessing> getExperimentScriptActions(
			final ExperimentReference experimentReference) {
		final Collection<ActionScriptBasedDataProcessing> scriptBasedProcessingActions = new ArrayList<ActionScriptBasedDataProcessing>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				for (ActionScriptBasedDataProcessing dp : p.getScriptBasedDataProcessingTools(experimentReference)) {
					scriptBasedProcessingActions.add(dp);
				}
			}
		};
		processPlugins(r);
		return scriptBasedProcessingActions;
	}
	
	public Collection<String> getSettingHelp(final String iniFileName, final String section, final String setting) {
		final Collection<String> helpTexts = new ArrayList<String>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				Collection<String> htc = p.getHelpForSettings(iniFileName, section, setting);
				if (htc != null)
					for (String dp : htc) {
						helpTexts.add(dp);
					}
			}
		};
		processPlugins(r);
		return helpTexts;
	}
	
	public Collection<AnalysisPipelineTemplate> getAnalysisTemplates() {
		final Collection<AnalysisPipelineTemplate> templates = new ArrayList<AnalysisPipelineTemplate>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				for (AnalysisPipelineTemplate template : p.getAnalysisTemplates()) {
					templates.add(template);
				}
			}
		};
		processPlugins(r);
		return templates;
	}
	
	public void writePipelineInis() throws Exception {
		for (final AnalysisPipelineTemplate template : getAnalysisTemplates()) {
			PipelineDesc pd = template.getDefaultPipelineDesc();
			AbstractPhenotypingTask pt = new TemplatePhenotypingTask(pd, template);
			try {
				pt.getImageProcessor().getPipeline(new ImageProcessorOptions(pd.getOptions(), null));
			} catch (Error e) {
				ErrorMsg.addErrorMessage("Could not process template " + pd.getName() + ": " + e.getMessage());
			}
		}
	}
	
	public Collection<ImageAnalysisBlock> getKnownAnalysisBlocks() {
		final Collection<ImageAnalysisBlock> blocks = new ArrayList<ImageAnalysisBlock>();
		RunnableOnIAPplugin r = new RunnableOnIAPplugin() {
			@Override
			public void processPlugin(IAPplugin p) {
				HashSet<String> known = new HashSet<String>();
				ImageAnalysisBlock[] bla = p.getImageAnalysisBlocks();
				if (bla != null)
					for (ImageAnalysisBlock bl : bla) {
						String className = bl.getClass().getCanonicalName();
						if (!known.contains(className)) {
							blocks.add(bl);
							known.add(className);
						}
					}
			}
		};
		processPlugins(r);
		return blocks;
	}
}
