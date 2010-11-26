/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image_utils;

import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * @author klukas
 * 
 */
public class PhytoTopImageProcessorOptions {
	private double rotationAngle;
	private double scaleX;
	private double scaleY;
	private int translateX;
	private int translateY;

	private double fluoEpsilonA;
	private double fluoEpsilonB;
	private double rgbEpsilonB;
	private double rgbEpsilonA;
	private double nearEpsilonA;
	private double nearEpsilonB;

	private int rgbNumberOfErodeLoops;
	private int rgbNumberOfDilateLoops;
	private int fluoNumberOfErodeLoops;
	private int fluoNumberOfDilateLoops;
	private int nearNumberOfErodeLoops;
	private int nearNumberOfDilateLoops;

	private int background;

	void initStandardValues() {
		setRotationAngle(-3);

		setScaleX(0.95);
		setScaleY(0.87);

		setTranslateX(0);
		setTranslateY(-15);

		setRgbEpsilonA(2.5);
		setRgbEpsilonB(2.5);

		setFluoEpsilonA(0.5);
		setFluoEpsilonB(0.5);

		setNearEpsilonA(0.5);
		setNearEpsilonB(1.0);

		setRgbNumberOfErodeLoops(2);
		setRgbNumberOfDilateLoops(5);
		setFluoNumberOfErodeLoops(4);
		setFluoNumberOfDilateLoops(20);
		setNearNumberOfErodeLoops(0);
		setNearNumberOfDilateLoops(0);

		setBackground(PhenotypeAnalysisTask.BACKGROUND_COLOR.getRGB());
	}

	// ########## SET ###############

	public void setRotationAngle(int rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public void setScaleXY(double scaleX, double scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	public void setTranslateX(int translateX) {
		this.translateX = translateX;
	}

	public void setTranslateY(int translateY) {
		this.translateY = translateY;
	}

	public void setTranslateXY(int translateX, int translateY) {
		this.translateX = translateX;
		this.translateY = translateY;
	}

	// SET Epsilon

	public void setFluoEpsilonA(double fluoEpsilonA) {
		this.fluoEpsilonA = fluoEpsilonA;
	}

	public void setFluoEpsilonB(double fluoEpsilonB) {
		this.fluoEpsilonB = fluoEpsilonB;
	}

	public void setFluoEpsilonAB(double fluoEpsilonA, double fluoEpsilonB) {
		this.fluoEpsilonA = fluoEpsilonA;
		this.fluoEpsilonB = fluoEpsilonB;
	}

	public void setRgbEpsilonB(double rgbEpsilonB) {
		this.rgbEpsilonB = rgbEpsilonB;
	}

	public void setRgbEpsilonA(double rgbEpsilonA) {
		this.rgbEpsilonA = rgbEpsilonA;
	}

	public void setRgbEpsilonAB(double rgbEpsilonA, double rgbEpsilonB) {
		this.rgbEpsilonA = rgbEpsilonA;
		this.rgbEpsilonB = rgbEpsilonB;
	}

	public void setNearEpsilonA(double nearEpsilonA) {
		this.nearEpsilonA = nearEpsilonA;
	}

	public void setNearEpsilonB(double nearEpsilonB) {
		this.nearEpsilonB = nearEpsilonB;
	}

	public void setNearEpsilonAB(double nearEpsilonA, double nearEpsilonB) {
		this.nearEpsilonA = nearEpsilonA;
		this.nearEpsilonB = nearEpsilonB;
	}

	public void setRgbNumberOfErodeLoops(int rgbNumberOfErodeLoops) {
		this.rgbNumberOfErodeLoops = rgbNumberOfErodeLoops;
	}

	public void setRgbNumberOfDilateLoops(int rgbNumberOfDilateLoops) {
		this.rgbNumberOfDilateLoops = rgbNumberOfDilateLoops;
	}

	public void setFluoNumberOfErodeLoops(int fluoNumberOfErodeLoops) {
		this.fluoNumberOfErodeLoops = fluoNumberOfErodeLoops;
	}

	public void setFluoNumberOfDilateLoops(int fluoNumberOfDilateLoops) {
		this.fluoNumberOfDilateLoops = fluoNumberOfDilateLoops;
	}

	public void setNearNumberOfErodeLoops(int nearNumberOfErodeLoops) {
		this.nearNumberOfErodeLoops = nearNumberOfErodeLoops;
	}

	public void setNearNumberOfDilateLoops(int nearNumberOfDilateLoops) {
		this.nearNumberOfDilateLoops = nearNumberOfDilateLoops;
	}

	public void setBackground(int background) {
		this.background = background;
	}

	// ########## GET #############

	public double getRotationAngle() {
		return rotationAngle;
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public int getTranslateX() {
		return translateX;
	}

	public int getTranslateY() {
		return translateY;
	}

	// GET Epsilon

	public double getRgbEpsilonA() {
		return rgbEpsilonA;
	}

	public double getRgbEpsilonB() {
		return rgbEpsilonB;
	}

	public double getFluoEpsilonB() {
		return fluoEpsilonB;
	}

	public double getFluoEpsilonA() {
		return fluoEpsilonA;
	}

	public double getNearEpsilonB() {
		return nearEpsilonB;
	}

	public double getNearEpsilonA() {
		return nearEpsilonA;
	}

	// GET Number of Erode und Dilate loops

	public int getNearNumberOfDilateLoops() {
		return nearNumberOfDilateLoops;
	}

	public int getNearNumberOfErodeLoops() {
		return nearNumberOfErodeLoops;
	}

	public int getFluoNumberOfDilateLoops() {
		return fluoNumberOfDilateLoops;
	}

	public int getFluoNumberOfErodeLoops() {
		return fluoNumberOfErodeLoops;
	}

	public int getRgbNumberOfDilateLoops() {
		return rgbNumberOfDilateLoops;
	}

	public int getRgbNumberOfErodeLoops() {
		return rgbNumberOfErodeLoops;
	}

	public int getBackground() {
		return background;
	}
}
