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

// Symbol.java

package org.bson.types;

import java.io.Serializable;

/**
 * Class to hold an instance of the BSON symbol type.
 */
public class Symbol implements Serializable {

    private static final long serialVersionUID = 1326269319883146072L;

    /**
     * Construct a new instance with the given symbol.
     *
     * @param s the symbol
     */
    public Symbol(String s) {
        _symbol = s;
    }

    /**
     * Gets the symbol.
     *
     * @return the symbol
     */
    public String getSymbol(){
        return _symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        String otherSymbol;
        if (o instanceof Symbol) {
            otherSymbol = ((Symbol) o)._symbol;
        }
        else if (o instanceof String) {
            otherSymbol = (String) o;
        }
        else {
            return false;
        }

        if (_symbol != null ? !_symbol.equals(otherSymbol) : otherSymbol != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _symbol != null ? _symbol.hashCode() : 0;
    }

    @Override
    public String toString(){
        return _symbol;
    }

    private final String _symbol;
}
