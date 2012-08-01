package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import java.awt.Color;
import java.util.ArrayList;

import org.ObjectRef;
import org.Vector2d;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MaskOperation;
import de.ipk.ag_ba.image.operations.MaskOperationDirect;
import de.ipk.ag_ba.image.operations.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.operations.blocks.cmds.WorkingImageTyp;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public abstract class BlockAutomaticParameterSearch extends AbstractImageAnalysisBlockFIS {
	
	// enum Property {
	// ROTATION_FLUO, ROTATION_NIR, TRANSLATION_FLUO_X, TRANSLATION_FLUO_Y, TRANSLATION_NIR_X, TRANSLATION_NIR_Y
	// }
	
	private final MorphologicalOperationSearchType typeOfSearch;
	private final WorkingImageTyp workingOnWhichImage;
	
	public BlockAutomaticParameterSearch(MorphologicalOperationSearchType typeOfSearch, WorkingImageTyp workingOnWhichImage) {
		this.typeOfSearch = typeOfSearch;
		this.workingOnWhichImage = workingOnWhichImage;
		
	}
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		return automaticSearch(typeOfSearch, workingOnWhichImage);
	}
	
	private FlexibleMaskAndImageSet automaticSearch(MorphologicalOperationSearchType typ, WorkingImageTyp workingOnWhichImage) {
		
		switch (workingOnWhichImage) {
			case FLUO_AND_NIR:
				options.clearAndAddBooleanSetting(Setting.PROCESS_NIR, true);
				break;
			
			case ONLY_FLUO:
				options.clearAndAddBooleanSetting(Setting.PROCESS_NIR, false);
				break;
		}
		
		ObjectRef resultValues = new ObjectRef();
		Vector2d t = null;
		FlexibleImage fluoMask = null;
		FlexibleImage fluoImage = null;
		FlexibleImage nirMask = null;
		FlexibleImage nirImage = null;
		FlexibleImage irMask = null;
		FlexibleImage irImage = null;
		
		// OK
		// ImageOperation.showTwoImagesAsOne(getInput().getMasks().getVis().getBufferedImage(), getInput().getMasks().getFluo().getBufferedImage());
		
		switch (typ) {
			case TRANSLATION:
				fluoMask = automaticProcessIntervallSearch(input().masks().fluo(), input().masks().vis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_FLUO_X, t.x);
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_FLUO_Y, t.y);
				// System.out.println("translation des Originalbildes: " + t.x + " " + t.y);
				fluoImage = new ImageOperation(input().images().fluo()).translate(t.x, t.y).getImage();
				
				if (options.getBooleanSetting(Setting.PROCESS_NIR)) {
					nirMask = automaticProcessIntervallSearch(input().masks().nir(), input().masks().vis(), resultValues, typ);
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_NIR_X, t.x);
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_NIR_Y, t.y);
					t = (Vector2d) resultValues.getObject();
					nirImage = new ImageOperation(input().images().nir()).translate(t.x, t.y).getImage();
				} else {
					nirImage = input().images().nir();
					nirMask = input().masks().nir();
				}
				
				break;
			
			case ROTATION:
				fluoMask = automaticProcessIntervallSearch(input().masks().fluo(), input().masks().vis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.ROTATION_FLUO, t.x);
				fluoImage = new ImageOperation(input().images().fluo()).rotate(t.x).getImage();
				
				if (options.getBooleanSetting(Setting.PROCESS_NIR)) {
					nirMask = automaticProcessIntervallSearch(input().masks().nir(), input().masks().vis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.ROTATION_NIR, t.x);
					nirImage = new ImageOperation(input().images().nir()).rotate(t.x).getImage();
				} else {
					nirImage = input().images().nir();
					nirMask = input().masks().nir();
				}
				break;
			
			case SCALING:
				fluoMask = automaticProcessIntervallSearch(input().masks().fluo(), input().masks().vis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_FLUO_X, t.x);
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_FLUO_Y, t.y);
				fluoImage = new ImageOperation(input().images().fluo()).scale(t.x, t.y).getImage();
				
				if (options.getBooleanSetting(Setting.PROCESS_NIR)) {
					nirMask = automaticProcessIntervallSearch(input().masks().nir(), input().masks().vis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_NIR_X, t.x);
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_NIR_Y, t.y);
					nirImage = new ImageOperation(input().images().nir()).scale(t.x, t.y).getImage();
				} else {
					nirImage = input().images().nir();
					nirMask = input().masks().nir();
				}
				break;
		}
		
		FlexibleImageSet processedImages = new FlexibleImageSet(input().images().vis(), fluoImage, nirImage, irImage);
		FlexibleImageSet processedMasks = new FlexibleImageSet(input().masks().vis(), fluoMask, nirMask, irMask);
		
		return new FlexibleMaskAndImageSet(processedImages, processedMasks);
	}
	
	private FlexibleImage automaticProcessIntervallSearch(FlexibleImage workMask, FlexibleImage visMaskImage, ObjectRef resultValue,
			MorphologicalOperationSearchType typ) {
		
		double bestValueX = 0;
		double bestValueY = 0;
		
		// OK
		// ImageOperation.showTwoImagesAsOne(visMaskImage.getBufferedImage(), workMask.getBufferedImage());
		
		if (typ == MorphologicalOperationSearchType.ROTATION) {
			bestValueX = automaticIntervallSearchPartly(workMask, visMaskImage, typ);
		} else {
			// scan X direction
			bestValueX = automaticIntervallSearchPartly(workMask, visMaskImage, 0, true, typ);
			// bestValueX = 1.0;
			// System.out.println("bestValueX: " + bestValueX + " next Process is Y-Value!");
			// scan Y direction
			bestValueY = automaticIntervallSearchPartly(workMask, visMaskImage, bestValueX, false, typ);
			// bestValueY = 1.0;
			// System.out.println("bestValueY: " + bestValueY + " next Process is next Block!");
			// System.out.println("bestValueY: " + bestValueY);
		}
		
		return automaticSearchValueApplyToMaskAndReturn(workMask, resultValue, bestValueX, bestValueY, typ);
	}
	
	private FlexibleImage automaticSearchValueApplyToMaskAndReturn(FlexibleImage workMask, ObjectRef resultValue, double bestValueX,
			double bestValueY, MorphologicalOperationSearchType typ) {
		
		if (bestValueX != 0 || bestValueY != 0) {
			ImageOperation io = new ImageOperation(workMask);
			
			switch (typ) {
				case SCALING:
					io.scale(bestValueX, bestValueY);
					// System.out.println("Scale X = " + bestValueX + ", Y = " + bestValueY);
					break;
				
				case TRANSLATION:
					// bestValueX = Math.round(bestValueX);
					// bestValueY = Math.round(bestValueY);
					
					io.translate(bestValueX, bestValueY);
					// System.out.println("Translate X = " + bestValueX + ", Y = " + bestValueY);
					break;
				
				case ROTATION:
					io.rotate(bestValueX);
					// System.out.println("Rotate X = " + bestValueX);
					break;
				
				default:
					break;
			}
			
			// debugOverlayImages(io.getImage(), visImage, LayeringTyp.ROW_IMAGE);
			Vector2d t = new Vector2d(bestValueX, bestValueY);
			resultValue.setObject(t);
			
			return io.getImage();
		} else {
			switch (typ) {
				case SCALING:
					// System.out.println("No scaling.");
					break;
				
				case TRANSLATION:
					// System.out.println("No translation.");
					break;
				
				case ROTATION:
					// System.out.println("No rotation.");
					break;
				
				default:
					break;
			}
			
			Vector2d t = new Vector2d(0d, 0d);
			resultValue.setObject(t);
			return workMask;
		}
		
	}
	
	private double automaticIntervallSearchPartly(FlexibleImage workMask, FlexibleImage visMaskImage, MorphologicalOperationSearchType typ) {
		
		return automaticIntervallSearchPartly(workMask, visMaskImage, 0, true, typ);
	}
	
	private double automaticIntervallSearchPartly(FlexibleImage workMask, FlexibleImage visMaskImage, double bestOtherValue,
			boolean scanParameterX, MorphologicalOperationSearchType operationType) {
		
		double borderLeft = 0;
		double borderRight = 0;
		
		switch (operationType) {
			case TRANSLATION:
				borderLeft = -10;
				borderRight = 10;
				break;
			
			case SCALING:
				borderLeft = 0.99;
				borderRight = 1.05;
				break;
			
			case ROTATION:
				borderLeft = -1.5;
				borderRight = 1.5;
				break;
		}
		
		return recursiveParameterSearch(workMask, visMaskImage, borderLeft, borderRight, 10, 0, scanParameterX, bestOtherValue, operationType);
	}
	
	private double recursiveParameterSearch(
			final FlexibleImage workMask, final FlexibleImage visMaskImage,
			double borderLeft, double borderRight,
			int n,
			int zaehler,
			final boolean scanParameterX,
			final double bestValueOfOtherTranslation,
			final MorphologicalOperationSearchType operation) {
		
		double intervallSteps = Math.abs(borderLeft - borderRight) / n;
		// ArrayList<MyThread> threads = new ArrayList<MyThread>();
		
		final ThreadSafeOptions bestValueTS = new ThreadSafeOptions();
		bestValueTS.setDouble(-Double.MAX_VALUE);
		
		final ThreadSafeOptions bestParameterTS = new ThreadSafeOptions();
		bestParameterTS.setDouble(Double.NaN);
		
		// boolean threaded = false;
		
		// if (threaded) {
		// zaehler = searchMultiThreaded(workMask, visMaskImage, borderLeft, borderRight, zaehler, scanParameterX, bestValueOfOtherTranslation, operation,
		// intervallSteps, bestValueTS, bestParameterTS);
		// } else {
		zaehler = searchSingleThreaded(workMask, visMaskImage, borderLeft, borderRight, zaehler, scanParameterX, bestValueOfOtherTranslation, operation,
				intervallSteps, bestValueTS, bestParameterTS);
		// }
		
		double newBorderLeft = bestParameterTS.getDouble() - intervallSteps;
		double newBorderRight = bestParameterTS.getDouble() + intervallSteps;
		
		boolean stopping = false;
		// System.out.println("intervallsteps: " + intervallSteps);
		switch (operation) {
			case ROTATION:
				if (intervallSteps < 0.2) // 0.1
					stopping = true;
				break;
			case TRANSLATION:
				if (intervallSteps < 2) // 1
					stopping = true;
				break;
			case SCALING:
				if (intervallSteps < 0.1) // 0.1
					stopping = true;
				break;
		}
		
		if (stopping) {
			// System.out.println("Calculation steps: " + zaehler);
			return bestParameterTS.getDouble();
		} else
			return recursiveParameterSearch(workMask, visMaskImage, newBorderLeft, newBorderRight,
					n, zaehler, scanParameterX, bestValueOfOtherTranslation, operation);
	}
	
	private int searchMultiThreaded(final FlexibleImage workMask,
			final FlexibleImage visMaskImage, double borderLeft, double borderRight, int zaehler,
			final boolean scanParameterX, final double bestValueOfOtherTranslation,
			final MorphologicalOperationSearchType operation, double intervallSteps,
			final ThreadSafeOptions bestValueTS, final ThreadSafeOptions bestParameterTS,
			int parentPriority) throws InterruptedException {
		ArrayList<MyThread> tl = new ArrayList<MyThread>();
		for (double step = borderLeft; step <= borderRight; step += intervallSteps) {// step = Math.round((intervallSteps + step) * accuracy) / accuracy) {
			zaehler++;
			final double fstep = step;
			tl.add(
					BackgroundThreadDispatcher.addTask(new Runnable() {
						@Override
						public void run() {
							innerLoop(workMask, visMaskImage, scanParameterX, bestValueOfOtherTranslation, operation, bestValueTS, bestParameterTS, fstep, null);
						}
					}, "Inner loop " + operation, 1, parentPriority, false));
		}
		
		BackgroundThreadDispatcher.waitFor(tl);
		
		return zaehler;
	}
	
	private int searchSingleThreaded(final FlexibleImage workMask, final FlexibleImage visMaskImage, double borderLeft, double borderRight, int zaehler,
			final boolean scanParameterX, final double bestValueOfOtherTranslation, final MorphologicalOperationSearchType operation, double intervallSteps,
			final ThreadSafeOptions bestValueTS, final ThreadSafeOptions bestParameterTS) {
		for (double step = borderLeft; step <= borderRight; step += intervallSteps) {// step = Math.round((intervallSteps + step) * accuracy) / accuracy) {
			zaehler++;
			final double fstep = step;
			innerLoop(workMask, visMaskImage, scanParameterX, bestValueOfOtherTranslation, operation, bestValueTS, bestParameterTS, fstep,
					new MaskOperationDirect(options.getBackground(), Color.GRAY.getRGB()));
		}
		
		return zaehler;
	}
	
	private void innerLoop(final FlexibleImage workMask, final FlexibleImage visMaskImage, final boolean scanParameterX,
			final double bestValueOfOtherTranslation, final MorphologicalOperationSearchType operation, final ThreadSafeOptions bestValueTS,
			final ThreadSafeOptions bestParameterTS, double step, MaskOperationDirect optUseSingleMaskOperationObject) {
		double value = -1;
		double noOperationValue = Double.NaN;
		switch (operation) {
			case TRANSLATION:
			case ROTATION:
				noOperationValue = 0;
				break;
			case SCALING:
				noOperationValue = 1;
				break;
		}
		if (scanParameterX)
			value = getMatchResultValue(workMask, visMaskImage, step, noOperationValue, operation, optUseSingleMaskOperationObject);
		else
			value = getMatchResultValue(workMask, visMaskImage, bestValueOfOtherTranslation, step, operation, optUseSingleMaskOperationObject);
		// System.out.println("" + operation + " mit dem Wert: " + step + " mit dem value: " + value);
		
		synchronized (bestParameterTS) {
			if (value > bestValueTS.getDouble()) {
				bestValueTS.setDouble(value);
				bestParameterTS.setDouble(step);
			} else
				if (value == bestValueTS.getDouble()) {
					switch (operation) {
						case ROTATION:
						case TRANSLATION:
							if (Math.abs(step) < Math.abs(bestParameterTS.getDouble())) {
								bestParameterTS.setDouble(step);
								// System.out.println("Wert der näher am Ausgangspunkt liegt wird genommen! " + stepF);
							}
							break;
						case SCALING:
							if (Math.abs(step - 1) < Math.abs(bestParameterTS.getDouble() - 1)) {
								bestParameterTS.setDouble(step);
								// System.out.println("Wert der näher am Ausgangspunkt liegt wird genommen!" + stepF);
							}
						default:
							break;
					}
				}
		}
	}
	
	//
	
	private double getMatchResultValue(FlexibleImage workMask, FlexibleImage visMaskImage, double valueX, double valueY,
			MorphologicalOperationSearchType typ, MaskOperationDirect optUseSingleMaskOperationObject) {
		if (workMask == null)
			return Double.NaN;
		ImageOperation io = new ImageOperation(workMask);
		// FlexibleImage changedMask = null;
		switch (typ) {
			case TRANSLATION:
				io.translate(valueX, valueY);
				break;
			
			case SCALING:
				io.scale(valueX, valueY);
				break;
			
			case ROTATION:
				io.rotate(valueX);
				break;
			
			default:
				break;
		}
		
		double correctionForDeletedArea = 1;
		
		if (optUseSingleMaskOperationObject == null) {
			// changedMask = io.getImage();
			// ImageOperation.showTwoImagesAsOne(visMaskImage.getBufferedImage(), io.getImageAsBufferedImage());
			MaskOperation o = new MaskOperation(visMaskImage, io.getImage(), null,
					options.getBackground(), Color.GRAY.getRGB());
			o.mergeMasks();
			// if (typ == MorphologicalOperationSearchType.SCALING) {
			// correctionForDeletedArea = 1 / valueX;
			// correctionForDeletedArea = correctionForDeletedArea / valueY;
			// }
			// ImageOperation.showTwoImagesAsOne(workMask.getBufferedImage(), o.getMaskAsBufferedImage());
			
			return o.getUnknownMeasurementValuePixels(correctionForDeletedArea);
		} else {
			optUseSingleMaskOperationObject.mergeMasks(visMaskImage.getAs1A(), io.getImageAs1dArray(), null,
					visMaskImage.getWidth(),
					visMaskImage.getHeight());
			return optUseSingleMaskOperationObject.getUnknownMeasurementValuePixels(correctionForDeletedArea);
		}
	}
}
