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


import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpSession;

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
public class HttpSessionControlImpl extends AbstractSessionControlImpl {
	
	/** The logger */
	static Logger logger = LoggerFactory.getLogger(HttpSessionControlImpl.class);
	
	// The bundle context
	//private BundleContext context;
	
	// The HttpSession
	private HttpSession session;
	
	public HttpSessionControlImpl(BundleContext context, HttpSession session) {
		super(context);
		//this.context = context;
		this.session = session;
	}
	
	/**
	 * Ensure the session is a new session
	 * 
	 * @return
	 */
	public boolean ensureNew(Factory factory) {
		if( session.isNew() )
			return true;
		
		session.invalidate();
		session = (HttpSession)factory.create();
		
		return session.isNew();
	}
	
	/**
	 * Tests whether the session is valid or not.
	 * 
	 * @return
	 */
	public boolean isValid() {
		return ( visitCount!=null );
	}
	
	/**
	 * Saves the session data to the HTTP session.
	 */
	public void saveTempSession() {
		for(Map.Entry<String, Object> e : properties.entrySet()){
			session.setAttribute(e.getKey(), e.getValue());
		}//for
	}
	
	/**
	 * Loads the session data from the HTTP session
	 */
	public void loadTempSession() {
		Enumeration<String> en = session.getAttributeNames();
		while(en.hasMoreElements()){
			String key = en.nextElement();
			Object value = session.getAttribute(key);
			properties.put(key, value);
		}//while
	}
	
	/**
	 * Clears the session data from the |0| session
	 */
	public void clearTempSession() {
		properties.clear();
	}


}
