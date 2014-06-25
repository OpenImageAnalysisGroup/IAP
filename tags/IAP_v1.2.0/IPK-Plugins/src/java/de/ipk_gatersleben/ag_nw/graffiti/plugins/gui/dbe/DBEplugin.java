/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 25.11.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.editcomponent.JComponentParameterEditor;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.DBE_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.AddDiagramLegendAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.CreateDistanceMatrixAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.ExtractMappingDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RecolorEdgesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RemoveMappingDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.SetNumericAttributeFromDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.InterpreteLabelNamesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.kegg_reaction.CreateKeggReactionNetworkAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectCompoundsAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectEdgesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectEnzymesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectNodesWithExperimentalDataAlgorithm;

public class DBEplugin
		extends DBE_PluginAdapter
		implements EditorPlugin {
	@SuppressWarnings("unchecked")
	private final HashMap valueEditComponents = new HashMap();
	
	@SuppressWarnings("unchecked")
	public DBEplugin() {
		valueEditComponents.put(
				JComponentParameter.class,
				JComponentParameterEditor.class);
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING)) {
			/*
			 * extensions = new Extension[] {
			 * new DBEextension()
			 * };
			 */
			algorithms = new Algorithm[] {
					new InterpreteLabelNamesAlgorithm(),
					// new ProcessHierarchynodesDepOnLeafNodes(),
					// new CreateKeggReactionNetworkAlgorithm(),
					// new PruneTreeAlgorithm(),
					// new CreateDirectChildrenClustersHistogramAlgorithm(),
					new ExtractMappingDataAlgorithm(),
					new RemoveMappingDataAlgorithm(),
					new CombineMappingData(),
					new AddDiagramLegendAlgorithm(),
					new SetNumericAttributeFromDataAlgorithm(),
					new RecolorEdgesAlgorithm(),
					// new ColorScaleLegendAlgorithm(),
					new CreateDistanceMatrixAlgorithm(),
					new ShowOrHideImageAttributesAlgorithm(),
					new SelectCompoundsAlgorithm(),
					new SelectEnzymesAlgorithm(),
					// new SelectReactionsAlgorithm(),
					new SplitNodeForSingleMappingData(),
					new MergeNodes(),
					new UserMappingAlgorithm(),
					// new TranspathPathwayLoader(),
					// new CreateHierarchyTree(),
					// new HierarchyWizard()
			};
		} else {
			algorithms = new Algorithm[] {
					new InterpreteLabelNamesAlgorithm(),
					new SelectNodesWithExperimentalDataAlgorithm(),
					new SelectEdgesAlgorithm(),
					new CreateKeggReactionNetworkAlgorithm()
			};
		}
	}
	
	@Override
	public ImageIcon getIcon() {
		try {
			ClassLoader cl = this.getClass().getClassLoader();
			String path = this.getClass().getPackage().getName().replace('.', '/');
			ImageIcon icon = new ImageIcon(cl.getResource(path + "/dbe_logo_16x16.png"));
			return icon;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			return super.getIcon();
		}
	}
	
	@Override
	public Map<?, ?> getAttributeComponents() {
		return null;
	}
	
	@Override
	public GraffitiComponent[] getGUIComponents() {
		return null;
	}
	
	@Override
	public Mode[] getModes() {
		return null;
	}
	
	@Override
	public GraffitiShape[] getShapes() {
		return null;
	}
	
	@Override
	public Tool[] getTools() {
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<Class<JComponentParameter>, Class<JComponentParameterEditor>> getValueEditComponents() {
		return valueEditComponents;
	}
	
	@Override
	public InspectorTab[] getInspectorTabs() {
		return null;
	}
	
}
