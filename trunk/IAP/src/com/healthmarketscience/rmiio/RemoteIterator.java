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
import java.io.Serializable;


/**
 * Interface for streaming a read-only collection of objects using rmi.
 * Interface mimics the Iterator interface, but allows for IOExceptions and
 * requires the implementation to be Serializable.  This interface is useful
 * for situations where a collection is too large to be in memory all at once
 * and instead needs to be streamed to the consumer of the data.  For example,
 * data could be read directly from a database and streamed to a remote object
 * using this utility.  Generally, one would use the
 * RemoteIteratorClient/Server classes to implement the remote functionality.
 * <p>
 * Since this interface is built for use in a remote fashion, there is also a
 * close() method to facilitate better resource management.  Consumers of the
 * iterator should ensure that the close method is called one way or another
 * (especially if the entire iteration is not consumed!), or resources may
 * not be utilized as efficiently on the server.
 * <p>
 * Note, implementations of this class are not required to be thread-safe.
 *
 * @author James Ahlborn
 */
public interface RemoteIterator<DataType>
  extends CloseableIOIterator<DataType>, Serializable, RemoteClient
{

  /**
   * Closes the iterator and releases the resources for the server
   * object.  Note that the remote object <i>may no longer be accessible</i>
   * after this call (depending on the implementation), so clients should not
   * attempt to use this iterator after making this call.
   */
  public void close() throws IOException;

}
