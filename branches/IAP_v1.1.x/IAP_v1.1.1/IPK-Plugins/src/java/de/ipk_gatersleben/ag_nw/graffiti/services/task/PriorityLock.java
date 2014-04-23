package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Source: http://www.coderanch.com/t/573336/threads/java/Priority-Semaphore-Lock
 * (probably public domain)
 */
public class PriorityLock {
	
	private final ReentrantLock lockA;
	private final ReentrantLock lockB;
	
	public enum Priority {
		HIGH,
		LOW
	}
	
	public PriorityLock() {
		this.lockA = new ReentrantLock(true);
		this.lockB = new ReentrantLock(true);
	}
	
	public void lock() {
		lock(Priority.LOW);
	}
	
	public void lock(final Priority priority) {
		switch (priority) {
			case LOW:
				lockA.lock();
			case HIGH:
				lockB.lock();
		}
	}
	
	public void unlock() {
		lockB.unlock();
		if (lockA.isHeldByCurrentThread()) {
			lockA.unlock();
		}
	}
}