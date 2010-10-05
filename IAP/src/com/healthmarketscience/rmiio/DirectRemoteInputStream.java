/*
Copyright (c) 2008 Health Market Science, Inc.

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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * RemoteInputStream implementation which mimics the RemoteInputStream
 * functionality while not actually causing any additional RMI invocations.
 * This class is <i>not recommended for general use</i>, but may be useful (or
 * even required) in certain scenarios.  It basically works by writing the
 * stream data directly into the ObjectOutputStream during serialization.
 * There are a variety of implications to this approach, so please read the
 * pros and cons list carefully before deciding to use this class.
 * <p>
 * <ul>
 * <li><b>Pros:</b></li>
 * <ul>
 *   <li>No extra RMI invocations are needed, so this implementation will not
 *       have problems with firewalls.</li>
 *   <li>Since this implementation is not an RMI server, no extra RMI related
 *       objects are instantiated (servers, stubs, etc.) and no export is
 *       needed.</li>
 * </ul>
 * <li><b>Cons:</b></li>
 * <ul>
 *   <li>Send operations cannot be retried automatically.  Once the underlying
 *       stream has begun serialization, it can no longer be reserialized.
 *       And, since potentially lots of data may be sent in one invocation,
 *       the chance of network failures is increased.  <i>All in all, this
 *       implementation is much more fragile in the face of network
 *       failures</i>.  Note, however, that the application layer may be able
 *       to manually handle retries if the underlying stream is "restartable",
 *       such as a stream based on a File.</li>
 *   <li>If the RPC implementation keeps the entire invocation in memory, you
 *       will have memory consumption problems again.  This should not be a
 *       problem with vanilla RMI, which should write the data directly to an
 *       underlying socket.</li>
 *   <li>The server side process cannot start processing the data until the
 *       entire stream is sent (whereas with the other implementations, the
 *       data can be processed as it is received).</li>
 *   <li>The stream data is temporarily stored on the server's local
 *       filesystem.  This can have any number of implications including
 *       slower performance, excess disk consumption, and/or exposure of
 *       sensitive data if temp file attributes are incorrect.</li>
 *   <li>This implementation is RMI specific, so it cannot be used with any
 *       non-RMI compatible RPC frameworks (e.g. CORBA).</li>
 * </ul>
 * </ul>
 * <p>
 * Finally, the good news is that since this implementation is a
 * RemoteInputStream, <i>the client-side decision to use this class will not
 * impact the server</i>.  If the need arises in the future, client code which
 * uses this class may switch over to using one of the more robust
 * RemoteInputStream implementations without any changes to the server.
 * 
 * @author James Ahlborn
 */
public class DirectRemoteInputStream
  implements RemoteInputStream, Closeable, Serializable
{
  private static final Log LOG = LogFactory.getLog(DirectRemoteInputStream.class);  

  private static final long serialVersionUID = 20080125L;  

  /** status of the consumption of the underlying stream */
  private enum ConsumptionState {
    /** the underlying stream has not been consumed yet */
    NONE,
    /** the underlying stream is being consumed locally */
    LOCAL,
    /** the underlying stream is being consumed by serialization */
    SERIAL;
  }
  
  /** chunk code which indicates that the next chunk of data is the default
      length */
  private static final int DEFAULT_CHUNK_CODE = 0;
  /** chunk code which indicates that the next chunk of data is a custom
      length (the next 4 bytes will include the integer value of that
      length) */
  private static final int CUSTOM_CHUNK_CODE = 1;
  /** chunk code which indicates the end of the embedded stream data. */
  private static final int EOF_CODE = 2;

  /** stream containing the actual data.  when this class is instantiated
      directly, this will be any stream.  when this class is deserialized,
      this will be a temporary file on the local filesystem. */
  private transient InputStream _in;
  /** optional monitor for the initial serialization of the underlying
      stream */
  private transient RemoteStreamMonitor<RemoteInputStreamServer> _monitor;
  /** indicates how this object is being consumed.  it can be consumed locally
      or for serialization, but not both. */
  private transient ConsumptionState _consumptionState;
  /** indicates whether or not the underlying stream has been completely
      consumed */
  private transient boolean _gotEOF;
  /** local file which is caching the streamed data, only used when this
      object is deserialized */
  private transient File _tmpFile;
  /** whether or not the bytes should be compressed when serialized */
  private final boolean _compress;

  public DirectRemoteInputStream(InputStream in) {
    this(in, true, RemoteInputStreamServer.DUMMY_MONITOR);
  }
  
  public DirectRemoteInputStream(InputStream in, boolean compress) {
    this(in, compress, RemoteInputStreamServer.DUMMY_MONITOR);
  }
  
  public DirectRemoteInputStream(
      InputStream in, boolean compress,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor)
  {
    _in = in;
    _compress = compress;
    _monitor = monitor;
    _consumptionState = ConsumptionState.NONE;
  }

  /**
   * Mark this object as being consumed locally.  This happens whenever the
   * underlying stream starts being consumed via the regular read/skip
   * methods.
   */
  private void markLocalConsumption() {
    if(_consumptionState == ConsumptionState.SERIAL) {
      throw new IllegalStateException(
          "locally consuming stream which was already serialized");
    }
    _consumptionState = ConsumptionState.LOCAL;
  }
  
  public boolean usingGZIPCompression()
    throws IOException, RemoteException
  {
    return _compress;
  }

  public int available()
    throws IOException, RemoteException
  {
    markLocalConsumption();
    return _in.available();
  }

  public void close(boolean readSuccess)
    throws IOException, RemoteException
  {
    close();
  }
  
  public byte[] readPacket(int packetId)
    throws IOException, RemoteException
  {
    // note, this code should always be used locally, so the incoming packetId
    // can be safely ignored
    
    if(_gotEOF) {
      return null;
    }
    
    markLocalConsumption();
    byte[] packet = PacketInputStream.readPacket(
        _in, new byte[RemoteInputStreamServer.DEFAULT_CHUNK_SIZE]);
    _gotEOF = (packet == null);
    return packet;
  }
  
  public long skip(long n, int skipId)
    throws IOException, RemoteException
  {
    // note, this code should always be used locally, so the incoming skipId
    // can be safely ignored
    
    markLocalConsumption();
    return _in.skip(n);
  }
  
  public void close()
    throws IOException
  {
    if(_consumptionState == ConsumptionState.NONE) {
      _consumptionState = ConsumptionState.LOCAL;
    }
    try {
      if(_in != null) {
        _in.close();
      }
    } finally {
      _in = null;
      _gotEOF = true;
      // attempt to delete temp file (if any)
      if(_tmpFile != null) {
        _tmpFile.delete();
        _tmpFile = null;
      }
    }
  }

  /**
   * Serializes this object and all of the underlying stream's data directly
   * to the given ObjectOutputStream.
   * @serialData the compression status of the stream, followed by the default
   *             chunk size for the serialized stream data (int), followed by
   *             chunks of the underlying stream.  each chunk has a chunk code
   *             which indicates how to handle it's length (either default,
   *             explicit as int, or EOF), and then the specified number of
   *             bytes if not EOF.
   */
  private void writeObject(ObjectOutputStream out)
     throws IOException
  {
    switch(_consumptionState) {
    case NONE:
      // this is the required state
      break;
    case LOCAL:
    case SERIAL:
      throw new NotSerializableException(
          getClass().getName() +
          " (underlying stream has already been consumed, type: " +
          _consumptionState + ")");
    default:
      throw new RuntimeException("unknown state " + _consumptionState);
    }

    out.defaultWriteObject();

    // once we start consuming the inputstream, we can't rewrite it
    _consumptionState = ConsumptionState.SERIAL;

    final int defaultChunkSize = RemoteInputStreamServer.DEFAULT_CHUNK_SIZE;

    // note, we create RemoteInputStreamServer instances, but we do not
    // actually export them.
    RemoteInputStreamServer server = null;
    try {
      if(_compress && (_tmpFile == null)) {
        // this is the first time the data is being read, and we need to
        // compress it as we read it.
        server = new GZIPRemoteInputStream(_in, _monitor, defaultChunkSize);
      } else {
        // we are re-serializing a previously serialized stream, so the data
        // is already compressed (if compression was desired)
        server = new SimpleRemoteInputStream(_in, _monitor, defaultChunkSize);
      }

      // record the default chunk size
      out.writeInt(defaultChunkSize);

      int packetId = RemoteStreamServer.INITIAL_VALID_SEQUENCE_ID;
      while(true) {

        byte[] packet = server.readPacket(packetId++);

        if(packet != null) {
          if(packet.length > 0) {
            // we have a packet with data, write it to the output stream.  if
            // the packet is a different length, record the length.
            if(packet.length == defaultChunkSize) {
              out.write(DEFAULT_CHUNK_CODE);
            } else {
              out.write(CUSTOM_CHUNK_CODE);
              out.writeInt(packet.length);
            }
            out.write(packet);
          }
        } else {
          // reached end of stream, indicate this
          out.write(EOF_CODE);
          break;
        }

      }

      // local stream is exhausted
      _gotEOF = true;

      // indicate successful read
      try {
        server.close(true);
      } catch(IOException e) {
        // log, but ignore failures here
        if(LOG.isDebugEnabled()) {
          LOG.debug("Failed closing server", e);
        }
      }
      
    } finally {
      RmiioUtil.closeQuietly(server);
      RmiioUtil.closeQuietly(this);
    }
  }

  /**
   * Reads the state of this object and all of the underlying stream's data
   * directly from the given ObjectInputStream.  The stream data is stored in
   * a temporary file in the default java temp directory with the name
   * {@code "stream_<num>.dat"}.
   */
  private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    // read the default chunk size from the incoming file
    final int defaultChunkSize = in.readInt();
    checkChunkSize(defaultChunkSize);
    
    // setup a temp file for the incoming data (make sure it gets cleaned up
    // somehow)
    _tmpFile = File.createTempFile("stream_", ".dat");
    _tmpFile.deleteOnExit();

    FileOutputStream out = new FileOutputStream(_tmpFile);
    try {
      // limit buffer size in case of malicious input
      byte[] transferBuf = new byte[
          Math.min(defaultChunkSize,
                   RemoteInputStreamServer.DEFAULT_CHUNK_SIZE)];
      while(true) {

        // read in another chunk
        int chunkCode = in.read();
        if(chunkCode == EOF_CODE) {
          // all done
          break;
        }
        
        int readLen = defaultChunkSize;
        if(chunkCode != DEFAULT_CHUNK_CODE) {
          readLen = in.readInt();
          checkChunkSize(readLen);
        }

        // copy chunk into temp file
        copy(in, out, transferBuf, readLen);

      }

      // attempt to close the temp file.  if successful, we're good to go
      out.close();

      // sweet, setup final state
      _monitor = RemoteInputStreamServer.DUMMY_MONITOR;
      _in = new BufferedInputStream(new FileInputStream(_tmpFile));

      // the underlying stream is now in it's initial state
      _consumptionState = ConsumptionState.NONE;
      _gotEOF = false;
      
    } finally {
      RmiioUtil.closeQuietly(out);
    }
    
  }

  /**
   * Throws an InvalidObjectException if the given chunkSize is invalid.
   */
  private static void checkChunkSize(int chunkSize) 
    throws IOException
  {
    if(chunkSize <= 0) {
      throw new InvalidObjectException("invalid chunk size " + chunkSize);
    }
  }
  
  /**
   * Copies the given number of bytes from the given InputStream to the given
   * OutputStream using the given buffer for transfer.  The given InputStream
   * is expected to have at least this many bytes left to read, otherwise an
   * InvalidObjectException will be thrown.
   */
  private static void copy(InputStream in, OutputStream out, byte[] buffer,
                           int length)
    throws IOException
  {
    while(length > 0) {
      int readLen = in.read(buffer, 0, Math.min(buffer.length, length));
      if(readLen < 0) {
        throw new InvalidObjectException("input stream data truncated");
      }
      out.write(buffer, 0, readLen);
      length -= readLen;
    }
  }
  
  
}
