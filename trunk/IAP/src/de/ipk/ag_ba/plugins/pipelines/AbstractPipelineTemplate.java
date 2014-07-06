package de.ipk.ag_ba.plugins.pipelines;

import org.StringManipulationTools;

import de.ipk.ag_ba.gui.PipelineDesc;

/**
 * @author klukas
 */
public abstract class AbstractPipelineTemplate implements AnalysisPipelineTemplate {
	
	@Override
	public PipelineDesc getDefaultPipelineDesc() {
		PipelineDesc pd = new PipelineDesc(
				StringManipulationTools.getFileSystemName(getTitle()) + ".pipeline.ini",
				null,
				getTitle(),
				getDescription(),
				getTestedIAPversion());
		return pd;
	}
	
	@Override
	public String getTestedIAPversion() {
		return "(not tested)";
	}
}
