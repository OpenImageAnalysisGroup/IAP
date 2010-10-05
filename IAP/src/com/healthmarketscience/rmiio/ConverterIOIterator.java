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

/**
 * Useful almost concrete CloseableIOIterator implementation for the common
 * situation where you need to convert data from one iterator into data in
 * another iterator.  Everything in this class is implemented as expected, and
 * the {@link #convert} method is called on each element as it is returned
 * from the {@link #nextImpl} method.
 *
 * @author James Ahlborn
 */
public abstract class ConverterIOIterator<InType, OutType>
  extends AbstractCloseableIOIterator<OutType>
{
  /** input iterator */
  private final CloseableIOIterator<? extends InType> _iter;
  
  public ConverterIOIterator(CloseableIOIterator<? extends InType> iter) {
    _iter = iter;
  }

  public boolean hasNext() throws IOException {
    return _iter.hasNext();
  }

  @Override
  protected OutType nextImpl() throws IOException {
    return convert(_iter.next());
  }

  @Override
  protected void closeImpl() {
    RmiioUtil.closeQuietly(_iter);
  }

  /**
   * Converts from the input type to the output type.  Called by the
   * {@link #nextImpl} method.
   * @param in element to convert
   * @return the input value converted to the output type
   */
  protected abstract OutType convert(InType in) throws IOException;
  
}
