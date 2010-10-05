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

/**
 * Adds support for packet based access to data from an InputStream.  Can be
 * more efficient for certain stream implementations, especially for remote
 * stream usage where data is coming across the wire in byte[] packets.
 *
 * @author James Ahlborn
 */
public abstract class PacketInputStream extends InputStream
{
  /** target size of packets returned from packet related methods */
  public static final int DEFAULT_PACKET_SIZE = 1024;

  /** empty packet.  useful for returns from partial reads where no data is
      currently available, but it's not EOF. */
  protected static final byte[] EMPTY_PACKET = new byte[0];
  
  /** the packet size for buffers created in the overflow buffer list */
  private final int _packetSize;
  /** whether or not packet reading should accept partial packets by default.
      allowing partial packet reads will generally cause more remote calls, but
      should reduce latency per-object */
  private final boolean _noDelay;
  
  public PacketInputStream() {
    this(DEFAULT_PACKET_SIZE);
  }

  public PacketInputStream(int packetSize) {
    this(packetSize, false);
  }

  public PacketInputStream(int packetSize, boolean noDelay) {
    _packetSize = packetSize;
    _noDelay = noDelay;
  }

  public int getPacketSize() {
    return _packetSize;
  }

  public boolean getNoDelay() {
    return _noDelay;
  }
  
  /**
   * Gets the next "packet" from the internal buffer and returns it (if any).
   * By default, this method will block until a fully filled packet is created
   * (equivalent to calling <code>readPacket(false)</code>).  If noDelay is
   * enabled, this method will allow partial packet reads (equivalent to
   * calling <code>readPacket(true)</code>).
   *
   * @return a fully filled array of byte's or <code>null</code> if the end of
   *         stream has been reached
   */
  public byte[] readPacket()
    throws IOException
  {
    return readPacket(_noDelay);
  }

  /**
   * Gets the next "packet" from the internal buffer and returns it (if any).
   * This method may block until a full packet is read, depending on the value
   * of readPartial.
   *
   * @param readPartial iff <code>false</code>, may block until a full packet
   *                    is read (or EOF), otherwise will return as much data
   *                    as is currently available (which may be 0).
   * @return a fully filled array of byte's or <code>null</code> if the end of
   *         stream has been reached.  if no data is available but EOF has not
   *         been reached, the returned buffer will have length 0.
   */
  public abstract byte[] readPacket(boolean readPartial)
    throws IOException;

  
  /**
   * Returns the number of full packets which can be read without blocking.
   */
  public abstract int packetsAvailable()
    throws IOException;

  
  /**
   * Reads a packet of data from the given input stream.  The given packet is
   * filled and returned if possible.  If not enough bytes are available, a
   * new packet will be created and returned.  If the stream is empty, {@code
   * null} will be returned.
   * 
   * @param in the InputStream from which to read data
   * @param packet the potential output packet (if enough data is available)
   * @return a filled packet of data if any available, {@code null} if the
   *         stream is empty
   */
  public static byte[] readPacket(InputStream in, byte[] packet)
    throws IOException
  {
    int readLen = in.read(packet, 0, packet.length);
    if(readLen > 0) {
      
      if(readLen < packet.length) {
        // shrink buffer for output
        byte[] tmpPacket = new byte[readLen];
        System.arraycopy(packet, 0, tmpPacket, 0, readLen);
        packet = tmpPacket;
      }
      return packet;
      
    } else if(readLen == 0) {
      
      return PacketInputStream.EMPTY_PACKET;
    }
    return null;
  }
  
}
