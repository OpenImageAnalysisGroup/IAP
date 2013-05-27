package de.ipk.ag_ba.plugins;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginEntry;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;

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
			public void processPlugin(IAPplugin p) {
				for (ActionDataProcessing dp : p.getDataProcessingTools(experimentReference)) {
					dataProcessingActions.add(dp);
				}
			}
		};
		processPlugins(r);
		return dataProcessingActions;
	}
}
