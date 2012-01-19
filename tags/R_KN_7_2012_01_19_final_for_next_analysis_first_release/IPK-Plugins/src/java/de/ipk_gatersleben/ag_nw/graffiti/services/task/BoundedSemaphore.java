package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import java.util.HashSet;

public class BoundedSemaphore {
	private int signals = 0;
	private int bound = 0;
	private final HashSet<String> owners = new HashSet<String>();
	
	public BoundedSemaphore(int upperBound) {
		this.bound = upperBound;
	}
	
	public synchronized void take(String takenBy) throws InterruptedException {
		// if (this.signals == bound) {
		// System.out.println("WAIT FOR:");
		// for (String o : owners)
		// System.out.println(o);
		// }
		while (this.signals == bound)
			wait();
		owners.add(takenBy);
		this.signals++;
		this.notifyAll();
	}
	
	public synchronized void release(String takenBy) throws InterruptedException {
		while (this.signals == 0)
			wait();
		owners.remove(takenBy);
		this.signals--;
		this.notifyAll();
	}
}