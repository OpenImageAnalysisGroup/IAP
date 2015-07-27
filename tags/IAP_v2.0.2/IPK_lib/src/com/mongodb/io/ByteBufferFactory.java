/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ByteBufferFactory.java

package com.mongodb.io;

import java.nio.ByteBuffer;

/**
 * @deprecated This class is NOT a part of public API and will be dropped in 3.x versions.
 */
@Deprecated
public interface ByteBufferFactory {
    public ByteBuffer get();

    /**
     * @deprecated This class is NOT a part of public API and will be dropped in 3.x versions.
     */
    @Deprecated
    public static class SimpleHeapByteBufferFactory implements ByteBufferFactory {
	public SimpleHeapByteBufferFactory( int size ){
	    _size = size;
	}
	
	public ByteBuffer get(){
	    return ByteBuffer.wrap( new byte[_size] );
	}

	final int _size;
    }
}
