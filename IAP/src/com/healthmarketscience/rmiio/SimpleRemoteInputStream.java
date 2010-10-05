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
import java.io.InputStream;

import com.healthmarketscience.rmiio.util.InputStreamAdapter;

/**
 * Concrete implementation of a RemoteInputStreamServer which sends
 * uncompressed data, which it will read directly from the underlying
 * InputStream.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 * @see #writeReplace
 *
 * @author James Ahlborn
 */
public class SimpleRemoteInputStream extends RemoteInputStreamServer
{
  private static final long serialVersionUID = 20080212L;  

  /** manages reading from the given stream in a packet-like manner */
  private transient final InputStreamAdapter _inAdapter;
  
  public SimpleRemoteInputStream(InputStream in) {
    this(in, DUMMY_MONITOR, DEFAULT_CHUNK_SIZE);
  }

  public SimpleRemoteInputStream(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor) {
    this(in, monitor, DEFAULT_CHUNK_SIZE);
  }

  public SimpleRemoteInputStream(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor,
    int chunkSize)
  {
    super(in, monitor, chunkSize);
    _inAdapter = InputStreamAdapter.create(in, _chunkSize);
  }
  
  public boolean usingGZIPCompression()
  {
    // no compression
    return false;
  }

  @Override
  protected int availableImpl()
    throws IOException
  {
    synchronized(getLock()) {
      return _in.available();
    }
  }

  @Override
  protected byte[] readPacket()
    throws IOException
  {
    // will be called synchronized
    
    // read another packet of data
    byte[] packet = _inAdapter.readPacket();
    if(packet != null) {
      _monitor.localBytesMoved(this, packet.length);
    }      
    return packet;
  }

  @Override
  protected long skip(long n)
    throws IOException
  {
    // will be called synchronized
    long numSkipped = _in.skip(n);
    _monitor.localBytesSkipped(this, numSkipped);
    return numSkipped;
  }
  
}
