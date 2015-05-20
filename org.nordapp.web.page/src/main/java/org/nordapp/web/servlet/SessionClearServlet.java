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
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.uno.impl.service.EngineBaseServiceImpl;
import org.i3xx.step.uno.model.daemon.Engine;
import org.i3xx.step.uno.model.service.EngineBaseService;
import org.nordapp.web.servlet.SessionControl.Action;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionClearServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(SessionClearServlet.class);
	
	private BundleContext context;

	public SessionClearServlet(BundleContext context) {
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
		// Session handler
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
		ctrl.attributes(Action.READ);
		if(ctrl.getAttribute(Session.ENGINE_BASE_INDEX)==null)
			throw new IllegalArgumentException("Runs a logout first, the EngineBaseIndex is not set.");
		
		int index = ((Integer)ctrl.getAttribute(Session.ENGINE_BASE_INDEX)).intValue();
		
		//session key
		BigInteger workSessionId = ctrl.decodeCert();
		if(workSessionId==null)
			throw new UnavailableException("Needs a valid Work-Session.");
		
		try{
			//
			// Gets the engine base service
			//
			EngineBaseService engineBaseService = 
					EngineBaseServiceImpl.getService(context, ctrl.getMandatorID(), String.valueOf(index));
			if(engineBaseService==null)
				throw new IOException("The mandator base service is not available (maybe down or a version conflict).");
			
			Engine engine = engineBaseService.getEngine(workSessionId);
			
			//clear and exit.
			engine.exit();
			
			//drops the engine
			engineBaseService.removeEngine(workSessionId);
			
		}catch(Exception e) {
			logger.error("Error running the step.", e);
		}
		
		//
		//
		//
		String resourcePath = getInitParameter("resource-path");
		String dest = rqHdl.readRequest(req, resourcePath);
		
		//
		// destroys the HTTP session
		//
		HttpSession s = req.getSession(false);
		if(s!=null) {
			s.invalidate();
		}
		
		//
		// Send the resource
		//
		ResponseHandler rHdl = new ResponseHandler(context, ctrl);
		rHdl.avoidCaching(resp);
		rHdl.sendPage(dest, resp);
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}
}
