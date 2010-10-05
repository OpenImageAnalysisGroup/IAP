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
import java.io.OutputStream;

/**
 * Adapts a single byte read/write call to the corresponding call to a byte
 * array read/write call. Yeah, I know it seems trivial, but I use it
 * everywhere I implement an InputStream or OutputStream.  This class is not
 * synchronized.
 *
 * @author James Ahlborn
 */
public final class SingleByteAdapter {

  /** buffer for single byte read/write calls */
  private final byte[] _tmpBuf = new byte[1];
  
  /**
   * Calls {@link OutputStream#write(byte[],int,int)} on the given
   * OutputStream using an internal buffer with the given byte written to it.
   */
  public void write(int b, OutputStream ostream) throws IOException
  {
    _tmpBuf[0] = (byte)b;
    ostream.write(_tmpBuf, 0, 1);
  }

  /**
   * Calls {@link InputStream#read(byte[],int,int)} on the given InputStream
   * using an internal buffer, and returns the relevant result (either the end
   * of stream flag or the byte that was read).
   */
  public int read(InputStream istream) throws IOException
  {
    int numRead = istream.read(_tmpBuf, 0, 1);
    if(numRead < 0) {
      return numRead;
    }
      
    // we have to use the 'bitwise and' here so that the byte doesn't get
    // sign extended into an int, thus changing the actual value returned to
    // the caller.
    return _tmpBuf[0] & 0xff;
  }
  
}
