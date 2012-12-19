package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis.ArabidopsisAnalysisSmallBlueRubberTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis.ArabidopsisAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.ImageAnalysisPipelineTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.roots.RootsAnalysisTask;

public class ImageAnalysisTasks {
	
	public ImageAnalysisTasks() {
		// empty
	}
	
	public ArrayList<AbstractPhenotypingTask> getKnownImageAnalysisTasks() {
		ArrayList<AbstractPhenotypingTask> res = new ArrayList<AbstractPhenotypingTask>();
		res.add(new MaizeAnalysisTask());
		for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
			res.add(new ImageAnalysisPipelineTask(pd.getName(), null, pd.getTooltip()));
		res.add(new ArabidopsisAnalysisTask());
		res.add(new ArabidopsisAnalysisSmallBlueRubberTask());
		res.add(new RootsAnalysisTask());
		return res;
	}
}
