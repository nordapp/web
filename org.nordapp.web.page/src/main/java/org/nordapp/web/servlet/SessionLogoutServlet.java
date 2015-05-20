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

import org.apache.shiro.subject.Subject;
import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.due.service.model.SessionService;
import org.i3xx.step.uno.impl.service.EngineBaseServiceImpl;
import org.i3xx.step.uno.model.daemon.Engine;
import org.i3xx.step.uno.model.service.EngineBaseService;
import org.nordapp.web.servlet.SessionControl.Action;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionLogoutServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(SessionLogoutServlet.class);
	
	private BundleContext context;

	public SessionLogoutServlet(BundleContext context) {
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
		
		String time = String.valueOf( System.currentTimeMillis() );
		
		if( ! ctrl.isValid())
			throw new UnavailableException("Needs a valid User-Session ("+time+").");
		
		// ============================ SECURITY ===========================
		
		//NaSubject subject = NaSecurityUtil.getSubject();
		//NaSession ssession = subject.getSession(false);
		//Subject subject = SecurityUtils.getSubject();
		
		//SessionKey key = new DefaultSessionKey( ctrl.getSecurityID() );
		//org.apache.shiro.session.Session ssession = SecurityUtils.getSecurityManager().getSession(key);
		
		Subject subject = new Subject.Builder().sessionId( ctrl.getSecurityID() ).buildSubject();
		System.out.println("Logout.User: "+ subject.getSession().getAttribute("MyUserName"));
		
		subject.logout();
		
		// ============================ ======== ===========================
		
		//
		// Session service
		//
		String cert = null;
		
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), "0");
		Integer baseIndex = ((Integer)mSession.getValue( Session.ENGINE_BASE_INDEX )).intValue();
		
		ctrl.setAttribute(Session.ENGINE_BASE_INDEX, baseIndex);
		ctrl.attributes(Action.WRITE);
		
		mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.decodeCert().toString());
		if(mSession==null)
			throw new UnavailableException("Needs a valid User-Session mandator-id:"+ctrl.getMandatorID()+
					", session:"+ctrl.decodeCert().toString());
		
		try{
			ServiceReference<SessionService> sesSrvRev = context.getServiceReference(SessionService.class);
			if(sesSrvRev==null)
				throw new IOException("The session service reference is not available (maybe down or a version conflict).");
			
			SessionService sessionService = context.getService(sesSrvRev);
			if(sessionService==null)
				throw new IOException("The session service is not available (maybe down or a version conflict).");
			
			boolean resl = sessionService.destroy(cert, ctrl.getMandatorID(), ctrl.decodeCert().toString());
			logger.info("The Session {} is destroyed (success:{}).", mSession.getSessionId(), resl);
		}catch(Throwable t){
			logger.error("Unable to destroy the session.", t);
		}
		
		//
		// Create a short time password to clear the session
		//
		ctrl.setShortTimePassword();
		ctrl.setAll();
		ctrl.saveTempSession();
		
		//
		// logout engine
		//
		BigInteger engineId = ctrl.decodeCert();

		try{
			//
			// Gets the engine base service
			//
			EngineBaseService engineBaseService = 
					EngineBaseServiceImpl.getService(context, ctrl.getMandatorID(), String.valueOf(baseIndex));
			if(engineBaseService==null)
				throw new IOException("The mandator base service is not available (maybe down or a version conflict).");
			
			Engine engine = engineBaseService.getEngine(engineId);
			engine.setLogin(false);
			
			//saves the content of the engine
			engine.save();
			
			//clear and exit.
			engine.exit();
			
			//saves the state of the engine
			engineBaseService.saveEngine(engineId);
			
			//removes the engine from the engine list only - the resources remain valid
			engineBaseService.dropEngine(engineId);
			
		}catch(Exception e) {
			logger.error("Error running the step.", e);
		}
		
		//
		//
		//
		String resourcePath = getInitParameter("resource-path");
		String dest = rqHdl.readRequest(req, resourcePath);
		
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
