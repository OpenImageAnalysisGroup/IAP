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
 * Interface common to most remote client implementations.
 *
 * @author James Ahlborn
 */
public interface RemoteClient {

  /** The default retry policy used if none is specified by the client. */
  public static final RemoteRetry DEFAULT_RETRY = RemoteRetry.SIMPLE;  
  
  /**
   * Sets the client side RemoteRetry policy to use for the underlying remote
   * communication layer.  For most client side implementations, this method
   * must be called before any other calls on this client object (any calls to
   * this method after one of those methods is called will have no affect).
   *
   * @param newRemoteRetry the new RemoteRetry policy to use for remote
   *                       communication.  {@code null} causes the
   *                       {@link #DEFAULT_RETRY} policy to be used.
   */
  public void setRemoteRetry(RemoteRetry newRemoteRetry);
  
}
