package de.ipk.ag_ba.image.operations.blocks.properties;

public class BlockResult {
	private final double value;
	private final int blockPositionInPipeline;
	
	public BlockResult(double value, int blockPositionInPipeline) {
		this.value = value;
		this.blockPositionInPipeline = blockPositionInPipeline;
		
	}
	
	public double getValue() {
		return value;
	}
	
	public int getBlockPositionInPipeline() {
		return blockPositionInPipeline;
	}
	
	@Override
	public String toString() {
		return "P=" + blockPositionInPipeline + ", Value=" + value;
	}
	
}
