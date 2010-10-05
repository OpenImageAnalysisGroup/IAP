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
import java.util.Iterator;



/**
 * Interface which mimics the Iterator interface but allows IOExceptions to be
 * thrown by the implementation.
 * <p>
 * If the implementation needs to implement some sort of cleanup operation,
 * implement {@link CloseableIOIterator} instead.
 *
 * @author James Ahlborn
 */
public interface IOIterator<DataType>
{

  /**
   * Returns <code>true</code> iff the iteration has more elements.
   */
  public boolean hasNext() throws IOException;

  /**
   * Returns the next element in the iteration.
   */
  public DataType next() throws IOException;



  /**
   * Utility class for using a normal Iterator as an IOIterator.
   * @deprecated use {@link RmiioUtil#adapt} instead
   */
  @Deprecated
  public static class Adapter<DataType>
    implements IOIterator<DataType>
  {
    private final Iterator<? extends DataType> _iter;

    public Adapter(Iterator<? extends DataType> iter) {
      _iter = iter;
    }

    public boolean hasNext() { return _iter.hasNext(); }

    public DataType next() { return _iter.next(); }

  }
  
}
