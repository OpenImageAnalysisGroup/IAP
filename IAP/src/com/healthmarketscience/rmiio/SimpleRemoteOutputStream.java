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
import java.io.OutputStream;

import com.healthmarketscience.rmiio.util.OutputStreamAdapter;


/**
 * Concrete implementation of a RemoteOutputStreamServer which expects to
 * receive uncompressed data, which it will write directly to the underlying
 * OutputStream.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 * @see #writeReplace
 *
 * @author James Ahlborn
 */
public class SimpleRemoteOutputStream extends RemoteOutputStreamServer 
{
  private static final long serialVersionUID = 20080212L;

  /** manages writing to the given stream in a packet-like manner */
  private transient final OutputStreamAdapter _outAdapter;
  
  public SimpleRemoteOutputStream(OutputStream out) {
    this(out, DUMMY_MONITOR);
  }

  public SimpleRemoteOutputStream(
    OutputStream out,
    RemoteStreamMonitor<RemoteOutputStreamServer> monitor) {
    super(out, monitor);
    _outAdapter = OutputStreamAdapter.create(out);
  }
  
  public boolean usingGZIPCompression()
  {
    // no compression
    return false;
  }

  @Override
  protected void flushImpl()
    throws IOException
  {
    synchronized(getLock()) {
      _out.flush();
    }
  }

  @Override
  protected void writePacket(byte[] packet)
    throws IOException
  {
    // will be called synchronized

    _outAdapter.writePacket(packet);
    _monitor.localBytesMoved(this, packet.length);
  }
  
}
