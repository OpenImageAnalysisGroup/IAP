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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for working with rmiio classes.
 *
 * @author James Ahlborn
 */
public class RmiioUtil
{
  private static final Log LOG = LogFactory.getLog(RmiioUtil.class);
  
  private RmiioUtil() {
  }

  /**
   * Adapts an {@link Iterator} to the {@link CloseableIOIterator} interface.
   * If the given iterator implements {@link Closeable}, it will be closed by
   * a close call on the wrapper.  The wrapper implementation is a subclass of
   * {@link AbstractCloseableIOIterator}, so the iterator will automagically
   * be closed if used with a SerialRemoteIteratorServer.
   */
  public static <T> CloseableIOIterator<T> adapt(Iterator<? extends T> iter)
  {
    return new IOIteratorAdapter<T>(iter);
  }

  /**
   * Closes the given Closeable if non-{@code null}, swallowing any
   * IOExceptions generated.
   */
  static void closeQuietly(Closeable closeable)
  {
    // yes, this has been written many times before and elsewhere, but i did
    // not want to add a dependency just for one method
    if(closeable != null) {
      try {
        closeable.close();
      } catch(IOException e) {
        // optionally log the exception, but otherwise ignore
        if(LOG.isDebugEnabled()) {
          LOG.debug("Failed closing closeable", e);
        }
      }
    }
  }
  
  /**
   * Adapts an Iterator to the CloseableIOIterator interface.
   */
  private static class IOIteratorAdapter<DataType>
    extends AbstractCloseableIOIterator<DataType>
  {
    private final Iterator<? extends DataType> _iter;

    public IOIteratorAdapter(Iterator<? extends DataType> iter) {
      _iter = iter;
    }

    public boolean hasNext() {
      return _iter.hasNext();
    }

    @Override
    protected DataType nextImpl() {
      return _iter.next();
    }

    @Override
    protected void closeImpl() {
      if(_iter instanceof Closeable) {
        closeQuietly((Closeable)_iter);
      }
    }
    
  }
  
}
