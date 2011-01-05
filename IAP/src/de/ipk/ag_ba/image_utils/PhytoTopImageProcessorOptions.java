/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image_utils;

import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * @author klukas
 */
public class PhytoTopImageProcessorOptions {
	private double scale;
	
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
	private int dilateRgbTop;
	private int erodeRgbTop;
	private int dilateFluoTop;
	private int erodeFluoTop;
	private int closingNirTop;
	
	private int postProcessDilateRgbTop;
	private int postProcessErodeRgbTop;
	private int postProcessDilateFluoTop;
	private int postProcessErodeFluoTop;
	private int postProcessDilateNirTop;
	private int postProcessErodeNirTop;
	
	private boolean processNir;
	
	private boolean debugTakeTimes;
	
	private boolean debugOverlayResultImage;
	
	public PhytoTopImageProcessorOptions() {
		this(1.0);
	}
	
	public PhytoTopImageProcessorOptions(double scale) {
		initStandardValues(scale);
	}
	
	void initStandardValues(double scale) {
		
		setScale(scale);
		
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
		
		setRgbNumberOfErodeLoops((int) Math.ceil(2 * scale));
		setRgbNumberOfDilateLoops((int) Math.ceil(5 * scale));
		setFluoNumberOfErodeLoops((int) Math.ceil(2 * scale));
		setFluoNumberOfDilateLoops((int) Math.ceil(5 * scale));
		setNearNumberOfErodeLoops((int) Math.ceil(1 * scale));
		setNearNumberOfDilateLoops((int) Math.ceil(2 * scale));
		
		setDilateRgbTop((int) Math.ceil(15 * scale));
		setErodeRgbTop((int) Math.ceil(10 * scale));
		setDilateFluoTop((int) Math.ceil(2 * scale));
		setErodeFluoTop((int) Math.ceil(1 * scale));
		
		setClosingNirTop((int) Math.ceil(1 * scale));
		
		setPostProcessDilateRgbTop((int) Math.ceil(1 * scale));
		setPostProcessErodeRgbTop((int) Math.ceil(1 * scale));
		setPostProcessDilateFluoTop((int) Math.ceil(1 * scale));
		setPostProcessErodeFluoTop((int) Math.ceil(1 * scale));
		setPostProcessDilateNirTop((int) Math.ceil(1 * scale));
		setPostProcessErodeNirTop((int) Math.ceil(1 * scale));
		
		setProcessNir(false);
		
		setDebugTakeTimes(false);
		setDebugOverlayResult(false);
	}
	
	// ########## SET ###############
	
	private void setScale(double scale) {
		this.scale = scale;
		
	}
	
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
	
	// ########## GET #############
	
	public double getScale() {
		return scale;
	}
	
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
		return PhenotypeAnalysisTask.BACKGROUND_COLORint;
	}
	
	public double getRotationAngleNir() {
		return 0;
	}
	
	public void setDilateRgbTop(int dilateRgbTop) {
		this.dilateRgbTop = dilateRgbTop;
	}
	
	public int getDilateRgbTop() {
		return dilateRgbTop;
	}
	
	public void setErodeRgbTop(int erodeRgbTop) {
		this.erodeRgbTop = erodeRgbTop;
	}
	
	public int getErodeRgbTop() {
		return erodeRgbTop;
	}
	
	public void setDilateFluoTop(int dilateFluoTop) {
		this.dilateFluoTop = dilateFluoTop;
	}
	
	public int getDilateFluoTop() {
		return dilateFluoTop;
	}
	
	public void setErodeFluoTop(int erodeFluoTop) {
		this.erodeFluoTop = erodeFluoTop;
	}
	
	public int getErodeFluoTop() {
		return erodeFluoTop;
	}
	
	public void setClosingNirTop(int closingNirTop) {
		this.closingNirTop = closingNirTop;
	}
	
	public int getClosingNirTop() {
		return closingNirTop;
	}
	
	public void setPostProcessDilateRgbTop(int postProcessDilateRgbTop) {
		this.postProcessDilateRgbTop = postProcessDilateRgbTop;
	}
	
	public int getPostProcessDilateRgbTop() {
		return postProcessDilateRgbTop;
	}
	
	public void setPostProcessErodeRgbTop(int postProcessErodeRgbTop) {
		this.postProcessErodeRgbTop = postProcessErodeRgbTop;
	}
	
	public int getPostProcessErodeRgbTop() {
		return postProcessErodeRgbTop;
	}
	
	public void setPostProcessDilateFluoTop(int postProcessDilateFluoTop) {
		this.postProcessDilateFluoTop = postProcessDilateFluoTop;
	}
	
	public int getPostProcessDilateFluoTop() {
		return postProcessDilateFluoTop;
	}
	
	public void setPostProcessErodeFluoTop(int postProcessErodeFluoTop) {
		this.postProcessErodeFluoTop = postProcessErodeFluoTop;
	}
	
	public int getPostProcessErodeFluoTop() {
		return postProcessErodeFluoTop;
	}
	
	public void setPostProcessDilateNirTop(int postProcessDilateNirTop) {
		this.postProcessDilateNirTop = postProcessDilateNirTop;
	}
	
	public int getPostProcessDilateNirTop() {
		return postProcessDilateNirTop;
	}
	
	public void setPostProcessErodeNirTop(int postProcessErodeNirTop) {
		this.postProcessErodeNirTop = postProcessErodeNirTop;
	}
	
	public int getPostProcessErodeNirTop() {
		return postProcessErodeNirTop;
	}
	
	public void setProcessNir(boolean processNir) {
		this.processNir = processNir;
	}
	
	public boolean isProcessNir() {
		return processNir;
	}
	
	public void setDebugTakeTimes(boolean enable) {
		debugTakeTimes = enable;
	}
	
	public boolean isDebugTakeTimes() {
		return debugTakeTimes;
	}
	
	public double getEpsilon() {
		return 0.00001;
	}
	
	public void setDebugOverlayResult(boolean enable) {
		this.debugOverlayResultImage = enable;
	}
	
	public boolean isDebugOverlayResult() {
		return debugOverlayResultImage;
	}
}
