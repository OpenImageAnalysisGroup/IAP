package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;
import java.util.ArrayList;

import org.ObjectRef;
import org.Vector2d;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MaskOperation;
import de.ipk.ag_ba.image.operations.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public abstract class BlockAutomaticParameterSearch extends AbstractImageAnalysisBlockFIS {
	
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
		Vector2d t;
		FlexibleImage fluoMask = null;
		FlexibleImage fluoImage = null;
		FlexibleImage nirMask = null;
		FlexibleImage nirImage = null;
		
		switch (typ) {
			case TRANSLATION:
				fluoMask = automaticProcessIntervallSearch(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				fluoImage = new ImageOperation(getInput().getImages().getFluo()).translate(t.x, t.y).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(getInput().getMasks().getNir(), getInput().getMasks().getVis(), resultValues, typ);
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
				fluoImage = new ImageOperation(getInput().getImages().getFluo()).rotate(t.x).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(getInput().getMasks().getNir(), getInput().getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					nirImage = new ImageOperation(getInput().getImages().getNir()).rotate(t.x).getImage();
				} else {
					nirImage = getInput().getImages().getNir();
					nirMask = getInput().getMasks().getNir();
				}
				break;
			
			case SCALING:
				fluoMask = automaticProcessIntervallSearch(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				fluoImage = new ImageOperation(getInput().getImages().getFluo()).scale(t.x, t.y).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(getInput().getMasks().getNir(), getInput().getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
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
		
		if (typ == MorphologicalOperationSearchType.ROTATION) {
			bestValueX = automaticIntervallSearchPartly(workMask, visMaskImage, typ);
		} else {
			// scan X direction
			bestValueX = automaticIntervallSearchPartly(workMask, visMaskImage, 0, true, typ);
			// scan Y direction
			bestValueY = automaticIntervallSearchPartly(workMask, visMaskImage, bestValueX, false, typ);
			
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
					System.out.println("No scaling.");
					break;
				
				case TRANSLATION:
					System.out.println("No translation.");
					break;
				
				case ROTATION:
					System.out.println("No rotation.");
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
				borderLeft = -50;
				borderRight = 50;
				break;
			
			case SCALING:
				borderLeft = 0.8;
				borderRight = 1.2;
				break;
			
			case ROTATION:
				borderLeft = -5.0;
				borderRight = 5.0;
				break;
		}
		
		return recursiveParameterSearch(workMask, visMaskImage, borderLeft, borderRight, 5, 0, scanParameterX, bestOtherValue, operationType);
	}
	
	private double recursiveParameterSearch(
			final FlexibleImage workMask, final FlexibleImage visMaskImage,
			double borderLeft, double borderRight,
			int n,
			int zaehler,
			final boolean scanParameterX,
			final double bestValueOfOtherTranslation,
			final MorphologicalOperationSearchType operation
			) {
		
		double intervallSteps = Math.abs(borderLeft - borderRight) / n;
		ArrayList<MyThread> threads = new ArrayList<MyThread>();
		
		final ThreadSafeOptions bestValueTS = new ThreadSafeOptions();
		bestValueTS.setDouble(-Double.MAX_VALUE);
		
		final ThreadSafeOptions bestParameterTS = new ThreadSafeOptions();
		bestParameterTS.setDouble(Double.NaN);
		
		for (double step = borderLeft; step <= borderRight; step += intervallSteps) {
			zaehler++;
			final double stepF = step;
			threads.add(BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					double value = -1;
					double noOperationValue = Double.NaN;
					switch (operation) {
						case TRANSLATION:
						case ROTATION:
							noOperationValue = 0;
						case SCALING:
							noOperationValue = 1;
					}
					if (scanParameterX)
						value = getMatchResultValue(workMask, visMaskImage, stepF, noOperationValue, operation);
					else
						value = getMatchResultValue(workMask, visMaskImage, bestValueOfOtherTranslation, stepF, operation);
					// System.out.println("" + operation + " mit dem Wert: " + stepF + " mit dem value: " + value);
					synchronized (bestValueTS) {
						if (value > bestValueTS.getDouble()) {
							bestValueTS.setDouble(value);
							bestParameterTS.setDouble(stepF);
						} else
							if (value == bestValueTS.getDouble()) {
								
								switch (operation) {
									case ROTATION:
									case TRANSLATION:
									if (Math.abs(stepF) < Math.abs(bestParameterTS.getDouble())) {
										bestParameterTS.setDouble(stepF);
										// System.out.println("Wert der näher am Ausgangspunkt liegt wird genommen! " + stepF);
									}
									break;
								case SCALING:
									if (Math.abs(stepF - 1) < Math.abs(bestParameterTS.getDouble() - 1)) {
										bestParameterTS.setDouble(stepF);
										// System.out.println("Wert der näher am Ausgangspunkt liegt wird genommen!" + stepF);
									}
								default:
									break;
							}
							
						}
					
					// if (value > bestValueTS.getDouble()) {
					// bestValueTS.setDouble(value);
					// bestTranslationTS.setDouble(translationF);
					// }
				}
			}
			}, "parameter search (step " + zaehler + ")", 0));
		}
		
		BackgroundThreadDispatcher.waitFor(threads);
		
		double newBorderLeft = bestParameterTS.getDouble() - intervallSteps;
		double newBorderRight = bestParameterTS.getDouble() + intervallSteps;
		
		boolean stopping = false;
		
		switch (operation) {
			case ROTATION:
				if (intervallSteps < 0.1)
					stopping = true;
				break;
			case TRANSLATION:
				if (intervallSteps < 1)
					stopping = true;
				break;
			case SCALING:
				if (intervallSteps < 0.1)
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
	
	// //////AAAAAAAAAAAAAAAAAAAAAAAAA
	
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
		
		MaskOperation o = new MaskOperation(visMaskImage, io.getImage(), null,
							options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		double correctionForDeletedArea = 1;
		if (typ == MorphologicalOperationSearchType.SCALING) {
			correctionForDeletedArea = 1 / valueX;
			correctionForDeletedArea = correctionForDeletedArea / valueY;
		}
		return o.getUnknownMeasurementValuePixels(correctionForDeletedArea);
	}
}
