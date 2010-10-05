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

package com.healthmarketscience.rmiio.exporter;


import java.io.IOException;

import com.healthmarketscience.rmiio.RemoteInputStream;

import static com.healthmarketscience.rmiio.exporter.RemoteStreamServerInvokerHelper.*;

/**
 * Base RemoteInputStream implementation which translates each method
 * invocation into a call to the {@link #invoke} method in a manner compatible
 * with the {@link RemoteStreamServerInvokerHelper} {@code invoke} method for
 * a RemoteInputStreamServer.  This class may be useful for exporting remote
 * stream servers in alternative RPC frameworks.
 *
 * @author James Ahlborn
 */
public abstract class RemoteInputStreamClientProxy
  implements RemoteInputStream
{

  public RemoteInputStreamClientProxy() {
  }

  public boolean usingGZIPCompression()
    throws IOException
  {
    return (Boolean)invoke(IN_USING_COMPRESSION_METHOD);
  }
  
  public int available()
    throws IOException
  {
    return (Integer)invoke(IN_AVAILABLE_METHOD);
  }

  public void close(boolean readSuccess)
    throws IOException
  {
    invoke(IN_CLOSE_METHOD, readSuccess);
  }

  public byte[] readPacket(int packetId)
    throws IOException
  {
    return (byte[])invoke(IN_READ_PACKET_METHOD, packetId);
  }

  public long skip(long n, int skipId)
    throws IOException
  {
    return (Long)invoke(IN_SKIP_METHOD, n, skipId);
  }

  /**
   * Invokes the given method name with the given parameters on the remote
   * RemoteInputStreamServer and returns the results.
   * @param methodCode the name of the method to invoke, one of the
   *                   {@code RemoteStreamServerInvokerHelper.IN_*_METHOD}
   *                   constants
   * @param parameters parameters for the method invocation (may be
   *                   {@code null} if the method takes no parameters)
   * @return the result of the method call, (or {@code null} for void methods)
   * @throws IOException if the remote server throws or there is a
   *         communication failure.
   */
  protected abstract Object invoke(int methodCode, Object... parameters)
    throws IOException;
  
}
