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
	
	private MorphologicalOperationSearchType typeOfSearch;
	
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
	
	private FlexibleImage automaticProcessIntervallSearch(FlexibleImage workMask, FlexibleImage visImage, ObjectRef resultValue,
			MorphologicalOperationSearchType typ) {
		
		double bestValueX = 0;
		double bestValueY = 0;
		
		if (typ == MorphologicalOperationSearchType.ROTATION) {
			bestValueX = automaticIntervallSearchPartly(workMask, visImage, typ);
		} else {
			// scan X direction
			bestValueX = automaticIntervallSearchPartly(workMask, visImage, 0, true, typ);
			// scan Y direction
			bestValueY = automaticIntervallSearchPartly(workMask, visImage, bestValueX, false, typ);
			
		}
		
		return automaticSearchValueApplyToMaskAndReturn(workMask, visImage, resultValue, bestValueX, bestValueY, typ);
	}
	
	private FlexibleImage automaticSearchValueApplyToMaskAndReturn(FlexibleImage workMask, FlexibleImage visImage, ObjectRef resultValue, double bestValueX,
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
	
	private double automaticIntervallSearchPartly(FlexibleImage workMask, FlexibleImage visImage, MorphologicalOperationSearchType typ) {
		
		return automaticIntervallSearchPartly(workMask, visImage, 0, true, typ);
	}
	
	private double automaticIntervallSearchPartly(FlexibleImage workMask, FlexibleImage visImage, double bestOtherValue,
			boolean scanParameterX, MorphologicalOperationSearchType operationType) {
		
		double borderLeft = 0;
		double borderRight = 0;
		double intervallTeiler = 1.0;
		
		switch (operationType) {
			case TRANSLATION:
				borderLeft = -20;
				borderRight = 20;
				intervallTeiler = 0.2;
				break;
			
			case SCALING:
				borderLeft = 0.8;
				borderRight = 1.2;
				intervallTeiler = 0.1;
				break;
			
			case ROTATION:
				borderLeft = -5.0;
				borderRight = 5.0;
				intervallTeiler = 0.1;
				break;
		}
		
		double intervallLength = Math.abs(borderLeft - borderRight);
		double intervallSteps = intervallLength * intervallTeiler;
		
		return recursiveParameterSearch(workMask, visImage, borderLeft, borderRight, 0.0, -100000000000.0, intervallTeiler, intervallSteps, 0,
				bestOtherValue, operationType, scanParameterX);
	}
	
	private double recursiveParameterSearch(final FlexibleImage workMask, final FlexibleImage visImage, double borderLeft, double borderRight,
			double bestTranslation,
			double bestValue, double intervallTeiler, double intervallSteps, int zaehler, final double bestValueOfOtherTranslation,
			final MorphologicalOperationSearchType operation, final boolean scanParameterX) {
		
		ArrayList<MyThread> threads = new ArrayList<MyThread>();
		
		final ThreadSafeOptions bestValueTS = new ThreadSafeOptions();
		bestValueTS.setDouble(bestValue);
		
		final ThreadSafeOptions bestTranslationTS = new ThreadSafeOptions();
		bestTranslationTS.setDouble(bestTranslation);
		
		for (double translation = borderLeft; translation <= borderRight; translation += intervallSteps) {
			if (translation == 0.0)
				break;
			
			zaehler++;
			final double translationF = translation;
			threads.add(BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					double value = -1;
					if (scanParameterX)
						value = getMatchResultValue(workMask, visImage, translationF, 0, operation);
					else
						value = getMatchResultValue(workMask, visImage, bestValueOfOtherTranslation, translationF, operation);
					synchronized (bestValueTS) {
						if (value > bestValueTS.getDouble()) {
							bestValueTS.setDouble(value);
							bestTranslationTS.setDouble(translationF);
						}
					}
				}
			}, "parameter search (step " + zaehler + ")", 0));
		}
		BackgroundThreadDispatcher.waitFor(threads);
		double newBorderLeft = bestTranslationTS.getDouble() - intervallSteps;
		double newBorderRight = bestTranslationTS.getDouble() + intervallSteps;
		double newIntervallTeiler = intervallTeiler * 2;
		double newIntervallLength = Math.abs(newBorderLeft - newBorderRight);
		double newIntervallSteps = newIntervallLength * newIntervallTeiler;
		
		if (newIntervallSteps < 1.0 || newIntervallTeiler > 1.0) {
			System.out.println("Calculation steps: " + zaehler);
			return bestTranslationTS.getDouble();
		} else
			return recursiveParameterSearch(workMask, visImage, newBorderLeft + newIntervallSteps, newBorderRight - newIntervallSteps,
					bestTranslationTS.getDouble(),
					bestValueTS.getDouble(),
					newIntervallTeiler, newIntervallSteps, zaehler, bestValueOfOtherTranslation, operation, scanParameterX);
		
	}
	
	private double getMatchResultValue(FlexibleImage workMask, FlexibleImage visImage, double valueX, double valueY, MorphologicalOperationSearchType typ) {
		ImageOperation io = new ImageOperation(workMask);
		FlexibleImage changedMask = null;
		
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
		
		changedMask = io.getImage();
		
		MaskOperation o = new MaskOperation(visImage, changedMask, null,
							options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		return o.getUnknownMeasurementValuePixels();
	}
}
