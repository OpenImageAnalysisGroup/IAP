package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

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
	protected FlexibleMaskAndImageSet run() {
		final FlexibleImageSet processedImages = new FlexibleImageSet();
		final FlexibleImageSet processedMasks = new FlexibleImageSet();
		
		prepare();
		
		String name = this.getClass().getSimpleName();
		
		BackgroundThreadDispatcher.waitFor(new MyThread[] {
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedImages.setVis(processVISimage());
					}
				}, name + " process VIS image", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedImages.setFluo(processFLUOimage());
					}
				}, name + "process FLU image", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedImages.setNir(processNIRimage());
					}
				}, name + "process NIR image", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedMasks.setVis(processVISmask());
					}
				}, name + "process VIS mask", 1),
				BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedMasks.setFluo(processFLUOmask());
					}
				}, name + "process FLU mask", 1), BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						processedMasks.setNir(processNIRmask());
					}
				}, name + "process NIR mask", 1) });
		
		return new FlexibleMaskAndImageSet(processedImages, processedMasks);
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
		return getInput().getImages().getNir();
	}
	
	protected FlexibleImage processVISmask() {
		return getInput().getMasks().getVis();
	}
	
	protected FlexibleImage processFLUOmask() {
		return getInput().getMasks().getFluo();
	}
	
	protected FlexibleImage processNIRmask() {
		return getInput().getMasks().getNir();
	}
	
}
