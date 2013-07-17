package iap.blocks.unused;

public class ClusterSizeAndClusterId implements Comparable<ClusterSizeAndClusterId> {
	
	public Integer id;
	public Integer size = 0;
	
	@Override
	public int compareTo(ClusterSizeAndClusterId o) {
		return size.compareTo(o.size);
	}
	
}
