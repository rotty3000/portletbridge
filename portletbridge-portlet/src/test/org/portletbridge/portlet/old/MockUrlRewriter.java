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

import org.portletbridge.UrlRewriter;

/**
 * @author jmccrindle
 */
public class MockUrlRewriter implements UrlRewriter {

    private String mock;

    /**
     * @param mock
     */
    public MockUrlRewriter(String mock) {
        this.mock = mock;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.UrlRewriter#rewrite(java.lang.String)
     */
    public String rewrite(String url) {
        return mock;
    }

}