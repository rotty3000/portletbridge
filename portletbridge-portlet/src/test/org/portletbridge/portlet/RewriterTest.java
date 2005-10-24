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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class RewriterTest extends TestCase {
    public void testOrs() {
        Pattern urlPattern = Pattern
                .compile("(url\\((?:'|\")?)(.*?)((?:'|\")?\\))|(@import\\s+[^url](?:'|\")?)(.*?)((?:'|\")|;|\\s+|$)");
        Matcher matcher = urlPattern.matcher("" + "@import 'one';\n"
                + "@import(url('two');\n" + "url('three');\n");
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String before = matcher.group(1);
            String url = matcher.group(2);
            String after = matcher.group(3);
            matcher.appendReplacement(sb, before + "MATCHED_URL(" + url + ")"
                    + after);
        }
        matcher.appendTail(sb);
        assertEquals("@import 'MATCHED_URL(one)';\n" +
            "@import(url('MATCHED_URL(two)');\n" + 
            "url('MATCHED_URL(three)');\n", sb.toString());
    }
    public void testImport() {
        Pattern urlPattern = Pattern
        .compile("(@import\\s+[^url](?:'|\")?)(.*?)((?:'|\")|;|\\s+|$)");
        String css = "@import('one');";        
        Matcher matcher = urlPattern.matcher(css);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String before = matcher.group(1);
            String url = matcher.group(2);
            String after = matcher.group(3);
            matcher.appendReplacement(sb, before + "MATCHED_URL(" + url + ")"
                    + after);
        }
        matcher.appendTail(sb);
        assertEquals("@import 'MATCHED_URL(one)' ;", sb.toString());
    }
    
    public void testCssRewriter() {
        Pattern urlPattern = Pattern
        .compile("(?:url\\((?:'|\")?(.*?)(?:'|\")?\\))|(?:@import\\s+[^url](?:'|\")?(.*?)(?:'|\")|;|\\s+|$)");
        String string = "" + "@import 'one';\n"
                + "@import url('two');\n" + "url('three');\n";
        Matcher matcher = urlPattern.matcher(string);
        StringBuffer sb = new StringBuffer();
        matcher.find();
        do {
            int group = extractGroup(matcher);
            if(group > 0) {
                String before = string.substring(matcher.start(), matcher.start(group));
                String url = matcher.group(group);
                String after = string.substring(matcher.end(group), matcher.end());
                System.out.println(before + "[" + url + "]" + after);
                matcher.appendReplacement(sb, before + "MATCHED_URL(" + url + ")" + after);
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        assertEquals("@import 'MATCHED_URL(one)';\n"
                + "@import url('MATCHED_URL(two))';\n"
                + "url('MATCHED_URL(three)');\n", sb.toString());
    }
    
    public void testMatching() {
        String string = "123456";
        Matcher matcher = Pattern.compile("4(5)6|.(.).").matcher(string);
        assertEquals(2, matcher.groupCount());
        matcher.find();
        int matchingGroup = extractGroup(matcher);
        assertEquals(0, matcher.start());
        assertEquals(3, matcher.end());
        assertEquals("123", string.substring(matcher.start(), matcher.end()));
        assertEquals(1, matcher.start(matchingGroup));
        assertEquals(2, matcher.end(matchingGroup));
        matcher.find(matcher.end());
        for(int i = 1; i <= matcher.groupCount(); i++) {
            if(matcher.start(i) > -1) {
                matchingGroup = i;
                break;
            }
        }
        assertEquals(3, matcher.start());
        assertEquals(6, matcher.end());
        assertEquals("456", string.substring(matcher.start(), matcher.end()));
        assertEquals(4, matcher.start(matchingGroup));
        assertEquals(5, matcher.end(matchingGroup));
    }
    
    private int extractGroup(Matcher matcher) {
        int matchingGroup = -1;
        for(int i = 1; i <= matcher.groupCount(); i++) {
            if(matcher.start(i) > -1) {
                matchingGroup = i;
                break;
            }
        }
        return matchingGroup;
    }
}
