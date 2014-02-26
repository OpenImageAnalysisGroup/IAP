package iap.blocks.data_structures;

import info.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public abstract class AbstractSnapshotAnalysisBlock extends AbstractImageAnalysisBlockFIS {
	
	public AbstractSnapshotAnalysisBlock() {
		super();
	}
	
	@Override
	protected MaskAndImageSet run() throws InterruptedException {
		if (!getBoolean("enabled", true))
			return input();
		final ImageSet processedImages = new ImageSet(input().images());
		final ImageSet processedMasks = new ImageSet(input().images());
		
		StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PREPARE + "");
		try {
			prepare();
			addExecutionTime(ExecutionTimeStep.BLOCK_PREPARE, pw.getTime());
		} catch (Error err1) {
			addExecutionTime(ExecutionTimeStep.BLOCK_PREPARE, -pw.getTime());
			reportError(err1, "could not pre-process block - error");
		} catch (Exception err2) {
			addExecutionTime(ExecutionTimeStep.BLOCK_PREPARE, -pw.getTime());
			reportError(err2, "could not pre-process block - exception");
		}
		
		String name = this.getClass().getSimpleName();
		
		final LocalComputeJob[] work = new LocalComputeJob[] {
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_VIS + "");
						try {
							processedImages.setVis(processVISimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(er, "could not process VIS image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(e, "could not process VIS image - exception");
						}
					}
				}, name + " process VIS image", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_FLUO + "");
						try {
							processedImages.setFluo(processFLUOimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(er, "could not process FLUO image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(e, "could not process FLUO image - exception");
						}
					}
				}, name + " process FLU image", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_NIR + "");
						try {
							processedImages.setNir(processNIRimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(er, "could not process NIR image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(e, "could not process NIR image - exception");
						}
					}
				}, name + " process NIR image", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_IR + "");
						try {
							processedImages.setIr(processIRimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(er, "could not process IR image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(e, "could not process IR image - exception");
						}
					}
				}, name + " process NIR image", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_VIS + "");
						try {
							processedMasks.setVis(processVISmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(er, "could not process VIS mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(e, "could not process VIS mask - exception");
						}
					}
				}, name + " process VIS mask", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_FLUO + "");
						try {
							processedMasks.setFluo(processFLUOmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(er, "could not process FLUO mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(e, "could not process FLUO mask - exception");
						}
					}
				}, name + " process FLU mask", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_NIR + "");
						try {
							processedMasks.setNir(processNIRmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(er, "could not process NIR mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(e, "could not process NIR mask - exception");
						}
					}
				}, name + " process NIR mask", true),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_IR + "");
						try {
							processedMasks.setIr(processIRmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(er, "could not process IR mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(e, "could not process IR mask - exception");
						}
					}
				}, name + " process IR mask", true) };
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_POST_PROCESS + "");
				try {
					BackgroundThreadDispatcher.waitFor(work);
					postProcess(processedImages, processedMasks);
					addExecutionTime(ExecutionTimeStep.BLOCK_POST_PROCESS, pw.getTime());
				} catch (Error e) {
					addExecutionTime(ExecutionTimeStep.BLOCK_POST_PROCESS, -pw.getTime());
					reportError(e, "Could not perform post-processing - error");
				} catch (Exception e) {
					addExecutionTime(ExecutionTimeStep.BLOCK_POST_PROCESS, -pw.getTime());
					reportError(e, "Could not perform post-processing - exception");
				}
			}
		};
		r.run();// BackgroundThreadDispatcher.addTask(r, "process block data", true).getResult();
		return new MaskAndImageSet(processedImages, processedMasks);
	}
	
	protected Image processVISimage() {
		return input().images().vis();
	}
	
	protected Image processFLUOimage() {
		return input().images().fluo();
	}
	
	protected Image processNIRimage() {
		if (input() != null && input().images() != null)
			return input().images().nir();
		else
			return null;
	}
	
	protected Image processIRimage() {
		if (input() != null && input().images() != null)
			return input().images().ir();
		else
			return null;
	}
	
	protected Image processVISmask() {
		return input().masks().vis();
	}
	
	protected Image processFLUOmask() {
		return input().masks().fluo();
	}
	
	protected Image processNIRmask() {
		return input().masks().nir();
	}
	
	protected Image processIRmask() {
		return input().masks().ir();
	}
	
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		// empty
	}
	
	@Override
	public String getDescriptionForParameters() {
		return null;
	}
	
	public boolean isBestAngle() {
		HashMap<String, ArrayList<BlockResultValue>> previousResults = optionsAndResults
				.searchResultsOfCurrentSnapshot("RESULT_top.fluo.main.axis.rotation", true, getWellIdx(), null);
		
		double sum = 0;
		int count = 0;
		
		for (ArrayList<BlockResultValue> b : previousResults.values()) {
			for (BlockResultValue c : b) {
				count++;
				sum += c.getValue();
			}
		}
		
		if (count == 0) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Can´t calculate leaf tips, no main axis calculation available!");
			return false;
		}
		
		ImageData currentImage = input().images().getAnyInfo();
		
		double mainRotationFromTopView = sum / count;
		double mindist = Double.MAX_VALUE;
		boolean currentImageIsBest = false;
		
		for (NumericMeasurementInterface nmi : currentImage.getParentSample()) {
			if (nmi instanceof ImageData) {
				Double r = ((ImageData) nmi).getPosition();
				if (r == null)
					r = 0d;
				double dist = Math.abs(mainRotationFromTopView - r);
				if (dist < mindist) {
					mindist = dist;
					if ((((ImageData) nmi).getPosition() + "").equals((currentImage.getPosition() + "")))
						currentImageIsBest = true;
					else
						currentImageIsBest = false;
				}
			}
		}
		
		if (!currentImageIsBest)
			return false;
		
		return true;
	}
}
