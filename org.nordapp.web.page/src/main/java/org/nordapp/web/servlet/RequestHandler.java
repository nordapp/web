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


import java.net.MalformedURLException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.nordapp.web.servlet.SessionControl.Condition;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;

public class RequestHandler {
	
	private static final Pattern pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\._-]*");
	
	@SuppressWarnings("unused")
	private BundleContext context;
	private SessionControl ctrl;
	
	public RequestHandler(BundleContext context, SessionControl ctrl) {
		this.context = context;
		this.ctrl = ctrl;
	}
	
	/**
	 * Reads the request, stores the data to the session control
	 * 
	 * @param req The HttpServletRequest
	 * @param resourcePath The resource path
	 * @return The file path of the requested resource.
	 * @throws MalformedURLException
	 */
	public String readRequest(HttpServletRequest req, String resourcePath) throws MalformedURLException {
		String reqResource = null;
		
		String[] elem = RequestPath.getPath(req);
		
		String srv = req.getServletPath().substring(1).toLowerCase();
		
		if(srv.equals("init")) {
			//long form '/mandator/group/artifact/resource'
			if(elem.length==4){
				ctrl.setGroupID(elem[1], Condition.IF_NULL);
				ctrl.setArtifactID(elem[2], Condition.IF_NULL);
				
				reqResource = elem[3];
			}
			//exception
			else
				throw new MalformedURLException("The URL needs the form '"+
						req.getServletPath()+"/id-of-the-mandator/group-id/artifact-id/requested-resource' or '"+
						req.getServletPath()+"/requested-resource' but was '"+
						req.getRequestURI()+"'");
		}
		
		//a whole path is allowed
		else {
			// Be aware of the pathStartIndex '/path' or
			// '/id-of-the-mandator/id-of-the-session/path'
			StringBuffer buf = new StringBuffer();
			for(int i=ctrl.getPathStartIndex();i<elem.length;i++){
				if( pattern.matcher(elem[i]).matches() ){
					if(buf.length()>0)
						buf.append('/');
					buf.append(elem[i]);
				}//fi
			}//for
			reqResource = buf.toString();
		}
		
		String dest = RequestPath.replacePath(resourcePath, reqResource);
		return dest;
	}

}
