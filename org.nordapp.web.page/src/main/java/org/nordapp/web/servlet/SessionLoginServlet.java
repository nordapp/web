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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;
import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.due.service.model.SessionService;
import org.i3xx.step.uno.impl.service.EngineBaseServiceImpl;
import org.i3xx.step.uno.model.daemon.Engine;
import org.i3xx.step.uno.model.service.EngineBaseService;
import org.i3xx.step.zero.security.impl.shiro.NaUsernamePasswordTokenImpl;
import org.i3xx.step.zero.security.model.NaAuthenticationToken;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionLoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(SessionLoginServlet.class);
	
	private BundleContext context;

	public SessionLoginServlet(BundleContext context) {
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
		
		String time = String.valueOf( System.currentTimeMillis() );
		
		if( ! ctrl.isValid())
			throw new UnavailableException("Needs a valid User-Session ("+time+").");
		
		// ============================ SECURITY ===========================
		
		NaAuthenticationToken token = new NaUsernamePasswordTokenImpl("admin", "ffpbanm");
		
		Subject subject = SecurityUtils.getSubject();
		if(subject==null)
			throw new IllegalStateException("Subject must not be null.");
		
		try{
			if( ! subject.isRemembered() ){
				logger.debug("Login.");
				subject.login( (AuthenticationToken) token.getToken() );
			}
		}catch(UnknownSessionException e){
			logger.info("Unable to login the session.");
			
			org.apache.shiro.session.Session anonSession = subject.getSession(false);
			if(anonSession!=null) {
				try{
					subject.logout();
				}catch(UnknownSessionException ee){
					logger.info("Unable to login the session (2).");
				}
			}
			logger.debug("Login (2).");
			subject.login( (AuthenticationToken) token.getToken() );
		}
		logger.debug("- done -");
		
		org.apache.shiro.session.Session scSession = subject.getSession();
		ctrl.setSecurityID( (String)scSession.getId() );
		
		//ssession.stop();
		
		//NaSubject subject = NaSecurityUtil.getSubject();
		//subject.isRemembered()
		//subject.login(token);
		
		//NaSession ssession = subject.getSession(true);
		scSession.setAttribute("MyUserName", "Stefan");
		scSession.setAttribute(SessionControl.certIDKey, ctrl.getCertID());
		
		// ============================ ======== ===========================
		
		//
		// Session service
		//
		String cert = null;
		String sessionId = ctrl.decodeCert().toString();
		String sessionTimeout = getInitParameter("session-timeout");
		
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), "0");
		Integer baseIndex = ((Integer)mSession.getValue( Session.ENGINE_BASE_INDEX )).intValue();
		
		mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), sessionId);
		if(mSession==null){
			try{
				ServiceReference<SessionService> sesSrvRev = context.getServiceReference(SessionService.class);
				if(sesSrvRev==null)
					throw new IOException("The session service reference is not available (maybe down or a version conflict).");
				
				SessionService sessionService = context.getService(sesSrvRev);
				if(sessionService==null)
					throw new IOException("The session service is not available (maybe down or a version conflict).");
				
				Map<String, String> params = new HashMap<String, String>();
				params.put(SessionControl.userIDKey, ctrl.getUserID());
				long timeout = Long.parseLong(sessionTimeout);
				mSession = sessionService.createSession(cert, ctrl.getMandatorID(), timeout, sessionId, params);
				//The flags whether the session is valid and alive
				ctrl.setAttribute(ResponseHandler.SESSION_IS_ALIVE, new Boolean(mSession.isAlive()));
				ctrl.setAttribute(ResponseHandler.SESSION_IS_VALID, new Boolean(mSession.isValid()));
				
				//
				// Logon
				//
				BigInteger engineId = BigInteger.valueOf( baseIndex );
				BigInteger jsSession = ctrl.decodeCert();
				EngineBaseService baseService = EngineBaseServiceImpl.getService(context, ctrl.getMandatorID(), engineId.toString());
				
				baseService.setupStore(jsSession);
				Engine engine = baseService.getEngine(jsSession);
				engine.setLogin(true);
				
				if( ! engine.hasNext()){
					engine.reinit();
				}
				
				//saves the content of the engine
				engine.save();
				
				//saves the state of the engine
				baseService.saveEngine(jsSession);
				
				//TODO:
				req.getSession().setMaxInactiveInterval(8*60*60*1000);
				
				logger.info("The session {} is created (mandator:{}).", mSession.getSessionId(), ctrl.getMandatorID());
			}catch(Throwable t){
				logger.error("Unable to create the session.", t);
			}
		}else{
			//The flags whether the session is valid and alive
			ctrl.setAttribute(ResponseHandler.SESSION_IS_ALIVE, new Boolean(mSession.isAlive()));
			ctrl.setAttribute(ResponseHandler.SESSION_IS_VALID, new Boolean(mSession.isValid()));
			
			logger.info("The session {} already exits (mandator:{}).", mSession.getSessionId(), ctrl.getMandatorID());
		}
		ctrl.setAll();
		ctrl.saveTempSession();
		
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
