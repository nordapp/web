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


import java.util.Map;

import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The session control encapsulates the control of the HTTPSession and
 * the Session of StepDue. If necessary, this control can be replaced
 * by something else you need (e.g Apache-Shiro).
 * 
 * @author Administrator
 *
 */
public final class SessionControlImpl extends AbstractSessionControlImpl {
	
	/** The logger */
	static Logger logger = LoggerFactory.getLogger(SessionControlImpl.class);
	
	// The bundle context
	private BundleContext context;
	
	public SessionControlImpl(BundleContext context) {
		super(context);
		this.context = context;
		//Transmits mandatorId and uuId in the URL
		this.pathStartIndex = 2;
	}
	
	/**
	 * Ensure the session is a new session
	 * Does nothing - clear the mandator to get a new session0
	 * 
	 * @return
	 */
	public boolean ensureNew(Factory factory) {
		return false;
	}
	
	/**
	 * Tests whether the session is valid or not.
	 * 
	 * @return
	 */
	public boolean isValid() {
		String cert = null; //TODO
		
		//The mandatorID and the certID must be set.
		if(certID==null)
			return false;
		
		//Test the mandator-0 session
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, "0");
		
		//
		// Maybe a stateful application must verify the session now
		// if no login -> short-time-password, if login -> verify session
		//
		/*
		if(isStateful()) {
			mSession = SessionServiceImpl.getSession(context, cert, mandatorID, decodeCert().toString());
			return ( mSession!=null && mSession.isValid() );
		}
		*/
		
		return (mSession!=null);
	}
	
	/**
	 * Saves the session data to the '0' session.
	 */
	public void saveTempSession() {
		String cert = null; //TODO
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, "0");
		if(mSession==null) {
			logger.warn("The '0' session of the mandator {} is not available;", mandatorID);
			return;
		}
		logger.trace("{}/{} => {}", mandatorID, certID, properties);
		mSession.setValue(certID, properties);
	}
	
	/**
	 * Loads the session data from the '0' session
	 */
	@SuppressWarnings("unchecked")
	public void loadTempSession() {
		String cert = null; //TODO
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, "0");
		if(mSession==null) {
			logger.warn("The '0' session of the mandator {} is not available;", mandatorID);
			return;
		}
		this.properties = (Map<String, Object>)mSession.getValue(certID);
		logger.trace("{}/{} => {}", mandatorID, certID, properties);
	}
	
	/**
	 * Clears the session data from the |0| session
	 */
	public void clearTempSession() {
		String cert = null; //TODO
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, "0");
		if(mSession==null) {
			logger.warn("The '0' session of the mandator {} is not available;", mandatorID);
			return;
		}
		mSession.setValue(certID, null);
	}


}
