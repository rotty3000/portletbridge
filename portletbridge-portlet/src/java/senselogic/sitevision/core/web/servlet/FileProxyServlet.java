/*
 * Copyright (C) Senselogic 2002, all rights reserved
 */
package senselogic.sitevision.core.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import senselogic.sitevision.portlet.proxy.web.ProxyService;

/**
 * Proxy for links in proxy portlet
 * <p/>
 * Handles URI's of the form:
 * /fileproxy/<fileid>/<URL - protocol>
 * Example:
 * /fileproxy/12345/foo.html
 *
 * @author rickard
 * @version $Revision: 1.1 $
 */
public class FileProxyServlet
  extends HttpServlet
{
  // Attributes ----------------------------------------------------
  

  ProxyService proxy;

  // HttpServlet overrides -----------------------------------------
  public void init()
     throws ServletException
  {
     proxy = ServerServices.getProxyService();
  }

  protected void doGet(HttpServletRequest aHttpServletRequest, HttpServletResponse aHttpServletResponse)
     throws ServletException, IOException
  {
     proxy.doGet(aHttpServletRequest, aHttpServletResponse);
  }

  protected void doPost(HttpServletRequest aHttpServletRequest, HttpServletResponse aHttpServletResponse)
     throws ServletException, IOException
  {
     proxy.doPost(aHttpServletRequest, aHttpServletResponse);
  }


}
