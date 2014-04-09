package iap.blocks.imageAnalysisTools.leafClustering;

public class FeatureObject {
	
	public Object feature;
	public FeatureObjectType featureObjectType;
	
	public FeatureObject(Object value, FeatureObjectType position) {
		this.feature = value;
		this.featureObjectType = position;
	}
	
	public enum FeatureObjectType {
		POSITION, VECTOR, NUMERIC, OBJECT
	}
}
