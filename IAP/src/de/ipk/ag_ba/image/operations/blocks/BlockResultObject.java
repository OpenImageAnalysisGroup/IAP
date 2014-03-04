package de.ipk.ag_ba.image.operations.blocks;

public class BlockResultObject extends BlockResultValue {
	
	private final Object value;
	
	public BlockResultObject(String name, Object value) {
		super(name, null, null);
		this.value = value;
	}
	
	public Object getObject() {
		return value;
	}
	
	@Override
	public String getString() {
		return getName() + ";" + value;
	}
	
	@Override
	public String toString() {
		return getString();
	}
}
