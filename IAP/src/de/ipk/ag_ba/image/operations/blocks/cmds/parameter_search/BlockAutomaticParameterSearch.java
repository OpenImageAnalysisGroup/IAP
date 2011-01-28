package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import java.awt.Color;

import org.ObjectRef;
import org.Vector2d;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MaskOperation;
import de.ipk.ag_ba.image.operations.MorphologicalOperationSearchType;
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
	
	public BlockAutomaticParameterSearch(MorphologicalOperationSearchType typeOfSearch) {
		this.typeOfSearch = typeOfSearch;
	}
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		return automaticSearch(typeOfSearch);
	}
	
	private FlexibleMaskAndImageSet automaticSearch(MorphologicalOperationSearchType typ) {
		
		ObjectRef resultValues = new ObjectRef();
		Vector2d t = null;
		FlexibleImage fluoMask = null;
		FlexibleImage fluoImage = null;
		FlexibleImage nirMask = null;
		FlexibleImage nirImage = null;
		
		// OK
		// ImageOperation.showTwoImagesAsOne(getInput().getMasks().getVis().getBufferedImage(), getInput().getMasks().getFluo().getBufferedImage());
		
		switch (typ) {
			case TRANSLATION:
				fluoMask = automaticProcessIntervallSearch(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_FLUO_X, t.x);
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_FLUO_Y, t.y);
				// System.out.println("translation des Originalbildes: " + t.x + " " + t.y);
				fluoImage = new ImageOperation(getInput().getImages().getFluo()).translate(t.x, t.y).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(getInput().getMasks().getNir(), getInput().getMasks().getVis(), resultValues, typ);
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_NIR_X, t.x);
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.TRANSLATION_NIR_Y, t.y);
					t = (Vector2d) resultValues.getObject();
					nirImage = new ImageOperation(getInput().getImages().getNir()).translate(t.x, t.y).getImage();
				} else {
					nirImage = getInput().getImages().getNir();
					nirMask = getInput().getMasks().getNir();
				}
				
				break;
			
			case ROTATION:
				fluoMask = automaticProcessIntervallSearch(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.ROTATION_FLUO, t.x);
				fluoImage = new ImageOperation(getInput().getImages().getFluo()).rotate(t.x).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(getInput().getMasks().getNir(), getInput().getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.ROTATION_NIR, t.x);
					nirImage = new ImageOperation(getInput().getImages().getNir()).rotate(t.x).getImage();
				} else {
					nirImage = getInput().getImages().getNir();
					nirMask = getInput().getMasks().getNir();
				}
				break;
			
			case SCALING:
				fluoMask = automaticProcessIntervallSearch(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_FLUO_X, t.x);
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_FLUO_Y, t.y);
				fluoImage = new ImageOperation(getInput().getImages().getFluo()).scale(t.x, t.y).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(getInput().getMasks().getNir(), getInput().getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_NIR_X, t.x);
					getProperties().setNumericProperty(getBlockPosition(), PropertyNames.SCALING_NIR_Y, t.y);
					nirImage = new ImageOperation(getInput().getImages().getNir()).scale(t.x, t.y).getImage();
				} else {
					nirImage = getInput().getImages().getNir();
					nirMask = getInput().getMasks().getNir();
				}
				break;
		}
		
		FlexibleImageSet processedImages = new FlexibleImageSet(getInput().getImages().getVis(), fluoImage, nirImage);
		FlexibleImageSet processedMasks = new FlexibleImageSet(getInput().getMasks().getVis(), fluoMask, nirMask);
		
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
			System.out.println("bestValueX: " + bestValueX + " next Process is Y-Value!");
			// scan Y direction
			bestValueY = automaticIntervallSearchPartly(workMask, visMaskImage, bestValueX, false, typ);
			// bestValueY = 1.0;
			System.out.println("bestValueY: " + bestValueY + " next Process is next Block!");
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
					System.out.println("Scale X = " + bestValueX + ", Y = " + bestValueY);
					break;
				
				case TRANSLATION:
					// bestValueX = Math.round(bestValueX);
					// bestValueY = Math.round(bestValueY);
					
					io.translate(bestValueX, bestValueY);
					System.out.println("Translate X = " + bestValueX + ", Y = " + bestValueY);
					break;
				
				case ROTATION:
					io.rotate(bestValueX);
					System.out.println("Rotate X = " + bestValueX);
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
		double accuracy = 0;
		
		switch (operationType) {
			case TRANSLATION:
				borderLeft = -20;
				borderRight = 20;
				accuracy = 0;
				break;
			
			case SCALING:
				borderLeft = 0.85;
				borderRight = 1.15;
				accuracy = 2;
				break;
			
			case ROTATION:
				borderLeft = -3.0;
				borderRight = 3.0;
				accuracy = 2;
				break;
		}
		accuracy = Math.pow(10, accuracy);
		return recursiveParameterSearch(workMask, visMaskImage, borderLeft, borderRight, 10, 0, scanParameterX, bestOtherValue, operationType, accuracy);
	}
	
	private double recursiveParameterSearch(
			final FlexibleImage workMask, final FlexibleImage visMaskImage,
			double borderLeft, double borderRight,
			int n,
			int zaehler,
			final boolean scanParameterX,
			final double bestValueOfOtherTranslation,
			final MorphologicalOperationSearchType operation,
			double accuracy
			) {
		
		double intervallSteps = Math.abs(borderLeft - borderRight) / n;
		// ArrayList<MyThread> threads = new ArrayList<MyThread>();
		
		final ThreadSafeOptions bestValueTS = new ThreadSafeOptions();
		bestValueTS.setDouble(-Double.MAX_VALUE);
		
		final ThreadSafeOptions bestParameterTS = new ThreadSafeOptions();
		bestParameterTS.setDouble(Double.NaN);
		
		for (double step = borderLeft; step <= borderRight; step = Math.round((intervallSteps + step) * accuracy) / accuracy) {
			zaehler++;
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
				value = getMatchResultValue(workMask, visMaskImage, step, noOperationValue, operation);
			else
				value = getMatchResultValue(workMask, visMaskImage, bestValueOfOtherTranslation, step, operation);
			// System.out.println("" + operation + " mit dem Wert: " + step + " mit dem value: " + value);
			
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
		
		// for (double step = borderLeft; step <= borderRight; step += intervallSteps) {
		// zaehler++;
		// final double stepF = step;
		// threads.add(BackgroundThreadDispatcher.addTask(new Runnable() {
		// @Override
		// public void run() {
		// double value = -1;
		// double noOperationValue = Double.NaN;
		// switch (operation) {
		// case TRANSLATION:
		// case ROTATION:
		// noOperationValue = 0;
		// case SCALING:
		// noOperationValue = 1;
		// }
		// if (scanParameterX)
		// value = getMatchResultValue(workMask, visMaskImage, stepF, noOperationValue, operation);
		// else
		// value = getMatchResultValue(workMask, visMaskImage, bestValueOfOtherTranslation, stepF, operation);
		// System.out.println("" + operation + " mit dem Wert: " + stepF + " mit dem value: " + value);
		//
		// synchronized (bestValueTS) {
		// if (value > bestValueTS.getDouble()) {
		// bestValueTS.setDouble(value);
		// bestParameterTS.setDouble(stepF);
		// } else
		// if (value == bestValueTS.getDouble()) {
		//
		// switch (operation) {
		// case ROTATION:
		// case TRANSLATION:
		// if (Math.abs(stepF) < Math.abs(bestParameterTS.getDouble())) {
		// bestParameterTS.setDouble(stepF);
		// // System.out.println("Wert der näher am Ausgangspunkt liegt wird genommen! " + stepF);
		// }
		// break;
		// case SCALING:
		// if (Math.abs(stepF - 1) < Math.abs(bestParameterTS.getDouble() - 1)) {
		// bestParameterTS.setDouble(stepF);
		// // System.out.println("Wert der näher am Ausgangspunkt liegt wird genommen!" + stepF);
		// }
		// default:
		// break;
		// }
		//
		// }
		//
		// // if (value > bestValueTS.getDouble()) {
		// // bestValueTS.setDouble(value);
		// // bestTranslationTS.setDouble(translationF);
		// // }
		// }
		// }
		// }, "parameter search (step " + zaehler + ")", 0));
		// }
		//
		// BackgroundThreadDispatcher.waitFor(threads);
		
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
				if (intervallSteps < 0.05) // 0.1
					stopping = true;
				break;
		}
		
		if (stopping) {
			// System.out.println("Calculation steps: " + zaehler);
			return bestParameterTS.getDouble();
		} else
			return recursiveParameterSearch(workMask, visMaskImage, newBorderLeft, newBorderRight,
					n, zaehler, scanParameterX, bestValueOfOtherTranslation, operation, accuracy);
	}
	
	//
	
	private double getMatchResultValue(FlexibleImage workMask, FlexibleImage visMaskImage, double valueX, double valueY, MorphologicalOperationSearchType typ) {
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
		
		// changedMask = io.getImage();
		// ImageOperation.showTwoImagesAsOne(visMaskImage.getBufferedImage(), io.getImageAsBufferedImage());
		MaskOperation o = new MaskOperation(visMaskImage, io.getImage(), null,
							options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		double correctionForDeletedArea = 1;
		// if (typ == MorphologicalOperationSearchType.SCALING) {
		// correctionForDeletedArea = 1 / valueX;
		// correctionForDeletedArea = correctionForDeletedArea / valueY;
		// }
		// ImageOperation.showTwoImagesAsOne(workMask.getBufferedImage(), o.getMaskAsBufferedImage());
		
		return o.getUnknownMeasurementValuePixels(correctionForDeletedArea);
	}
}
