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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A "pipe" implementation for IOIterators which is designed for use by two
 * separate threads.  Common use would be a ""client" process acting as a
 * bridge between two different "servers", where data is being read from one
 * server via RemoteIterator and written to another server via RemoteIterator.
 * <p>
 * Note, although the Sink and Source are designed for use by separate
 * threads, the objects themselves are not thread safe, so a given Sink/Source
 * may not be used by more than one thread without external synchronization.
 *
 * @author James Ahlborn
 */
public class IOIteratorPipe<DataType> {

  public static final int DEFAULT_QUEUE_SIZE = 100;
  
  /** placeholder object for <code>null</code> values in the queue
      (BlockingQueues cannot handle <code>null</code>) */
  private static final Object NULL_OBJECT = new Object();
  /** end of data indicator */
  private static final Object FINAL_OBJECT = new Object();

  /** if <code>true</code>, the Sink had all data successfully added,
      otherwise, the Sink closed abnormally */
  private boolean _sinkFinished;
  /** if <code>true</code>, the Source has stopped reading data from the
      pipe. */
  private boolean _sourceClosed;
  /** queue used to transfer objects from Sink to Source */
  private final BlockingQueue<Object> _queue;
  /** object into which data is pushed to put data into the pipe */
  private final Sink _sink;
  /** object from which data is pulled to get data from the pipe */
  private final Source _source;

  /**
   * Constructs an IOIteratorPipe with the <code>DEFAULT_QUEUE_SIZE</code>
   * internal queue size.
   */
  public IOIteratorPipe() {
    this(DEFAULT_QUEUE_SIZE);
  }

  /**
   * Constructs an IOIteratorPipe with the given internal queue size.
   *
   * @param queueSize the maximum number of objects which will be held by this
   *                  object at any given time.
   */
  public IOIteratorPipe(int queueSize) {
    _queue = new LinkedBlockingQueue<Object>(queueSize);
    _sink = new Sink();
    _source = new Source();
  }
  
  /**
   * @return the Sink for pushing data into this pipe.
   */
  public Sink getSink() {
    return _sink;
  }

  /**
   * @return the Source for getting data from this pipe.
   */
  public Source getSource() {
    return _source;
  }

  /**
   * The Sink for this pipe.  Data is added to the pipe via the
   * <code>addNext</code> method.  When all the data is added, the
   * <code>setFinished</code> method should be called to indicate that all
   * data has been added.  The <code>close</code> method should be called
   * regardless of whether or not all data was added.  Calling the close
   * method before the setFinished method is called indicates abnormal
   * termination of the Sink.  Abnormal termination of one end of the pipe
   * will (eventually) cause an IOException to be thrown at the other end of
   * the pipe.  All methods will block if the internal queue has reached its
   * maximum size, which is why the Source and Sink must be driven via
   * separate threads.
   * <p>
   * Example:
   * <pre>
   *    IOIteratorPipe&lt;String&gt; pipe;
   *    IOIterator&lt;String&gt; sourceIter;
   *    try {
   *      pipe.getSink().addAll(sourceIter);
   *    } finally {
   *      pipe.getSource().close();
   *    }
   * </pre>
   */
  public class Sink implements Closeable
  {
    private Sink() {}

    /**
     * Implementation of adding an object to the pipe's queue.
     */
    private void addNextImpl(Object data)
      throws IOException
    {
      if(data == null) {
        // blocking queues cannot handle null
        data = NULL_OBJECT;
      }
      try {
        _queue.put(data);

        // as long as we read the _sourceClosed value after adding to the
        // queue, the _sourceClosed variable is correctly synchronized
        if((data != FINAL_OBJECT) && _sourceClosed) {
          throw new IOException("Source closed abnormally");
        }
        
      } catch(InterruptedException e) {
        // pass the interrupt along
        Thread.currentThread().interrupt();
        throw (IOException)(new InterruptedIOException()).initCause(e);
      }
    }

    /**
     * Adds the next object to the pipe.  May block if pipe's internal queue
     * size is at maximum capacity.
     */
    public void addNext(DataType data)
      throws IOException
    {
      addNextImpl(data);
    }
    
    /**
     * Indicates that all objects have been successfully added to the pipe.
     * May block if pipe's internal queue size is at maximum capacity.
     */
    public void setFinished()
      throws IOException
    {
      // as long as we set this value before adding to the queue, the
      // _sinkFinished variable is correctly synchronized
      _sinkFinished = true;
      addNextImpl(FINAL_OBJECT);
    }
  
    /**
     * Must be called regardless whether or not all data was added.  Calling
     * this method before the setFinished method is called (indicating
     * abnormal termination of the Sink) will result in abnormal termination
     * of the Source.  May block if pipe's internal queue size is at maximum
     * capacity.
     */
    public void close()
      throws IOException
    {
      if(!_sinkFinished) {
        // abnormal close, clear any waiting objects
        _queue.clear();
      }
      addNextImpl(FINAL_OBJECT);
    }

    /**
     * Convenience method for adding all the data from the given IOIterator to
     * the Sink.
     */
    public void addAll(IOIterator<DataType> srcIter)
      throws IOException
    {
      while(srcIter.hasNext()) {
        addNext(srcIter.next());
      }
      setFinished();
    }

  }

  /**
   * The Source for this pipe.  Data is received from the pipe using the
   * IOIterator interface methods in the standard fashion.  The
   * <code>close</code> method should be called regardless of whether or not
   * all data was received from the pipe.  Calling the close method before all
   * objects are received from the internal queue indicates abnormal
   * termination of the Source.  Abnormal termination of one end of the pipe
   * will (eventually) cause an IOException to be thrown at the other end of
   * the pipe.  All methods will block if the internal queue is empty, which
   * is why the Source and Sink must be driven via separate threads.
   * <p>
   * Example:
   * <pre>
   *    IOIteratorPipe&lt;String&gt; pipe;
   *    try {
   *      while(pipe.getSource().hasNext()) {
   *        String next = pipe.getSource().next();
   *        // ... do something with next ...
   *      }
   *    } finally {
   *      pipe.getSource().close();
   *    }
   * </pre>
   */
  public class Source extends AbstractCloseableIOIterator<DataType>
  {
    /** the next object to return from a call to <code>getNext</code>.  If
        <code>null</code>, this object has not yet been initialized.  If
        NULL_OBJECT, getNext will return <code>null</code>. If FINAL_OBJECT,
        the Sink has stopped adding objects. */
    private Object _next;

    private Source() {}
    
    private void getNext() throws IOException {
      try {
        _next = _queue.take();
        // as long as we read the _sinkFinished value after removing from the
        // queue, the _sinkFinished variable is correctly synchronized
        if((_next == FINAL_OBJECT) && !_sinkFinished) {
          throw new IOException("Sink closed abnormally");
        }
      } catch(InterruptedException e) {
        // pass the interrupt along
        Thread.currentThread().interrupt();
        throw (IOException)(new InterruptedIOException()).initCause(e);
      }
    }
    
    public boolean hasNext() throws IOException {
      if(_next == null) {
        // this object is not initialized yet
        getNext();
      }
      return(_next != FINAL_OBJECT);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected DataType nextImpl() throws IOException {
      Object cur = _next;
      getNext();

      if(cur == NULL_OBJECT) {
        // blocking queues cannot handle null
        cur = null;
      }

      return (DataType)cur;
    }

    @Override
    protected void closeImpl()
    {
      // as long as we set this value before clearing the queue, the
      // _sourceClosed variable is correctly synchronized
      _sourceClosed = true;
      _queue.clear();
    }    
  }
  
}
