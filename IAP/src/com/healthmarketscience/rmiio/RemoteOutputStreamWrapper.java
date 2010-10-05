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
import java.rmi.RemoteException;
import org.apache.commons.logging.Log;


/**
 * Wrapper for a RemoteOutputStream stub which handles retry magic under the
 * hood.  The retry policy for a given method call will use the internal
 * policy for the default methods, but may be overridden on a per-call basis
 * using the extended methods.
 *
 * @author James Ahlborn
 */
public class RemoteOutputStreamWrapper
  extends RemoteWrapper<RemoteOutputStream>
  implements RemoteOutputStream
{

  public RemoteOutputStreamWrapper(RemoteOutputStream stub,
                                   RemoteRetry retry,
                                   Log log) {
    super(stub, retry, log);
  }

  public boolean usingGZIPCompression()
    throws IOException
  {
    return usingGZIPCompression(_retry);
  }

  public boolean usingGZIPCompression(RemoteRetry retry)
    throws IOException
  {
    return retry.call(new RemoteRetry.Caller<Boolean>()
      {
        @Override
        public Boolean call() throws IOException {
          return _stub.usingGZIPCompression();
        }
      }, _log, RemoteException.class);
  }

  public void close(boolean writeSuccess)
    throws IOException
  {
    close(writeSuccess, _retry);
  }

  public void close(final boolean writeSuccess, RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.close(writeSuccess);
        }
      }, _log, IOException.class);
  }

  public void flush()
    throws IOException
  {
    flush(_retry);
  }

  public void flush(RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.flush();
        }
      }, _log, IOException.class);
  }

  public void writePacket(byte[] packet, int packetId)
    throws IOException
  {
    writePacket(packet, packetId, _retry);
  }

  public void writePacket(final byte[] packet, final int packetId,
                          RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.writePacket(packet, packetId);
        }
      }, _log, IOException.class);
  }

}
