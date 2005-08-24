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
package org.portletbridge.portlet;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author JMcCrindle
 * @author rickard
 */
public class BridgeFunctions {

    private final URI currentUrl;

    private final RenderRequest request;

    private final RenderResponse response;

    private final String servletName;

    private final PortletBridgeMemento memento;

    private final Pattern scope;

    private final PerPortletMemento perPortletMemento;

    private Pattern urlPattern = Pattern
            .compile("(url\\((?:'|\")?)(.*?)((?:'|\")?\\))");

    private Pattern importPattern = Pattern
            .compile("(@import\\s+[^url](?:'|\")?)(.*?)((?:'|\")|;|\\s+|$)");

    private Pattern windowOpenPattern = Pattern
        .compile("(open\\(')([^']*)(')|(open\\(\")([^\"]*)(\")");

    private final IdGenerator idGenerator;

    public BridgeFunctions(IdGenerator idGenerator, PortletBridgeMemento memento,
            PerPortletMemento perPortletMemento, String servletName,
            URI currentUrl, RenderRequest request, RenderResponse response) {
        this.idGenerator = idGenerator;
        this.memento = memento;
        this.perPortletMemento = perPortletMemento;
        this.servletName = servletName;
        this.currentUrl = currentUrl;
        this.request = request;
        this.response = response;
        this.scope = perPortletMemento.getScope();
    }

    public String link(String link) {
        if (link.startsWith("javascript:")) {
            return script(link);
        } else if (link.equals("#")) {
            return link;
        } else {
            return rewrite(link, true);
        }
    }

    private String rewrite(String link, boolean checkScope) {
        String trim = link.trim();
        URI url = currentUrl.resolve(trim);
        if (url.getScheme().equals("http") || url.getScheme().equals("https")) {
            if (!checkScope || shouldRewrite(url)) {
                BridgeRequest bridgeRequest = memento.createBridgeRequest(
                        response, idGenerator.nextId(), url);
                String name = url.getPath();
                int lastIndex = name.lastIndexOf('/');
                if (lastIndex != -1) {
                    name = name.substring(lastIndex + 1);
                    if (name.equals("") && lastIndex > 0)
                        name = url.getPath().substring(
                                url.getPath().lastIndexOf('/', lastIndex - 1));

                }
                if (name.startsWith("/"))
                    name = name.substring(1);
                name = request.getContextPath() + '/' + servletName + '/'
                        + bridgeRequest.getId() + "/" + name;
                return name;
            } else {
                return url.toString();
            }
        } else {
            return link;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.StyleSheetRewriter#rewrite(java.lang.String)
     */
    public StringBuffer rewriteUrls(StringBuffer css) {
        if (css == null)
            return null;
        Matcher matcher = urlPattern.matcher(css);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String before = matcher.group(1);
            String url = matcher.group(2);
            String after = matcher.group(3);
            matcher.appendReplacement(sb, before + rewrite(url, true) + after);
        }
        matcher.appendTail(sb);
        return sb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.StyleSheetRewriter#rewrite(java.lang.String)
     */
    public StringBuffer rewriteImports(StringBuffer css) {
        if (css == null)
            return null;
        Matcher matcher = importPattern.matcher(css);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String before = matcher.group(1);
            String url = matcher.group(2);
            String after = matcher.groupCount() == 3 ? matcher.group(3) : "";
            matcher.appendReplacement(sb, before + rewrite(url, true) + after);
        }
        matcher.appendTail(sb);
        return sb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.StyleSheetRewriter#rewrite(java.lang.String)
     */
    public String style(String css) {
        return rewriteUrls(rewriteImports(new StringBuffer(css))).toString();
    }

    private boolean shouldRewrite(URI uri) {
        return scope.matcher(uri.toString()).matches();
    }

    public String script(String script) {
        try {
            Matcher matcher = windowOpenPattern.matcher(script);
            String result = "";
            int idx = 0;

            while (matcher.find()) {
                // Check which of the two cases matched
                String url;
                int group = matcher.start(2) == -1 ? 5 : 2;
                result += script.substring(idx, matcher.start(group));
                url = matcher.group(group);
                result += link(url);
                idx = matcher.end(group);
                idx = matcher.end(group);
            }
            result += script.substring(idx);

            return result;
        } catch (Exception e) {
            return script;
        }
    }

    public URI getCurrentUrl() {
        return currentUrl;
    }

    public PortletBridgeMemento getMemento() {
        return memento;
    }

    public PerPortletMemento getPerPortletMemento() {
        return perPortletMemento;
    }

    public RenderRequest getRequest() {
        return request;
    }

    public RenderResponse getResponse() {
        return response;
    }

    public String getServletName() {
        return servletName;
    }

}