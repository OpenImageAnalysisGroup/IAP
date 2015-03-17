package org;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

/**
 * @author klukas
 */
public interface RunnableExecutor {
	void execInParallel(ArrayList<Runnable> rl, String desc, UncaughtExceptionHandler handler);
}
