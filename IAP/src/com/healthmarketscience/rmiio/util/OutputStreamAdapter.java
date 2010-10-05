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

package com.healthmarketscience.rmiio.util;

import java.io.IOException;
import java.io.OutputStream;

import com.healthmarketscience.rmiio.PacketOutputStream;


/**
 * Utility class for optimizing different write strategies based on the type
 * of the underlying OutputStream.
 *
 * @author James Ahlborn
 */
public abstract class OutputStreamAdapter
{

  private OutputStreamAdapter() {
  }

  /**
   * @return the underlying OutputStream
   */
  public abstract OutputStream getOutputStream();

  /**
   * Puts the given "packet" into the output stream.  The packet should be
   * filled with data.  The caller is giving control of the buffer to the
   * PacketOutputStream, and therefore should not attempt to use the byte[]
   * again.
   *
   * @param packet fully filled array of bytes to give to the OutputStream
   */
  public abstract void writePacket(byte[] packet) throws IOException;

  /**
   * @param istream stream to wrap and for which the implementation is
   *                optimized
   * @return an OutputStreamAdapter optimized for the stream type
   */
  public static OutputStreamAdapter create(
      OutputStream istream)
  {
    if(istream instanceof PacketOutputStream) {
      return new PacketAdapter((PacketOutputStream)istream);
    }
    return new DefaultAdapter(istream);
  }

  /**
   * OutputStreamAdapter implementation for PacketOutputStreams.
   */
  private static class PacketAdapter extends OutputStreamAdapter
  {
    private final PacketOutputStream _postream;
    
    private PacketAdapter(PacketOutputStream postream) {
      _postream = postream;
    }

    @Override
    public PacketOutputStream getOutputStream() { return _postream; }
    
    @Override
    public void writePacket(byte[] packet) throws IOException {
      _postream.writePacket(packet);
    }
    
  }

  /**
   * OutputStreamAdapter implementation for normal OutputStreams.
   */
  private static class DefaultAdapter extends OutputStreamAdapter
  {
    private final OutputStream _ostream;
    
    private DefaultAdapter(OutputStream ostream) {
      _ostream = ostream;
    }

    @Override
    public OutputStream getOutputStream() { return _ostream; }
    
    @Override
    public void writePacket(byte[] packet) throws IOException {
      _ostream.write(packet, 0, packet.length);
    }
    
  }
  
  
}
