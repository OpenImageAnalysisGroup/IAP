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

import java.io.Serializable;
import java.util.Iterator;

/**
 * Trivial implementation of RemoteIterator for small collections.  The given
 * Iterable must be Serializable.
 *
 * @author James Ahlborn
 */
public class SimpleRemoteIterator<DataType>
  implements RemoteIterator<DataType>, Serializable
{
  private static final long serialVersionUID = -4737864032220987188L;

  /** the serializable collection */
  private final Iterable<DataType> _iterable;
  /** the current iterator for said collection */
  private transient Iterator<DataType> _iter;

  public SimpleRemoteIterator(Iterable<DataType> iterable)
  {
    _iterable = iterable;
  }

  private void init() {
    // initialize the iterator reference if necessary
    if(_iter == null) {
      _iter = _iterable.iterator();
    }
  }
  
  public boolean hasNext()
  {
    init();
    return _iter.hasNext();
  }

  public DataType next()
  {
    init();
    return _iter.next();
  }
  
  public void close() {
    // nothing to do
  }

  public void setRemoteRetry(RemoteRetry newRemoteRetry) {
    // ignored
  }
  
}
