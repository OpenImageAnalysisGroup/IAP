package placement;

import javax.swing.JFrame;

public class DebugFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private Constraints constraints;
	
	@SuppressWarnings("unused")
	private Blocks blocks;
	
	private DebugPanel panel;
	
	DebugFrame(Blocks blocks, Constraints constraints) {
		super();
		this.blocks = blocks;
		this.constraints = constraints;
		if (blocks.head == null) {
			this.dispose();
			return;
		}
		panel = new DebugPanel(blocks, constraints);
		panel.setSize(324, 700);
		setSize(324, 700);
		setLocation(700, 0);
		add(panel);
		setVisible(true);
	}
	
	void animate() {
		if (panel != null) {
			panel.updateDrawing();
		}
	}
}
