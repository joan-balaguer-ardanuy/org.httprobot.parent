/*
 * Copyright (c) 2002-2013 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import com.gargoylesoftware.htmlunit.html.DomCharacterData;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;

/**
 * A JavaScript object for CharacterData.
 *
 * @version $Revision: 7931 $
 * @author David K. Taylor
 * @author Chris Erskine
 */
@JsxClass(domClasses = DomCharacterData.class)
public class CharacterDataImpl extends Node {

    /**
     * Creates an instance. JavaScript objects must have a default constructor.
     */
    public CharacterDataImpl() {
    }

    /**
     * Gets the JavaScript property "data" for this character data.
     * @return the String of data
     */
    @JsxGetter
    public Object getData() {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        return domCharacterData.getData();
    }

    /**
     * Sets the JavaScript property "data" for this character data.
     * @param newValue the new String of data
     */
    @JsxSetter
    public void setData(final String newValue) {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        domCharacterData.setData(newValue);
    }

    /**
     * Gets the number of character in the character data.
     * @return the number of characters
     */
    @JsxGetter
    public int getLength() {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        return domCharacterData.getLength();
    }

    /**
     * Append a string to character data.
     * @param arg the string to be appended to the character data
     */
    @JsxFunction
    public void appendData(final String arg) {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        domCharacterData.appendData(arg);
    }

    /**
     * Delete characters from character data.
     * @param offset the position of the first character to be deleted
     * @param count the number of characters to be deleted
     */
    @JsxFunction
    public void deleteData(final int offset, final int count) {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        domCharacterData.deleteData(offset, count);
    }

    /**
     * Insert a string into character data.
     * @param offset the position within the first character at which
     * the string is to be inserted.
     * @param arg the string to insert
     */
    @JsxFunction
    public void insertData(final int offset, final String arg) {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        domCharacterData.insertData(offset, arg);
    }

    /**
     * Replace characters of character data with a string.
     * @param offset the position within the first character at which
     * the string is to be replaced.
     * @param count the number of characters to be replaced
     * @param arg the string that replaces the count characters beginning at
     * the character at offset.
     */
    @JsxFunction
    public void replaceData(final int offset, final int count, final String arg) {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        domCharacterData.replaceData(offset, count, arg);
    }

    /**
     * Extract a substring from character data.
     * @param offset the position of the first character to be extracted
     * @param count the number of characters to be extracted
     * @return a string that consists of the count characters of the character
     *         data starting from the character at position offset
     */
    @JsxFunction
    public String substringData(final int offset, final int count) {
        final DomCharacterData domCharacterData = (DomCharacterData) getDomNodeOrDie();
        return domCharacterData.substringData(offset, count);
    }
}
