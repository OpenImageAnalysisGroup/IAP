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

package org.bson.types;

import org.bson.BSON;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Generic binary holder.
 */
public class Binary implements Serializable {

    private static final long serialVersionUID = 7902997490338209467L;

    /**
     * Creates a Binary object with the default binary type of 0
     *
     * @param data raw data
     */
    public Binary(byte[] data) {
        this(BSON.B_GENERAL, data);
    }

    /**
     * Creates a Binary with the specified type and data.
     *
     * @param type the binary type
     * @param data the binary data
     */
    public Binary(byte type, byte[] data) {
        _type = type;
        _data = data;
    }

    /**
     * Get the binary sub type as a byte.
     *
     * @return the binary sub type as a byte.
     */
    public byte getType() {
        return _type;
    }

    /**
     * Get a copy of the binary value.
     *
     * @return a copy of the binary value.
     */
    public byte[] getData() {
        return _data;
    }

    /**
     * Get the length of the data.
     *
     * @return the length of the binary array.
     */
    public int length() {
        return _data.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Binary)) {
            return false;
        }

        Binary binary = (Binary) o;

        if (_type != binary._type) {
            return false;
        }
        if (!Arrays.equals(_data, binary._data)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) _type;
        result = 31 * result + (_data != null ? Arrays.hashCode(_data) : 0);
        return result;
    }

    final byte _type;
    final byte[] _data;
}
