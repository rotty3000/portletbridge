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
import java.rmi.dgc.VMID;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import junit.framework.TestCase;

/**
 * @author JMcCrindle
 */
public class RandomTest extends TestCase {

    public void testClash() throws Exception {
        Random random = new Random();
        Set set = new HashSet();
        for(int i = 0; i < 10000; i++) {
            String hex = Integer.toHexString(random.nextInt());
            if(set.contains(hex)) {
                System.out.println(i);
            } else {
                set.add(hex);
            }
        }
    }

    public void testSubstring() throws Exception {
        String blah = "blah";
        assertEquals("ah", blah.substring(2));
    }
    
    public void testUri() throws Exception {
        URI uri = new URI("test");
    }
    
//    public void testVMID() throws Exception {
//        String value = new VMID().toString();
//        String one = value.substring(0, 16);
//        String two = value.substring(17, 24);
//        String three = value.substring(25, 36);
//        String four = value.substring(38, 42);
//        System.out.println(value);
//        System.out.println(one + ":" + two + ":" + three + ":-" + four);
//        StringBuffer result = new StringBuffer();
//        result.append(Base64.encodeBase64(Hex.decodeHex(one.toCharArray())));
//        result.append(Base64.encodeBase64(Hex.decodeHex(two.toCharArray())));
//        result.append(Base64.encodeBase64(Hex.decodeHex(three.toCharArray())));
//        result.append(Base64.encodeBase64(Hex.decodeHex(four.toCharArray())));
//        System.out.println(result);
//    }
    
    public void testURI() throws Exception {
        URI uri = new URI("http://slashdot.org");
        assertEquals("", uri.getPath());
        uri = new URI("http://slashdot.org/");
        assertEquals("/", uri.getPath());
    }
    
    public void testShift() {
        int x = 256233124;
        System.out.println(Integer.toBinaryString(x));
        do {
            System.out.println("x=" + Integer.toBinaryString(x));
            System.out.println("6=" + Integer.toBinaryString(x & 63));
            System.out.println("b=" + table[(x & 63)]);
            x >>>= 6;
        } while(x > 0);
    }
    
    private static final char[] table = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
            'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '*', '-' 
    };
}
