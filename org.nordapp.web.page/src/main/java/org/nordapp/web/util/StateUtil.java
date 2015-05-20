package org.nordapp.web.util;

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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nordapp.web.servlet.SessionControl;
import org.nordapp.web.util.link.LinkHeader;
import org.nordapp.web.util.link.LinkRelation;
import org.osgi.framework.BundleContext;

public class StateUtil {

	/**
	 * Sets the current HTTP settings to the link header
	 * 
	 * @param lhdr The LinkHeader
	 * @param req
	 */
	public static final void setDefault(LinkHeader lhdr, HttpServletRequest req) {
		lhdr.setLink_protocol( req.getProtocol() );
		lhdr.setLink_host( req.getLocalAddr() );
		lhdr.setLink_port( req.getLocalPort() );
		lhdr.setLink_servlet( req.getServletPath() );
		//lhdr.setLink_uuid( ctrl.getCertID() );
	}
	
	/**
	 * @param context
	 * @param ctrl
	 * @param resource
	 * @throws Exception 
	 */
	public static final void setState(BundleContext context, SessionControl ctrl, LinkHeader lhdr, String resource) throws IOException {
		
		StateReader srd = new StateReader(context, ctrl);
		for(String line : srd.getState("/resources/state/"+resource)) {
			LinkRelation rel;
			try {
				rel = new LinkRelation(line);
			} catch (Exception e) {
				throw new IOException(e.toString());
			}
			//Map<String, String> props = srd.parseLine(line);
			Map<String, String> props = rel.getParameters();
			
			if(props.containsKey("protocol"))
				lhdr.setLink_protocol( props.get("protocol") );
			if(props.containsKey("host"))
				lhdr.setLink_host( props.get("host") );
			if(props.containsKey("port"))
				lhdr.setLink_host( props.get("port") );
			if(props.containsKey("servlet"))
				lhdr.setLink_servlet( props.get("servlet") );
			if(props.containsKey("host"))
				lhdr.setLink_host( props.get("host") );
			if(props.containsKey("path"))
				lhdr.setLink_path( props.get("path") );
			
			if(rel.getTarget()!=null && props.containsKey("rel"))
				lhdr.add( rel.getTarget(), props.get("rel") );
		}
	}
}
