package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public abstract class AbstractSnapshotAnalysisBlockFIS extends AbstractImageAnalysisBlockFIS {
	
	public AbstractSnapshotAnalysisBlockFIS() {
		super();
	}
	
	@Override
	protected FlexibleMaskAndImageSet run() throws InterruptedException {
		final FlexibleImageSet processedImages = new FlexibleImageSet();
		final FlexibleImageSet processedMasks = new FlexibleImageSet();
		
		prepare();
		
		String name = this.getClass().getSimpleName();
		
		BackgroundThreadDispatcher.waitFor(new MyThread[] {
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setVis(processVISimage());
						} catch (InterruptedException e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + " process VIS image", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setFluo(processFLUOimage());
						} catch (InterruptedException e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + "process FLU image", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedImages.setNir(processNIRimage());
						} catch (InterruptedException e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + "process NIR image", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setVis(processVISmask());
						} catch (InterruptedException e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + "process VIS mask", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setFluo(processFLUOmask());
						} catch (InterruptedException e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + "process FLU mask", 1), BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						try {
							processedMasks.setNir(processNIRmask());
						} catch (InterruptedException e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, name + "process NIR mask", 1) });
		
		postProcess(processedImages, processedMasks);
		
		return new FlexibleMaskAndImageSet(processedImages, processedMasks);
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
