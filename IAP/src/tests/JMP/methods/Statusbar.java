package tests.JMP.methods;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;

public class Statusbar {
	int status;
	int statusb = -1;
	int max;
	int min;
	
	public Statusbar(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public void update(int i) {
		status = i;
		status = (int) (10 * ((i - min) / (double) ((max - 1) - min)));
		if (status != statusb) {
			System.out.print("#");
			statusb = status;
		}
	}
	
	public void update(ArrayList<LocalComputeJob> jobList) {
		int finished = 0;
		for (LocalComputeJob lcj : jobList) {
			if (lcj.isFinished())
				finished++;
		}
		update(finished);
	}
}
