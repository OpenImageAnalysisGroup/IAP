package de.ipk.ag_ba.image.operations.blocks;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResult;

public class BlockResultObject extends BlockResult {
	
	private final Object value;
	private final String name;
	
	public BlockResultObject(String name, Object value, int blockPositionInPipeline) {
		super(Double.NaN, blockPositionInPipeline);
		this.name = name;
		this.value = value;
	}
	
	public Object getObject() {
		return value;
	}
	
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
