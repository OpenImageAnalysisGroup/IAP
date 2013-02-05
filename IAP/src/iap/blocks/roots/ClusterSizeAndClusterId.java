package iap.blocks.roots;

public class ClusterSizeAndClusterId implements Comparable<ClusterSizeAndClusterId> {
	
	Integer id, size = 0;
	
	@Override
	public int compareTo(ClusterSizeAndClusterId o) {
		return size.compareTo(o.size);
	}
	
}
