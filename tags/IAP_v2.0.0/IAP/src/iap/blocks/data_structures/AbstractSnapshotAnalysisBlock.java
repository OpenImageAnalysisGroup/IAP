package iap.blocks.data_structures;

import info.StopWatch;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

/**
 * @author Christian Klukas
 */
public abstract class AbstractSnapshotAnalysisBlock extends AbstractImageAnalysisBlockFIS {
	
	public AbstractSnapshotAnalysisBlock() {
		super();
	}
	
	@Override
	protected MaskAndImageSet run() throws InterruptedException {
		if (!getBoolean("enabled", true))
			return input();
		
		StopWatch ppw = new StopWatch(ExecutionTimeStep.BLOCK_PREPARE + "");
		try {
			prepare();
			addExecutionTime(ExecutionTimeStep.BLOCK_PREPARE, ppw.getTime());
		} catch (Error err1) {
			addExecutionTime(ExecutionTimeStep.BLOCK_PREPARE, -ppw.getTime());
			reportError(err1, "could not pre-process block - error");
		} catch (Exception err2) {
			addExecutionTime(ExecutionTimeStep.BLOCK_PREPARE, -ppw.getTime());
			reportError(err2, "could not pre-process block - exception");
		}
		
		String name = this.getClass().getSimpleName();
		boolean directRun = true;
		final LocalComputeJob[] work = new LocalComputeJob[] {
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.NIR))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_NIR + "");
						try {
							input().images().setNir(processNIRimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(er, "could not process NIR image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(e, "could not process NIR image - exception");
						}
					}, name + " process NIR image", false, directRun),
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.IR))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_IR + "");
						try {
							input().images().setIr(processIRimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(er, "could not process IR image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(e, "could not process IR image - exception");
						}
					}, name + " process IR image", false, directRun),
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.NIR))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_NIR + "");
						try {
							input().masks().setNir(processNIRmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(er, "could not process NIR mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_NIR, -pw.getTime());
							reportError(e, "could not process NIR mask - exception");
						}
					}, name + " process NIR mask", false, directRun),
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.IR))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_IR + "");
						try {
							input().masks().setIr(processIRmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(er, "could not process IR mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_IR, -pw.getTime());
							reportError(e, "could not process IR mask - exception");
						}
					}, name + " process IR mask", false, directRun),
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.VIS))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_VIS + "");
						try {
							input().images().setVis(processVISimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(er, "could not process VIS image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(e, "could not process VIS image - exception");
						}
					}, name + " process VIS image", false, directRun),
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.VIS))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_VIS + "");
						try {
							input().masks().setVis(processVISmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(er, "could not process VIS mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_VIS, -pw.getTime());
							reportError(e, "could not process VIS mask - exception");
						}
					}, name + " process VIS mask", false, directRun),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						if (!getCameraInputTypes().contains(CameraType.FLUO))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_FLUO + "");
						try {
							input().images().setFluo(processFLUOimage());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(er, "could not process FLUO image - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(e, "could not process FLUO image - exception");
						}
					}
				}, name + " process FLU image", false, directRun),
				BackgroundThreadDispatcher.addTask(() -> {
						if (!getCameraInputTypes().contains(CameraType.FLUO))
							return;
						StopWatch pw = new StopWatch(ExecutionTimeStep.BLOCK_PROCESS_FLUO + "");
						try {
							input().masks().setFluo(processFLUOmask());
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, pw.getTime());
						} catch (Error er) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(er, "could not process FLUO mask - error");
						} catch (Exception e) {
							addExecutionTime(ExecutionTimeStep.BLOCK_PROCESS_FLUO, -pw.getTime());
							reportError(e, "could not process FLUO mask - exception");
						}
					}, name + " process FLU mask", false, directRun) };
		
		BackgroundThreadDispatcher.waitFor(work);
		
		ppw = new StopWatch(ExecutionTimeStep.BLOCK_POST_PROCESS + "");
		try {
			postProcess(input().images(), input().masks());
			addExecutionTime(ExecutionTimeStep.BLOCK_POST_PROCESS, ppw.getTime());
		} catch (Error e) {
			addExecutionTime(ExecutionTimeStep.BLOCK_POST_PROCESS, -ppw.getTime());
			reportError(e, "Could not perform post-processing - error");
		} catch (Exception e) {
			addExecutionTime(ExecutionTimeStep.BLOCK_POST_PROCESS, -ppw.getTime());
			reportError(e, "Could not perform post-processing - exception");
		}
		return input();
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
	
}
