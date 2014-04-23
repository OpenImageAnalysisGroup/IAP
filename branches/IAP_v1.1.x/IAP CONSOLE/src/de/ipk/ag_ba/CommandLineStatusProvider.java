package de.ipk.ag_ba;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

public class CommandLineStatusProvider implements BackgroundTaskStatusProviderSupportingExternalCall {
	
	int printed = 0;
	private final String progressString;
	private final int maxOutput;
	
	private double currentProgress = -1;
	private boolean pleaseStop;
	
	private String s1, s2, s3, p1;
	
	public CommandLineStatusProvider(String progressString, int maxOutput) {
		this.progressString = progressString;
		this.maxOutput = maxOutput;
		
	}
	
	@Override
	public int getCurrentStatusValue() {
		return (int) currentProgress;
	}
	
	@Override
	public synchronized void setCurrentStatusValue(int value) {
		setCurrentStatusValueFine(value);
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		return currentProgress;
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		return p1 != null ? p1 + s1 : s1;
	}
	
	@Override
	public String getCurrentStatusMessage2() {
		return s2;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return s3;
	}
	
	@Override
	public void pleaseStop() {
		this.pleaseStop = true;
	}
	
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	@Override
	public void pleaseContinueRun() {
	}
	
	@Override
	public synchronized void setCurrentStatusValueFine(double value) {
		if (value > 0) {
			if (printed == 0)
				System.out.print(" [");
			if (maxOutput > 0) {
				// e.g. [####...
				while (value / 100d * maxOutput >= printed) {
					System.out.print(progressString);
					printed++;
				}
			} else {
				// [9876...
				while (value / 100d * 10 >= printed) {
					if (printed > 0)
						System.out.print((10 - printed) + "");
					printed++;
				}
			}
		}
	}
	
	@Override
	public boolean wantsToStop() {
		return pleaseStop;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		s1 = status;
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		s2 = status;
	}
	
	@Override
	public synchronized void setCurrentStatusValueFineAdd(double smallProgressStep) {
		if (currentProgress < 0)
			currentProgress = 0;
		currentProgress += smallProgressStep;
		setCurrentStatusValueFine(currentProgress);
	}
	
	public void finishPrint() {
		if (printed > 0) {
			if (maxOutput < 0) {
				while (printed <= 10) {
					if (printed > 0)
						System.out.print((10 - printed) + "");
					printed++;
				}
			} else {
				while (printed < maxOutput)
					System.out.print(progressString);
			}
			System.out.println("]");
		} else
			System.out.println();
	}
	
	@Override
	public void setPrefix1(String prefix1) {
		this.p1 = prefix1;
	}
}
