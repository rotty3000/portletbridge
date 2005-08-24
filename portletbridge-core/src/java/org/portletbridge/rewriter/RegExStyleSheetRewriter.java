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
package org.portletbridge.rewriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.portletbridge.PortletBridgeException;
import org.portletbridge.StyleSheetRewriter;
import org.portletbridge.UrlRewriter;

/**
 * @author JMcCrindle
 */
public class RegExStyleSheetRewriter implements StyleSheetRewriter {

    private UrlRewriter urlRewriter = null;
    private Pattern urlPattern = Pattern.compile("(url\\((?:'|\")?)(.*?)((?:'|\")?\\))");
    private Pattern importPattern = Pattern.compile("(@import\\s+[^url](?:'|\")?)(.*?)((?:'|\")|;|\\s+|$)");
    
    /**
     * @param urlRewriter
     */
    public RegExStyleSheetRewriter(UrlRewriter urlRewriter) {
        this.urlRewriter = urlRewriter;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.StyleSheetRewriter#rewrite(java.lang.String)
     */
    public StringBuffer rewriteUrls(StringBuffer css) throws PortletBridgeException {
        if(css == null) return null;
        Matcher matcher = urlPattern.matcher(css);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String before = matcher.group(1);
            String url = matcher.group(2);
            String after = matcher.group(3);
            matcher.appendReplacement(sb, before + urlRewriter.rewrite(url) + after);
        }
        matcher.appendTail(sb);
        return sb;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.StyleSheetRewriter#rewrite(java.lang.String)
     */
    public StringBuffer rewriteImports(StringBuffer css) throws PortletBridgeException {
        if(css == null) return null;
        Matcher matcher = importPattern.matcher(css);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String before = matcher.group(1);
            String url = matcher.group(2);
            String after = matcher.groupCount() == 3 ? matcher.group(3) : "";
            matcher.appendReplacement(sb, before + urlRewriter.rewrite(url) + after);
        }
        matcher.appendTail(sb);
        return sb;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.StyleSheetRewriter#rewrite(java.lang.String)
     */
    public String rewrite(String css) throws PortletBridgeException {
        return rewriteUrls(rewriteImports(new StringBuffer(css))).toString();
    }

}
