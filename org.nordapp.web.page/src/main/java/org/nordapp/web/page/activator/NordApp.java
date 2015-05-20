package org.nordapp.web.page.activator;

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


import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.Servlet;

import org.nordapp.web.servlet.DEVServlet;
import org.nordapp.web.servlet.DebugSessionServlet;
import org.nordapp.web.servlet.FileServlet;
import org.nordapp.web.servlet.IOServlet;
import org.nordapp.web.servlet.InitServlet;
import org.nordapp.web.servlet.PageServlet;
import org.nordapp.web.servlet.SessionCallServlet;
import org.nordapp.web.servlet.SessionClearServlet;
import org.nordapp.web.servlet.SessionDataServlet;
import org.nordapp.web.servlet.SessionLoginServlet;
import org.nordapp.web.servlet.SessionLogoutServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NordApp implements BundleActivator {
	
	static Logger logger = LoggerFactory.getLogger(NordApp.class);
	
	private static final String initResourcePath = "resources/{}";
	private static final String initPropertyPath = "properties/{}";
	private static final String initBinaryPath = "js/{}";
	private static final String initSessionTimeout = String.valueOf((long)24*60*60*1000);
	
	private List<ServiceRegistration<?>> registration;
	
	public NordApp() {
		registration = new ArrayList<ServiceRegistration<?>>();
	}

	public void start(BundleContext context) throws Exception {
		
		logger.info("Nordapp service started");
		
		String initStartPage = "init.htm";
		
		Dictionary<String, Object> props = null;
		
		props = getDefaultProps();
	    props.put("alias", "/init");
	    props.put("init.startpage", initStartPage);
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new InitServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/page");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new PageServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/file");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new FileServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/io");
	    props.put("init.file-query", "{id: \"{}\"}");

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new IOServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/login");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new SessionLoginServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/call");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new SessionCallServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/data");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new SessionDataServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/logout");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new SessionLogoutServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/clear");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new SessionClearServlet(context), props) );
	    
		props = getDefaultProps();
	    props.put("alias", "/dev");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new DEVServlet(context), props) );
		
		props = getDefaultProps();
	    props.put("alias", "/debug");
		addDefaultPath(props);

	    this.registration.add( 
	    		context.registerService(Servlet.class.getName(), new DebugSessionServlet(context), props) );
		
	}

	public void stop(BundleContext context) throws Exception {
		
		logger.info("Nordapp service stops");
		
		for(ServiceRegistration<?> reg : this.registration) {
			reg.unregister();
		}
	}
	
	/**
	 * Creates a dictionary containing the default properties
	 * 
	 * @return
	 */
	private Dictionary<String, Object> getDefaultProps() {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		
		//control server cache
	    props.put("useFileMappedBuffer", "false");
	    props.put("cacheControl", "max-age=0, public");
	    
	    //default session timeout
	    props.put("init.session-timeout", initSessionTimeout);
	    
		return props;
	}
	
	/**
	 * Adds the default path
	 * 
	 * @param props
	 */
	private void addDefaultPath(Dictionary<String, Object> props) {
		//
	    props.put("init.property-path", initPropertyPath);
	    props.put("init.resource-path", initResourcePath);
	    props.put("init.binary-path", initBinaryPath);
	}

}
