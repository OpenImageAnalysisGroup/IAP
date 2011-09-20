package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public abstract class AbstractSnapshotAnalysisBlockFIS extends AbstractImageAnalysisBlockFIS {
	
	private int prio = 4;
	
	public AbstractSnapshotAnalysisBlockFIS() {
		super();
	}
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		int parentPriority = getParentPriority();
		final FlexibleImageSet processedImages = new FlexibleImageSet(getInput().getImages());
		final FlexibleImageSet processedMasks = new FlexibleImageSet(getInput().getImages());
		
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
				}, name + " process VIS image", parentPriority + 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setFluo(processFLUOimage());
						} catch (Exception e) {
							reportError(e, "could not process FLU image");
						}
					}
				}, name + " process FLU image", parentPriority + 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setNir(processNIRimage());
						} catch (Exception e) {
							reportError(e, "could not process NIR image");
						}
					}
				}, name + " process NIR image", 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setVis(processVISmask());
						} catch (Exception e) {
							reportError(e, "could not process VIS mask");
						}
					}
				}, name + " process VIS mask", parentPriority + 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedMasks.setFluo(processFLUOmask());
					}
				}, name + " process FLU mask", parentPriority + 1, parentPriority), BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedMasks.setNir(processNIRmask());
					}
				}, name + " process NIR mask", parentPriority + 1, parentPriority) });
		
		try {
			postProcess(processedImages, processedMasks);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		
		return new FlexibleMaskAndImageSet(processedImages, processedMasks);
	}
	
	protected int getParentPriority() {
		return prio;
	}
	
	public void setParentPriority(int prio) {
		this.prio = prio;
	}
	
	protected void prepare() {
		// empty
	}
	
	protected FlexibleImage processVISimage() {
		return getInput().getImages().getVis();
	}
	
	protected FlexibleImage processFLUOimage() {
		return getInput().getImages().getFluo();
	}
	
	protected FlexibleImage processNIRimage() {
		if (getInput() != null && getInput().getImages() != null)
			return getInput().getImages().getNir();
		else
			return null;
	}
	
	protected FlexibleImage processVISmask() {
		return getInput().getMasks().getVis();
	}
	
	protected FlexibleImage processFLUOmask() {
		if (getInput() == null)
			System.out.println("ERROR 1");
		if (getInput().getMasks() == null)
			System.out.println("ERROR 2");
		return getInput().getMasks().getFluo();
	}
	
	protected FlexibleImage processNIRmask() {
		if (getInput() == null)
			System.out.println("ERROR 3");
		if (getInput().getMasks() == null)
			System.out.println("ERROR 4");
		return getInput().getMasks().getNir();
	}
	
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		// empty
	}
}
