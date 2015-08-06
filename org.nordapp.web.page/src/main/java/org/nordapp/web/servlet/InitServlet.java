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
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.i3xx.step.zero.service.impl.mandator.MandatorServiceImpl;
import org.i3xx.step.zero.service.model.mandator.Mandator;
import org.nordapp.web.util.RequestPath;
import org.nordapp.web.util.StateUtil;
import org.nordapp.web.util.link.LinkHeader;
import org.nordapp.web.util.link.LinkHeaderImpl;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The InitServlet loads the start page and generates the session id
 * 
 * @author Stefan
 *
 */
public class InitServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(InitServlet.class);
	
	private BundleContext context;
	
	public InitServlet(BundleContext context) {
		super();
		
		this.context = context;
	}
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		
		try{
		
		final String mandatorId = RequestPath.getMandator(req);
		
		String[] elem = RequestPath.getPath(req);
		if(elem.length<3)
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"/id-of-the-mandator/group-id/artifact-id' but has '"+
					req.getRequestURI()+"'");
		
		final String groupId = elem[1];
		final String artifactId = elem[2];
		
		//SessionControl ctrl = new HttpSessionControlImpl(context, req.getSession());
		SessionControl ctrl = new SessionControlImpl(context);
		
		//set mandatorId
		ctrl.setMandatorID(mandatorId);
		
		//set groupId and artifactId
		ctrl.setGroupID(groupId);
		ctrl.setArtifactID(artifactId);
		
		//
		// Create session id
		//
		ctrl.ensureNew(new SessionControl.Factory() {
			public Object create() {
				return req.getSession(true);
			}
		});
		ctrl.generateCert();
		
		//
		// Gets the configured mandator.
		//
		Mandator mandator = MandatorServiceImpl.getMandator(context, mandatorId);
		if(mandator==null)
			throw new IOException("The mandator '"+mandatorId+"' is not available on this system.");
		
		String pwdTo = mandator.getProperty(groupId+"."+artifactId+".password.timeout");
		if(logger.isDebugEnabled()) {
			
			logger.debug("Mandator debug factory-id:{} service-id:{} id:{} root:{} title:{} path:{}", mandator.getFactoryPid(),
					mandator.getServicePid(),
					mandator.getId(),
					mandator.getRoot(),
					mandator.getTitle(),
					mandator.getPath()
					);
			String[] ar = mandator.getPropertyKeys();
			for(int i=0;ar!=null && i<ar.length;i++){
				logger.debug("Mandator property {}='{}'", ar[i], mandator.getProperty(ar[i]));
			}
		}
		Integer pwdTimeout = new Integer(60000);
		if(pwdTo!=null) {
			try {
				pwdTimeout = Integer.valueOf(pwdTo);
			}catch(Exception ee){
				logger.debug("The timeout '{}' has not a number format. Exception: {}", pwdTo, ee.toString());
			}
		}
		
		//
		// Create short time password
		//
		ctrl.setPasswordTimeout(pwdTimeout);
		ctrl.setShortTimePassword();
		ctrl.watchShortTimePassword();
		
		//
		//
		//
		String loginHtm = getInitParameter("startpage");
		String resourcePath = getInitParameter("resource-path");
		
		String dest = RequestPath.replacePath(resourcePath, loginHtm);
		
		if(logger.isDebugEnabled() || logger.isTraceEnabled()) {
			logger.debug("Initialize app mandator: {}, group: {}, artifact: {}, timeout: {}, path: '{}', page: {}.",
					mandatorId, groupId, artifactId, pwdTimeout, mandator.getPath(), dest);
		}else{
			logger.info("Initialize app mandator: {}, group: {}, artifact: {}, timeout: {}", mandatorId, elem[1], elem[2], pwdTimeout);
		}
		
		//
		// Set RESTful states to HTTP links
		//
		
		LinkHeader lhdr = new LinkHeaderImpl();
		try{
			StateUtil.setDefault(lhdr, req);
			
			lhdr.setLink_uuid( null );
			lhdr.add( RequestPath.replacePath("/{}/{}/{}", ctrl.getMandatorID(), ctrl.getGroupID(), ctrl.getArtifactID()), "self");
			
			lhdr.setLink_mandator( ctrl.getMandatorID() );
			lhdr.setLink_uuid( ctrl.getCertID() );
			StateUtil.setState(context, ctrl, lhdr, "init.state");
			
			logger.debug( lhdr.toString() );
			ctrl.setSessionFlags(SessionControl.FLAG_SESSION_IS_STATEFUL, true);
		}catch(IOException io){
			logger.debug("The session is not stateful", io);
			ctrl.setSessionFlags(SessionControl.FLAG_SESSION_IS_STATEFUL, false);
		}
		
		//
		// 
		//
		ctrl.setAll();
		ctrl.saveTempSession();
		
		//
		// Read the resource and write the request
		//
		
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		rsHdl.setLinkHeader(resp, lhdr.toString());
		rsHdl.sendPage(dest, resp);
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
			logger.error("An error occurs.", e);
		}
	}

}
