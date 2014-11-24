package de.ipk.ag_ba.gui.picture_gui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class StreamBackgroundTaskHelper {
	
	private final String desc;
	
	public StreamBackgroundTaskHelper(String desc) {
		this.desc = desc;
	}
	
	public void processInts(IntStream range, final IntConsumer task,
			UncaughtExceptionHandler handler) {
		UncaughtExceptionHandler handlerF = checkHandler(handler);
		LinkedList<LocalComputeJob> work = new LinkedList<>();
		int threads = SystemAnalysis.getNumberOfCPUs();
		if (threads < 1)
			threads = 1;
		final LinkedList<Integer> values = new LinkedList<>();
		range.forEach((v) -> values.add(v));
		IntStream.range(0, threads).forEach((thread) -> {
			try {
				work.add(BackgroundThreadDispatcher.addTask(() -> {
					Integer v = null;
					do {
						synchronized (values) {
							v = values.poll();
						}
						if (v != null)
							task.accept(v);
					} while (v != null);
				}, desc + " " + thread));
			} catch (Exception e) {
				handlerF.uncaughtException(Thread.currentThread(), e);
			}
		});
		try {
			BackgroundThreadDispatcher.waitFor(work);
		} catch (InterruptedException e) {
			handlerF.uncaughtException(Thread.currentThread(), e);
		}
	}
	
	private UncaughtExceptionHandler checkHandler(UncaughtExceptionHandler handler) {
		if (handler == null)
			return (t, e) -> {
				throw new RuntimeException(e);
			};
		else
			return handler;
	}
	
	public void processSrings(Stream<String> values, Consumer<String> c,
			UncaughtExceptionHandler handler) {
		UncaughtExceptionHandler handlerF = checkHandler(handler);
		LinkedList<LocalComputeJob> work = new LinkedList<>();
		int threads = SystemAnalysis.getNumberOfCPUs();
		if (threads < 1)
			threads = 1;
		values.forEach((v) -> {
			try {
				work.add(BackgroundThreadDispatcher.addTask(() -> {
					c.accept(v);
				}, desc + " " + v));
			} catch (Exception e) {
				handlerF.uncaughtException(Thread.currentThread(), e);
			}
		});
		try {
			BackgroundThreadDispatcher.waitFor(work);
		} catch (InterruptedException e) {
			handlerF.uncaughtException(Thread.currentThread(), e);
		}
	}
	
}
