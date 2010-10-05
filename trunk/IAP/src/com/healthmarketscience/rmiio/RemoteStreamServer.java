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
import java.io.InterruptedIOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.Unreferenced;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.healthmarketscience.rmiio.exporter.RemoteStreamExporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Common base class for remote stream implementations which handles the
 * basic status of the stream (whether or not it is exported, closed).
 *
 * @author James Ahlborn
 */
public abstract class RemoteStreamServer<StreamServerType, StreamType>
  implements Remote, Unreferenced, Closeable, Serializable
{
  protected static final Log LOG = LogFactory.getLog(RemoteStreamServer.class);

  private static final long serialVersionUID = 20080212L;  

  /** the initial sequence id for server methods which have not yet been
      invoked */
  protected static final int INITIAL_INVALID_SEQUENCE_ID = -1;
  /** the initial sequence id for client code which has not yet invoked any
      remote methods */
  protected static final int INITIAL_VALID_SEQUENCE_ID = 0;
  
  /** this set will temporarily maintain a hard reference to a newly created
      RemoteStreamServer (via a HardRefMonitor) so that the stream remote stub
      isn't prematurely garbage collected.  see HardRefMonitor for more
      details. */
  private static final Set<Object> _hardRefSet =
    Collections.synchronizedSet(new HashSet<Object>());

  private enum State {
    OPEN, ABORTED, CLOSED;
  }
  
  /** whether or not this stream has been closed yet */
  private transient final AtomicReference<State> _state =
    new AtomicReference<State>(State.OPEN);
  /** the monitor which is following our progress */
  protected transient RemoteStreamMonitor<StreamServerType> _monitor;
  /** the exporter used to export this stream */
  private transient RemoteStreamExporter _exporter;
  /** the implicitly exported stub for this object, created by a call to
      writeReplace, if any */
  private transient volatile StreamType _writeReplacement;

  public RemoteStreamServer(RemoteStreamMonitor<StreamServerType> monitor) {
    _monitor = monitor;
  }


  /**
   * Convenience method which exports this object for use using the exporter
   * retrieved from {@link RemoteStreamExporter#getInstance}.
   */
  public StreamType export()
    throws RemoteException
  {
    return RemoteStreamExporter.getInstance().export(this);
  }

  /**
   * Indicates to this object that it was exported remotely.  Should be called
   * by the RemoteStreamExporter after this object has been exported.
   * @param exporter the exporter that exported this object
   */
  public synchronized void exported(RemoteStreamExporter exporter)
    throws RemoteException
  {
    if(_exporter != null) {
      throw new IllegalStateException("Re-exporting still exported stream " +
                                      this);
    }
    
    // keep a reference to the exporter which exported us
    _exporter = exporter;
    
    if(!(HardRefMonitor.class.isInstance(_monitor))) {
      // we temporarily wrap the monitor in order to keep our remote stub from
      // getting prematurely garbage collected.  see HardRefMonitor for more
      // details.  (we do this after a successful export only, or else we may
      // have a memory leak because this object would get stuck in the
      // _hardRefSet and never removed).
      _monitor = new HardRefMonitor(_monitor);
    }
  }

  /**
   * Makes this object no longer accessible remotely.
   */
  private synchronized void unexport()
  {
    _writeReplacement = null;
    try {
      if(HardRefMonitor.class.isInstance(_monitor)) {
        // premature unexport, make sure to ditch local hard reference
        HardRefMonitor.class.cast(_monitor).cleanup();
      }

      if(_exporter != null) {
        _exporter.unexport(this);
      } else {
        LOG.info("Unexporting object " + this + " which was not exported");
      }
    } finally {
      _exporter = null;
    }
  }

  public void unreferenced()
  {
    // close up everything.  note, that if we get here, the remote end did
    // *not* call the close(boolean) method successfully, so this is a "dirty"
    // *close.
    try {
      finish(false, false);
    } catch(IOException ignored) {
      if(LOG.isDebugEnabled()) {
        LOG.debug("Ignoring exception while closing unreferenced stream",
                  ignored);
      }
    }
  }

  /**
   * @return <code>true</code> iff this stream server has been closed (one way
   *         or another), <code>false</code> otherwise.
   */
  public final boolean isClosed() {
    return(_state.get() == State.CLOSED);
  }

  /**
   * Forces this stream server to close (if not already closed), will
   * <b>break</b> any outstanding client interactions.  Should be called one
   * way or another on the server object (may be left to the
   * <code>unreferenced</code> method if the server object must live beyond
   * the creation method call).
   */
  public final void close() {
    unreferenced();
  }
  
  /**
   * Cleans up after this stream.  Unexports the Remote object, closes the
   * underlying stream, and makes the final call(s) to the stream monitor.
   *
   * @param remoteClose indicates whether this was a remote close() call
   *                    or a local cleanup close after a failed transfer.
   * @param transferSuccess <code>true</code> iff all data was successfully
   *                        transferred, <code>false</code> otherwise
   */
  protected final void finish(boolean remoteClose,
                              boolean transferSuccess)
    throws IOException
  {
    State oldState = _state.getAndSet(State.CLOSED);
    if(oldState == State.CLOSED) {
      // nothing more to do (already closed)
      return;
    }

    boolean closeCompleted = false;
    try {
    
      // do actual close work
      closeImpl(transferSuccess);
      closeCompleted = true;
      
    } catch(IOException e) {

      // update the monitor
      _monitor.failure(getAsSub(), e);
      throw e;
      
    } catch(RuntimeException e) {

      // update the monitor
      _monitor.failure(getAsSub(), e);
      throw e;
      
    } finally {

      try {
        
        // update the monitor
        _monitor.closed(getAsSub(), (remoteClose && closeCompleted &&
                                     (oldState == State.OPEN)));
        
      } finally {
        
        // finally, unexport ourselves
        unexport();
      }
    }
  }

  /**
   * Aborts the current transfer without closing this RemoteStreamServer.
   * This method is thread safe.  This will usually shutdown a currently
   * working transfer faster than just closing the RemoteStreamServer directly
   * (because this will cause the client to get an IOException instead of a
   * RemoteException, which may cause retries, etc.).  This RemoteStreamServer
   * should still be closed as normal.
   */
  public final void abort() throws IOException
  {
    // can only abort it currently in the OPEN state.  note we don't care if
    // this set fails, because that implies the stream was closed first, which
    // is no big deal
    _state.compareAndSet(State.OPEN, State.ABORTED);
  }

  /**
   * Throws an IOException if the stream has been aborted.  Should be called
   * at the beginning of any method which accesses the underlying stream,
   * except for the <code>close</code> method.
   */
  protected final void checkAborted() throws IOException
  {
    if(_state.get() == State.ABORTED) {
      throw new InterruptedIOException("stream server was aborted");
    }
  }

  /**
   * Manages serialization for all remote stream instances by returning the
   * result of a call to {@link #export} on this instance as a Serializable
   * replacement for an instance of this class.  While generally the developer
   * should be managing the call to export, implementing this method in a
   * useful way makes the simple things simple (passing a reference to a
   * server implementation in a remote method call will "do the right thing",
   * replacing the actual reference to this instance with a reference to an
   * automagically generated remote reference to this server instance).
   * 
   * @return an exported remote stub for this instance
   * @throws NotSerializableException if the export attempt fails
   * @serialData the serialized data is the object returned by the
   *             {@link #export} method
   */
  protected final Object writeReplace() 
    throws ObjectStreamException
  {
    // note, we only want to do implicit export once.  it's possible that a
    // remote invocation failed and needs to be re-attempted, in which case we
    // don't want to re-export this instance, cause that will fail.
    StreamType replacement = _writeReplacement;
    if(replacement == null) {
      try {
        replacement = export();
        _writeReplacement = replacement;
      } catch(RemoteException e) {
        throw (NotSerializableException)
          (new NotSerializableException(
            getClass().getName() + ": Could not export stream server"))
          .initCause(e);
      }
    }
    return replacement;
  }

  /**
   * Sets the monitor of this class to the actual monitor (instead of the
   * temporary HardRefMonitor).  Only called by HardRefMonitor.
   */
  private void setRealMonitor(
      RemoteStreamMonitor<StreamServerType> realMonitor) {
    _monitor = realMonitor;
  }  
  
  /**
   * Closes (possibly flushes) the underlying streams and cleans up any
   * resources.  Called by the finish() method.
   *
   * @param transferSuccess <code>true</code> iff all data was successfully
   *                        transferred, <code>false</code> otherwise
   */
  protected abstract void closeImpl(boolean transferSuccess)
    throws IOException;

  /**
   * Returns a handle to the object used to lock the underlying stream
   * operations for this remote stream.
   */
  protected abstract Object getLock();

  /**
   * @return the class of the remote stream interface for this server
   */
  public abstract Class<StreamType> getRemoteClass();
  
  /**
   * Returns a handle to this object as a subclass instance.
   */
  protected abstract StreamServerType getAsSub();  

  /**
   * Utility class which temporarily maintains a hard reference to the
   * RemoteStreamServer so that its remote stub does not get prematurely
   * garbage collected.  Basically the remote stub garbage collection will
   * collect any stubs with no remote references and no *local* (hard)
   * references.  In a common usage of a RemoteStreamServer, the class is
   * created on the fly, handed to the remote client, and then the local
   * server discards its local (hard) reference to the stream.  This presents
   * a small window of opportunity where the remote stub can be garbage
   * collected before the remote client ever gets a handle to it (causing the
   * remote client to immediately get a NoSuchObjectException).  This class
   * solves that problem by maintaining a hard reference in a private set
   * (_hardRefSet) until the remote client has successfully called this class,
   * at which point this class removes itself from existence and everything
   * proceeds happily.  Note that even if the remote client never makes a
   * call, this class will get cleaned up when the unreferenced() method is
   * called, so there is no risk of memory leak.
   */
  private class HardRefMonitor implements RemoteStreamMonitor<StreamServerType>
  {
    /** handle to the actual monitor for the outer RemoteStreamServer. */
    private final RemoteStreamMonitor<StreamServerType> _realMonitor;
    
    public HardRefMonitor(RemoteStreamMonitor<StreamServerType> realMonitor) {
      _realMonitor = realMonitor;
      _hardRefSet.add(this);
    }

    private void cleanup() {
      // now that a remote method has been called, we no longer need the local
      // hard reference
      _hardRefSet.remove(this);

      // break the indirection to this monitor by resetting the monitor in the
      // RemoteStreamServer to the real one
      setRealMonitor(_realMonitor);
    }
    
    public void failure(StreamServerType stream, Exception e) {
      cleanup();
      _realMonitor.failure(stream, e);
    }

    public void bytesMoved(StreamServerType stream, int numBytes,
                                 boolean isReattempt) {
      cleanup();
      _realMonitor.bytesMoved(stream, numBytes, isReattempt);
    }
    
    public void bytesSkipped(StreamServerType stream, long numBytes,
                             boolean isReattempt) {
      cleanup();
      _realMonitor.bytesSkipped(stream, numBytes, isReattempt);
    }

    public void localBytesMoved(StreamServerType stream, int numBytes) {
      cleanup();
      _realMonitor.localBytesMoved(stream, numBytes);
    }
    
    public void localBytesSkipped(StreamServerType stream, long numBytes) {
      cleanup();
      _realMonitor.localBytesSkipped(stream, numBytes);
    }

    public void closed(StreamServerType stream, boolean clean) {
      cleanup();
      _realMonitor.closed(stream, clean);
    }
    
  }
  
}
