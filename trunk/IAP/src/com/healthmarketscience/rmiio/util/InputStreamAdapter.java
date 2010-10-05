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
import java.io.InputStream;

import com.healthmarketscience.rmiio.PacketInputStream;

/**
 * Utility class for optimizing different read strategies based on the type of
 * the underlying InputStream.
 *
 * @author James Ahlborn
 */
public abstract class InputStreamAdapter
{

  private InputStreamAdapter() {
  }

  /**
   * @return the underlying InputStream
   */
  public abstract InputStream getInputStream();

  /**
   * Gets the next "packet" from the internal buffer and returns it (if any).
   * This method will probably block until a fully filled packet is created
   * (equivalent to calling <code>readPacket(false)</code>).
   * <p>
   * Note, this method is slightly optimized for current usage, so an
   * implementation caveat is that the returned packet must be "consumed"
   * before another call to readPacket is made.
   *
   * @return a fully filled array of byte's or <code>null</code> if the end of
   *         stream has been reached
   */
  public abstract byte[] readPacket() throws IOException;

  /**
   * @return a buffer containing the bytes read by the last successful
   *         {@link #readTemp} call
   */
  public abstract byte[] getTempBuffer();

  /**
   * Reads some number of bytes from the underlying stream and puts them in a
   * buffer available from the {@link #getTempBuffer} call.
   * 
   * @return the number of bytes read, or -1 if end of stream was reached
   */
  public abstract int readTemp() throws IOException;
  
  /**
   * @param istream stream to wrap and for which the implementation is
   *                optimized
   * @param packetSize recommended packet size for any created packets
   * @return a new InputStreamAdapter optimized for the stream type
   */
  public static InputStreamAdapter create(InputStream istream, int packetSize)
  {
    if(istream instanceof PacketInputStream) {
      return new PacketAdapter((PacketInputStream)istream);
    }
    return new DefaultAdapter(istream, packetSize);
  }

  /**
   * InputStreamAdapter implementation for PacketInputStreams.
   */
  private static class PacketAdapter extends InputStreamAdapter
  {
    private final PacketInputStream _pistream;
    /** the last packet returned during a readTemp call */
    private byte[] _temp;
    
    private PacketAdapter(PacketInputStream pistream) {
      _pistream = pistream;
    }

    @Override
    public PacketInputStream getInputStream() { return _pistream; }
    
    @Override
    public byte[] readPacket() throws IOException {
      return _pistream.readPacket();
    }

    @Override
    public byte[] getTempBuffer() {
      return _temp;
    }
  
    @Override
    public int readTemp() throws IOException {
      _temp = _pistream.readPacket();
      return((_temp != null) ? _temp.length : -1);
    }
    
  }
  
  /**
   * InputStreamAdapter implementation for normal InputStreams.
   */
  private static class DefaultAdapter extends InputStreamAdapter
  {
    private final InputStream _istream;
    /** temporary buffer for reading data from the underlying InputStream */
    private final byte[] _temp;

    private DefaultAdapter(InputStream istream, int packetSize) {
      _istream = istream;
      _temp = new byte[packetSize];
    }

    @Override
    public InputStream getInputStream() { return _istream; }
    
    @Override
    public byte[] readPacket() throws IOException {
      return PacketInputStream.readPacket(_istream, _temp);
    }

    @Override
    public byte[] getTempBuffer() {
      return _temp;
    }
  
    @Override
    public int readTemp() throws IOException {
      return _istream.read(_temp, 0, _temp.length);
    }
    
  }  

}
