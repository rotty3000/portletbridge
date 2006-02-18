package org.portletbridge.portlet;

import java.net.URI;

import javax.portlet.RenderRequest;

import org.portletbridge.ResourceException;

public interface InitUrlFactory {
	URI getInitUrl(RenderRequest request) throws ResourceException;
}
