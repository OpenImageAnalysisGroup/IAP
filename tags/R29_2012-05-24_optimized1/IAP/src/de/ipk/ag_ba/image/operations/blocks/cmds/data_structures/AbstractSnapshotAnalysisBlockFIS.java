package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import org.ErrorMsg;
import org.graffiti.plugin.parameter.Parameter;

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
		final FlexibleImageSet processedImages = new FlexibleImageSet(input().images());
		final FlexibleImageSet processedMasks = new FlexibleImageSet(input().images());
		
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
		return input().images().vis();
	}
	
	protected FlexibleImage processFLUOimage() {
		return input().images().fluo();
	}
	
	protected FlexibleImage processNIRimage() {
		if (input() != null && input().images() != null)
			return input().images().nir();
		else
			return null;
	}
	
	protected FlexibleImage processIRimage() {
		if (input() != null && input().images() != null)
			return input().images().getIr();
		else
			return null;
	}
	
	protected FlexibleImage processVISmask() {
		return input().masks().vis();
	}
	
	protected FlexibleImage processFLUOmask() {
		if (input() == null)
			System.out.println("ERROR 1");
		if (input().masks() == null)
			System.out.println("ERROR 2");
		return input().masks().fluo();
	}
	
	protected FlexibleImage processNIRmask() {
		if (input() == null)
			System.out.println("ERROR 3");
		if (input().masks() == null)
			System.out.println("ERROR 4");
		return input().masks().nir();
	}
	
	protected FlexibleImage processIRmask() {
		if (input() == null)
			System.out.println("ERROR 5");
		if (input().masks() == null)
			System.out.println("ERROR 6");
		return input().masks().getIr();
	}
	
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		// empty
	}
	
	@Override
	public Parameter[] getParameters() {
		// empty
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// empty
	}
}
