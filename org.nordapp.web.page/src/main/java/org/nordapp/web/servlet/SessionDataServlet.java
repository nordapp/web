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
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.uno.impl.service.EngineBaseServiceImpl;
import org.i3xx.step.uno.model.daemon.Engine;
import org.i3xx.step.uno.model.service.EngineBaseService;
import org.i3xx.step.zero.service.impl.mandator.MandatorServiceImpl;
import org.i3xx.step.zero.service.model.mandator.Mandator;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets the attributes from the step script engine
 * 
 * @author Stefan
 *
 */
public class SessionDataServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(SessionDataServlet.class);
	
	private BundleContext context;

	public SessionDataServlet(BundleContext context) {
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
		
		@SuppressWarnings("unused")
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
		
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), "0");
		Integer baseIndex = ((Integer)mSession.getValue( Session.ENGINE_BASE_INDEX )).intValue();
		//String sessionId = session.getId();
		
		String[] elem = RequestPath.getPath(req);
		if(elem.length!=0 && elem.length!=1)
			throw new MalformedURLException("The URL needs the (optional) form '"+
					req.getServletPath()+"/rw' but was '"+
					req.getRequestURI()+"'");
		
		BigInteger engineId = ctrl.decodeCert();
		
		byte[] bytes = new byte[0];
		try{
			//
			// Gets the engine base service
			//
			EngineBaseService engineBaseService = 
					EngineBaseServiceImpl.getService(context, ctrl.getMandatorID(), String.valueOf(baseIndex));
			if(engineBaseService==null)
				throw new IOException("The mandator base service is not available (maybe down or a version conflict).");
			
			//
			// Run the step
			//
			Mandator mandator = MandatorServiceImpl.getMandator(context, ctrl.getMandatorID());
			if(mandator==null)
				throw new IOException("The mandator service is not available (maybe down or a version conflict).");
			
			Engine engine = engineBaseService.getEngine(engineId);
			if( !engine.isLogin())
				throw new IllegalAccessException("There is no login to this session.");
			
			try{
				bytes = engine.toJSON(null).getBytes();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}catch(Exception e) {
			logger.error("Error running the step.", e);
		}
		
		//
		// Send the resource
		//
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		rsHdl.avoidCaching(resp);
		rsHdl.sendData(bytes, resp);
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}
}
