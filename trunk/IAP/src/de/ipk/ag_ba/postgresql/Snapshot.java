package de.ipk.ag_ba.postgresql;

import java.sql.Timestamp;

/**
 * @author entzian
 */
public class Snapshot {

	private String creator, measurement_label, id_tag, camera_label, path_image, path_null_image;
	private Timestamp time_stamp;
	private double weight_before, weight_after;
	private int water_amount;
	private double xFactor;
	private double yFactor;
	private String path_image_config_blob;
	private String userDefinedCameraLabel;

	@Override
	public String toString() {
		return "creator=" + creator + ", label=" + measurement_label + ", id_tag=" + id_tag + ", camera_label="
							+ camera_label + ", user_camera_label=" + userDefinedCameraLabel + ", image=" + path_image
							+ ", null_image=" + path_null_image + ", config_blob=" + path_image_config_blob;

	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreator() {
		return creator;
	}

	public void setMeasurement_label(String measurement_label) {
		this.measurement_label = measurement_label;
	}

	public String getMeasurement_label() {
		return measurement_label;
	}

	public void setId_tag(String id_tag) {
		this.id_tag = id_tag;
	}

	public String getId_tag() {
		return id_tag;
	}

	public void setCamera_label(String camera_label) {
		this.camera_label = camera_label;
	}

	public String getCamera_label() {
		return camera_label;
	}

	public void setPath_image(String path_image) {
		this.path_image = path_image;
	}

	public String getPath_image() {
		return path_image;
	}

	public void setPath_null_image(String path_null_image) {
		this.path_null_image = path_null_image;
	}

	public String getPath_null_image() {
		return path_null_image;
	}

	public void setPath_image_config_blob(String path_image_config_blob) {
		this.path_image_config_blob = path_image_config_blob;
	}

	public String getPath_image_config_blob() {
		return path_image_config_blob;
	}

	public void setTime_stamp(Timestamp time_stamp) {
		this.time_stamp = time_stamp;
	}

	public Timestamp getTimestamp() {
		return time_stamp;
	}

	public void setWeight_before(double weight_before) {
		this.weight_before = weight_before;
	}

	public double getWeight_before() {
		return weight_before;
	}

	public void setWeight_after(double weight_after) {
		this.weight_after = weight_after;
	}

	public double getWeight_after() {
		return weight_after;
	}

	public void setWater_amount(int water_amount) {
		this.water_amount = water_amount;
	}

	public int getWater_amount() {
		return water_amount;
	}

	public void setXfactor(double xfactor) {
		this.xFactor = xfactor;
	}

	public void setYfactor(double yfactor) {
		this.yFactor = yfactor;
	}

	public double getXfactor() {
		return xFactor;
	}

	public double getYfactor() {
		return yFactor;
	}

	public void setUserDefinedCameraLabeL(String userDefinedCameraLabel) {
		this.userDefinedCameraLabel = userDefinedCameraLabel;
	}

	public String getUserDefinedCameraLabel() {
		return userDefinedCameraLabel;
	}
}
