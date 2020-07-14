package org;

import java.util.concurrent.CountDownLatch;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

/**
 * @author klukas
 */
public class JavaFX {
	private static boolean init = getInit();
	
	private static boolean getInit() {
		if (true)
			return true;
		final CountDownLatch latch = new CountDownLatch(1);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JFXPanel(); // initializes JavaFX environment
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return true;
	}
	
	public static void init() {
		if (!init)
			init = getInit();
	}
}
