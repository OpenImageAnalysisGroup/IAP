package de.ipk_gatersleben.ag_pbi.mmd.unittests;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.SpatialExperimentDataLoader;

public class TestConditionMerging {
	
	@Test
	public void testConditionMerging() throws URISyntaxException {
		ArrayList<File> files = new ArrayList<File>();
		
		DataMappingTypeManager3D.replaceVantedMappingTypeManager();
		
		files.add(new File(MultimodalDataHandlingAddon.class.getResource("unittests/condition_merging.xlsx").toURI())); //$NON-NLS-1$
		
		List<ExperimentInterface> exps = new SpatialExperimentDataLoader().process(files, null, null);
		Experiment exp = (Experiment) exps.get(0);
		
		HashSet<ConditionInterface> cons = new HashSet<ConditionInterface>();
		for (SubstanceInterface sub : exp)
			for (ConditionInterface con : sub)
				cons.add(con);
		
		Assert.assertEquals(8, cons.size());
		
	}
}
