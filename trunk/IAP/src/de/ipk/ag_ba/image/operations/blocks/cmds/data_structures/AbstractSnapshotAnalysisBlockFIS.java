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
	protected FlexibleMaskAndImageSet run() throws InterruptedException {
		int parentPriority = getParentPriority();
		final FlexibleImageSet processedImages = new FlexibleImageSet(getInput().getImages());
		final FlexibleImageSet processedMasks = new FlexibleImageSet(getInput().getImages());
		
		try {
			prepare();
		} catch (Error err1) {
			System.out.println("ERROR: ERROR: " + err1.getLocalizedMessage());
			err1.printStackTrace();
		} catch (Exception err2) {
			System.out.println("ERROR: EXCEPTION: " + err2.getLocalizedMessage());
			err2.printStackTrace();
			ErrorMsg.addErrorMessage(err2);
		}
		
		String name = this.getClass().getSimpleName();
		
		BackgroundThreadDispatcher.waitFor(new MyThread[] {
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setVis(processVISimage());
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process VIS image", 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setFluo(processFLUOimage());
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process FLU image", 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setNir(processNIRimage());
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process NIR image", 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setVis(processVISmask());
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process VIS mask", 1, parentPriority),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setFluo(processFLUOmask());
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process FLU mask", 1, parentPriority), BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setNir(processNIRmask());
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process NIR mask", 1, parentPriority) });
		
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
	
	protected FlexibleImage processVISimage() throws InterruptedException {
		return getInput().getImages().getVis();
	}
	
	protected FlexibleImage processFLUOimage() throws InterruptedException {
		return getInput().getImages().getFluo();
	}
	
	protected FlexibleImage processNIRimage() throws InterruptedException {
		return getInput().getImages().getNir();
	}
	
	protected FlexibleImage processVISmask() throws InterruptedException {
		return getInput().getMasks().getVis();
	}
	
	protected FlexibleImage processFLUOmask() throws InterruptedException {
		return getInput().getMasks().getFluo();
	}
	
	protected FlexibleImage processNIRmask() throws InterruptedException {
		return getInput().getMasks().getNir();
	}
	
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		// empty
	}
}
