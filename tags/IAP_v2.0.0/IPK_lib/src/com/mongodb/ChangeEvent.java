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

import static org.bson.util.Assertions.notNull;

class ChangeEvent<T> {
    private final T oldValue;
    private final T newValue;

    /**
     *
     * @param oldValue the value before the change
     * @param newValue the value after the change
     */
    public ChangeEvent(final T oldValue, final T newValue) {
        this.oldValue = notNull("oldValue", oldValue);
        this.newValue = notNull("newValue", newValue);
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ChangeEvent<?> that = (ChangeEvent<?>) o;

        if (!newValue.equals(that.newValue)) {
            return false;
        }

        if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = oldValue != null ? oldValue.hashCode() : 0;
        result = 31 * result + newValue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChangeEvent{"
               + "oldValue=" + oldValue
               + ", newValue=" + newValue
               + '}';
    }
}
