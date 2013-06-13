package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.PipelineDesc;

public class ImageAnalysisTasks {
	
	public ImageAnalysisTasks() {
		// empty
	}
	
	public ArrayList<AbstractPhenotypingTask> getKnownImageAnalysisTasks() throws Exception {
		ArrayList<AbstractPhenotypingTask> res = new ArrayList<AbstractPhenotypingTask>();
		for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates()) {
			res.add(new UserDefinedImageAnalysisPipelineTask(pd));
		}
		
		// res.add(new MaizeAnalysisTask());
		// res.add(new ArabidopsisAnalysisTask());
		// res.add(new ArabidopsisAnalysisSmallBlueRubberTask());
		// res.add(new RootsAnalysisTask());
		
		return res;
	}
}
