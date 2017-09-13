package iap.blocks.preprocessing;

import org.Vector2i;

/**
 * @author klukas
 */
public class ClusterPositionsAndSize {
	public Vector2i[] positions;
	public int[] size;
	
	public ClusterPositionsAndSize(Vector2i[] clusterPositions, int[] clusterSize) {
		positions = clusterPositions;
		size = clusterSize;
	}
}
