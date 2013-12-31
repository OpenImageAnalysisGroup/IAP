package de.ipk.ag_ba.commands.experiment.scripts;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;

public class ScriptExecutor {
	
	public static TreeMap<Long, String> start(String name, final String cmd, String[] params,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			int timeoutMinutes, final File optCurrentDir) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final String nameF = cmd;
		final ArrayList<String> lastOutput = new ArrayList<String>();
		final TreeMap<Long, String> output = new TreeMap<Long, String>();
		final ObjectRef myRef = new ObjectRef();
		final String[] parameter = params;
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Process ls_proc;
					String[] nameArray = new String[parameter.length + 1];
					nameArray[0] = nameF;
					for (int i = 0; i < parameter.length; i++)
						nameArray[i + 1] = parameter[i];
					
					ls_proc = Runtime.getRuntime().exec(nameArray, SystemAnalysis.getEnvArray(), optCurrentDir);
					
					myRef.setObject(ls_proc);
					
					final DataInputStream ls_in = new DataInputStream(ls_proc.getInputStream());
					final DataInputStream ls_in2 = new DataInputStream(ls_proc.getErrorStream());
					LocalComputeJob t1 = null;
					try {
						t1 = new LocalComputeJob(new Runnable() {
							@SuppressWarnings("deprecation")
							@Override
							public void run() {
								String response;
								try {
									while ((response = ls_in.readLine()) != null) {
										output.put(System.nanoTime() + tso.addLong(1), "" + response);
										System.out.println(response);
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText2(StringManipulationTools.stringReplace(response, "\"", ""));
										synchronized (lastOutput) {
											lastOutput.add(response);
											while (lastOutput.size() > 20)
												lastOutput.remove(0);
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, "PDF OUT");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					LocalComputeJob t2 = null;
					try {
						t2 = new LocalComputeJob(new Runnable() {
							@SuppressWarnings("deprecation")
							@Override
							public void run() {
								String response;
								try {
									while ((response = ls_in2.readLine()) != null) {
										output.put(System.nanoTime() + tso.addLong(1), "<font color='red'>" + response + "</font>");
										System.err.println(response);
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText2(SystemAnalysis.getCurrentTime() + ": ERROR: "
													+ StringManipulationTools.stringReplace(response, "\"", ""));
										synchronized (lastOutput) {
											lastOutput.add(SystemAnalysis.getCurrentTime() + ": ERROR: " + response);
											while (lastOutput.size() > 20)
												lastOutput.remove(0);
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, "PDF ERR");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					try {
						BackgroundThreadDispatcher.addTask(t1);
						BackgroundThreadDispatcher.addTask(t2);
						BackgroundThreadDispatcher.waitFor(new LocalComputeJob[] { t1, t2 });
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (optStatus != null)
						optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
					if (optStatus != null)
						optStatus.setCurrentStatusText2(SystemAnalysis.getCurrentTime() + ": Finished PDF creation");
				} catch (IOException e) {
					output.put(System.nanoTime(), "ERROR: EXCEPTION: " + e.getMessage());
					if (optStatus != null)
						optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
					if (optStatus != null)
						optStatus.setCurrentStatusText2(SystemAnalysis.getCurrentTime() + ": ERROR: " + e.getMessage());
					tso.setBval(1, true);
				}
				tso.setBval(0, true);
				System.out.println(SystemAnalysis.getCurrentTime() + ">FINISHED PDF CREATION TASK");
			}
		};
		
		Thread t = new Thread(r, "Execute " + name);
		t.start();
		
		try {
			long start = System.currentTimeMillis();
			while (!tso.getBval(0, false)) {
				Thread.sleep(20);
				if (timeoutMinutes > 0) {
					long now = System.currentTimeMillis();
					if (now - start > 1000 * 60 * timeoutMinutes && myRef.getObject() != null) {
						output.put(System.nanoTime(), "ERROR: TIME-OUT: " +
								"Command execution took more than " +
								timeoutMinutes + " minutes and has therefore been cancelled.");
						tso.setBval(1, true);
						if (myRef.getObject() != null) {
							Process ls_proc = (Process) myRef.getObject();
							ls_proc.destroy();
						}
						break;
					}
				}
			}
		} catch (InterruptedException e) {
			tso.setBval(1, true);
			throw new UnsupportedOperationException(e);
		}
		return output;
	}
	
}
