/*
Copyright (c) 2007 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.rmiio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.healthmarketscience.rmiio.exporter.RemoteStreamExporter;


/**
 * Base class for implementing the server side of a RemoteIterator.  This
 * object manages RemoteInputStreamServer which connects the client and the
 * server.  Implementations of this class must provide an InputStream which is
 * the source of the data going over the wire.  Note, users of this class
 * should ensure that the close() method is called one way or another, or
 * shutdown of the process may be delayed.
 *
 * @author James Ahlborn
 */
public class RemoteIteratorServer<DataType> implements Closeable
{

  /** InputStream which is used by the RemoteInputStreamServer to read the
      objects. */
  protected final InputStream _localIStream;
  /** handle to the remote pipe linking this class to the client */
  private final RemoteInputStreamServer _remoteIStream;

  public RemoteIteratorServer(InputStream localIStream)
    throws IOException
  {
    this(localIStream, true, RemoteInputStreamServer.DUMMY_MONITOR);
  }
  
  public RemoteIteratorServer(InputStream localIStream, boolean useCompression)
    throws IOException
  {
    this(localIStream, useCompression, RemoteInputStreamServer.DUMMY_MONITOR);
  }
  
  public RemoteIteratorServer(
      InputStream localIStream,
      boolean useCompression,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor)
    throws IOException
  {
    this(localIStream, useCompression, monitor,
         RemoteInputStreamServer.DEFAULT_CHUNK_SIZE);
  }

  public RemoteIteratorServer(
      InputStream localIStream,
      boolean useCompression,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      int chunkSize)
    throws IOException
  {
    _localIStream = localIStream;
    if(useCompression) {
      _remoteIStream = new GZIPRemoteInputStream(_localIStream, monitor,
                                                 chunkSize) {
          private static final long serialVersionUID = 0L;
          @Override
          protected void closeImpl(boolean readSuccess)
            throws IOException {
            // hook into any remote/Unreferenced close calls
            try {
              super.closeImpl(readSuccess);
            } finally {
              RemoteIteratorServer.this.closeImpl(readSuccess);
            }
          }
        };
          
    } else {
      _remoteIStream = new SimpleRemoteInputStream(_localIStream, monitor,
                                                   chunkSize) {
          private static final long serialVersionUID = 0L;
          @Override
          protected void closeImpl(boolean readSuccess)
            throws IOException {
            // hook into any remote/Unreferenced close calls
            try {
              super.closeImpl(readSuccess);
            } finally {
              RemoteIteratorServer.this.closeImpl(readSuccess);
            }
          }
        };
    }
  }

  /**
   * Package-protected: gets handle to the RemoteInputStream which the
   * RemoteInputStreamClient should use.  Does the export of the object, so
   * should only be called <b>once</b> by the client.
   * @param exporter the exporter which will handle exporting the underlying
   *                 RemoteInputStream on the appropriate RPC framework
   */
  RemoteInputStream getRemoteInputStream(RemoteStreamExporter exporter)
    throws IOException
  {
    return((exporter == null) ? _remoteIStream.export() :
           exporter.export(_remoteIStream));
  }

  /**
   * @return <code>true</code> iff this iterator server has been closed (one
   *         way or another), <code>false</code> otherwise.
   */
  public final boolean isClosed() {
    return _remoteIStream.isClosed();
  }

  /**
   * Forces this iterator server to close (if not already closed), will
   * <b>break</b> any outstanding client interactions.  Should be called one
   * way or another on the server object (may be left to the underlying stream
   * <code>unreferenced</code> method if the server object must live beyond
   * the creation method call).
   */
  public final void close() {
    // note, this will eventually call our closeImpl method below due to the
    // override on creation.  this method is final because we do not want to
    // add additional behavior here which might not get invoked on remote
    // close (whereas closeImpl will get invoked on remote close).
    _remoteIStream.close();
  }
  
  /**
   * Aborts the current transfer without closing this RemoteIteratorServer.
   * This method is thread safe.  This will usually shutdown a currently
   * working transfer faster than just closing the RemoteIteratorServer
   * directly (because this will cause the client to get an IOException
   * instead of a RemoteException, which may cause retries, etc.).  This
   * RemoteIteratorServer should still be closed as normal.
   */
  public void abort()
    throws IOException
  {
    _remoteIStream.abort();
  }
  
  /**
   * Cleans up any local resources after the underlying stream server is
   * closed.  Since this method is called by the close() method on the
   * underlying remote stream, <i>it will be invoked on a successful remote
   * close</i>.
   *
   * @param readSuccess <code>true</code> iff all data was successfully
   *                    transferred, <code>false</code> otherwise
   */
  protected void closeImpl(boolean readSuccess)
    throws IOException
  {
    // base class does nothing
  }
  
}
