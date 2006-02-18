package org.portletbridge.portlet;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.portletbridge.ResourceException;

public class DefaultInitUrlFactory implements InitUrlFactory, Serializable {

	private static final long serialVersionUID = 8646992143972717917L;

	public URI getInitUrl(RenderRequest request) throws ResourceException {
		PortletPreferences preferences = request.getPreferences();
        String initUrlPreference = preferences.getValue("initUrl", null);
        if (initUrlPreference == null || initUrlPreference.trim().length() == 0) {
            throw new ResourceException("error.initurl",
                    "preference not defined");
        }
        try {
            return new URI(initUrlPreference);
        } catch (URISyntaxException e) {
            throw new ResourceException("error.initurl", e.getMessage(), e);
        }
	}

}
