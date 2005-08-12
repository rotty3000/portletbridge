/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.portletbridge.portlet.old;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Default implementation of the HttpResult class which can be serialized.
 * 
 * @author jmccrindle
 */
public class DefaultHttpResult implements HttpResult {

    private final String url;

    private final String contentType;

    private final byte[] content;

    /**
     * @param url the url that got this content
     * @param contentType the content type of the data
     * @param content the content of the httpresult
     */
    public DefaultHttpResult(String url, String contentType, byte[] content) {
        super();
        this.url = url;
        this.contentType = contentType;
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getContents() {
        return new ByteArrayInputStream(content);
    }

    public String getUrl() {
        return url;
    }
}