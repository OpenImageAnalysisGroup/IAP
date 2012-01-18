/*
 * Copyright (c) 2007 Health Market Science, Inc.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * You can contact Health Market Science at info@healthmarketscience.com
 * or at the following address:
 * Health Market Science
 * 2700 Horizon Drive
 * Suite 200
 * King of Prussia, PA 19406
 */

package com.healthmarketscience.rmiio;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Convenience interface which combines IOIterator and Closeable.
 * 
 * @author James Ahlborn
 */
public interface CloseableIOIterator<DataType> extends IOIterator<DataType>,
																			Closeable {
	
	/**
	 * Utility class for using a normal Iterator as a CloseableIOIterator.
	 * 
	 * @deprecated use {@link RmiioUtil#adapt} instead
	 */
	@Deprecated
	public static class Adapter<DataType>
			implements CloseableIOIterator<DataType> {
		private final Iterator<? extends DataType> _iter;
		
		public Adapter(Iterator<? extends DataType> iter) {
			_iter = iter;
		}
		
		public boolean hasNext() {
			return _iter.hasNext();
		}
		
		public DataType next() {
			return _iter.next();
		}
		
		public void close() {
		}
	}
	
}
