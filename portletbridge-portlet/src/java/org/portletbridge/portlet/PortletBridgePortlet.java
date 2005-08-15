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

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.portletbridge.ResourceException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author JMcCrindle
 */
public class PortletBridgePortlet extends GenericPortlet {

    private Portlet viewPortlet = null;
    private Portlet editPortlet = null;
    private Portlet helpPortlet = null;

    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#init()
     */
    public void init() throws PortletException {
        
        
        TemplateFactory templateFactory = new DefaultTemplateFactory();
        
        // initialise portlets
        viewPortlet = createViewPortlet(templateFactory);
        editPortlet = createEditPortlet(templateFactory);
        helpPortlet = createHelpPortlet(templateFactory);
        
        if(viewPortlet != null) {
            viewPortlet.init(this.getPortletConfig());
        }
        if(editPortlet != null) {
            editPortlet.init(this.getPortletConfig());
        }
        if(helpPortlet != null) {
            helpPortlet.init(this.getPortletConfig());
        }
    }

    protected BridgeEditPortlet createEditPortlet(TemplateFactory templateFactory) throws PortletException {
        PortletConfig config = this.getPortletConfig();
        ResourceBundle resourceBundle = config.getResourceBundle(Locale.getDefault());

        // get the edit stylesheet reference
        String editStylesheet = config.getInitParameter("editStylesheet");
        if (editStylesheet == null) {
            throw new PortletException(resourceBundle
                    .getString("error.edit.stylesheet"));
        }

        BridgeEditPortlet bridgeEditPortlet = new BridgeEditPortlet();
        try {
            bridgeEditPortlet.setTemplates(templateFactory.getTemplates(editStylesheet));
        } catch (ResourceException e) {
            throw new PortletException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new PortletException(e);
        }
        return bridgeEditPortlet;
    }
    
    protected BridgeHelpPortlet createHelpPortlet(TemplateFactory templateFactory) throws PortletException {
        return new BridgeHelpPortlet();
    }
    
    /**
     * @return
     * @throws PortletException
     */
    protected BridgeViewPortlet createViewPortlet(TemplateFactory templateFactory) throws PortletException {
        PortletConfig config = this.getPortletConfig();
        ResourceBundle resourceBundle = config.getResourceBundle(Locale.getDefault());

        // get the memento session key
        String mementoSessionKey = config.getInitParameter("mementoSessionKey");
        if (mementoSessionKey == null) {
            throw new PortletException(resourceBundle
                    .getString("error.mementoSessionKey"));
        }
        // get the servlet name
        String servletName = config.getInitParameter("servletName");
        if (servletName == null) {
            throw new PortletException(resourceBundle
                    .getString("error.servletName"));
        }
        // get parserClassName
        String parserClassName = config.getInitParameter("parserClassName");
        if (parserClassName == null) {
            throw new PortletException(resourceBundle
                    .getString("error.parserClassName"));
        }
        // setup parser
        DefaultBridgeTransformer transformer = null;
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader(parserClassName);
            transformer = new DefaultBridgeTransformer(templateFactory, parser, servletName);
        } catch (SAXNotRecognizedException e) {
            throw new PortletException(e);
        } catch (SAXNotSupportedException e) {
            throw new PortletException(e);
        } catch (SAXException e) {
            throw new PortletException(e);
        }

        BridgeViewPortlet bridgeViewPortlet = new BridgeViewPortlet();
        
        bridgeViewPortlet.setHttpClientTemplate(new DefaultHttpClientTemplate());
        bridgeViewPortlet.setTransformer(transformer);
        bridgeViewPortlet.setMementoSessionKey(mementoSessionKey);
        return bridgeViewPortlet;
    }

    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if(viewPortlet != null) {
            viewPortlet.render(request, response);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doEdit(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if(editPortlet != null) {
            editPortlet.render(request, response);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        PortletMode portletMode = request.getPortletMode();
        if(portletMode.equals(PortletMode.VIEW)) {
            viewPortlet.processAction(request, response);
        } else if(portletMode.equals(PortletMode.EDIT)) {
            editPortlet.processAction(request, response);
        } else if (portletMode.equals(PortletMode.HELP)) {
            helpPortlet.processAction(request, response);
        }
    }
    
    
}
