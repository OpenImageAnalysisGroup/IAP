package de.ipk.ag_ba.mongo.field_status;

import com.mongodb.BasicDBObject;

/**
 * The info processed by this class may be plant IDs, not carrier IDs.
 * (needs to be checked)
 * 
 * @author klukas
 */
public class FieldStatusObject extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @return Number of lanes in greenhouse / phytochamber.
	 */
	public int numberOfLanes() {
		return getInt("lane_cnt");
	}
	
	/**
	 * @param lane
	 *           Desired lane (starts with 0)
	 * @return Number of carriers in given lane.
	 */
	public int numberOfCarriers(int lane) {
		return getInt("lane_" + lane + "_carrier_cnt");
	}
	
	/**
	 * @param lane
	 *           Desired lane (starts with 0)
	 * @return Carrier IDs in given lane.
	 */
	public String[] carriers(int lane) {
		return getString("lane_" + lane + "_carrier_ids").split(",");
	}
	
	/**
	 * @param lane
	 *           Desired lane (starts with 0)
	 * @return A string providing the number of carriers in the lane as well as their IDs in the following
	 *         format : N:id1,id2, id3, ... (Number of carriers : List of IDs, divided by comma).
	 *         Example: 5:1107BA1350,1107BA1351,1107BA1352, ...
	 */
	public String carrierInfo(int lane) {
		return numberOfCarriers(lane) + ":" + getString("lane_" + lane + "_carrier_ids");
	}
}
