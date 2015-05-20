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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.ParameterParser;
import org.i3xx.step.due.service.model.ResourceService;
import org.nordapp.web.servlet.SessionControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StateReader {
	
	/** The bundle context */
	private BundleContext context;
	
	/** The session control */
	private SessionControl ctrl;

	public StateReader(BundleContext context, SessionControl ctrl) {
		this.context = context;
		this.ctrl = ctrl;
	}
	
	/**
	 * Reads the state file
	 * 
	 * @param dest The destination of the file
	 * @return
	 * @throws IOException
	 */
	public List<String> getState(String dest) throws IOException {
		
		//Get the resource
		ServiceReference<ResourceService> rsr = context.getServiceReference(ResourceService.class);
		if(rsr==null)
			throw new IOException("The resource service is not available (maybe down or a version conflict).");

		ResourceService rs = context.getService(rsr);
		if(rs==null)
			throw new IOException("The resource service is not available (maybe down or a version conflict).");
		
		Map<String, String> props = new HashMap<String, String>();
		List<String> list = new ArrayList<String>();
		
		InputStream in = rs.getResourceAsStream(
				ctrl.getMandatorID(), ctrl.getGroupID(), ctrl.getArtifactID(),
				dest, ResourceService.FILE_RESOURCE, props);
		
		//use an input stream that supports mark.
		LineNumberReader r = new LineNumberReader( new InputStreamReader(in) );
		String line = r.readLine();
		while(line!=null) {
			line = line.trim();
			//skip empty lines and comments
			if( !(line.equals("") || line.startsWith("#")) ) {
				list.add(line);
			}
			line = r.readLine();
		}
		
		return list;
	}
	
	/**
	 * Parses a line that defines an applications RESTful state.
	 * 
	 * @param line
	 * @return
	 */
	public Map<String, String> parseLine(String line) {
		
		Map<String, String> parameters = null;
		parameters = new ParameterParser().parse(line, ';');
		
		return parameters;
	}

}
