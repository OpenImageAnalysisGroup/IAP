package iap.blocks.data_structures;

public enum ExecutionTimeStep {
	BLOCK_PREPARE, BLOCK_POST_PROCESS, BLOCK_PROCESS_IR, BLOCK_PROCESS_NIR, BLOCK_PROCESS_FLUO, BLOCK_PROCESS_VIS;
	
	@Override
	public String toString() {
		switch (this) {
			case BLOCK_PREPARE:
				return "Prepare Block Execution";
			case BLOCK_POST_PROCESS:
				return "Post-Process Results";
			case BLOCK_PROCESS_VIS:
				return "Process Visible-light Images";
			case BLOCK_PROCESS_FLUO:
				return "Process Fluorescence Images";
			case BLOCK_PROCESS_NIR:
				return "Process Near-Infrared Images";
			case BLOCK_PROCESS_IR:
				return "Process Infrared Images";
		}
		return super.toString();
	}
}
