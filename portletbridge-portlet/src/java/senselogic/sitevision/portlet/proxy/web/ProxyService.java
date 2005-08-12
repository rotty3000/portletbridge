/*
 * Copyright (C) Senselogic 2002, all rights reserved
 */
package senselogic.sitevision.portlet.proxy.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.portletbridge.portlet.ResourceUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

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
public class ProxyService
{
  // Attributes ----------------------------------------------------
  HttpClient client;

  Stack parsers = new Stack();
  Templates defaultTemplate;

  Map templateCache = new HashMap();
  Map xslCache = new IdentityHashMap();

  Set requestHeaderSkip = new HashSet();
  Log log;

  VersionManager vm;

  // Constructors --------------------------------------------------
  public ProxyService()
  {
     //log = LogFactory.getLog(this);

     // Set of headers which should not be copied to proxied requests
     requestHeaderSkip.add("cookie");
     requestHeaderSkip.add("content-type");
     requestHeaderSkip.add("content-length");

     // Load default transformation template
     try
     {
        Source templateSource = new StreamSource(getClass().getResource("default.xsl").toExternalForm());
        defaultTemplate = TransformerFactory.newInstance().newTemplates(templateSource);
     } catch (TransformerConfigurationException e)
     {
        log.error("Could not load default template", e);
        throw new IllegalStateException("Could not start proxy service:" + e.getMessage());
     }
  }

  // Public --------------------------------------------------------
  public void setVersionManager(VersionManager aVm)
  {
     vm = aVm;
  }

  // Z implementation ----------------------------------------------
  public void doPost(HttpServletRequest aHttpServletRequest, HttpServletResponse aHttpServletResponse)
     throws ServletException, IOException
  {

     String requestPath = URLDecoder.decode(aHttpServletRequest.getRequestURI(), "UTF-8");
     String[] urls = requestPath.split("/", 4);
     String urlId = urls[2];
     String urlString = (String) getId2Url(aHttpServletRequest.getSession()).get(urlId);

     // URL not found in cache - might have been invalidated
     if (urlString == null)
     {
        aHttpServletResponse.sendError(404);
        return;
     }

     String[] urlIdStrings = urlString.split(",");
     String portletId = urlIdStrings[1];
     String pageUrl = urlIdStrings[0];
     String url = urlIdStrings[2];
     try
     {
        URI uri = new URI(url);
        uri = uri.normalize();
        url = uri.toASCIIString();
     } catch (URISyntaxException e)
     {
        // Ignore
     }

     HttpClient client = getClient(aHttpServletRequest);
     PostMethod post = new PostMethod(url);

     // Copy headers to request
     copyRequestHeaders(aHttpServletRequest, post);

     // Copy input stream to output
     post.setRequestEntity(new InputStreamRequestEntity(aHttpServletRequest.getInputStream()));

     try
     {
        int responseCode = client.executeMethod(post);

        if (responseCode == HttpStatus.SC_OK)
        {
           Header responseHeader = post.getResponseHeader("Content-Type");
           if (responseHeader != null && responseHeader.getValue().startsWith("text/html"))
           {
              // Fetch content for portlet rendering
              InputStream stream = post.getResponseBodyAsStream();
              String content = ResourceUtil.getString(stream, post.getResponseCharSet());
              stream.close();
              setContent(aHttpServletRequest, portletId, content, post.getURI().toString());

              // Redirect back to page
              aHttpServletResponse.sendRedirect(pageUrl);
           } else
           {
              // Return content as-is

              // Copy response headers
              copyResponseHeaders(post, aHttpServletResponse);

              // Copy data
              InputStream in = post.getResponseBodyAsStream();
              if (in != null)
              {
                 OutputStream out = aHttpServletResponse.getOutputStream();
                 ResourceUtil.copy(in, out, 4096);
              }
           }
        } else if (responseCode == HttpStatus.SC_MOVED_TEMPORARILY)
        {
           String redirectUrl = post.getResponseHeader("Location").getValue();

           fetch(aHttpServletRequest, redirectUrl, portletId, aHttpServletResponse, pageUrl, urlId);

        } else
        {
           aHttpServletResponse.sendError(responseCode);
        }
     } catch (Exception e)
     {
        //LogFactory.getLog(this).error("Could not fetch URL:" + url, e);
        aHttpServletResponse.sendError(500);
     } finally
     {
        post.releaseConnection();
     }
  }

  public void doGet(HttpServletRequest aHttpServletRequest, HttpServletResponse aHttpServletResponse)
     throws ServletException, IOException
  {

     String requestPath = URLDecoder.decode(aHttpServletRequest.getRequestURI(), "UTF-8");
     String[] urls = requestPath.split("/", 4);
     String urlId = urls[2];
     String urlString = (String) getId2Url(aHttpServletRequest.getSession()).get(urlId);

     // URL not found in cache - might have been invalidated
     if (urlString == null)
     {
        aHttpServletResponse.sendError(404);
        return;
     }

     String[] urlIdStrings = urlString.split(",");
     String portletId = urlIdStrings[1];
     String pageUrl = urlIdStrings[0];
     String url = urlIdStrings[2];
     try
     {
        String query = aHttpServletRequest.getQueryString();
        if (query != null)
        {
           StringTokenizer tokenizer = new StringTokenizer(query, "&=", true);
           String newQuery = "";
           while (tokenizer.hasMoreElements())
           {
              String token = (String) tokenizer.nextElement();
              if (token.equals("&") || token.equals("="))
                 newQuery += token;
              else
                 newQuery += URLEncoder.encode(URLDecoder.decode(token, "UTF-8"), "ISO-8859-1");
           }

           url += "?" + newQuery;
        }
        URI uri = new URI(url);

        url = uri.toString();
     } catch (URISyntaxException e)
     {
        // Ignore
     }

     fetch(aHttpServletRequest, url, portletId, aHttpServletResponse, pageUrl, urlId);
  }

  private void fetch(
     HttpServletRequest aHttpServletRequest, String aUrl, String aPortletId, HttpServletResponse aHttpServletResponse,
     String aPageUrl, String aUrlId)
     throws IOException
  {
     HttpClient client = getClient(aHttpServletRequest);
     GetMethod get = new GetMethod();
     org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(aUrl, false);
     get.setURI(uri);
     get.setFollowRedirects(true);


     // Copy headers to request
     copyRequestHeaders(aHttpServletRequest, get);

     try
     {
        int responseCode = client.executeMethod(get);

        if (responseCode == HttpStatus.SC_OK)
        {
           Header responseHeader = get.getResponseHeader("Content-Type");
           if (responseHeader != null && responseHeader.getValue().startsWith("text/html"))
           {
              // Fetch content for portlet rendering
              InputStream stream = get.getResponseBodyAsStream();
              String content = ResourceUtil.getString(stream, get.getResponseCharSet());
              stream.close();
              setContent(aHttpServletRequest, aPortletId, content, get.getURI().toString());

              // Redirect back to page
              aHttpServletResponse.setHeader("Location", aPageUrl + "&id=" + aUrlId);
              aHttpServletResponse.sendError(HttpServletResponse.SC_MOVED_TEMPORARILY);
           } else
           {
              // Return content as-is

              // Copy response headers
              copyResponseHeaders(get, aHttpServletResponse);

              // Copy data
              InputStream in = get.getResponseBodyAsStream();
              if (in != null)
              {
                 OutputStream out = aHttpServletResponse.getOutputStream();
                 ResourceUtil.copy(in, out, 4096);
              }
           }
        } else
        {
           copyResponseHeaders(get, aHttpServletResponse);
           aHttpServletResponse.sendError(responseCode);
        }
     } catch (Exception e)
     {
        //LogFactory.getLog(this).error("Could not fetch URL:" + aUrl, e);
        aHttpServletResponse.sendError(500);
     } finally
     {
        get.releaseConnection();
     }
  }

  private void copyResponseHeaders(HttpMethodBase aGet, HttpServletResponse aHttpServletResponse)
  {
     Header[] headers = aGet.getResponseHeaders();
     for (int i = 0; i < headers.length; i++)
     {
        Header header = headers[i];
        aHttpServletResponse.setHeader(header.getName(), header.getValue());
     }
  }

  public void doView(RenderRequest aRenderRequest, RenderResponse aRenderResponse)
     throws PortletException, IOException
  {
     // Get scope of request
     URI baseUrl = null;
     URI[] scopeUrls;
     PortletSession portletSession = aRenderRequest.getPortletSession();
     try
     {
        String burl = aRenderRequest.getPreferences().getValue("url", null);
        if (burl == null || burl.equals("")) return;
        baseUrl = new URI(burl);
        List scope = (List) ((ObjectPortletPreferences) aRenderRequest.getPreferences()).getObject("scope", null);
        if (scope == null)
        {
           scopeUrls = new URI[0];
        } else
        {
           scopeUrls = new URI[scope.size()];
           for (int i = 0; i < scope.size(); i++)
           {
              String url = (String) scope.get(i);
              scopeUrls[i] = new URI(url);
           }
        }
     } catch (URISyntaxException e)
     {
        throw new PortletException(e);
     }

     String currentUrl = "";
     try
     {
        long time = -1;

        long start = System.currentTimeMillis();

        StreamResult result;
        StringWriter writer = null;
        if (vm.getCurrentVersion() == Versionable.DEFAULT)
        {
           writer = new StringWriter();
           result = new StreamResult(writer);

        } else
        {
           result = new StreamResult(aRenderResponse.getWriter());
        }

        String content = (String) portletSession.getAttribute(
           aRenderResponse.getNamespace() + "content", PortletSession.APPLICATION_SCOPE);
        currentUrl = (String) portletSession.getAttribute(
           aRenderResponse.getNamespace() + "contentUrl", PortletSession.APPLICATION_SCOPE);
        InputSource inputSource;
        if (content == null)
        {
           String urlId = aRenderRequest.getParameter("id");
           if (urlId != null)
           {
              Map idMap = getId2Url(aRenderRequest.getPortletSession());
              String urlString = (String) idMap.get(urlId);
              if (urlString != null)
              {
                 String[] urlIdStrings = urlString.split(",");
                 currentUrl = urlIdStrings[2];
              } else
              {
                 currentUrl = baseUrl.toString();
              }
           } else
           {
              currentUrl = baseUrl.toString();
           }

           fetch(currentUrl, result, aRenderRequest, aRenderResponse);

        } else
        {
           inputSource = new InputSource(new StringReader(content));
           transform(currentUrl, inputSource, result, aRenderRequest, aRenderResponse);

           portletSession.removeAttribute(
              aRenderResponse.getNamespace() + "content", PortletSession.APPLICATION_SCOPE);
           portletSession.removeAttribute(
              aRenderResponse.getNamespace() + "contentUrl", PortletSession.APPLICATION_SCOPE);
        }

        long end = System.currentTimeMillis();
        time = end - start;

        if (writer != null)
        {
           PrintWriter out = aRenderResponse.getWriter();
           final String output = writer.toString();
           out.print(output);
           if (time != -1)
              out.print("<div style='solid thin black'>URL:" + baseUrl + " Time:" + time + "ms</div>");
           String encoded = TextUtils.htmlEncode(output);
           encoded = encoded.replaceAll("\\n", "<br>");
           out.print("<pre style='solid thin black'>" + encoded + "</pre>");
        }
     } catch (ConnectException e)
     {
        aRenderResponse.getWriter().print(
           new i18nUtil(this, aRenderRequest.getLocale()).format("unknownhost", currentUrl));

        portletSession.removeAttribute(aRenderResponse.getNamespace() + "content", PortletSession.APPLICATION_SCOPE);
        portletSession.removeAttribute(
           aRenderResponse.getNamespace() + "contentUrl", PortletSession.APPLICATION_SCOPE);
     } catch (UnknownHostException e)
     {
        aRenderResponse.getWriter().print(
           new i18nUtil(this, aRenderRequest.getLocale()).format("unknownhost", e.getMessage()));

        portletSession.removeAttribute(aRenderResponse.getNamespace() + "content", PortletSession.APPLICATION_SCOPE);
        portletSession.removeAttribute(
           aRenderResponse.getNamespace() + "contentUrl", PortletSession.APPLICATION_SCOPE);
     } catch (Exception e)
     {
        portletSession.removeAttribute(aRenderResponse.getNamespace() + "content", PortletSession.APPLICATION_SCOPE);
        portletSession.removeAttribute(
           aRenderResponse.getNamespace() + "contentUrl", PortletSession.APPLICATION_SCOPE);
        throw new PortletException(e);
     }
  }

  public void fetch(String currentUrl, Result aResult, RenderRequest aRenderRequest, RenderResponse aRenderResponse)
     throws UnknownHostException, ConnectException, IOException
  {
     // Get content
     GetMethod get = new GetMethod(currentUrl);

     // Transfer properties to request
     Enumeration properties = aRenderRequest.getPropertyNames();
     while (properties.hasMoreElements())
     {
        String propertyName = (String) properties.nextElement();
        if (requestHeaderSkip.contains(propertyName))
           continue;
        Enumeration values = aRenderRequest.getProperties(propertyName);
        while (values.hasMoreElements())
        {
           String property = (String) values.nextElement();
           get.setRequestHeader(propertyName, property);
        }
     }

     try
     {
        // Handle content and errors
        long start = System.currentTimeMillis();
        HttpClient httpClient = getClient(aRenderRequest);
        int responseCode = httpClient.executeMethod(get);
        long end = System.currentTimeMillis();
        long time = end - start;
        currentUrl = get.getURI().toString();
        InputSource inputSource;
        if (responseCode != HttpStatus.SC_OK)
        {
           String response = get.getResponseBodyAsString();
           //LogFactory.getLog(this).warn(
           //   "Proxy portlet received response " + responseCode + " for URL " + currentUrl + ":" + response);
           inputSource = new InputSource(new StringReader("<html><body>Error " + responseCode + "</body></html"));
        } else
        {
           inputSource = new InputSource(
              new InputStreamReader(get.getResponseBodyAsStream(), get.getResponseCharSet()));
        }

        try
        {
           transform(currentUrl, inputSource, aResult, aRenderRequest, aRenderResponse);
        } catch (TransformerException e)
        {
           throw (IOException) new IOException("Could not transform " + currentUrl).initCause(e);
        } catch (SAXException e)
        {
           throw (IOException) new IOException("Could not transform " + currentUrl).initCause(e);
        }
     } finally
     {
        get.releaseConnection();
     }
  }

  public void transform(
     String aCurrentUrl, InputSource aSource, Result aResult, RenderRequest aRenderRequest,
     RenderResponse aRenderResponse)
     throws IOException, TransformerException, SAXException
  {
     XMLReader parser = null; // getParser();

     // Convert HTML to XML (SAX events)
     SAXSource source = new SAXSource(parser, aSource);

     String xsl = aRenderRequest.getPreferences().getValue("xsl", null);

     Templates proxyTemplate;

     try
     {
        if (xsl == null)
           proxyTemplate = defaultTemplate; // Use default template
        else
        {
           String namespace = aRenderResponse.getNamespace();
           proxyTemplate = getTemplate(namespace, xsl);
        }

        Transformer transformer = proxyTemplate.newTransformer();

        if (aResult instanceof DOMResult)
           transformer = TransformerFactory.newInstance().newTransformer();

        ProxyFunctions functions = null;
        try
        {
           URI[] scopeUrls;
           List scope = (List) ((ObjectPortletPreferences) aRenderRequest.getPreferences()).getObject("scope", null);
           if (scope == null)
           {
              scopeUrls = new URI[0];
           } else
           {
              scopeUrls = new URI[scope.size()];
              for (int i = 0; i < scope.size(); i++)
              {
                 String url = (String) scope.get(i);
                 scopeUrls[i] = new URI(url);
              }
           }

           functions = new ProxyFunctions(
              new URI(aCurrentUrl), scopeUrls, aResult, ((RenderRequestImpl) aRenderRequest), aRenderResponse, this);
        } catch (URISyntaxException e)
        {
           throw (IOException) new IOException("Could not parse URI's").initCause(e);
        }

        transform(functions, transformer, source, aResult);
     } catch (TransformerException e)
     {
        String msg = "<p><b>Error in transformation</b> <br />What:<i>" + TextUtils.htmlEncode(e.getMessage()) + "</i>";
        if (e.getLocator() != null)
           msg += "<br />Where: Line " + e.getLocator().getLineNumber() + ", Column " + e.getLocator().getColumnNumber();
        msg += "</p>";
        StreamSource errorSource = new StreamSource(new StringReader(msg));
        TransformerFactory.newInstance().newTransformer().transform(errorSource, aResult);
     }
  }

  public String getUrlId(RenderRequest aRequest, RenderResponse aResponse, String aUrl)
  {
     String url = aResponse.createRenderURL() + "," + aResponse.getNamespace() + "," + aUrl;
     PortletSession portletSession = aRequest.getPortletSession();
     Map url2id = getUrl2Id(portletSession);
     String id = (String) url2id.get(url);
     if (id == null)
     {
        Random rnd = (Random) portletSession.getAttribute("proxy.rnd", PortletSession.APPLICATION_SCOPE);
        if (rnd == null)
        {
           rnd = new Random();
           portletSession.setAttribute("proxy.rnd", rnd, PortletSession.APPLICATION_SCOPE);
        }

        id = Integer.toHexString(rnd.nextInt());

        Map id2url = getId2Url(portletSession);

        url2id.put(url, id);
        id2url.put(id, url);
     }

     return id;
  }

  public Map getUrl2Id(PortletSession aSession)
  {
     Map url2id = (Map) aSession.getAttribute("proxy.url2id", PortletSession.APPLICATION_SCOPE);
     if (url2id == null)
     {
        url2id = new HashMap();
        aSession.setAttribute("proxy.url2id", url2id, PortletSession.APPLICATION_SCOPE);
     }
     return url2id;
  }

  public Map getId2Url(PortletSession aSession)
  {
     Map id2url = (Map) aSession.getAttribute("proxy.id2url", PortletSession.APPLICATION_SCOPE);
     if (id2url == null)
     {
        id2url = new HashMap();
        aSession.setAttribute("proxy.id2url", id2url, PortletSession.APPLICATION_SCOPE);
     }
     return id2url;
  }

  public Map getId2Url(HttpSession aSession)
  {
     Map id2url = (Map) aSession.getAttribute("proxy.id2url");
     if (id2url == null)
     {
        id2url = new HashMap();
        aSession.setAttribute("proxy.id2url", id2url);
     }
     return id2url;
  }

  private Templates getTemplate(String aNamespace, String aXsl)
     throws TransformerConfigurationException, TransformerException
  {
     final TransformerException[] exceptionHolder = new TransformerException[1];
     Templates proxyTemplate;
     proxyTemplate = (Templates) templateCache.get(aNamespace);
     if (proxyTemplate == null || aXsl != xslCache.get(proxyTemplate))
     {
        // Parse custom template
        Source templateSource = new StreamSource(
           new StringReader(aXsl), getClass().getResource("default.xsl").toExternalForm());
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setErrorListener(
           new ErrorListener()
           {
              public void error(TransformerException exception)
                 throws TransformerException
              {
                 exceptionHolder[0] = exception;
              }

              public void fatalError(TransformerException exception)
                 throws TransformerException
              {
                 exceptionHolder[0] = exception;
              }

              public void warning(TransformerException exception)
                 throws TransformerException
              {
                 exceptionHolder[0] = exception;
              }
           });
        try
        {
           proxyTemplate = transformerFactory.newTemplates(templateSource);
        } catch (TransformerConfigurationException e)
        {
           if (exceptionHolder[0] == null)
              exceptionHolder[0] = e;
        }

        if (exceptionHolder[0] != null)
           throw exceptionHolder[0];

        proxyTemplate.getOutputProperties().setProperty("indent", "yes");
        xslCache.put(proxyTemplate, aXsl);
        templateCache.put(aNamespace, proxyTemplate);
     }
     return proxyTemplate;
  }

  private void setContent(HttpServletRequest aRequest, String aPortletId, String aContent, String aUrl)
  {
     HttpSession session = aRequest.getSession();
     session.setAttribute(aPortletId + "content", aContent);
     session.setAttribute(aPortletId + "contentUrl", aUrl);
  }

  private void transform(ProxyFunctions functions, Transformer transformer, Source aSource, Result aResult)
     throws TransformerException
  {
     transformer.setParameter("proxy", functions);
     transformer.transform(aSource, aResult);
  }

  private XMLReader getParser()
     throws SAXNotRecognizedException, SAXNotSupportedException
  {
     try
     {
        return (XMLReader) parsers.pop();
     } catch (EmptyStackException e)
     {
        XMLReader parser = null; // new Parser();
//        parser.setProperty(
//           "http://www.ccil.org/~cowan/tagsoup/properties/schema", new HTMLSchema()
//           {
//              public String getURI()
//              {
//                 return "";
//              }
//           });
//        parser.setFeature(Parser.namespacesFeature, false);
//        parser.setFeature(Parser.namespacePrefixesFeature, false);
        return parser;
     }
  }

//  private void returnParser(Parser aParser)
//  {
//     parsers.push(aParser);
//  }

  private HttpState getState(PortletRequest aRequest)
  {
     PortletSession portletSession = aRequest.getPortletSession();
     HttpState state = (HttpState) portletSession.getAttribute("proxystate");
     if (state == null)
     {
        state = new HttpState();
//         state.setCredentials();
        portletSession.setAttribute("proxystate", state);
     }
     return state;
  }

  private HttpClient getClient(PortletRequest aRequest)
  {
     final PortletSession session = aRequest.getPortletSession();
     HttpClient client = (HttpClient) session.getAttribute("proxy.client", PortletSession.APPLICATION_SCOPE);
     if (client == null)
     {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connectionManager);
        session.setAttribute("proxy.client", client, PortletSession.APPLICATION_SCOPE);

        try
        {
//           User user = UserUtil.getUser(aRequest);
//           String password = (String) UserUtil.getSubject().getPrivateCredentials(String.class).iterator().next();

//           String name = user.getName().toLowerCase();
//           client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(name, password));
        } catch (Exception e)
        {
           // Ignore
        }
     }

     return client;
  }

  private HttpClient getClient(HttpServletRequest aRequest)
  {
     HttpSession session = aRequest.getSession();
     HttpClient client = (HttpClient) session.getAttribute("proxy.client");
     if (client == null)
     {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connectionManager);
        session.setAttribute("proxy.client", client);

        try
        {
//           User user = UserUtil.getUser(aRequest);
//           String password = (String) UserUtil.getSubject().getPrivateCredentials(String.class).iterator().next();
//
//           client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user.getName(), password));
        } catch (Exception e)
        {
           // Ignore
        }
     }

     return client;
  }

  private void copyRequestHeaders(HttpServletRequest aHttpServletRequest, HttpMethodBase aMethod)
  {
     Enumeration properties = aHttpServletRequest.getHeaderNames();
     while (properties.hasMoreElements())
     {
        String propertyName = (String) properties.nextElement();
        Enumeration values = aHttpServletRequest.getHeaders(propertyName);
        while (values.hasMoreElements())
        {
           String property = (String) values.nextElement();
           aMethod.setRequestHeader(propertyName, property);
        }
     }

     // Conditional cookie transfer
     try
     {
        if (aMethod.getURI().getHost().equals(aHttpServletRequest.getHeader("host")))
        {
           String cookie = aHttpServletRequest.getHeader("cookie");
           if (cookie != null)
              aMethod.setRequestHeader("cookie", cookie);
        }
     } catch (URIException e)
     {
//        LogFactory.getLog(this).warn("Could not transfer cookie", e);
     }
  }
}
