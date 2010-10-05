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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.healthmarketscience.rmiio.util.SingleByteAdapter;
import com.healthmarketscience.rmiio.util.PipeBuffer;

/**
 * Utility which provides a wrapper OutputStream for the client of a
 * RemoteOutputStream.  The wrapper will automagically handle any compression
 * needs of the remote stream.  RemoteException's will be retried using the
 * given RemoteRetry implementation.  Users should generally not need to wrap
 * the returned stream with a BufferedOutputStream as buffering will be done
 * by the returned implementation (unless *large* amounts of buffering are
 * desired).
 * <p>
 * <i>Warning, beware layering a PrintWriter or PrintStream on top of a
 * RemoteOutputStream</i>, as both of the aforementioned classes "swallow"
 * IOExceptions (well, they don't swallow them, you just have to test for
 * them).  In such a scenario, a client will not detect a problem in the
 * remote stream unless they specifically test for an error in the
 * PrintWriter/PrintStream.
 *
 * @author James Ahlborn
 */
public class RemoteOutputStreamClient
{
  /** The default retry policy used by this class's wrappers if none is
      specified by the caller. */
  public static final RemoteRetry DEFAULT_RETRY = RemoteClient.DEFAULT_RETRY;
  
  protected static final Log LOG =
    LogFactory.getLog(RemoteOutputStreamClient.class);

  /** default chunk size for shuffling data over the wire. */
  public static final Integer DEFAULT_CHUNK_SIZE =
    RemoteInputStreamServer.DEFAULT_CHUNK_SIZE;

  private RemoteOutputStreamClient() {}


  /**
   * Wraps a RemoteOutputStream as an OutputStream using the
   * {@link RemoteRetry#SIMPLE} retry policy.
   *
   * @param remoteOut a remote output stream interface
   * @return an OutputStream which will write to the given RemoteOutputStream
   */
  public static OutputStream wrap(RemoteOutputStream remoteOut)
    throws IOException
  {
    return wrap(remoteOut, DEFAULT_RETRY, DEFAULT_CHUNK_SIZE);
  }
  
  /**
   * Wraps a RemoteOutputStream as an OutputStream using the given retry
   * policy.
   *
   * @param remoteOut a remote output stream interface
   * @param retry RemoteException retry policy to use, if <code>null</code>,
   *              {@link #DEFAULT_RETRY} will be used.
   * @return an OutputStream which will write to the given RemoteOutputStream
   */
  public static OutputStream wrap(RemoteOutputStream remoteOut,
                                  RemoteRetry retry)
    throws IOException
  {
    return wrap(remoteOut, retry, DEFAULT_CHUNK_SIZE);
  }
  
  /**
   * Wraps a RemoteOutputStream as an OutputStream using the given retry
   * policy.
   *
   * @param remoteOut a remote output stream interface
   * @param retry RemoteException retry policy to use, if <code>null</code>,
   *              {@link #DEFAULT_RETRY} will be used.
   * @param chunkSize target value for the byte size of the packets of data
   *                  sent over the wire.  note that this is a suggestion,
   *                  actual packet sizes may vary.  if <code>null</code>,
   *                  {@link #DEFAULT_CHUNK_SIZE} will be used.
   * @return an OutputStream which will write to the given RemoteOutputStream
   */
  public static OutputStream wrap(RemoteOutputStream remoteOut,
                                  RemoteRetry retry,
                                  Integer chunkSize)
    throws IOException
  {
    if(retry == null) {
      retry = DEFAULT_RETRY;
    }
    if(chunkSize == null) {
      chunkSize = DEFAULT_CHUNK_SIZE;
    }
    OutputStream retStream =
      new RemoteOutputStreamImpl(remoteOut, retry, chunkSize);

    // determine if using compression (use wrapped _remoteOut with retry
    // builtin)
    if(((RemoteOutputStreamImpl)retStream)._remoteOut.usingGZIPCompression()) {
      // handle compression in the data
      retStream =
        new SaferGZIPOutputStream(retStream, chunkSize);
    }
    
    return retStream;
  }

  /**
   * OutputStream implementation which reads data from a RemoteOutputStream
   * server.
   */
  private static class RemoteOutputStreamImpl extends OutputStream
  {
    /** temp buffer to support the single byte write() method */
    private final SingleByteAdapter _singleByteAdapter =
      new SingleByteAdapter();
    /** handle to the RemoteOutputStream server */
    private final RemoteOutputStream _remoteOut;
    /** the target chunk size for data packets sent over the wire */
    private final int _chunkSize;
    /** PipeBuffer wrapper for building up the next packet of outgoing
        data */
    private final PipeBuffer _byteBuffer;
    /** the next sequence id to use for a remote call */
    private int nextActionId = RemoteStreamServer.INITIAL_VALID_SEQUENCE_ID;
    /** keep track of successful remote close calls, so that double closing
        the stream does not cause spurious errors (in the normal case) */
    private volatile boolean _remoteCloseSuccessful;
    /** keep track of whether any write attempts failed */
    private volatile boolean _writeSuccess = true;

    public RemoteOutputStreamImpl(RemoteOutputStream remoteOut,
                                  RemoteRetry retry,
                                  int chunkSize) {
      // wrap the remote stub with automatic retry facility using given retry
      // policy
      _remoteOut = new RemoteOutputStreamWrapper(remoteOut, retry, LOG);
      _chunkSize = chunkSize;
      _byteBuffer = new PipeBuffer(_chunkSize);
    }
    
    @Override
    public void close()
      throws IOException
    {
      if(_remoteCloseSuccessful) {
        // we've already successfully called close on the remote stream,
        // calling it again would result in an exception because the remote
        // server will be gone
        return;
      }
      
      try {
        // only flush local data, let close() call flush remote
        flush(false);
      } catch(IOException ignored) {
        if(LOG.isDebugEnabled()) {
          LOG.debug("Ignoring exception while flushing stream", ignored);
        }
      }

      // close the remote stream
      _remoteOut.close(_writeSuccess);
      
      // only set this if the close call is successful (does not throw)
      _remoteCloseSuccessful = true;
    }

    /**
     * Sends the current packet(s) of data to the RemoteOutputStream server.
     */
    private void flushPackets(boolean flushPartial)
      throws IOException
    {
      // caller should synch
      while(_byteBuffer.hasRemaining() &&
            (flushPartial || (_byteBuffer.packetsAvailable() > 0))) {
        byte[] packet = _byteBuffer.readPacket();
        _remoteOut.writePacket(packet, nextActionId++);
      }
    }

    /**
     * Flushes any local data to the remote server and (optionally) the remote
     * stream as well.
     */
    private synchronized void flush(boolean remoteFlush)
      throws IOException
    {
      // note, all the flush methods go through this method, so no need to
      // check each one individually
      boolean success = false;
      try {
        // first, flush all local bytes
        flushPackets(true);

        if(remoteFlush) {
          // now, flush remote
          _remoteOut.flush();
        }
        success = true;
      } finally {
        if(!success) {
          _writeSuccess = false;
        }
      }        
    }

    @Override
    public void flush()
      throws IOException
    {
      flush(true);
    }
    
    @Override
    public synchronized void write(int b)
      throws IOException
    {
      _singleByteAdapter.write(b, this);
    }

    @Override
    public void write(byte[] b)
      throws IOException
    {
      write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len)
      throws IOException
    {
      // note, all the write methods go through this method, so no need to
      // check each one individually
      boolean success = false;
      try {
        _byteBuffer.write(b, off, len);
        flushPackets(false);
        success = true;
      } finally {
        if(!success) {
          _writeSuccess = false;
        }
      }
    }
    
  }

  /**
   * Subclass of GZIPOutputStream which makes a better attempt at closing the
   * underlying RemoteOutputStream, even if the data has not been successfully
   * written.
   */
  private static class SaferGZIPOutputStream extends GZIPOutputStream
  {
    private SaferGZIPOutputStream(OutputStream out, int size)
      throws IOException
    {
      super(out, size);
    }

    @Override
    public void close()
      throws IOException
    {
      // GZIPOutputStream will not close underlying stream if it fails on
      // final write, but that means remote stream won't get closed.  we want
      // to force remote stream close regardless of success
      Exception closeFailure = null;
      try {
        super.close();
      } catch(Exception e) {
        closeFailure = e;
      } finally {
        out.close();
      }
      if(closeFailure != null) {
        if(closeFailure instanceof IOException) {
          throw (IOException)closeFailure;
        }
        throw (RuntimeException)closeFailure;
      }
    }
  }
  
}
