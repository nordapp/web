package org.nordapp.web.servlet;

/*
 * #%L
 * NordApp OfficeBase :: Web
 * %%
 * Copyright (C) 2014 - 2015 I.D.S. DialogSysteme GmbH
 * %%
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
 * #L%
 */


import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The InitServlet loads the start page and generates the session id
 * 
 * @author Stefan
 *
 */
public class DebugSessionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(DebugSessionServlet.class);
	
	private BundleContext context;
	
	public DebugSessionServlet(BundleContext context) {
		super();
		
		this.context = context;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Enumeration<String> en = null;
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>");
		buffer.append("<body>");
		buffer.append("<head>Test the session</head>");
		buffer.append("<table>");
		
		//----------------------------------------------------------------------------------------------
		
		buffer.append("<tr><td colspan=\"2\" style=\"color:red;text-align:center\">Servlet:</td></tr>");
		buffer.append("<tr><td>Bundle Id</td><td>"+context.getBundle().getBundleId()+"</td></tr>");
		buffer.append("<tr><td>Symbolic Name</td><td>"+context.getBundle().getSymbolicName()+"</td></tr>");
		buffer.append("<tr><td>State</td><td>"+context.getBundle().getState()+"</td></tr>");
		buffer.append("<tr><td>Location</td><td>"+context.getBundle().getLocation()+"</td></tr>");
		buffer.append("<tr><td>Last Modified</td><td>"+context.getBundle().getLastModified()+"</td></tr>");
		buffer.append("<tr><td>Version</td><td>"+context.getBundle().getVersion()+"</td></tr>");
		
		
		buffer.append("<tr><td colspan=\"2\" style=\"color:red;text-align:center\">Request:</td></tr>");
		buffer.append("<tr><td>Auth Type</td><td>"+req.getAuthType()+"</td></tr>");
		buffer.append("<tr><td>Character Encoding</td><td>"+req.getCharacterEncoding()+"</td></tr>");
		buffer.append("<tr><td>Content Length</td><td>"+req.getContentLength()+"</td></tr>");
		buffer.append("<tr><td>Content Type</td><td>"+req.getContentType()+"</td></tr>");
		buffer.append("<tr><td>Context Path</td><td>"+req.getContextPath()+"</td></tr>");
		buffer.append("<tr><td>Local Addr</td><td>"+req.getLocalAddr()+"</td></tr>");
		buffer.append("<tr><td>Local Name</td><td>"+req.getLocalName()+"</td></tr>");
		buffer.append("<tr><td>Local Port</td><td>"+req.getLocalPort()+"</td></tr>");
		buffer.append("<tr><td>Method</td><td>"+req.getMethod()+"</td></tr>");
		buffer.append("<tr><td>Path Info</td><td>"+req.getPathInfo()+"</td></tr>");
		buffer.append("<tr><td>Path Translated</td><td>"+req.getPathTranslated()+"</td></tr>");
		buffer.append("<tr><td>Protocol</td><td>"+req.getProtocol()+"</td></tr>");
		buffer.append("<tr><td>Query String</td><td>"+req.getQueryString()+"</td></tr>");
		buffer.append("<tr><td>Remote Addr</td><td>"+req.getRemoteAddr()+"</td></tr>");
		buffer.append("<tr><td>Remote Host</td><td>"+req.getRemoteHost()+"</td></tr>");
		buffer.append("<tr><td>Remote User</td><td>"+req.getRemoteUser()+"</td></tr>");
		buffer.append("<tr><td>Requested SessionId</td><td>"+req.getRequestedSessionId()+"</td></tr>");
		buffer.append("<tr><td>Request URI</td><td>"+req.getRequestURI()+"</td></tr>");
		buffer.append("<tr><td>Scheme</td><td>"+req.getScheme()+"</td></tr>");
		buffer.append("<tr><td>Server Name</td><td>"+req.getServerName()+"</td></tr>");
		buffer.append("<tr><td>Server Port</td><td>"+req.getServerPort()+"</td></tr>");
		buffer.append("<tr><td>Servlet Path</td><td>"+req.getServletPath()+"</td></tr>");
		buffer.append("<tr><td>Requested SessionId From Cookie</td><td>"+req.isRequestedSessionIdFromCookie()+"</td></tr>");
		buffer.append("<tr><td>Requested SessionId From URL</td><td>"+req.isRequestedSessionIdFromURL()+"</td></tr>");
		buffer.append("<tr><td>Requested SessionId Valid</td><td>"+req.isRequestedSessionIdValid()+"</td></tr>");
		buffer.append("<tr><td>Secure</td><td>"+req.isSecure()+"</td></tr>");
		en = req.getAttributeNames();
		while(en.hasMoreElements()){
			String nm = en.nextElement();
			buffer.append("<tr><td>Attr: "+nm+"</td><td>"+req.getAttribute(nm)+"</td></tr>");
		}
		en = req.getHeaderNames();
		while(en.hasMoreElements()){
			String nm = en.nextElement();
			buffer.append("<tr><td>Header: "+nm+"</td><td>"+req.getHeader(nm)+"</td></tr>");
		}
		en = req.getParameterNames();
		while(en.hasMoreElements()){
			String nm = en.nextElement();
			buffer.append("<tr><td>Param: "+nm+"</td><td>"+req.getParameter(nm)+"</td></tr>");
		}
		
		
		HttpSession hs = req.getSession();
		buffer.append("<tr><td colspan=\"2\" style=\"color:red;text-align:center\">Session:</td></tr>");
		buffer.append("<tr><td>Id</td><td>"+hs.getId()+"</td></tr>");
		buffer.append("<tr><td>IsNew</td><td>"+hs.isNew()+"</td></tr>");
		buffer.append("<tr><td>Created since epoch (ms)</td><td>"+hs.getCreationTime()+"</td></tr>");
		buffer.append("<tr><td>Accessed since epoch (ms)</td><td>"+hs.getLastAccessedTime()+"</td></tr>");
		buffer.append("<tr><td>Max inactivate interval (ms)</td><td>"+hs.getMaxInactiveInterval()+"</td></tr>");
		en = hs.getAttributeNames();
		while(en.hasMoreElements()){
			String nm = en.nextElement();
			buffer.append("<tr><td>Attr: "+nm+"</td><td>"+hs.getAttribute(nm)+"</td></tr>");
		}
		
		//@SuppressWarnings("unused")
		final String mandatorId = RequestPath.getMandator(req);
		final String uuid = RequestPath.getSession(req);
		
		//SessionControl ctrl = new HttpSessionControlImpl(context, req.getSession());
		SessionControl ctrl = new SessionControlImpl(context);
		ctrl.setMandatorID(mandatorId);
		ctrl.setCertID(uuid);
		
		ctrl.loadTempSession();
		ctrl.getAll();
		buffer.append("<tr><td colspan=\"2\" style=\"color:red;text-align:center\">Control:</td></tr>");
		buffer.append("<tr><td>ArtifactID</td><td>"+ctrl.getArtifactID()+"</td></tr>");
		buffer.append("<tr><td>CertID</td><td>"+ctrl.getCertID()+"</td></tr>");
		buffer.append("<tr><td>GroupID</td><td>"+ctrl.getGroupID()+"</td></tr>");
		buffer.append("<tr><td>MandatorID</td><td>"+ctrl.getMandatorID()+"</td></tr>");
		buffer.append("<tr><td>UserID</td><td>"+ctrl.getUserID()+"</td></tr>");
		buffer.append("<tr><td>VisitCount</td><td>"+ctrl.getVisitCount()+"</td></tr>");
		buffer.append("<tr><td>Is stateful</td><td>"+ctrl.isStateful()+"</td></tr>");
		buffer.append("<tr><td>Is valid</td><td>"+ctrl.isValid()+"</td></tr>");
		for(String nm : ctrl.getAttributeNames()){
			buffer.append("<tr><td>Attr: "+nm+"</td><td>"+ctrl.getAttribute(nm)+"</td></tr>");
		}
		List<String> arr = ctrl.getShortTimePassword();
		for(int i=0;arr!=null && i<arr.size();i++){
			buffer.append("<tr><td>Shorttime pwd:</td><td>"+arr.get(i)+"</td></tr>");
		}
		
		//----------------------------------------------------------------------------------------------
		
		buffer.append("</table>");
		buffer.append("</body>");
		buffer.append("</html>");
		
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		rsHdl.avoidCaching(resp);
		rsHdl.sendData(buffer.toString().getBytes(), resp);
	}

}
