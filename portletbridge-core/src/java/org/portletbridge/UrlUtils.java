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
package org.portletbridge;

/**
 * @author JMcCrindle
 */
public class UrlUtils {

    public static String getPath(String url) {
        if(url == null) return null;
        url = url.trim();
        int i = url.lastIndexOf('/');
        if(i >= 0) {
            return url.substring(0, i);
        } else {
            return url;
        }
    }
    
    public static boolean isRelativeHttp(String url) {
        if(url == null || url.length() == 0) return true;
        return !isFullHttp(url) && url.charAt(0) != '/'; 
    }
    
    public static boolean isAbsoluteHttp(String url) {
        return !isAbsoluteWithHostHttp(url) && url.charAt(0) == '/'; 
    }
    
    public static boolean isAbsoluteWithHostHttp(String url) {
        return url.length() > 1 && url.charAt(0) == '/' && url.charAt(1) == '/'; 
    }
    
    public static boolean isFull(String url) {
        int indexOfFirstSlash = url.indexOf('/');
        int indexOfColon = url.indexOf(':');
        if(indexOfColon >= 0 && indexOfFirstSlash >= 0 && indexOfColon < indexOfFirstSlash) {
            return true;
        }
        return false;
    }

    public static boolean isFullHttp(String url) {
        int indexOfFirstSlash = url.indexOf('/');
        int indexOfColon = url.indexOf(':');
        if(indexOfColon >= 0 && indexOfFirstSlash >= 0 && indexOfColon < indexOfFirstSlash) {
            return url.startsWith("http");
        }
        return false;
    }

    public static String safeAppend(String path, String resource) {
        if(resource == null || resource.length() == 0) resource = "/";
        if(path == null || path.length() == 0) path = "/";
        if(isFull(resource)) {
            return resource;
        }
        boolean pathEndsWithSlash = path.charAt(path.length() - 1) == '/';
        boolean resourceStartsWithSlash = resource.charAt(0) == '/';
        return (pathEndsWithSlash ? path.substring(0, path.length() - 1) : path)
            + "/" + (resourceStartsWithSlash ? resource.substring(1) : resource);
    }

    public static String rewriteRelative(String baseUrl, String resource) {
        if(resource == null) resource = "";
        resource = resource.trim();
        if(resource.length() > 0 && resource.charAt(0) == '/') {
            return resource;
        } else {
            return safeAppend(baseUrl, resource);
        }
    }

    /**
     * @param currentUrl
     * @return
     */
    public static String getProtocolHostPort(String currentUrl) {
        if(isFullHttp(currentUrl)) {
            int indexOfThirdSlash = currentUrl.indexOf('/');
            if(indexOfThirdSlash != -1) {
                indexOfThirdSlash = currentUrl.indexOf('/', indexOfThirdSlash + 2);
                if(indexOfThirdSlash == -1) {
                    return currentUrl;
                } else {
                    return currentUrl.substring(0, indexOfThirdSlash);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param currentUrl
     * @return
     */
    public static String getProtocol(String currentUrl) {
        int indexOfColon = currentUrl.indexOf(':');
        if(indexOfColon != -1) {
            return currentUrl.substring(0, indexOfColon);
        } else {
            return null;
        }
    }

}
