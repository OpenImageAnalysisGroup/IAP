package de.ipk_gatersleben.ag_nw.graffiti.services;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.ObjectRef;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class MyOutputStream extends OutputStream {
	
	BackgroundTaskStatusProviderSupportingExternalCallImpl status;
	FileOutputStream fileOutputStream;
	long maxBytes = 0;
	long received = 0;
	long startTime = System.currentTimeMillis();
	
	public String lastStatus = "Download initiated";
	private ObjectRef lastStatusRef = null;
	
	private void updateStatus(int rec) {
		received += rec;
		long currentTime = System.currentTimeMillis();
		int speed = (int) Math.round(received / 1024d / (currentTime - startTime) * 1000d);
		if (maxBytes > 0) {
			lastStatus = "Received: " + received / 1024 + " KB / " +
								maxBytes / 1024 + " KB, " + speed + " KB/sec";
			status.setCurrentStatusText2(lastStatus);
		} else {
			lastStatus = "Received: " + received / 1024 + " KB, " + speed + " KB/sec";
			status.setCurrentStatusText2(lastStatus);
		}
		lastStatusRef.setObject(lastStatus);
		if (maxBytes > 1024) {
			if (maxBytes > 0) {
				status.setCurrentStatusValueFine(100d * received / maxBytes);
			}
		}
	}
	
	public void setMaxBytes(long max) {
		this.maxBytes = max;
	}
	
	public MyOutputStream(
						ObjectRef lastStatusRef, BackgroundTaskStatusProviderSupportingExternalCallImpl status,
						FileOutputStream fileOutputStream) {
		this.maxBytes = 0;
		this.status = status;
		this.lastStatusRef = lastStatusRef;
		this.fileOutputStream = fileOutputStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		fileOutputStream.write(b);
		updateStatus(1);
	}
	
	@Override
	public void close() throws IOException {
		fileOutputStream.close();
	}
	
	@Override
	public void flush() throws IOException {
		fileOutputStream.flush();
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		fileOutputStream.write(b, off, len);
		updateStatus(len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		fileOutputStream.write(b);
		updateStatus(b.length);
	}
}
