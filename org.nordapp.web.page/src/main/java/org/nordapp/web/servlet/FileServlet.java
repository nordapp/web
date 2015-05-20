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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(FileServlet.class);
	
	private BundleContext context;

	public FileServlet(BundleContext context) {
		super();
		
		this.context = context;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try{
		
		//@SuppressWarnings("unused")
		final String mandatorId = RequestPath.getMandator(req);
		final String uuid = RequestPath.getSession(req);
		
		//
		// Session handler (HTTP) and session control (OSGi)
		//
		//SessionControl ctrl = new HttpSessionControlImpl(context, req.getSession());
		SessionControl ctrl = new SessionControlImpl(context);
		ctrl.setMandatorID(mandatorId);
		ctrl.setCertID(uuid);
		
		RequestHandler rqHdl = new RequestHandler(context, ctrl);
		
		ctrl.loadTempSession();
		ctrl.getAll();
		ctrl.incRequestCounter();
		ctrl.setAll();
		ctrl.saveTempSession();
		
		//
		// Session service
		//
		String cert = null;
		String time = String.valueOf( System.currentTimeMillis() );
		
		if(logger.isTraceEnabled()) {
			logger.trace("Request page from session {}",ctrl.getCertID());
		}
		
		if( ! ctrl.isValid())
			throw new UnavailableException("Needs a valid User-Session ("+time+").");
		
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.decodeCert().toString());
		if(mSession==null){
			
			if(logger.isTraceEnabled()) {
				logger.trace("The session:{} of the mandator:{} is not found.", ctrl.decodeCert().toString()
						, ctrl.getMandatorID());
			}
			
			List<String> list = ctrl.getShortTimePassword();
			if( ctrl.getCertID()==null || (! list.contains(ctrl.getCertID())) ) {
				
				if(logger.isTraceEnabled()) {
					for(String pwd : list) {
						boolean f = ctrl.getCertID()!=null && ctrl.getCertID().equals(pwd);
						logger.trace("Match pwd:{} to:{} result:{}.", ctrl.getCertID(), pwd, new Boolean(f));
					}//for
				}//fi
				
				throw new UnavailableException("Needs a valid User-Session ("+ctrl.getCertID()+"/"+time+").");
			}
		}
		
		//
		//
		//
		String resourcePath = getInitParameter("resource-path");
		String dest = rqHdl.readRequest(req, resourcePath);
		logger.debug("The client {} requests the page'{}'.", ctrl.getCertID(), dest);
		
		//
		// Send the resource
		//
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		rsHdl.avoidCaching(resp);
		rsHdl.sendFile(dest, resp);
		
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}/* doGET */
}
