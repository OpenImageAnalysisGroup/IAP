/*
 * Copyright (c) 2007 Health Market Science, Inc.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * You can contact Health Market Science at info@healthmarketscience.com
 * or at the following address:
 * Health Market Science
 * 2700 Horizon Drive
 * Suite 200
 * King of Prussia, PA 19406
 */

package com.healthmarketscience.rmiio;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.healthmarketscience.rmiio.util.EncodingInputStream;

/**
 * Utility which provides a wrapper InputStream for the client of a
 * RemoteInputStream. The wrapper will automagically handle any compression
 * needs of the remote stream. RemoteException's will be retried using the
 * given RemoteRetry implementation. Note that the InputStreams will not
 * support mark()/reset() stream functionality. Users should generally not
 * need to wrap the returned stream with a BufferedInputStream as buffering
 * will be done by the returned implementation (unless *large* amounts of
 * buffering are desired).
 * 
 * @author James Ahlborn
 */
public class RemoteInputStreamClient {
	protected static final Log LOG =
			LogFactory.getLog(RemoteInputStreamClient.class);
	
	private RemoteInputStreamClient() {
	}
	
	/**
	 * Wraps a RemoteInputStream as an InputStream using the {@link RemoteClient#DEFAULT_RETRY} retry policy.
	 * 
	 * @param remoteIn
	 *           a remote input stream interface
	 * @return an InputStream which will read from the given RemoteInputStream
	 */
	public static InputStream wrap(RemoteInputStream remoteIn)
			throws IOException {
		return wrap(remoteIn, RemoteClient.DEFAULT_RETRY);
	}
	
	/**
	 * Wraps a RemoteInputStream as an InputStream using the given retry
	 * strategy.
	 * 
	 * @param remoteIn
	 *           a remote input stream interface
	 * @param retry
	 *           RemoteException retry policy to use, if <code>null</code>, {@link RemoteClient#DEFAULT_RETRY} will be used.
	 * @return an InputStream which will read from the given RemoteInputStream
	 */
	public static InputStream wrap(RemoteInputStream remoteIn,
											RemoteRetry retry)
			throws IOException {
		if (retry == null) {
			retry = RemoteClient.DEFAULT_RETRY;
		}
		InputStream retStream = new RemoteInputStreamImpl(remoteIn, retry);
		
		// determine if using compression (use wrapped _remoteIn with retry
		// builtin)
		if (((RemoteInputStreamImpl) retStream)._remoteIn.usingGZIPCompression()) {
			// handle compression in the data
			retStream =
					new SaferGZIPInputStream(retStream,
											RemoteInputStreamServer.DEFAULT_CHUNK_SIZE);
		}
		
		return retStream;
	}
	
	/**
	 * InputStream implementation which reads data from a RemoteInputStream
	 * server.
	 */
	private static final class RemoteInputStreamImpl extends EncodingInputStream {
		/** handle to the RemoteInputStream server */
		private final RemoteInputStream _remoteIn;
		/** output stream to which we write the bytes from the remote server */
		private final PacketOutputStream _ostream;
		/** the next sequence id to use for a remote call */
		private int nextActionId = RemoteStreamServer.INITIAL_VALID_SEQUENCE_ID;
		/**
		 * keep track of successful remote close calls, so that double closing
		 * the stream does not cause spurious errors (in the normal case)
		 */
		private volatile boolean _remoteCloseSuccessful;
		/** keep track of whether any over-the-wire read calls failed */
		private volatile boolean _readSuccess = true;
		
		public RemoteInputStreamImpl(RemoteInputStream remoteIn,
											RemoteRetry retry) {
			super(RemoteInputStreamServer.DEFAULT_CHUNK_SIZE);
			// wrap the remote stub with automatic retry facility using given retry
			// policy
			_remoteIn = new RemoteInputStreamWrapper(remoteIn, retry, LOG);
			
			// note, we call this here because this subclass is final, otherwise we
			// would not want to call this in the constructor
			_ostream = createOutputStream();
		}
		
		@Override
		public synchronized int available()
				throws IOException {
			return super.available();
		}
		
		@Override
		public synchronized int read()
				throws IOException {
			return super.read();
		}
		
		@Override
		public synchronized int read(byte[] b)
				throws IOException {
			return super.read(b);
		}
		
		@Override
		public synchronized int read(byte[] buf, int pos, int len)
				throws IOException {
			return super.read(buf, pos, len);
		}
		
		@Override
		public synchronized long skip(long len)
				throws IOException {
			return super.skip(len);
		}
		
		@Override
		public synchronized byte[] readPacket(boolean readPartial)
				throws IOException {
			return super.readPacket(readPartial);
		}
		
		@Override
		public synchronized int packetsAvailable()
				throws IOException {
			return super.packetsAvailable();
		}
		
		@Override
		public void close()
				throws IOException {
			if (_remoteCloseSuccessful) {
				// we've already successfully called close on the remote stream,
				// calling it again would result in an exception because the remote
				// server will be gone
				return;
			}
			
			// close the remote stream
			_remoteIn.close(_readSuccess);
			
			// only set this if the close call is successful (does not throw)
			_remoteCloseSuccessful = true;
		}
		
		@Override
		protected void encode(int suggestedLength)
				throws IOException {
			// grab more data from remote server
			boolean success = false;
			byte[] packet = null;
			try {
				packet = _remoteIn.readPacket(nextActionId++);
				success = true;
			} finally {
				if (!success) {
					_readSuccess = false;
				}
			}
			if (packet != null) {
				_ostream.writePacket(packet);
			} else {
				_ostream.close();
			}
		}
		
		@Override
		protected long encodeSkip(long len)
				throws IOException {
			boolean success = false;
			try {
				long result = _remoteIn.skip(len, nextActionId++);
				success = true;
				return result;
			} finally {
				if (!success) {
					_readSuccess = false;
				}
			}
		}
		
	}
	
	/**
	 * Subclass of GZIPInputStream which makes a better attempt at closing the
	 * underlying RemoteInputStream, even if the data has not been successfully
	 * read.
	 */
	private static class SaferGZIPInputStream extends GZIPInputStream {
		private SaferGZIPInputStream(InputStream in, int size)
				throws IOException {
			super(in, size);
		}
		
		@Override
		public void close()
				throws IOException {
			// GZIPInputStream will not close underlying stream if it fails on final
			// read, but that means remote stream won't get closed. we want to
			// force remote stream close regardless of success
			Exception closeFailure = null;
			try {
				super.close();
			} catch (Exception e) {
				closeFailure = e;
			} finally {
				in.close();
			}
			if (closeFailure != null) {
				if (closeFailure instanceof IOException) {
					throw (IOException) closeFailure;
				}
				throw (RuntimeException) closeFailure;
			}
		}
	}
	
}
