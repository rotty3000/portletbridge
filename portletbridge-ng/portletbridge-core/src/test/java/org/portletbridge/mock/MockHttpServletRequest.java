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
package org.portletbridge.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author JMcCrindle
 */
public class MockHttpServletRequest implements HttpServletRequest {
    
    private HttpSession session = null;
    private URL url = null;
    private String contextPath = null;
    private Hashtable headers = new Hashtable();

    /**
     * 
     */
    public MockHttpServletRequest() {
        super();
    }
    
    public void setupSession(HttpSession session) {
        this.session = session;
    }
    
    public void setupUrl(URL url) {
        this.url = url;
    }
    
    public void setupContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return headers.keys();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    public String getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return contextPath;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return url.getQuery();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        return url.getPath();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer(url.toString());
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean create) {
        return session != null ? session : new MockHttpSession();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        return session;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
