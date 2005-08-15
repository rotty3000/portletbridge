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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.portletbridge.ResourceException;

/**
 * @author JMcCrindle
 */
public class DefaultTemplateFactory implements TemplateFactory {

    /**
     * Creates compiled templates for a particular stylesheet for performance.
     * 
     * @param stylesheet
     *            the stylesheet to compile
     * @return @throws
     *         ResourceException if the stylesheet could not be found.
     * @throws TransformerFactoryConfigurationError
     *             if there was a problem finding a suitable transformer
     *             factory.
     */
    public Templates getTemplates(String stylesheet)
            throws ResourceException, TransformerFactoryConfigurationError {
        if (stylesheet == null) {
            throw new ResourceException("error.stylesheet");
        }
        Templates result = null;
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            URL resourceUrl = null;
            if (stylesheet.startsWith("classpath:")) {
                String substring = stylesheet.substring(10);
                resourceUrl= this.getClass().getResource(
                        substring);
            } else {
                resourceUrl = new URL(stylesheet);
            }
            if (resourceUrl == null) {
                throw new ResourceException("error.stylesheet.notfound",
                        stylesheet);
            }
            result = factory.newTemplates(new StreamSource(resourceUrl.toExternalForm()));
        } catch (TransformerConfigurationException e) {
            throw new ResourceException("error.transformer", e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new ResourceException("error.stylesheet.url", e.getMessage(),
                    e);
        }
        return result;
    }
}
