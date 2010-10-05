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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Convenience base class for CloseableIOIterator implementations, especially
 * suited for use as the local iterator for a RemoteIteratorServer
 * instance.  This implementation manages the closing of the local resources
 * through three separate mechanisms.  The {@link #close} method will be
 * called:
 * <ul>
 * <li>by the {@link #next} method when the {@link #hasNext} method starts
 *     returning {@code false}</li>
 * <li>when the close method is called directly (duh)</li>
 * <li>if used with a RemoteIteratorServer, when the server is shutdown</li>
 * </ul>
 * This three-pronged attack provides a pretty strong guarantee that the local
 * resources will be closed at some point in time.  Note that the
 * implementation of the {@link #close} method will call the
 * {@link #closeImpl} method at most once.  Extraneous invocations will be
 * ignored.
 *
 * @author James Ahlborn
 */
public abstract class AbstractCloseableIOIterator<DataType>
  implements CloseableIOIterator<DataType>
{

  /** value which guarantees that the {@link #closeImpl} method is called at
      most once */
  private final AtomicBoolean _closed = new AtomicBoolean();
  
  public AbstractCloseableIOIterator() {
  }

  public final DataType next()
    throws IOException
  {
    if(!hasNext()) {
      throw new NoSuchElementException();
    }
    // grab current element
    DataType next = nextImpl();
    if(!hasNext()) {
      // all done with local resources, close them
      close();
    }
    // return current element
    return next;
  }
  
  public final void close()
  {
    // only close once
    if(_closed.compareAndSet(false, true)) {
      closeImpl();
    }
  }

  /**
   * Does the actual work of the {@link #next} method.  Will only be called if
   * {@link #hasNext} is currently returning {@code true}.
   */
  protected abstract DataType nextImpl() throws IOException;

  /**
   * Does the actual closing of the local resources.  Will be called at most
   * once by the {@link #close} method regardless of how many times that
   * method is invoked.
   * <p>
   * Note, this method does not throw {@code IOException} because it can be
   * called in a variety of different scenarios and throwing an IOException
   * would be useless in many of them (and often, failure to close is merely a
   * nuisance, not a cause for failure).
   */
  protected abstract void closeImpl();
  
}
