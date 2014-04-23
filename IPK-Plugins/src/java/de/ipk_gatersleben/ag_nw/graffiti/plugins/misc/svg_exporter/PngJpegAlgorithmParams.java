package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

public class PngJpegAlgorithmParams {
	
	private boolean createHTMLmap = false;
	private boolean useTransparency = false;
	private boolean includeURLinTooltip = false;
	private boolean includeTooltip = true;
	private boolean customTarget = false;
	
	private String customTargetValue = null;
	
	private SizeSetting scaleSetting = SizeSetting.ZOOM;
	private SizeSettingZoom scaleZoomSetting = SizeSettingZoom.L100;
	
	private boolean scaleFixedUseWidth = true;
	private int scaleFixedUseWidthOrHeightValue = 800;
	
	private int scaleDPIprintSize = 20;
	private int scaleDPIprintDPI = 300;
	private SizeSettingDPIunit scaleDPIprintSizeUnit = SizeSettingDPIunit.cm;
	
	private int maxWidth = -1;
	private int maxHeight = -1;
	private boolean createJPG = false;
	
	private int clipX = -1;
	private int clipY = -1;
	
	public void setCreateHTMLmap(boolean createHTMLmap) {
		this.createHTMLmap = createHTMLmap;
	}
	
	public boolean isCreateHTMLmap() {
		return createHTMLmap;
	}
	
	public void setUseTransparency(boolean useTransp) {
		this.useTransparency = useTransp;
	}
	
	public boolean useTransparency() {
		return useTransparency;
	}
	
	public void setIncludeURLinTooltip(boolean includeURLinTooltip) {
		this.includeURLinTooltip = includeURLinTooltip;
	}
	
	public boolean isIncludeURLinTooltip() {
		return includeURLinTooltip;
	}
	
	public void setIncludeTooltip(boolean includeTooltip) {
		this.includeTooltip = includeTooltip;
	}
	
	public boolean isIncludeTooltip() {
		return includeTooltip;
	}
	
	public void setCustomTarget(boolean customTarget) {
		this.customTarget = customTarget;
	}
	
	public boolean isCustomTarget() {
		return customTarget;
	}
	
	public void setCustomTarget(String value) {
		customTargetValue = value;
		customTarget = true;
	}
	
	public String getCustomTarget() {
		return customTargetValue;
	}
	
	public void setScaleSetting(SizeSetting scaleSetting) {
		this.scaleSetting = scaleSetting;
	}
	
	public SizeSetting getScaleSetting() {
		return scaleSetting;
	}
	
	public void setScaleZoomSetting(SizeSettingZoom scaleZoomSetting) {
		this.scaleZoomSetting = scaleZoomSetting;
	}
	
	public SizeSettingZoom getScaleZoomSetting() {
		return scaleZoomSetting;
	}
	
	public void setScaleFixedUseWidth(boolean scaleFixedUseWidth) {
		this.scaleFixedUseWidth = scaleFixedUseWidth;
	}
	
	public boolean isScaleFixedUseWidth() {
		return scaleFixedUseWidth;
	}
	
	public void setScaleFixedUseWidthOrHeightValue(int scaleFixedUseWidthOrHeightValue) {
		this.scaleFixedUseWidthOrHeightValue = scaleFixedUseWidthOrHeightValue;
	}
	
	public int getScaleFixedUseWidthOrHeightValue() {
		return scaleFixedUseWidthOrHeightValue;
	}
	
	public void setScaleDPIprintSize(int scaleDPIprintSize) {
		this.scaleDPIprintSize = scaleDPIprintSize;
	}
	
	public int getScaleDPIprintSize() {
		return scaleDPIprintSize;
	}
	
	public void setScaleDPIprintDPI(int scaleDPIprintDPI) {
		this.scaleDPIprintDPI = scaleDPIprintDPI;
	}
	
	public int getScaleDPIprintDPI() {
		return scaleDPIprintDPI;
	}
	
	public void setScaleDPIprintSizeUnit(SizeSettingDPIunit scaleDPIprintSizeUnit) {
		this.scaleDPIprintSizeUnit = scaleDPIprintSizeUnit;
	}
	
	public SizeSettingDPIunit getScaleDPIprintSizeUnit() {
		return scaleDPIprintSizeUnit;
	}
	
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	
	public int getMaxWidth() {
		return maxWidth;
	}
	
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}
	
	public int getMaxHeight() {
		return maxHeight;
	}
	
	public void setCreateJPG(boolean createJPG) {
		this.createJPG = createJPG;
	}
	
	public boolean isCreateJPG() {
		return createJPG;
	}
	
	public void setClipX(int clipX) {
		this.clipX = clipX;
	}
	
	public int getClipX() {
		return clipX;
	}
	
	public void setClipY(int clipY) {
		this.clipY = clipY;
	}
	
	public int getClipY() {
		return clipY;
	}
}
