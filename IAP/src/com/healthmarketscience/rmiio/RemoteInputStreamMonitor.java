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



/**
 * Implementation of RemoteStreamMonitor for RemoteInputStreamServers which
 * does nothing.
 *
 * @author James Ahlborn
 */
public class RemoteInputStreamMonitor
  implements RemoteStreamMonitor<RemoteInputStreamServer>
{
  public RemoteInputStreamMonitor() {
    
  }

  public void failure(RemoteInputStreamServer stream, Exception e) {}

  public void bytesMoved(RemoteInputStreamServer stream,
                         int numBytes, boolean isReattempt) {}

  public void bytesSkipped(RemoteInputStreamServer stream,
                           long numBytes, boolean isReattempt) {}

  public void localBytesMoved(RemoteInputStreamServer stream, int numBytes) {}

  public void localBytesSkipped(RemoteInputStreamServer stream,
                                long numBytes) {}
  
  public void closed(RemoteInputStreamServer stream,
                     boolean clean) {}
  
}
