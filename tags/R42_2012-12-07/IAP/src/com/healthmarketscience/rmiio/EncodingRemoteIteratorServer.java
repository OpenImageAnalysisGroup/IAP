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
import java.io.OutputStream;

import com.healthmarketscience.rmiio.util.EncodingInputStream;

/**
 * Base class for implementing the server side of a RemoteIterator where the
 * objects need to be encoded on the fly. This object sends objects to the
 * RemoteIteratorClient through the internal RemoteInputStreamServer as
 * needed. Implementations of this class must implement the writeNextObject()
 * method, which should write the next object in the iteration to the local
 * output stream (which in turn will forward to data to the
 * RemoteIteratorClient). An Implementation should override closeIterator()
 * to do cleanup (like flushing and closing the output stream) after the last
 * object has been sent. The local output stream must <b>only</b> be written
 * during a call to writeNextObject() or closeIterator(). Note, users of this
 * class should ensure that the close() method is called one way or another,
 * or shutdown of the process may be delayed.
 * <p>
 * Note, a real-time-ish iterator is available if compression is disabled and noDelay is enabled (noDelay is ignored if compression is enabled). This will
 * attempt to send objects across the wire ASAP, at the expense of more wire traffic.
 * 
 * @author James Ahlborn
 */
public abstract class EncodingRemoteIteratorServer<DataType>
		extends RemoteIteratorServer<DataType> {
	/**
	 * the OutputStream which subclasses should use to write out an object
	 * during a call to writeNextObject() (and possibly closeIterator()).
	 */
	protected OutputStream _localOStream;
	
	public EncodingRemoteIteratorServer()
			throws IOException {
		this(true, RemoteInputStreamServer.DUMMY_MONITOR);
	}
	
	public EncodingRemoteIteratorServer(boolean useCompression)
			throws IOException {
		this(useCompression, false, RemoteInputStreamServer.DUMMY_MONITOR);
	}
	
	public EncodingRemoteIteratorServer(boolean useCompression,
													boolean noDelay)
			throws IOException {
		this(useCompression, noDelay, RemoteInputStreamServer.DUMMY_MONITOR);
	}
	
	public EncodingRemoteIteratorServer(
			boolean useCompression,
			RemoteStreamMonitor<RemoteInputStreamServer> monitor)
			throws IOException {
		this(useCompression, false, monitor);
	}
	
	public EncodingRemoteIteratorServer(
			boolean useCompression,
			boolean noDelay,
			RemoteStreamMonitor<RemoteInputStreamServer> monitor)
			throws IOException {
		this(useCompression, noDelay, monitor,
				RemoteInputStreamServer.DEFAULT_CHUNK_SIZE);
	}
	
	public EncodingRemoteIteratorServer(
			boolean useCompression,
			boolean noDelay,
			RemoteStreamMonitor<RemoteInputStreamServer> monitor,
			int chunkSize)
			throws IOException {
		// note, noDelay makes no sense if we are using compression, so just
		// disable in that case (this is not techically incorrect, compression
		// just overrides noDelay)
		super(new EncodingInputStreamImpl(chunkSize, (noDelay && !useCompression)),
				useCompression, monitor, chunkSize);
		((EncodingInputStreamImpl) _localIStream).setOuter(this);
		_localOStream = ((EncodingInputStreamImpl) _localIStream).getOutputStream();
	}
	
	/**
	 * Closes any resources held by this iterator. Subclasses should
	 * flush/close OutputStream during this call.
	 */
	protected void closeIterator()
			throws IOException {
		_localOStream.close();
	}
	
	/**
	 * If there are more objects in the iteration, writes an object to the
	 * _localOStream and returns <code>true</code>, otherwise returns <code>false</code>.
	 */
	protected abstract boolean writeNextObject()
			throws IOException;
	
	/**
	 * InputStream which lies under the RemoteInputStream and "reads" objects as
	 * necessary when data is requested by the client. the _localOStream used
	 * by the implementation of the EncodingRemoteIteratorServer actually
	 * forwards data to this class, which fills a buffer passed in during a
	 * read() call (and possibly an overflow buffer if necessary).
	 */
	private static class EncodingInputStreamImpl extends EncodingInputStream {
		/**
		 * because of the way things are created, this class cannot be an inner
		 * class. instead, we get a reference to our outer class after initial
		 * construction.
		 */
		private EncodingRemoteIteratorServer<?> _outer;
		
		private EncodingInputStreamImpl(int chunkSize, boolean noDelay) {
			super(chunkSize, noDelay);
		}
		
		private void setOuter(EncodingRemoteIteratorServer<?> outer) {
			_outer = outer;
		}
		
		private OutputStream getOutputStream() {
			return createOutputStream();
		}
		
		@Override
		public void encode(int suggestedLength)
				throws IOException {
			if (!_outer.writeNextObject()) {
				// this call should close the OutputStream
				_outer.closeIterator();
			}
		}
		
	}
	
}
