package de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.IniIoProvider;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class UserDefinedImageAnalysisPipelineTask extends AbstractPhenotypingTask {
	
	private final String pipelineFileName;
	private final String desc;
	private final IniIoProvider iniIO;
	
	public UserDefinedImageAnalysisPipelineTask(String name, IniIoProvider iniIO, String desc) {
		this.pipelineFileName = name;
		this.iniIO = iniIO;
		this.desc = desc;
	}
	
	@Override
	public String getTaskDescription() {
		return desc;
	}
	
	@Override
	public String getName() {
		return pipelineFileName;
	}
	
	@Override
	public IniIoProvider getIniIo() {
		return iniIO;
	}
	
	@Override
	protected boolean analyzeTopImages() {
		return true;
	}
	
	@Override
	protected boolean analyzeSideImages() {
		return true;
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new AbstractImageProcessor() {
			private BackgroundTaskStatusProviderSupportingExternalCall status;
			
			@Override
			public void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status) {
				this.status = status;
				
			}
			
			@Override
			public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
				return status;
			}
			
			@Override
			public IAP_RELEASE getVersionTag() {
				return null;
			}
			
			@Override
			public BlockPipeline getPipeline(ImageProcessorOptions options) {
				return getPipelineFromBlockList(null, iniIO, null);
			}
		};
	}
	
}
