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

package com.mongodb;

enum BSONBinarySubType {
    /**
     * Binary data.
     */
    Binary((byte) 0x00),

    /**
     * A function.
     */
    Function((byte) 0x01),

    /**
     * Obsolete binary data subtype (use Binary instead).
     */
    OldBinary((byte) 0x02),

    /**
     * A UUID in a driver dependent legacy byte order.
     */
    UuidLegacy((byte) 0x03),

    /**
     *  A UUID in standard network byte order.
     */
    UuidStandard((byte) 0x04),

    /**
     * An MD5 hash.
     */
    MD5((byte) 0x05),

    /**
     *  User defined binary data.
     */
    UserDefined((byte) 0x80);

    private final byte value;

    BSONBinarySubType(final byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

}