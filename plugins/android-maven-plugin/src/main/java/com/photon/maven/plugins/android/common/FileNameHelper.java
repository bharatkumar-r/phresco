/*
 * Copyright (C) 2009 Jayway AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.photon.maven.plugins.android.common;

/**
 * FileNameHelper can make a valid filename.
 */
public class FileNameHelper {
    //    { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    private static final String ILLEGAL_CHARACTERS_REGEX = "[/\\n\\r\\t\\\0\\f`\\?\\*\\\\<>\\|\":]";
    private static final String SEPERATOR = "_";

    public static String fixFileName(String fileName) {
        return fileName.replaceAll(ILLEGAL_CHARACTERS_REGEX, SEPERATOR);
    }

}