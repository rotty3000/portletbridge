/*
 * Copyright (C) Senselogic 2002, all rights reserved
 */
package senselogic.sitevision.portlet.proxy.web;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import senselogic.sitevision.core.web.servlet.ServerServices;

/**
 * Portlet that implements a reverse proxy.
 * <p/>
 * The basic idea is to use TagSoup to fetch HTML, convert it to XML (as SAX events), run it
 * through an XSLT transformation in order to rewrite the content (links, etc.) and then output
 * it again as HTML. Links to resource are rewritten to go through a servlet.
 *
 * @author Rickard ?berg
 * @version $Revision: 1.1 $
 */
public class ProxyPortlet
  extends GenericSiteVisionPortlet
{
  // Attributes ----------------------------------------------------
  ProxyService proxy;

  // GenericPortlet overrides --------------------------------------
  public void init()
     throws PortletException
  {
     proxy = ServerServices.getProxyService();
  }

  protected void doView(RenderRequest aRenderRequest, RenderResponse aRenderResponse)
     throws PortletException, IOException
  {
     proxy.doView(aRenderRequest, aRenderResponse);
  }
}
