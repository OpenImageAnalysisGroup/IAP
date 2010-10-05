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
import java.io.ObjectOutputStream;
import java.util.Iterator;


/**
 * Implementation of RemoteIteratorServer which uses java serialization to
 * send objects to the RemoteIteratorClient.  Objects are grabbed from the
 * localIterator as needed and serialized to the local output stream.  Note
 * that a RemoteIterator is accepted for the local iterator so that the
 * iterator may throw IOExceptions.
 * <p>
 * Note, the objects are written to the ObjectOutputStream using the
 * {@link java.io.ObjectOutputStream#writeUnshared} method, and the
 * {@link java.io.ObjectOutputStream#reset} method is called periodically on
 * the output stream.  These measures are taken because memory can build up in
 * the ObjectOutputStream over time and a large data set can run the client
 * and/or server out of memory.  In general, the objects being iterated over
 * most likely do not have shared references, so nothing will be lost by this
 * choice.  However, if shared references are desired, the
 * {@link #serializeObject} method can be overriden by a custom subclass to
 * change this behavior.
 * <p>
 * In the event that a RemoteIterator is being used to return low-latency,
 * low-bandwidth update data to the client, the noDelay option can be enabled
 * for the underlying stream which will effectively disable buffering of data
 * on the server side.  This can be very useful for implementing remote
 * progress monitors, for example.
 * <p>
 * Note, since it is a common idiom for the local iterator to implement
 * Closeable in order to close local resources, this implementation will
 * automatically close a Closeable local iterator after the underlying server
 * is shutdown.
 * 
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 *
 * @author James Ahlborn
 */
public class SerialRemoteIteratorServer<DataType>
  extends EncodingRemoteIteratorServer<DataType>
{

  /** Default value for the setting indicating how often the
      ObjectOutputStream should be reset */
  public static final int DEFAULT_RESET_NUM_OBJECTS = 1000;
  
  /** the output stream which does the java serialization work */
  private ObjectOutputStream _objOStream;
  /** local iterator from which we are getting Serializable objects */
  private final IOIterator<DataType> _localIterator;
  /** keeps track of num objects written so we can do periodic reset */
  private int _numObjectsWrittenSinceLastReset;
  /** setting which indicates how often the ObjectOutputStream should be reset
      (after this many objects are written). */
  private final int _resetNumObjects;

  public SerialRemoteIteratorServer(Iterator<DataType> localIterator)
    throws IOException
  {
    this(true, localIterator);
  }

  public SerialRemoteIteratorServer(boolean useCompression,
                                    Iterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, false, localIterator);
  }

  public SerialRemoteIteratorServer(boolean useCompression,
                                    boolean noDelay,
                                    Iterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, noDelay, RemoteInputStreamServer.DUMMY_MONITOR,
         localIterator);
  }

  public SerialRemoteIteratorServer(
      boolean useCompression,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      Iterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, false, monitor, localIterator);
  }
  
  public SerialRemoteIteratorServer(
      boolean useCompression,
      boolean noDelay,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      Iterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, noDelay, monitor,
         RemoteInputStreamServer.DEFAULT_CHUNK_SIZE, localIterator);
  }
  
  public SerialRemoteIteratorServer(
      boolean useCompression,
      boolean noDelay,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      int chunkSize,
      Iterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, noDelay, monitor, chunkSize,
         RmiioUtil.adapt(localIterator));
  }
  
  public SerialRemoteIteratorServer(IOIterator<DataType> localIterator)
    throws IOException
  {
    this(true, localIterator);
  }

  public SerialRemoteIteratorServer(boolean useCompression,
                                    IOIterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, false, localIterator);
  }

  public SerialRemoteIteratorServer(boolean useCompression,
                                    boolean noDelay,
                                    IOIterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, noDelay, RemoteInputStreamServer.DUMMY_MONITOR,
         localIterator);
  }

  public SerialRemoteIteratorServer(
      boolean useCompression,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      IOIterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, false, monitor, localIterator);
  }

  public SerialRemoteIteratorServer(
      boolean useCompression,
      boolean noDelay,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      IOIterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, noDelay, monitor,
         RemoteInputStreamServer.DEFAULT_CHUNK_SIZE,
         localIterator);
  }

  public SerialRemoteIteratorServer(
      boolean useCompression,
      boolean noDelay,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      int chunkSize,
      IOIterator<DataType> localIterator)
    throws IOException
  {
    this(useCompression, noDelay, monitor, chunkSize, localIterator,
         DEFAULT_RESET_NUM_OBJECTS);
  }

  public SerialRemoteIteratorServer(
      boolean useCompression,
      boolean noDelay,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      int chunkSize,
      IOIterator<DataType> localIterator,
      int resetNumObjects)
    throws IOException
  {
    super(useCompression, noDelay, monitor, chunkSize);
    _localIterator = localIterator;
    _resetNumObjects = resetNumObjects;
  }
  
  @Override
  protected boolean writeNextObject()
    throws IOException
  {
    // have to create output stream on demand because constructor generates
    // output!
    if(_objOStream == null) {
      _objOStream = new ObjectOutputStream(_localOStream);
    }
      
    if(_localIterator.hasNext()) {
      // write out next object
      serializeObject(_objOStream, _localIterator.next());
      return true;
    }

    // no more
    return false;
  }

  @Override
  protected void closeIterator()
    throws IOException
  {
    if(_objOStream != null) {
      // close (flush) object stream
      _objOStream.close();
    }
    // close parent
    super.closeIterator();
  }

  @Override
  protected void closeImpl(boolean readSuccess)
    throws IOException
  {
    // close our local iterator if it is Closeable.  Swallow exceptions
    // because at this point, they do not matter.
    if(_localIterator instanceof Closeable) {
      RmiioUtil.closeQuietly((Closeable)_localIterator);
    }
    super.closeImpl(readSuccess);
  }
  
  /**
   * Writes the given object to the given output stream.  The default
   * implementation uses {@link java.io.ObjectOutputStream#writeUnshared} as
   * well as periodically calls {@link java.io.ObjectOutputStream#reset} on
   * the output stream.  Subclasses may choose to change this behavior by
   * overriding this method.
   *
   * @param ostream the output stream to which the object should be written
   * @param obj the object to write
   */
  protected void serializeObject(ObjectOutputStream ostream, Object obj)
    throws IOException
  {
    ostream.writeUnshared(obj);
    _numObjectsWrittenSinceLastReset++;
    if(_numObjectsWrittenSinceLastReset >= _resetNumObjects) {
      ostream.reset();
      _numObjectsWrittenSinceLastReset = 0;
    }
  }
  
}
