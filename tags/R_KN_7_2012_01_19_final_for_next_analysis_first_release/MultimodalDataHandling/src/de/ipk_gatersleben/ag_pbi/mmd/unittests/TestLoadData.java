package de.ipk_gatersleben.ag_pbi.mmd.unittests;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.SpatialExperimentDataLoader;

public class TestLoadData {
	
	@Test
	public void testLoadNormalAndTransposedSpatialDataTemplate() throws URISyntaxException {
		ArrayList<File> files = new ArrayList<File>();
		
		DataMappingTypeManager3D.replaceVantedMappingTypeManager();
		
		files.add(new File(MultimodalDataHandlingAddon.class.getResource("spatial_template.xls").toURI()));
		files.add(new File(MultimodalDataHandlingAddon.class.getResource("spatial_template_transposed.xls").toURI()));
		
		List<ExperimentInterface> exps = new SpatialExperimentDataLoader().process(files, null, null);
		
		Assert.assertEquals(2, exps.size());
		
		Assert.assertEquals(exps.get(0).toString(), exps.get(1).toString());
	}
	
}
