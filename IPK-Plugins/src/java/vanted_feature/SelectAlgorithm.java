package vanted_feature;

import java.util.ArrayList;
import java.util.Collection;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectEdgesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectNodesWithExperimentalDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.InvertSelectionAlgorithm;

public class SelectAlgorithm extends LaunchGui {
	
	public SelectAlgorithm() {
		super();
		algBTsize = ButtonSize.DYNAMIC;
		modal = false;
	}
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		
		for (Algorithm a : PluginFeatureHierarchyCommands.getSelectionAlgorithms())
			res.add(a);
		
		if (res.size() > 0)
			res.add(null);
		
		res.add(new InvertSelectionAlgorithm());
		res.add(new AbstractAlgorithm() {
			@Override
			public String getName() {
				return "Clear Selection";
			}
			
			@Override
			public void execute() {
				Selection selection = new Selection("cleared selection");
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(selection);
			}
		});
		
		if (res.size() > 2)
			res.add(null);
		
		if (PluginFeatureClusterCommands.getSelectClusterAlgorithm() != null)
			res.add(PluginFeatureClusterCommands.getSelectClusterAlgorithm());
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING)) {
			res.add(new SelectNodesWithExperimentalDataAlgorithm());
			res.add(new SelectEdgesAlgorithm());
		}
		
		return res;
	}
	
	@Override
	public boolean closeDialogBeforeExecution(Algorithm algorithm) {
		return false;// algorithm instanceof SelectClusterAlgorithm || algorithm instanceof SelectNodesWithExperimentalDataAlgorithm || algorithm instanceof
		// SelectEdgesAlgorithm;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Selection...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
}
