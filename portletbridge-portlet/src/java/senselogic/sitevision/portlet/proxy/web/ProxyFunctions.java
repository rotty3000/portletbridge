package senselogic.sitevision.portlet.proxy.web;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import senselogic.sitevision.core.web.servlet.ServerServices;

/**
 * Functions available to XSL templates doing HTML content rewriting.
 *
 * @author Rickard ?berg
 * @version $Revision: 1.1 $
 */
public class ProxyFunctions
{
  // Attributes ----------------------------------------------------
  URI current;
  URI[] proxyScope;
  RenderRequest request;
  RenderResponse response;
  ProxyService proxy;
  Result result;

  // Constructors --------------------------------------------------
  public ProxyFunctions(
     URI currentUrl, URI[] aScope, Result aResult, RenderRequestImpl aRequest, RenderResponse aResponse,
     ProxyService aService)
  {
     current = currentUrl;
     request = aRequest;
     result = aResult;
     response = aResponse;
     proxyScope = aScope;
     proxy = aService;
  }

  // Public --------------------------------------------------------
  public String link(String link)
  {
     if (link.startsWith("javascript:"))
     {
        return script(link);
     } else if (link.equals("#"))
     {
        return link;
     } else
     {
        return rewrite(link, true);
     }
  }

  public String rewrite(String link)
  {
     return rewrite(link, false);
  }

  public String script(String aScript)
  {
     try
     {
        Pattern windowOpen = Pattern.compile("(open\\(')([^']*)(')|(open\\(\")([^\"]*)(\")");
        Matcher matcher = windowOpen.matcher(aScript);
        String result = "";
        int idx = 0;

        while (matcher.find())
        {
           // Check which of the two cases matched
           String url;
           int group = matcher.start(2) == -1 ? 5 : 2;
           result += aScript.substring(idx, matcher.start(group));
           url = matcher.group(group);
           result += link(url);
           idx = matcher.end(group);
           idx = matcher.end(group);
        }
        result += aScript.substring(idx);

        return result;
     } catch (Exception e)
     {
        // LogFactory.getLog(this).warn("Could not rewrite script:" + aScript, e);
        return aScript;
     }
  }

  public String style(String aStyle)
     throws IOException
  {
     StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(aStyle));
     int token;
     while ((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)
     {
        if (token == StreamTokenizer.TT_WORD)
        {
           if (tokenizer.sval.equals("import"))
           {
              tokenizer.nextToken();
              String href = tokenizer.sval;
              String newHref = link(href);
              if (!href.equals(newHref))
                 aStyle = aStyle.replaceAll(href, newHref);
           }
        }
     }

     return aStyle;
  }

  public String url()
  {
     return current.toString();
  }

  public int width()
  {
     return ServerServices.getPortletContextManager().getPortlet().getWidth();
  }

  public int height()
  {
     return ServerServices.getPortletContextManager().getPortlet().getHeight();
  }

  public String rewriteJavaScript(String aScript)
  {
     if (aScript.startsWith("javascript:"))
     {
        return script(aScript);
     } else
     {
        return aScript;
     }
  }

  public String resolve(String href)
  {
     if (href.startsWith("javascript:") || href.startsWith("mailto:"))
        return href;

     try
     {
        return current.resolve(href).toString();
     } catch (IllegalArgumentException e)
     {
        final String newHref = href.trim();
        if (newHref.equals(href))
           throw e;
        return resolve(newHref);
     }
  }

  public Node get(String href)
     throws IOException
  {
//     try
//     {
        String currentUrl = resolve(href);
        //Document doc = new net.sf.saxon.om.DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Document doc = null;
        DOMResult dom = new DOMResult(doc, currentUrl);
        proxy.fetch(currentUrl, dom, request, response);
        return doc.getDocumentElement();
//     } catch (ParserConfigurationException e)
//     {
//        throw (IOException) new IOException().initCause(e);
//     }
  }

  // Private -------------------------------------------------------
  private String rewrite(String link, boolean checkScope)
  {
     try
     {
        URI url = current.resolve(link.trim());
        if (url.getScheme().equals("http") || url.getScheme().equals("https"))
        {
           if (!checkScope || shouldRewrite(url))
           {
              String id = proxy.getUrlId(request, response, url.toString());
              String name = url.getPath();
              int lastIndex = name.lastIndexOf('/');
              if (lastIndex != -1)
              {
                 name = name.substring(lastIndex + 1);
                 if (name.equals("") && lastIndex > 0)
                    name = url.getPath().substring(url.getPath().lastIndexOf('/', lastIndex - 1));

              }
              if (name.startsWith("/"))
                 name = name.substring(1);
              name = "/fileproxy/" + id + "/" + name;
              return name;
           } else
           {
              return url.toString();
           }
        } else
        {
           return link;
        }
     } catch (Exception e)
     {
        boolean fixed = false;
        if (link.indexOf(' ') != -1)
        {
           link = link.replace(' ', '+');
           fixed = true;
        }

        if (link.indexOf('\\') != -1)
        {
           link = link.replace('\\', '/');
           fixed = true;
        }

        if (fixed)
           return rewrite(link, checkScope);

        //LogFactory.getLog(this).warn("Could not rewrite link:" + link, e);
        return link;
     }
  }

  private boolean shouldRewrite(URI uri)
  {
     for (int i = 0; i < proxyScope.length; i++)
     {
        URI proxyUri = proxyScope[i];
        if (uri.getScheme().equals(proxyUri.getScheme()) &&
           uri.getHost().equals(proxyUri.getHost()) &&
           uri.getPath().startsWith(proxyUri.getPath()))
           return true;
     }

     return false;
  }
}
