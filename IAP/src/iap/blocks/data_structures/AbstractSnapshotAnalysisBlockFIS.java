package iap.blocks.data_structures;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

public abstract class AbstractSnapshotAnalysisBlockFIS extends AbstractImageAnalysisBlockFIS {
	
	private int prio = 4;
	
	public AbstractSnapshotAnalysisBlockFIS() {
		super();
	}
	
	@Override
	protected MaskAndImageSet run() throws InterruptedException {
		if (!getBoolean("enabled", true))
			return input();
		int parentPriority = getParentPriority();
		final ImageSet processedImages = new ImageSet(input().images());
		final ImageSet processedMasks = new ImageSet(input().images());
		
		try {
			prepare();
		} catch (Error err1) {
			reportError(err1, "BLOCK PREPARE ERROR: " + err1.getLocalizedMessage());
		} catch (Exception err2) {
			reportError(err2, "BLOCK PREPARE EXCEPTION: " + err2.getLocalizedMessage());
		}
		
		String name = this.getClass().getSimpleName();
		
		BackgroundThreadDispatcher.waitFor(new MyThread[] {
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setVis(processVISimage());
						} catch (Exception e) {
							reportError(e, "could not process VIS image");
						}
					}
				}, name + " process VIS image", parentPriority + 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setFluo(processFLUOimage());
						} catch (Exception e) {
							reportError(e, "could not process FLU image");
						}
					}
				}, name + " process FLU image", parentPriority + 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setNir(processNIRimage());
						} catch (Exception e) {
							reportError(e, "could not process NIR image");
						}
					}
				}, name + " process NIR image", 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setIr(processIRimage());
						} catch (Exception e) {
							reportError(e, "could not process IR image");
						}
					}
				}, name + " process NIR image", 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setVis(processVISmask());
						} catch (Exception e) {
							e.printStackTrace();
							reportError(e, "could not process VIS mask");
						}
					}
				}, name + " process VIS mask", parentPriority + 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setFluo(processFLUOmask());
						} catch (Exception e) {
							reportError(e, "could not process FLUO mask");
						}
					}
				}, name + " process FLU mask", parentPriority + 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setNir(processNIRmask());
						} catch (Exception e) {
							reportError(e, "could not process NIR mask");
						}
						
					}
				}, name + " process NIR mask", parentPriority + 1, parentPriority, false),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setIr(processIRmask());
						} catch (Exception e) {
							reportError(e, "could not process IR mask");
						}
						
					}
				}, name + " process IR mask", parentPriority + 1, parentPriority, false) });
		
		try {
			postProcess(processedImages, processedMasks);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		
		return new MaskAndImageSet(processedImages, processedMasks);
	}
	
	protected int getParentPriority() {
		return prio;
	}
	
	public void setParentPriority(int prio) {
		this.prio = prio;
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
}
