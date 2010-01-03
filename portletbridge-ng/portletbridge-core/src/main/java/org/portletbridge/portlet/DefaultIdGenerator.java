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

import java.util.UUID;


/**
 * Default Id Generator. Uses the {@link java.util.UUID UUID} to generate
 * url friendly id's.
 * 
 * @author jmccrindle
 */
public class DefaultIdGenerator implements IdGenerator {

	
    /**
     * Constructs a new DefaultIdGenerator
     */
    public DefaultIdGenerator() {
    }

    /**
     * {@inheritDoc}
     * 
     * This implementation returns a random UUID as a String without "-"
     * characters.
     * 
     * @see java.util.UUID#randomUUID() randomUUID
     * @see java.util.UUID#toString() toString
     */
    public synchronized String nextId() {
    	UUID uuid = UUID.randomUUID();
    	return uuid.toString().replaceAll("-", "");
    }
    

    /**
     * @return the name of this class
     */
    @Override
    public String toString() {
    	return "DefaultIdGenerator";
    }
}
