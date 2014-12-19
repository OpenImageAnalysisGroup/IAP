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

package org.bson;

import org.bson.types.ObjectId;

/**
 * A callback interface for describing the structure of a BSON document. Implementations of this define how to turn BSON read from MongoDB
 * into Java objects.
 *
 * See the <a href="http://bsonspec.org/spec.html">BSON Spec</a>.
 */
public interface BSONCallback {

    /**
     * Signals the start of a BSON document, which usually maps onto some Java object.
     *
     * @mongodb.driver.manual core/document/ MongoDB Documents
     */
    void objectStart();

    /**
     * Signals the start of a BSON document, which usually maps onto some Java object.
     *
     * @param name the field name of the document.
     * @mongodb.driver.manual core/document/ MongoDB Documents
     */
    void objectStart(String name);

    /**
     * @param array true if this object is an array
     * @deprecated instead, use {@link #arrayStart()} if {@code array} is true, and {@link #objectStart()} if {@code array} is false
     */
    @Deprecated
    void objectStart(boolean array);

    /**
     * Called at the end of the document/array, and returns this object.
     *
     * @return the Object that has been read from this section of the document.
     */
    Object objectDone();

    /**
     * Resets the callback, clearing all state.
     */
    void reset();

    /**
     * Returns the finished top-level Document.
     *
     * @return the top level document read from the database.
     */
    Object get();

    /**
     * Factory method for BSONCallbacks.
     *
     * @return a new BSONCallback.
     */
    BSONCallback createBSONCallback();

    /**
     * Signals the start of a BSON array.
     *
     * @mongodb.driver.manual tutorial/query-documents/#read-operations-arrays Arrays
     */
    void arrayStart();

    /**
     * Signals the start of a BSON array, with its field name.
     *
     * @param name the name of this array field
     * @mongodb.driver.manual tutorial/query-documents/#read-operations-arrays Arrays
     */
    void arrayStart(String name);

    /**
     * Called the end of the array, and returns the completed array.
     *
     * @return an Object representing the array that has been read from this section of the document.
     */
    Object arrayDone();

    /**
     * Called when reading a BSON field that exists but has a null value.
     *
     * @param name the name of the field
     */
    void gotNull(String name);

    /**
     * Called when reading a field with an Undefined value.
     *
     * @param name the name of the field
     */
    void gotUndefined(String name);

    /**
     * Called when reading a field with a MinKey value.
     *
     * @param name the name of the field
     */
    void gotMinKey(String name);

    /**
     * Called when reading a field with a MaxKey value.
     *
     * @param name the name of the field
     */
    void gotMaxKey(String name);

    /**
     * Called when reading a field with a Boolean value.
     *
     * @param name the name of the field
     * @param value    the field's value
     */
    void gotBoolean(String name, boolean value);

    /**
     * Called when reading a field with a Double value.
     *
     * @param name the name of the field
     * @param value    the field's value
     */
    void gotDouble(String name, double value);

    /**
     * Called when reading a field with an Int32 value.
     *
     * @param name the name of the field
     * @param value    the field's value
     */
    void gotInt(String name, int value);

    /**
     * Called when reading a field with an Int64 value.
     *
     * @param name the name of the field
     * @param value    the field's value
     */
    void gotLong(String name, long value);

    /**
     * Called when reading a field with a DateTime value.
     *
     * @param name   the name of the field
     * @param millis the date and time in milliseconds
     */
    void gotDate(String name, long millis);

    /**
     * Called when reading a field with a String value.
     *
     * @param name the name of the field
     * @param value    the field's value
     */
    void gotString(String name, String value);

    /**
     * Called when reading a field with a Symbol value.
     *
     * @param name the name of the field
     * @param value    the field's value
     */
    void gotSymbol(String name, String value);

    /**
     * Called when reading a field with a Regex value.
     *
     * @param name    the name of the field
     * @param pattern the regex pattern
     * @param flags   the optional flags for the regular expression
     * @mongodb.driver.manual reference/operator/query/regex/ $regex
     */
    void gotRegex(String name, String pattern, String flags);

    /**
     * Called when reading a field with a Timestamp value.
     *
     * @param name the name of the field
     * @param time the time in seconds since epoch
     * @param inc  an incrementing ordinal for operations within a given second
     * @mongodb.driver.manual reference/bson-types/#timestamps Timestamps
     */
    void gotTimestamp(String name, int time, int inc);

    /**
     * Called when reading a field with an ObjectID value.
     *
     * @param name the name of the field
     * @param id   the object ID
     */
    void gotObjectId(String name, ObjectId id);

    /**
     * Invoked when {@link org.bson.BSONDecoder} encountered a DBPointer(0x0c) type field in a byte sequence.
     *
     * @param name the name of the field
     * @param ns   the namespace to which reference is pointing to
     * @param id   the if of the object to which reference is pointing to
     */
    void gotDBRef(String name, String ns, ObjectId id);

    /**
     * This method is not used.
     *
     * @param name the name of the field
     * @param data the field's value
     * @deprecated
     */
    @Deprecated
    void gotBinaryArray(String name, byte[] data);

    /**
     * Called when reading a field with a binary value. Note that binary values have a subtype, which may
     * determine how the value is processed.
     *
     * @param name the name of the field
     * @param type one of the binary subtypes
     * @param data the field's value
     */
    void gotBinary(String name, byte type, byte[] data);

    /**
     * Called when reading a field with a {@link java.util.UUID} value.  This is a binary value of subtype UuidLegacy.
     *
     * @param name  the name of the field
     * @param part1 the first part of the UUID
     * @param part2 the second part of the UUID
     */
    void gotUUID(String name, long part1, long part2);

    /**
     * Called when reading a field with a JavaScript value.
     *
     * @param name the name of the field
     * @param code the JavaScript code
     */
    void gotCode(String name, String code);

    /**
     * Called when reading a field with a JavaScript with Scope value.
     *
     * @param name  the name of the field
     * @param code  the JavaScript code
     * @param scope a document representing the scope for the code
     */
    void gotCodeWScope(String name, String code, Object scope);
}
